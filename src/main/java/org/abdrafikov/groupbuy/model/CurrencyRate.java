package org.abdrafikov.groupbuy.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.abdrafikov.groupbuy.model.base.BaseEntity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Entity
@Table(
        name = "currency_rates",
        uniqueConstraints = {
                @jakarta.persistence.UniqueConstraint(
                        name = "uk_currency_rate_base_currency_code",
                        columnNames = {"base_currency", "currency_code"}
                )
        }
)
public class CurrencyRate extends BaseEntity {

    @Setter
    @Column(name = "base_currency", nullable = false, length = 3)
    private String baseCurrency;

    @Setter
    @Column(name = "currency_code", nullable = false, length = 3)
    private String currencyCode;

    @Setter
    @Column(name = "rate_from_base", nullable = false, precision = 20, scale = 10)
    private BigDecimal rateFromBase;

    @Setter
    @Column(name = "fetched_at", nullable = false)
    private LocalDateTime fetchedAt;
}
