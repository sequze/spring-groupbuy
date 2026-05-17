package org.abdrafikov.groupbuy.service.currency;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CurrencyConversionServiceTest {

    @Test
    void convertsAmountToBaseCurrency() {
        CurrencyRateService currencyRateService = mock(CurrencyRateService.class);
        when(currencyRateService.getSnapshot()).thenReturn(new CurrencyRateSnapshot("RUB", Map.of(
                Currency.getInstance("RUB"), BigDecimal.ONE,
                Currency.getInstance("USD"), new BigDecimal("0.011")
        )));
        CurrencyConversionService service = new CurrencyConversionService(currencyRateService, "RUB");

        CurrencyConversionResult result = service.convertToBase(new BigDecimal("10.00"), "usd");

        assertThat(result.amount()).isEqualByComparingTo("909.09");
        assertThat(result.currency()).isEqualTo("RUB");
    }

    @Test
    void convertsAmountToConfiguredBaseCurrency() {
        CurrencyRateService currencyRateService = mock(CurrencyRateService.class);
        when(currencyRateService.getSnapshot()).thenReturn(new CurrencyRateSnapshot("EUR", Map.of(
                Currency.getInstance("EUR"), BigDecimal.ONE,
                Currency.getInstance("USD"), new BigDecimal("1.1"),
                Currency.getInstance("RUB"), new BigDecimal("100")
        )));
        CurrencyConversionService service = new CurrencyConversionService(currencyRateService, "EUR");

        CurrencyConversionResult result = service.convertToBase(new BigDecimal("10.00"), "usd");

        assertThat(result.amount()).isEqualByComparingTo("9.09");
        assertThat(result.currency()).isEqualTo("EUR");
    }

    @Test
    void keepsAmountWhenSourceCurrencyIsBaseCurrency() {
        CurrencyRateService currencyRateService = mock(CurrencyRateService.class);
        CurrencyConversionService service = new CurrencyConversionService(currencyRateService, "RUB");

        CurrencyConversionResult result = service.convertToBase(new BigDecimal("10"), "rub");

        assertThat(result.amount()).isEqualByComparingTo("10.00");
        assertThat(result.currency()).isEqualTo("RUB");
    }

    @Test
    void propagatesPredictableConversionErrors() {
        CurrencyRateService currencyRateService = mock(CurrencyRateService.class);
        when(currencyRateService.getSnapshot()).thenThrow(new CurrencyConversionException("Сервис курсов валют временно недоступен."));
        CurrencyConversionService service = new CurrencyConversionService(currencyRateService, "RUB");

        assertThatThrownBy(() -> service.convertToBase(new BigDecimal("10"), "USD"))
                .isInstanceOf(CurrencyConversionException.class)
                .hasMessageContaining("недоступен");
    }
}
