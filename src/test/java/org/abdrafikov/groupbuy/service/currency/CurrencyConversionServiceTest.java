package org.abdrafikov.groupbuy.service.currency;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CurrencyConversionServiceTest {

    @Test
    void convertsAmountToBaseCurrency() {
        CurrencyConversionService service = new CurrencyConversionService(
                (source, target) -> new BigDecimal("91.234"),
                "RUB"
        );

        CurrencyConversionResult result = service.convertToBase(new BigDecimal("10.00"), "usd");

        assertThat(result.amount()).isEqualByComparingTo("912.34");
        assertThat(result.currency()).isEqualTo("RUB");
    }

    @Test
    void keepsAmountWhenSourceCurrencyIsBaseCurrency() {
        CurrencyConversionService service = new CurrencyConversionService(
                (source, target) -> {
                    throw new AssertionError("External API must not be called for base currency");
                },
                "RUB"
        );

        CurrencyConversionResult result = service.convertToBase(new BigDecimal("10"), "rub");

        assertThat(result.amount()).isEqualByComparingTo("10.00");
        assertThat(result.currency()).isEqualTo("RUB");
    }

    @Test
    void propagatesPredictableConversionErrors() {
        CurrencyConversionService service = new CurrencyConversionService(
                (source, target) -> {
                    throw new CurrencyConversionException("Сервис курсов валют временно недоступен.");
                },
                "RUB"
        );

        assertThatThrownBy(() -> service.convertToBase(new BigDecimal("10"), "USD"))
                .isInstanceOf(CurrencyConversionException.class)
                .hasMessageContaining("недоступен");
    }
}
