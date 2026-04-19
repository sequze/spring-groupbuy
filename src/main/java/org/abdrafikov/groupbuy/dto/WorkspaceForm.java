package org.abdrafikov.groupbuy.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WorkspaceForm {

    @NotBlank(message = "Название workspace обязательно")
    @Size(max = 150, message = "Название workspace должно быть не длиннее 150 символов")
    private String name;

    @Size(max = 1000, message = "Описание должно быть не длиннее 1000 символов")
    private String description;

    private boolean active = true;
}
