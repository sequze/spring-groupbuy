package org.abdrafikov.groupbuy.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.abdrafikov.groupbuy.model.base.BaseEntity;
import org.abdrafikov.groupbuy.model.choices.WorkspaceRole;

import java.time.LocalDateTime;
@Getter
@Entity
@Table(
        name = "workspace_members",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_workspace_member_workspace_user", columnNames = {"workspace_id", "user_id"})
        }
)
public class WorkspaceMember extends BaseEntity {

    @Setter
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "workspace_id", nullable = false)
    private Workspace workspace;

    @Setter
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Setter
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private WorkspaceRole role;

    @Column(name = "joined_at", nullable = false)
    private LocalDateTime joinedAt;

    @Setter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invited_by_id")
    private User invitedBy;

    @PrePersist
    protected void onJoin() {
        if (joinedAt == null) {
            joinedAt = LocalDateTime.now();
        }
    }

}