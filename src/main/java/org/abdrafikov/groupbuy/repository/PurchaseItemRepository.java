package org.abdrafikov.groupbuy.repository;

import org.abdrafikov.groupbuy.model.PurchaseItem;
import org.abdrafikov.groupbuy.model.choices.PurchaseItemStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PurchaseItemRepository extends JpaRepository<PurchaseItem, Long> {

    @EntityGraph(attributePaths = {"workspace", "author"})
    List<PurchaseItem> findByWorkspaceIdOrderByCreatedAtDesc(Long workspaceId);

    @EntityGraph(attributePaths = {"workspace", "author"})
    List<PurchaseItem> findByWorkspaceIdAndStatusOrderByCreatedAtDesc(Long workspaceId, PurchaseItemStatus status);

    @EntityGraph(attributePaths = {"workspace", "author"})
    Optional<PurchaseItem> findById(Long id);
}
