package org.abdrafikov.groupbuy.repository;

import org.abdrafikov.groupbuy.model.WorkspaceMember;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface WorkspaceMemberRepository extends JpaRepository<WorkspaceMember, Long> {

    Optional<WorkspaceMember> findByWorkspaceIdAndUserId(Long workspaceId, Long userId);

    @EntityGraph(attributePaths = {"user", "invitedBy", "workspace", "workspace.owner"})
    List<WorkspaceMember> findByWorkspaceIdOrderByJoinedAtAsc(Long workspaceId);
}
