package org.abdrafikov.groupbuy.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.abdrafikov.groupbuy.model.choices.OrderStatus;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class OrderForm {

    @NotNull(message = "Workspace обязателен")
    private Long workspaceId;

    @NotBlank(message = "Название заказа обязательно")
    @Size(max = 200, message = "Название заказа должно быть не длиннее 200 символов")
    private String title;

    @Size(max = 1000, message = "Описание должно быть не длиннее 1000 символов")
    private String description;

    @NotNull(message = "Статус обязателен")
    private OrderStatus status = OrderStatus.DRAFT;

    private List<OrderItemForm> items = new ArrayList<>();
}
