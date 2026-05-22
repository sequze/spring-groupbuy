package org.abdrafikov.groupbuy.repository;

import org.abdrafikov.groupbuy.model.Workspace;
import org.abdrafikov.groupbuy.model.choices.PurchaseItemStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface WorkspaceRepository extends JpaRepository<Workspace, Long> {

    @EntityGraph(attributePaths = {"owner"})
    List<Workspace> findDistinctByMembersUserIdOrderByCreatedAtDesc(Long userId);

    @EntityGraph(attributePaths = {"owner"})
    Optional<Workspace> findById(Long id);

    @EntityGraph(attributePaths = {"owner"})
    Optional<Workspace> findByJoinToken(String joinToken);

    @Query("""
            select w.id
            from Workspace w
            where exists (
                select member.id
                from WorkspaceMember member
                where member.workspace = w
                  and member.user.id = :userId
            )
              and exists (
                select item.id
                from PurchaseItem item
                where item.workspace = w
                  and item.status = :status
            )
            """)
    List<Long> findWorkspaceIdsWithPurchaseItemStatusForMember(
            @Param("userId") Long userId,
            @Param("status") PurchaseItemStatus status
    );
}
