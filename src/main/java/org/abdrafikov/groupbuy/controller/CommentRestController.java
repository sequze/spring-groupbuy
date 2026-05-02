package org.abdrafikov.groupbuy.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.abdrafikov.groupbuy.api.generated.api.CommentsApi;
import org.abdrafikov.groupbuy.api.generated.dto.CommentDto;
import org.abdrafikov.groupbuy.api.generated.dto.CreateCommentRequest;
import org.abdrafikov.groupbuy.api.generated.dto.UpdateCommentRequest;
import org.abdrafikov.groupbuy.service.CommentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.time.ZoneId;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class CommentRestController implements CommentsApi {

    private final CommentService commentService;

    @Override
    public ResponseEntity<List<CommentDto>> listComments(Long purchaseItemId) {
        List<CommentDto> comments = (purchaseItemId == null
                ? commentService.getAllAccessible()
                : commentService.getByPurchaseItem(purchaseItemId)).stream()
                .map(this::toGeneratedDto)
                .toList();
        return ResponseEntity.ok(comments);
    }

    @Override
    public ResponseEntity<CommentDto> getComment(Long id) {
        return ResponseEntity.ok(toGeneratedDto(commentService.getById(id)));
    }

    @Override
    public ResponseEntity<CommentDto> createComment(@Valid CreateCommentRequest createCommentRequest) {
        org.abdrafikov.groupbuy.dto.CommentDto comment = commentService.create(
                createCommentRequest.getPurchaseItemId(),
                createCommentRequest.getContent()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(toGeneratedDto(comment));
    }

    @Override
    public ResponseEntity<CommentDto> updateComment(Long id, @Valid UpdateCommentRequest updateCommentRequest) {
        return ResponseEntity.ok(toGeneratedDto(commentService.updateContent(id, updateCommentRequest.getContent())));
    }

    @Override
    public ResponseEntity<Void> deleteComment(Long id) {
        commentService.delete(id);
        return ResponseEntity.noContent().build();
    }

    private CommentDto toGeneratedDto(org.abdrafikov.groupbuy.dto.CommentDto comment) {
        CommentDto dto = new CommentDto();
        dto.setId(comment.getId());
        dto.setPurchaseItemId(comment.getPurchaseItemId());
        dto.setAuthorId(comment.getAuthorId());
        dto.setAuthorDisplayName(comment.getAuthorDisplayName());
        dto.setContent(comment.getContent());
        dto.setCreatedAt(comment.getCreatedAt().atZone(ZoneId.systemDefault()).toOffsetDateTime());
        return dto;
    }
}
