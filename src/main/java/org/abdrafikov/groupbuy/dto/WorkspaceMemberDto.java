package org.abdrafikov.groupbuy.dto;

import lombok.Builder;
import lombok.Getter;
import org.abdrafikov.groupbuy.model.choices.WorkspaceRole;

import java.time.LocalDateTime;

@Getter
@Builder
public class WorkspaceMemberDto {

    private final Long id;
    private final Long userId;
    private final String displayName;
    private final String email;
    private final WorkspaceRole role;
    private final String roleLabel;
    private final LocalDateTime joinedAt;
    private final String invitedByDisplayName;
    private final boolean owner;
    private final boolean canManageRole;
    private final boolean canRemove;
}
