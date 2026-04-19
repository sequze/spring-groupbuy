package org.abdrafikov.groupbuy.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class OrderItemDto {

    private final Long purchaseItemId;
    private final String title;
    private final Integer quantity;
    private final String unit;
    private final BigDecimal price;
    private final String currency;
    private final BigDecimal subtotal;
}
