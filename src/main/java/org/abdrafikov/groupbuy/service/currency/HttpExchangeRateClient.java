package org.abdrafikov.groupbuy.service.currency;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.math.BigDecimal;
import java.util.Map;

@Service
public class HttpExchangeRateClient implements ExchangeRateClient {

    private final RestTemplate restTemplate;
    private final String baseUrl;

    public HttpExchangeRateClient(
            RestTemplate restTemplate,
            @Value("${groupbuy.currency.api.base-url:https://open.er-api.com/v6}") String baseUrl
    ) {
        this.restTemplate = restTemplate;
        this.baseUrl = baseUrl;
    }

    @Override
    public BigDecimal getRate(String sourceCurrency, String targetCurrency) {
        String url = UriComponentsBuilder.fromUriString(baseUrl)
                .pathSegment("latest", sourceCurrency)
                .toUriString();

        ExchangeRateResponse response;
        try {
            response = restTemplate.getForObject(url, ExchangeRateResponse.class);
        } catch (RestClientException ex) {
            throw new CurrencyConversionException("Сервис курсов валют временно недоступен. Попробуйте сохранить позицию позже.", ex);
        }

        if (response == null || !"success".equalsIgnoreCase(response.result())) {
            throw new CurrencyConversionException("Не удалось получить курс валюты " + sourceCurrency + ".");
        }

        BigDecimal rate = response.rates() == null ? null : response.rates().get(targetCurrency);
        if (rate == null) {
            throw new CurrencyConversionException("Курс " + sourceCurrency + " -> " + targetCurrency + " не найден.");
        }
        return rate;
    }

    private record ExchangeRateResponse(
            String result,
            @JsonProperty("base_code") String baseCode,
            Map<String, BigDecimal> rates
    ) {
    }
}
