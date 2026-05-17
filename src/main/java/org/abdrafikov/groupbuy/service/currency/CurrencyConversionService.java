package org.abdrafikov.groupbuy.service.currency;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Locale;

@Service
public class CurrencyConversionService {

    private final ExchangeRateClient exchangeRateClient;
    @Getter
    private final String baseCurrency;

    public CurrencyConversionService(
            ExchangeRateClient exchangeRateClient,
            @Value("${groupbuy.currency.base:RUB}") String baseCurrency
    ) {
        this.exchangeRateClient = exchangeRateClient;
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

        BigDecimal rate = exchangeRateClient.getRate(normalizedSourceCurrency, baseCurrency);
        return new CurrencyConversionResult(scale(amount.multiply(rate)), baseCurrency);
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
