package org.abdrafikov.groupbuy.service.currency;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class HttpExchangeRateClientTest {

    @Test
    void readsTargetCurrencyRate() {
        RestTemplate restTemplate = new RestTemplate();
        MockRestServiceServer server = MockRestServiceServer.bindTo(restTemplate).build();
        HttpExchangeRateClient client = new HttpExchangeRateClient(restTemplate, "https://v6.exchangerate-api.com/v6", "test-token");

        server.expect(requestTo("https://v6.exchangerate-api.com/v6/test-token/latest/EUR"))
                .andRespond(withSuccess("""
                        {
                          "result": "success",
                          "base_code": "EUR",
                          "conversion_rates": {
                            "USD": 1.1,
                            "RUB": 100
                          }
                        }
                        """, MediaType.APPLICATION_JSON));

        Map<String, BigDecimal> rates = client.getRates("eur");

        assertThat(rates.get("USD")).isEqualByComparingTo("1.1");
        assertThat(rates.get("RUB")).isEqualByComparingTo("100");
        server.verify();
    }

    @Test
    void wrapsExternalApiFailure() {
        RestTemplate restTemplate = new RestTemplate();
        MockRestServiceServer server = MockRestServiceServer.bindTo(restTemplate).build();
        HttpExchangeRateClient client = new HttpExchangeRateClient(restTemplate, "https://v6.exchangerate-api.com/v6", "test-token");

        server.expect(requestTo("https://v6.exchangerate-api.com/v6/test-token/latest/EUR"))
                .andRespond(withServerError());

        assertThatThrownBy(() -> client.getRates("EUR"))
                .isInstanceOf(CurrencyConversionException.class)
                .hasMessageContaining("временно недоступен");
        server.verify();
    }
}
