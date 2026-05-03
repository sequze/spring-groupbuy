package org.abdrafikov.groupbuy.repository;

import org.abdrafikov.groupbuy.model.Comment;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    @EntityGraph(attributePaths = {"purchaseItem", "purchaseItem.workspace", "author"})
    List<Comment> findAllByOrderByCreatedAtDesc();

    @EntityGraph(attributePaths = {"purchaseItem", "purchaseItem.workspace", "author"})
    List<Comment> findByPurchaseItemWorkspaceIdOrderByCreatedAtDesc(Long workspaceId);

    @EntityGraph(attributePaths = {"purchaseItem", "purchaseItem.workspace", "author"})
    List<Comment> findByPurchaseItemIdOrderByCreatedAtDesc(Long purchaseItemId);

    @EntityGraph(attributePaths = {"purchaseItem", "purchaseItem.workspace", "author"})
    Optional<Comment> findById(Long id);
}
