package org.abdrafikov.groupbuy.repository;

import org.abdrafikov.groupbuy.model.Workspace;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface WorkspaceRepository extends JpaRepository<Workspace, Long> {

    @EntityGraph(attributePaths = {"owner"})
    List<Workspace> findDistinctByMembersUserIdOrderByCreatedAtDesc(Long userId);

    @EntityGraph(attributePaths = {"owner"})
    Optional<Workspace> findById(Long id);

    @EntityGraph(attributePaths = {"owner"})
    Optional<Workspace> findByJoinToken(String joinToken);
}
