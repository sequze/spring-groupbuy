package org.abdrafikov.groupbuy.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.abdrafikov.groupbuy.model.choices.WorkspaceRole;

@Getter
@Setter
public class WorkspaceMemberRoleForm {

    @NotNull(message = "Выберите роль участника")
    private WorkspaceRole role;
}
