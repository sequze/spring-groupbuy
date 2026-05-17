package org.abdrafikov.groupbuy.service.currency;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;

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
        HttpExchangeRateClient client = new HttpExchangeRateClient(restTemplate, "https://open.er-api.com/v6");

        server.expect(requestTo("https://open.er-api.com/v6/latest/USD"))
                .andRespond(withSuccess("""
                        {
                          "result": "success",
                          "base_code": "USD",
                          "rates": {
                            "RUB": 91.23
                          }
                        }
                        """, MediaType.APPLICATION_JSON));

        BigDecimal rate = client.getRate("USD", "RUB");

        assertThat(rate).isEqualByComparingTo("91.23");
        server.verify();
    }

    @Test
    void wrapsExternalApiFailure() {
        RestTemplate restTemplate = new RestTemplate();
        MockRestServiceServer server = MockRestServiceServer.bindTo(restTemplate).build();
        HttpExchangeRateClient client = new HttpExchangeRateClient(restTemplate, "https://open.er-api.com/v6");

        server.expect(requestTo("https://open.er-api.com/v6/latest/USD"))
                .andRespond(withServerError());

        assertThatThrownBy(() -> client.getRate("USD", "RUB"))
                .isInstanceOf(CurrencyConversionException.class)
                .hasMessageContaining("временно недоступен");
        server.verify();
    }
}
