package org.abdrafikov.groupbuy.service.currency;

import org.abdrafikov.groupbuy.config.CacheConfig;
import org.abdrafikov.groupbuy.model.CurrencyRate;
import org.abdrafikov.groupbuy.repository.CurrencyRateRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class CurrencyRateService {

    private static final Logger log = LoggerFactory.getLogger(CurrencyRateService.class);

    private final ExchangeRateClient exchangeRateClient;
    private final CurrencyRateRepository currencyRateRepository;
    private final CacheManager cacheManager;
    private final String baseCurrency;

    public CurrencyRateService(
            ExchangeRateClient exchangeRateClient,
            CurrencyRateRepository currencyRateRepository,
            CacheManager cacheManager,
            @Value("${groupbuy.currency.base:RUB}") String baseCurrency
    ) {
        this.exchangeRateClient = exchangeRateClient;
        this.currencyRateRepository = currencyRateRepository;
        this.cacheManager = cacheManager;
        this.baseCurrency = normalizeCurrency(baseCurrency);
    }

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void refreshOnStartup() {
        refreshRates();
    }

    @Scheduled(cron = "0 0 */6 * * *")
    @Transactional
    public void refreshRates() {
        try {
            CurrencyRateSnapshot snapshot = saveRates(exchangeRateClient.getRates(baseCurrency));
            updateCache(snapshot);
            log.info("Currency rates refreshed for {}: {} rates", baseCurrency, snapshot.ratesFromBase().size());
        } catch (CurrencyConversionException ex) {
            log.warn("Currency rates refresh failed: {}", ex.getMessage());
        } catch (RuntimeException ex) {
            log.warn("Currency rates refresh failed unexpectedly", ex);
        }
    }

    @Transactional(readOnly = true)
    @Cacheable(
            cacheNames = CacheConfig.CURRENCY_RATES_CACHE,
            key = "T(org.abdrafikov.groupbuy.config.CacheConfig).CURRENCY_RATES_SNAPSHOT_KEY"
    )
    public CurrencyRateSnapshot getSnapshot() {
        return buildSnapshotFromDatabase();
    }

    private CurrencyRateSnapshot saveRates(Map<String, BigDecimal> ratesFromBase) {
        LocalDateTime fetchedAt = LocalDateTime.now();
        Map<Currency, BigDecimal> snapshotRates = new HashMap<>();
        Map<String, CurrencyRate> existingRates = currencyRateRepository.findByBaseCurrency(baseCurrency).stream()
                .collect(Collectors.toMap(CurrencyRate::getCurrencyCode, Function.identity()));
        Set<String> actualCodes = new HashSet<>();
        List<CurrencyRate> ratesToSave = new ArrayList<>();

        ratesFromBase.forEach((currencyCode, rate) -> {
            Currency currency = parseCurrency(currencyCode);
            if (currency == null) {
                return;
            }

            String normalizedCode = currency.getCurrencyCode();
            actualCodes.add(normalizedCode);
            CurrencyRate currencyRate = existingRates.getOrDefault(normalizedCode, new CurrencyRate());
            currencyRate.setBaseCurrency(baseCurrency);
            currencyRate.setCurrencyCode(normalizedCode);
            currencyRate.setRateFromBase(rate);
            currencyRate.setFetchedAt(fetchedAt);
            ratesToSave.add(currencyRate);
            snapshotRates.put(currency, rate);
        });

        if (snapshotRates.isEmpty()) {
            throw new CurrencyConversionException("Сервис курсов валют не вернул поддерживаемых валют.");
        }
        snapshotRates.putIfAbsent(Currency.getInstance(baseCurrency), BigDecimal.ONE);

        currencyRateRepository.saveAll(ratesToSave);
        currencyRateRepository.deleteAll(existingRates.values().stream()
                .filter(currencyRate -> !actualCodes.contains(currencyRate.getCurrencyCode()))
                .toList());
        return new CurrencyRateSnapshot(baseCurrency, snapshotRates);
    }

    private CurrencyRateSnapshot buildSnapshotFromDatabase() {
        Map<Currency, BigDecimal> rates = new HashMap<>();
        currencyRateRepository.findByBaseCurrency(baseCurrency).forEach(currencyRate -> {
            Currency currency = parseCurrency(currencyRate.getCurrencyCode());
            if (currency != null && currencyRate.getRateFromBase() != null) {
                rates.put(currency, currencyRate.getRateFromBase());
            }
        });
        rates.putIfAbsent(Currency.getInstance(baseCurrency), BigDecimal.ONE);
        return new CurrencyRateSnapshot(baseCurrency, rates);
    }

    private void updateCache(CurrencyRateSnapshot snapshot) {
        Cache cache = cacheManager.getCache(CacheConfig.CURRENCY_RATES_CACHE);
        if (cache != null) {
            cache.put(CacheConfig.CURRENCY_RATES_SNAPSHOT_KEY, snapshot);
        }
    }

    private Currency parseCurrency(String currencyCode) {
        if (currencyCode == null || currencyCode.isBlank()) {
            return null;
        }
        try {
            return Currency.getInstance(currencyCode.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            log.debug("Skipping unsupported currency code {}", currencyCode);
            return null;
        }
    }

    private String normalizeCurrency(String currency) {
        if (currency == null || currency.isBlank()) {
            throw new CurrencyConversionException("Не задана базовая валюта приложения.");
        }
        return currency.trim().toUpperCase(Locale.ROOT);
    }
}
