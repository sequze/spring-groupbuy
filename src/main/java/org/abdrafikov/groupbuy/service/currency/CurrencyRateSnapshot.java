package org.abdrafikov.groupbuy.service.currency;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.Map;

public record CurrencyRateSnapshot(String baseCurrency, Map<Currency, BigDecimal> ratesFromBase) {

    public CurrencyRateSnapshot {
        ratesFromBase = Map.copyOf(ratesFromBase);
    }
}
