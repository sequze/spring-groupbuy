package org.abdrafikov.groupbuy.dto;

import lombok.Builder;
import lombok.Getter;
import org.abdrafikov.groupbuy.model.choices.OrderStatus;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Builder
public class OrderDto {

    private final Long id;
    private final Long workspaceId;
    private final String workspaceName;
    private final String createdByDisplayName;
    private final String title;
    private final String description;
    private final OrderStatus status;
    private final BigDecimal totalAmount;
    private final String currency;
    private final int itemCount;
    private final List<OrderItemDto> items;
    private final boolean canEdit;
    private final boolean canDelete;
}
