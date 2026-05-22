package org.abdrafikov.groupbuy.mapper;

import org.abdrafikov.groupbuy.dto.CommentDto;
import org.abdrafikov.groupbuy.model.Comment;
import org.springframework.stereotype.Component;

@Component
public class CommentMapper {

    public CommentDto toDto(Comment comment, boolean canManage) {
        return CommentDto.builder()
                .id(comment.getId())
                .purchaseItemId(comment.getPurchaseItem().getId())
                .purchaseItemTitle(comment.getPurchaseItem().getTitle())
                .authorId(comment.getAuthor().getId())
                .authorDisplayName(comment.getAuthor().getDisplayName())
                .content(comment.getContent())
                .createdAt(comment.getCreatedAt())
                .edited(comment.isEdited())
                .canEdit(canManage)
                .canDelete(canManage)
                .build();
    }
}
