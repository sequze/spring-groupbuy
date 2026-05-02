package org.abdrafikov.groupbuy.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class OrderItemOptionDto {

    private final Long purchaseItemId;
    private final String title;
    private final String unit;
    private final String priceLabel;
}
