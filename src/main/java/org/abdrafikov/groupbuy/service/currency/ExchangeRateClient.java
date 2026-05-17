package org.abdrafikov.groupbuy.service.currency;

import java.math.BigDecimal;

public interface ExchangeRateClient {

    BigDecimal getRate(String sourceCurrency, String targetCurrency);
}
