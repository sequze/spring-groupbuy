package org.abdrafikov.groupbuy.service.currency;

import java.math.BigDecimal;
import java.util.Map;

public interface ExchangeRateClient {

    Map<String, BigDecimal> getRates(String baseCurrency);
}
