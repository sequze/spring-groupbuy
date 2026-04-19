package org.abdrafikov.groupbuy.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CommentForm {

    @NotNull(message = "Позиция закупки обязательна")
    private Long purchaseItemId;

    @NotBlank(message = "Комментарий обязателен")
    @Size(max = 2000, message = "Комментарий должен быть не длиннее 2000 символов")
    private String content;
}
