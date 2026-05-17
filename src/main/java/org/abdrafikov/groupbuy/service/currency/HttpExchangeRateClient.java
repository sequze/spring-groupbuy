package org.abdrafikov.groupbuy.service.currency;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.math.BigDecimal;
import java.util.Locale;
import java.util.Map;

@Service
@Slf4j
public class HttpExchangeRateClient implements ExchangeRateClient {

    private final RestTemplate restTemplate;
    private final String baseUrl;
    private final String apiToken;

    public HttpExchangeRateClient(
            RestTemplate restTemplate,
            @Value("${groupbuy.currency.api.base-url:https://v6.exchangerate-api.com/v6}") String baseUrl,
            @Value("${groupbuy.currency.api_token:}") String apiToken
    ) {
        this.restTemplate = restTemplate;
        this.baseUrl = baseUrl;
        this.apiToken = apiToken;
    }

    @Override
    public Map<String, BigDecimal> getRates(String baseCurrency) {
        if (apiToken == null || apiToken.isBlank()) {
            log.warn("Exchange rate API token is not configured");
            throw new CurrencyConversionException("Не задан токен сервиса курсов валют.");
        }
        String normalizedBaseCurrency = normalizeCurrency(baseCurrency);
        if (normalizedBaseCurrency == null) {
            log.warn("Exchange rate request skipped because base currency is blank");
            throw new CurrencyConversionException("Не задана базовая валюта для получения курсов.");
        }

        String url = UriComponentsBuilder.fromUriString(baseUrl)
                .pathSegment(apiToken, "latest", normalizedBaseCurrency)
                .toUriString();

        ExchangeRateResponse response;
        try {
            response = restTemplate.getForObject(url, ExchangeRateResponse.class);
        } catch (RestClientException ex) {
            log.warn(
                    "Exchange rate API request failed: baseCurrency={}, baseUrl={}",
                    normalizedBaseCurrency,
                    baseUrl,
                    ex
            );
            throw new CurrencyConversionException("Сервис курсов валют временно недоступен. Попробуйте сохранить позицию позже.", ex);
        }

        if (response == null || !"success".equalsIgnoreCase(response.result())) {
            log.warn(
                    "Exchange rate API returned unsuccessful response: baseCurrency={}, result={}",
                    normalizedBaseCurrency,
                    response == null ? null : response.result()
            );
            throw new CurrencyConversionException("Не удалось получить курсы валют.");
        }

        Map<String, BigDecimal> rates = response.allRates();
        if (rates == null || rates.isEmpty()) {
            log.warn("Exchange rate API returned empty rates: baseCurrency={}", normalizedBaseCurrency);
            throw new CurrencyConversionException("Сервис курсов валют вернул пустой список курсов.");
        }
        return rates.entrySet().stream()
                .filter(entry -> entry.getKey() != null && entry.getValue() != null)
                .collect(java.util.stream.Collectors.toUnmodifiableMap(
                        entry -> entry.getKey().trim().toUpperCase(Locale.ROOT),
                        Map.Entry::getValue
                ));
    }

    private String normalizeCurrency(String currency) {
        if (currency == null || currency.isBlank()) {
            return null;
        }
        return currency.trim().toUpperCase(Locale.ROOT);
    }

    private record ExchangeRateResponse(
            String result,
            @JsonProperty("base_code") String baseCode,
            @JsonProperty("conversion_rates") Map<String, BigDecimal> conversionRates,
            Map<String, BigDecimal> rates
    ) {
        private Map<String, BigDecimal> allRates() {
            return conversionRates != null ? conversionRates : rates;
        }
    }
}
