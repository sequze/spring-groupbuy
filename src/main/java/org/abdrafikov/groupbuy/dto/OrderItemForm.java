package org.abdrafikov.groupbuy.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrderItemForm {

    @NotNull
    private Long purchaseItemId;

    private boolean selected;

    @Min(value = 1, message = "Количество должно быть больше 0")
    private Integer quantity = 1;
}
