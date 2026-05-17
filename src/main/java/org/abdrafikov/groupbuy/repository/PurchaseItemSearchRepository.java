package org.abdrafikov.groupbuy.repository;

import org.abdrafikov.groupbuy.model.PurchaseItem;
import org.abdrafikov.groupbuy.model.choices.PurchaseItemStatus;

import java.util.List;

public interface PurchaseItemSearchRepository {

    List<PurchaseItem> search(Long workspaceId, PurchaseItemStatus status, String titleQuery);
}
