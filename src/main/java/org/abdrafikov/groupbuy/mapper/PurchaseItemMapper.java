package org.abdrafikov.groupbuy.mapper;

import org.abdrafikov.groupbuy.dto.PurchaseItemDto;
import org.abdrafikov.groupbuy.model.PurchaseItem;
import org.abdrafikov.groupbuy.service.currency.CurrencyConversionResult;
import org.springframework.stereotype.Component;

@Component
public class PurchaseItemMapper {

    public PurchaseItemDto toDto(
            PurchaseItem item,
            boolean canEdit,
            boolean canModerateStatus,
            CurrencyConversionResult currentPrice
    ) {
        return PurchaseItemDto.builder()
                .id(item.getId())
                .workspaceId(item.getWorkspace().getId())
                .workspaceName(item.getWorkspace().getName())
                .authorDisplayName(item.getAuthor().getDisplayName())
                .title(item.getTitle())
                .description(item.getDescription())
                .productUrl(item.getProductUrl())
                .quantity(item.getQuantity())
                .unit(item.getUnit())
                .priceAmount(item.getPriceAmount())
                .priceCurrency(item.getPriceCurrency())
                .basePriceAmount(currentPrice.amount())
                .baseCurrency(currentPrice.currency())
                .status(item.getStatus())
                .rejectionReason(item.getRejectionReason())
                .canEdit(canEdit)
                .canModerateStatus(canModerateStatus)
                .canDelete(canEdit)
                .build();
    }
}
