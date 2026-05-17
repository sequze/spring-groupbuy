package org.abdrafikov.groupbuy.service.currency;

import java.math.BigDecimal;

public record CurrencyConversionResult(BigDecimal amount, String currency) {
}
