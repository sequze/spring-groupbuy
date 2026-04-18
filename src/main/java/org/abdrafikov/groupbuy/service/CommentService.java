package org.abdrafikov.groupbuy.service;

import lombok.RequiredArgsConstructor;
import org.abdrafikov.groupbuy.dto.CommentDto;
import org.abdrafikov.groupbuy.dto.CommentForm;
import org.abdrafikov.groupbuy.dto.PurchaseItemDto;
import org.abdrafikov.groupbuy.exception.AccessDeniedException;
import org.abdrafikov.groupbuy.exception.ResourceNotFoundException;
import org.abdrafikov.groupbuy.model.Comment;
import org.abdrafikov.groupbuy.model.PurchaseItem;
import org.abdrafikov.groupbuy.model.User;
import org.abdrafikov.groupbuy.repository.CommentRepository;
import org.abdrafikov.groupbuy.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final PurchaseItemService purchaseItemService;
    private final CurrentUserService currentUserService;
    private final WorkspaceService workspaceService;

    @Transactional(readOnly = true)
    public List<CommentDto> getByWorkspace(Long workspaceId) {
        Long currentUserId = currentUserService.getCurrentUserId();
        workspaceService.getAccessibleWorkspace(workspaceId, currentUserId);
        return commentRepository.findByPurchaseItemWorkspaceIdOrderByCreatedAtDesc(workspaceId).stream()
                .map(comment -> toDto(comment, currentUserId))
                .toList();
    }

    @Transactional(readOnly = true)
    public CommentDto getById(Long id) {
        Long currentUserId = currentUserService.getCurrentUserId();
        Comment comment = getAccessibleComment(id, currentUserId);
        return toDto(comment, currentUserId);
    }

    @Transactional(readOnly = true)
    public CommentForm getCreateForm(Long purchaseItemId) {
        Long currentUserId = currentUserService.getCurrentUserId();
        purchaseItemService.getAccessibleItem(purchaseItemId, currentUserId);

        CommentForm form = new CommentForm();
        form.setPurchaseItemId(purchaseItemId);
        return form;
    }

    @Transactional(readOnly = true)
    public CommentForm getEditForm(Long id) {
        Long currentUserId = currentUserService.getCurrentUserId();
        Comment comment = getAccessibleComment(id, currentUserId);
        ensureCanManage(comment, currentUserId);

        CommentForm form = new CommentForm();
        form.setPurchaseItemId(comment.getPurchaseItem().getId());
        form.setContent(comment.getContent());
        return form;
    }

    @Transactional(readOnly = true)
    public List<PurchaseItemDto> getPurchaseItemOptions(Long workspaceId) {
        return purchaseItemService.getByWorkspace(workspaceId);
    }

    @Transactional
    public CommentDto create(CommentForm form) {
        Long currentUserId = currentUserService.getCurrentUserId();
        PurchaseItem purchaseItem = purchaseItemService.getAccessibleItem(form.getPurchaseItemId(), currentUserId);
        User author = getCurrentUserEntity();

        Comment comment = new Comment();
        comment.setPurchaseItem(purchaseItem);
        comment.setAuthor(author);
        comment.setContent(form.getContent());
        commentRepository.save(comment);
        return toDto(comment, currentUserId);
    }

    @Transactional
    public CommentDto update(Long id, CommentForm form) {
        Long currentUserId = currentUserService.getCurrentUserId();
        Comment comment = getAccessibleComment(id, currentUserId);
        ensureCanManage(comment, currentUserId);

        PurchaseItem purchaseItem = purchaseItemService.getAccessibleItem(form.getPurchaseItemId(), currentUserId);
        comment.setPurchaseItem(purchaseItem);
        comment.setContent(form.getContent());
        comment.setEdited(true);
        return toDto(comment, currentUserId);
    }

    @Transactional
    public void delete(Long id) {
        Long currentUserId = currentUserService.getCurrentUserId();
        Comment comment = getAccessibleComment(id, currentUserId);
        ensureCanManage(comment, currentUserId);
        commentRepository.delete(comment);
    }

    @Transactional(readOnly = true)
    public Comment getAccessibleComment(Long id, Long currentUserId) {
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Комментарий не найден"));
        workspaceService.getAccessibleWorkspace(comment.getPurchaseItem().getWorkspace().getId(), currentUserId);
        return comment;
    }

    private void ensureCanManage(Comment comment, Long currentUserId) {
        if (workspaceService.isGlobalAdmin()) {
            return;
        }
        boolean isAuthor = comment.getAuthor().getId().equals(currentUserId);
        boolean isWorkspaceAdmin = workspaceService.isWorkspaceAdmin(comment.getPurchaseItem().getWorkspace().getId(), currentUserId);
        if (!isAuthor && !isWorkspaceAdmin) {
            throw new AccessDeniedException("Изменять комментарий может автор или администратор workspace");
        }
    }

    private User getCurrentUserEntity() {
        return userRepository.findById(currentUserService.getCurrentUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Текущий пользователь не найден"));
    }

    private CommentDto toDto(Comment comment, Long currentUserId) {
        boolean canManage = comment.getAuthor().getId().equals(currentUserId)
                || workspaceService.isWorkspaceAdmin(comment.getPurchaseItem().getWorkspace().getId(), currentUserId)
                || workspaceService.isGlobalAdmin();

        return CommentDto.builder()
                .id(comment.getId())
                .purchaseItemId(comment.getPurchaseItem().getId())
                .purchaseItemTitle(comment.getPurchaseItem().getTitle())
                .authorDisplayName(comment.getAuthor().getDisplayName())
                .content(comment.getContent())
                .edited(comment.isEdited())
                .canEdit(canManage)
                .canDelete(canManage)
                .build();
    }
}
