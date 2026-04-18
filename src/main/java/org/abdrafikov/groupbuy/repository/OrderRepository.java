package org.abdrafikov.groupbuy.repository;

import org.abdrafikov.groupbuy.model.Order;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

    @EntityGraph(attributePaths = {"workspace", "createdBy", "items", "items.purchaseItem"})
    List<Order> findByWorkspaceIdOrderByCreatedAtDesc(Long workspaceId);

    @EntityGraph(attributePaths = {"workspace", "createdBy", "items", "items.purchaseItem"})
    Optional<Order> findById(Long id);
}
