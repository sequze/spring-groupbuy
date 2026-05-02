package org.abdrafikov.groupbuy.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WorkspaceJoinForm {

    @NotBlank(message = "Токен обязателен")
    @Size(max = 100, message = "Токен должен быть не длиннее 100 символов")
    private String token;
}
