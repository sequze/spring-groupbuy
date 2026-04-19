package org.abdrafikov.groupbuy.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.abdrafikov.groupbuy.model.choices.PurchaseItemStatus;

import java.math.BigDecimal;

@Getter
@Setter
public class PurchaseItemForm {

    @NotNull(message = "Workspace обязателен")
    private Long workspaceId;

    @NotBlank(message = "Название позиции обязательно")
    @Size(max = 200, message = "Название должно быть не длиннее 200 символов")
    private String title;

    @Size(max = 2000, message = "Описание должно быть не длиннее 2000 символов")
    private String description;

    @NotNull(message = "Количество обязательно")
    @Min(value = 1, message = "Количество должно быть больше 0")
    private Integer quantity;

    @Size(max = 50, message = "Единица измерения должна быть не длиннее 50 символов")
    private String unit;

    @DecimalMin(value = "0.0", inclusive = false, message = "Цена должна быть больше 0")
    private BigDecimal priceAmount;

    @Size(min = 3, max = 3, message = "Код валюты должен содержать 3 символа")
    private String priceCurrency;

    @NotNull(message = "Статус обязателен")
    private PurchaseItemStatus status = PurchaseItemStatus.NEW;

    @Size(max = 1000, message = "Причина отклонения должна быть не длиннее 1000 символов")
    private String rejectionReason;
}
