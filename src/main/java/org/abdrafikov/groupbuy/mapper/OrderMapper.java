package org.abdrafikov.groupbuy.mapper;

import org.abdrafikov.groupbuy.dto.OrderDto;
import org.abdrafikov.groupbuy.dto.OrderItemDto;
import org.abdrafikov.groupbuy.dto.OrderItemOptionDto;
import org.abdrafikov.groupbuy.model.Order;
import org.abdrafikov.groupbuy.model.OrderItem;
import org.abdrafikov.groupbuy.model.PurchaseItem;
import org.abdrafikov.groupbuy.service.currency.CurrencyConversionResult;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
public class OrderMapper {

    public OrderDto toDto(
            Order order,
            List<OrderItemDto> items,
            boolean canManage,
            String snapshotCurrency,
            CurrencyConversionResult currentTotal
    ) {
        return OrderDto.builder()
                .id(order.getId())
                .workspaceId(order.getWorkspace().getId())
                .workspaceName(order.getWorkspace().getName())
                .createdByDisplayName(order.getCreatedBy().getDisplayName())
                .title(order.getTitle())
                .description(order.getDescription())
                .status(order.getStatus())
                .totalAmount(order.getTotalAmount())
                .currency(snapshotCurrency)
                .currentTotalAmount(currentTotal.amount())
                .currentCurrency(currentTotal.currency())
                .itemCount(items.size())
                .items(items)
                .canEdit(canManage)
                .canDelete(canManage)
                .build();
    }

    public OrderItemDto toItemDto(
            OrderItem item,
            BigDecimal subtotal,
            CurrencyConversionResult currentPrice,
            BigDecimal currentSubtotal
    ) {
        return OrderItemDto.builder()
                .purchaseItemId(item.getPurchaseItem().getId())
                .title(item.getItemTitleSnapshot())
                .quantity(item.getQuantitySnapshot())
                .unit(item.getPurchaseItem().getUnit())
                .price(item.getPriceSnapshot())
                .currency(item.getCurrencySnapshot())
                .subtotal(subtotal)
                .currentPrice(currentPrice.amount())
                .currentCurrency(currentPrice.currency())
                .currentSubtotal(currentSubtotal)
                .build();
    }

    public OrderItemOptionDto toOptionDto(PurchaseItem purchaseItem, String priceLabel) {
        return OrderItemOptionDto.builder()
                .purchaseItemId(purchaseItem.getId())
                .title(purchaseItem.getTitle())
                .unit(purchaseItem.getUnit())
                .priceLabel(priceLabel)
                .build();
    }
}
