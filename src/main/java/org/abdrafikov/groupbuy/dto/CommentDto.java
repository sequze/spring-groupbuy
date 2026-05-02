package org.abdrafikov.groupbuy.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CommentDto {

    private final Long id;
    private final Long purchaseItemId;
    private final String purchaseItemTitle;
    private final String authorDisplayName;
    private final String content;
    private final boolean edited;
    private final boolean canEdit;
    private final boolean canDelete;
}
