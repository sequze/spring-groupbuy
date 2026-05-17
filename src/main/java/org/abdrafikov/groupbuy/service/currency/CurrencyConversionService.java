package org.abdrafikov.groupbuy.service.currency;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Currency;
import java.util.Locale;
import java.util.Map;

@Service
public class CurrencyConversionService {

    private final CurrencyRateService currencyRateService;
    @Getter
    private final String baseCurrency;

    public CurrencyConversionService(
            CurrencyRateService currencyRateService,
            @Value("${groupbuy.currency.base:RUB}") String baseCurrency
    ) {
        this.currencyRateService = currencyRateService;
        this.baseCurrency = normalizeCurrency(baseCurrency);
    }

    public CurrencyConversionResult convertToBase(BigDecimal amount, String sourceCurrency) {
        if (amount == null) {
            return new CurrencyConversionResult(null, baseCurrency);
        }

        String normalizedSourceCurrency = normalizeCurrency(sourceCurrency);
        if (normalizedSourceCurrency == null) {
            normalizedSourceCurrency = baseCurrency;
        }

        if (baseCurrency.equals(normalizedSourceCurrency)) {
            return new CurrencyConversionResult(scale(amount), baseCurrency);
        }

        Map<Currency, BigDecimal> ratesFromBase = currencyRateService.getSnapshot().ratesFromBase();
        BigDecimal sourceRateFromBase = getRateFromBase(ratesFromBase, normalizedSourceCurrency);

        return new CurrencyConversionResult(scale(amount.divide(sourceRateFromBase, 10, RoundingMode.HALF_UP)), baseCurrency);
    }

    private BigDecimal getRateFromBase(Map<Currency, BigDecimal> ratesFromBase, String currencyCode) {
        Currency currency;
        try {
            currency = Currency.getInstance(currencyCode);
        } catch (IllegalArgumentException ex) {
            throw new CurrencyConversionException("Код валюты " + currencyCode + " не поддерживается.", ex);
        }

        BigDecimal rate = ratesFromBase.get(currency);
        if (rate == null) {
            throw new CurrencyConversionException("Курс " + baseCurrency + " -> " + currencyCode + " не найден.");
        }
        if (BigDecimal.ZERO.compareTo(rate) == 0) {
            throw new CurrencyConversionException("Курс " + currencyCode + " некорректен.");
        }
        return rate;
    }

    private static BigDecimal scale(BigDecimal amount) {
        return amount.setScale(2, RoundingMode.HALF_UP);
    }

    private static String normalizeCurrency(String currency) {
        if (currency == null || currency.isBlank()) {
            return null;
        }
        return currency.trim().toUpperCase(Locale.ROOT);
    }
}
