package org.abdrafikov.groupbuy.service;

import lombok.RequiredArgsConstructor;
import org.abdrafikov.groupbuy.dto.PurchaseItemDto;
import org.abdrafikov.groupbuy.dto.PurchaseItemForm;
import org.abdrafikov.groupbuy.dto.WorkspaceDto;
import org.abdrafikov.groupbuy.exception.AccessDeniedException;
import org.abdrafikov.groupbuy.exception.ResourceNotFoundException;
import org.abdrafikov.groupbuy.model.PurchaseItem;
import org.abdrafikov.groupbuy.model.User;
import org.abdrafikov.groupbuy.model.Workspace;
import org.abdrafikov.groupbuy.model.choices.PurchaseItemStatus;
import org.abdrafikov.groupbuy.repository.PurchaseItemRepository;
import org.abdrafikov.groupbuy.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class PurchaseItemService {

    private final PurchaseItemRepository purchaseItemRepository;
    private final UserRepository userRepository;
    private final WorkspaceService workspaceService;
    private final CurrentUserService currentUserService;

    @Transactional(readOnly = true)
    public List<PurchaseItemDto> getByWorkspace(Long workspaceId) {
        Long currentUserId = currentUserService.getCurrentUserId();
        workspaceService.getAccessibleWorkspace(workspaceId, currentUserId);
        return purchaseItemRepository.findByWorkspaceIdOrderByCreatedAtDesc(workspaceId).stream()
                .map(item -> toDto(item, currentUserId))
                .toList();
    }

    @Transactional(readOnly = true)
    public PurchaseItemDto getById(Long id) {
        Long currentUserId = currentUserService.getCurrentUserId();
        PurchaseItem item = getAccessibleItem(id, currentUserId);
        return toDto(item, currentUserId);
    }

    @Transactional(readOnly = true)
    public PurchaseItemForm getCreateForm(Long workspaceId) {
        Long currentUserId = currentUserService.getCurrentUserId();
        workspaceService.getAccessibleWorkspace(workspaceId, currentUserId);

        PurchaseItemForm form = new PurchaseItemForm();
        form.setWorkspaceId(workspaceId);
        form.setStatus(PurchaseItemStatus.NEW);
        return form;
    }

    @Transactional(readOnly = true)
    public PurchaseItemForm getEditForm(Long id) {
        Long currentUserId = currentUserService.getCurrentUserId();
        PurchaseItem item = getAccessibleItem(id, currentUserId);
        ensureCanEditContent(item, currentUserId);

        PurchaseItemForm form = new PurchaseItemForm();
        form.setWorkspaceId(item.getWorkspace().getId());
        form.setTitle(item.getTitle());
        form.setDescription(item.getDescription());
        form.setQuantity(item.getQuantity());
        form.setUnit(item.getUnit());
        form.setPriceAmount(item.getPriceAmount());
        form.setPriceCurrency(item.getPriceCurrency());
        form.setStatus(item.getStatus());
        form.setRejectionReason(item.getRejectionReason());
        return form;
    }

    @Transactional(readOnly = true)
    public boolean canModerateStatus(Long purchaseItemId) {
        Long currentUserId = currentUserService.getCurrentUserId();
        PurchaseItem item = getAccessibleItem(purchaseItemId, currentUserId);
        return canModerateStatus(item, currentUserId);
    }

    @Transactional(readOnly = true)
    public List<WorkspaceDto> getWorkspaceOptions() {
        return workspaceService.getWorkspaceOptions();
    }

    @Transactional
    public PurchaseItemDto create(PurchaseItemForm form) {
        Long currentUserId = currentUserService.getCurrentUserId();
        Workspace workspace = workspaceService.getAccessibleWorkspace(form.getWorkspaceId(), currentUserId);
        User author = getCurrentUserEntity();

        PurchaseItem item = new PurchaseItem();
        item.setWorkspace(workspace);
        item.setAuthor(author);
        applyCreateForm(item, form);
        purchaseItemRepository.save(item);
        return toDto(item, currentUserId);
    }

    @Transactional
    public PurchaseItemDto update(Long id, PurchaseItemForm form) {
        Long currentUserId = currentUserService.getCurrentUserId();
        PurchaseItem item = getAccessibleItem(id, currentUserId);
        ensureCanEditContent(item, currentUserId);

        if (!item.getWorkspace().getId().equals(form.getWorkspaceId())) {
            Workspace newWorkspace = workspaceService.getAccessibleWorkspace(form.getWorkspaceId(), currentUserId);
            item.setWorkspace(newWorkspace);
        }

        applyEditForm(item, form, getCurrentUserEntity(), currentUserId);
        return toDto(item, currentUserId);
    }

    @Transactional
    public void delete(Long id) {
        Long currentUserId = currentUserService.getCurrentUserId();
        PurchaseItem item = getAccessibleItem(id, currentUserId);
        ensureCanEditContent(item, currentUserId);
        purchaseItemRepository.delete(item);
    }

    @Transactional(readOnly = true)
    public PurchaseItem getAccessibleItem(Long id, Long currentUserId) {
        PurchaseItem item = purchaseItemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Позиция закупки не найдена"));
        workspaceService.getAccessibleWorkspace(item.getWorkspace().getId(), currentUserId);
        return item;
    }

    private User getCurrentUserEntity() {
        return userRepository.findById(currentUserService.getCurrentUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Текущий пользователь не найден"));
    }

    private void ensureCanEditContent(PurchaseItem item, Long currentUserId) {
        if (workspaceService.isGlobalAdmin()) {
            return;
        }
        boolean isAuthor = item.getAuthor().getId().equals(currentUserId);
        boolean isWorkspaceAdmin = workspaceService.isWorkspaceAdmin(item.getWorkspace().getId(), currentUserId);
        if (!isAuthor && !isWorkspaceAdmin) {
            throw new AccessDeniedException("Изменять содержимое позиции может автор или администратор workspace");
        }
    }

    private boolean canModerateStatus(PurchaseItem item, Long currentUserId) {
        return workspaceService.isGlobalAdmin()
                || workspaceService.isWorkspaceAdmin(item.getWorkspace().getId(), currentUserId);
    }

    private PurchaseItemDto toDto(PurchaseItem item, Long currentUserId) {
        boolean canEdit = item.getAuthor().getId().equals(currentUserId)
                || workspaceService.isWorkspaceAdmin(item.getWorkspace().getId(), currentUserId)
                || workspaceService.isGlobalAdmin();
        boolean canModerateStatus = canModerateStatus(item, currentUserId);

        return PurchaseItemDto.builder()
                .id(item.getId())
                .workspaceId(item.getWorkspace().getId())
                .workspaceName(item.getWorkspace().getName())
                .authorDisplayName(item.getAuthor().getDisplayName())
                .title(item.getTitle())
                .description(item.getDescription())
                .quantity(item.getQuantity())
                .unit(item.getUnit())
                .priceAmount(item.getPriceAmount())
                .priceCurrency(item.getPriceCurrency())
                .status(item.getStatus())
                .rejectionReason(item.getRejectionReason())
                .canEdit(canEdit)
                .canModerateStatus(canModerateStatus)
                .canDelete(canEdit)
                .build();
    }

    private void applyCreateForm(PurchaseItem item, PurchaseItemForm form) {
        item.setTitle(form.getTitle());
        item.setDescription(form.getDescription());
        item.setQuantity(form.getQuantity());
        item.setUnit(form.getUnit());
        item.setPriceAmount(form.getPriceAmount());
        item.setPriceCurrency(form.getPriceCurrency() == null ? null : form.getPriceCurrency().toUpperCase());
        item.setBasePriceAmount(form.getPriceAmount());
        item.setBaseCurrency(form.getPriceCurrency() == null ? "RUB" : form.getPriceCurrency().toUpperCase());
        item.setStatus(PurchaseItemStatus.NEW);
        item.setRejectionReason(null);
        item.setApprovedAt(null);
        item.setApprovedBy(null);
        item.setRejectedAt(null);
        item.setRejectedBy(null);
    }

    private void applyEditForm(PurchaseItem item, PurchaseItemForm form, User actingUser, Long currentUserId) {
        item.setTitle(form.getTitle());
        item.setDescription(form.getDescription());
        item.setQuantity(form.getQuantity());
        item.setUnit(form.getUnit());
        item.setPriceAmount(form.getPriceAmount());
        item.setPriceCurrency(form.getPriceCurrency() == null ? null : form.getPriceCurrency().toUpperCase());
        item.setBasePriceAmount(form.getPriceAmount());
        item.setBaseCurrency(form.getPriceCurrency() == null ? "RUB" : form.getPriceCurrency().toUpperCase());

        if (!canModerateStatus(item, currentUserId)) {
            if (form.getStatus() != item.getStatus()) {
                throw new AccessDeniedException("Менять статус позиции может только администратор workspace");
            }
            if (!Objects.equals(form.getRejectionReason(), item.getRejectionReason())) {
                throw new AccessDeniedException("Менять причину отклонения может только администратор workspace");
            }
            return;
        }

        item.setStatus(form.getStatus());
        item.setRejectionReason(form.getRejectionReason());

        if (form.getStatus() == PurchaseItemStatus.APPROVED) {
            item.setApprovedAt(LocalDateTime.now());
            item.setApprovedBy(actingUser);
            item.setRejectedAt(null);
            item.setRejectedBy(null);
            item.setRejectionReason(null);
        } else if (form.getStatus() == PurchaseItemStatus.REJECTED) {
            item.setRejectedAt(LocalDateTime.now());
            item.setRejectedBy(actingUser);
            item.setApprovedAt(null);
            item.setApprovedBy(null);
        } else {
            item.setApprovedAt(null);
            item.setApprovedBy(null);
            item.setRejectedAt(null);
            item.setRejectedBy(null);
            item.setRejectionReason(null);
        }
    }
}
