package org.abdrafikov.groupbuy.repository;

import org.abdrafikov.groupbuy.model.PurchaseItem;
import org.abdrafikov.groupbuy.model.choices.PurchaseItemStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PurchaseItemRepository extends JpaRepository<PurchaseItem, Long>, PurchaseItemSearchRepository {

    @Query("""
            select item
            from PurchaseItem item
            join fetch item.workspace
            join fetch item.author
            where item.workspace.id = :workspaceId
            order by item.createdAt desc
            """)
    List<PurchaseItem> findByWorkspaceIdOrderByCreatedAtDesc(@Param("workspaceId") Long workspaceId);

    @EntityGraph(attributePaths = {"workspace", "author"})
    List<PurchaseItem> findByWorkspaceIdAndStatusOrderByCreatedAtDesc(Long workspaceId, PurchaseItemStatus status);

    @EntityGraph(attributePaths = {"workspace", "author"})
    Optional<PurchaseItem> findById(Long id);
}
