package org.abdrafikov.groupbuy.dto;

import lombok.Builder;
import lombok.Getter;
import org.abdrafikov.groupbuy.model.choices.PurchaseItemStatus;

import java.math.BigDecimal;

@Getter
@Builder
public class PurchaseItemDto {

    private final Long id;
    private final Long workspaceId;
    private final String workspaceName;
    private final String authorDisplayName;
    private final String title;
    private final String description;
    private final Integer quantity;
    private final String unit;
    private final BigDecimal priceAmount;
    private final String priceCurrency;
    private final PurchaseItemStatus status;
    private final String rejectionReason;
    private final boolean canEdit;
    private final boolean canModerateStatus;
    private final boolean canDelete;
}
