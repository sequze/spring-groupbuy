package org.abdrafikov.groupbuy.mapper;

import org.abdrafikov.groupbuy.dto.WorkspaceDto;
import org.abdrafikov.groupbuy.dto.WorkspaceMemberDto;
import org.abdrafikov.groupbuy.model.User;
import org.abdrafikov.groupbuy.model.Workspace;
import org.abdrafikov.groupbuy.model.WorkspaceMember;
import org.abdrafikov.groupbuy.model.choices.WorkspaceRole;
import org.springframework.stereotype.Component;

@Component
public class WorkspaceMapper {

    public WorkspaceDto toDto(
            Workspace workspace,
            boolean currentUserOwner,
            boolean currentUserAdmin,
            boolean canLeave,
            boolean hasApprovedPurchaseItems
    ) {
        return WorkspaceDto.builder()
                .id(workspace.getId())
                .name(workspace.getName())
                .description(workspace.getDescription())
                .ownerDisplayName(workspace.getOwner().getDisplayName())
                .joinToken(workspace.getJoinToken())
                .active(workspace.isActive())
                .currentUserOwner(currentUserOwner)
                .currentUserAdmin(currentUserAdmin)
                .canLeave(canLeave)
                .hasApprovedPurchaseItems(hasApprovedPurchaseItems)
                .build();
    }

    public WorkspaceMemberDto toMemberDto(
            Workspace workspace,
            WorkspaceMember member,
            String roleLabel,
            boolean owner,
            boolean canManageRole,
            boolean canRemove
    ) {
        User user = member.getUser();
        User invitedBy = member.getInvitedBy();
        return WorkspaceMemberDto.builder()
                .id(member.getId())
                .userId(user.getId())
                .displayName(user.getDisplayName())
                .email(user.getEmail())
                .role(member.getRole())
                .roleLabel(roleLabel)
                .joinedAt(member.getJoinedAt())
                .invitedByDisplayName(invitedBy == null ? null : invitedBy.getDisplayName())
                .owner(owner)
                .canManageRole(canManageRole)
                .canRemove(canRemove)
                .build();
    }

    public String toWorkspaceRoleLabel(WorkspaceRole role) {
        return switch (role) {
            case SPACE_ADMIN -> "Админ";
            case SPACE_MEMBER -> "Участник";
        };
    }
}
