package org.abdrafikov.groupbuy.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.abdrafikov.groupbuy.dto.PurchaseItemDto;
import org.abdrafikov.groupbuy.dto.PurchaseItemForm;
import org.abdrafikov.groupbuy.dto.WorkspaceDto;
import org.abdrafikov.groupbuy.exception.AccessDeniedException;
import org.abdrafikov.groupbuy.exception.ResourceNotFoundException;
import org.abdrafikov.groupbuy.mapper.PurchaseItemMapper;
import org.abdrafikov.groupbuy.model.PurchaseItem;
import org.abdrafikov.groupbuy.model.User;
import org.abdrafikov.groupbuy.model.Workspace;
import org.abdrafikov.groupbuy.model.choices.PurchaseItemStatus;
import org.abdrafikov.groupbuy.repository.PurchaseItemRepository;
import org.abdrafikov.groupbuy.repository.UserRepository;
import org.abdrafikov.groupbuy.service.currency.CurrencyConversionResult;
import org.abdrafikov.groupbuy.service.currency.CurrencyConversionException;
import org.abdrafikov.groupbuy.service.currency.CurrencyConversionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
@Slf4j
public class PurchaseItemService {

    private final PurchaseItemRepository purchaseItemRepository;
    private final UserRepository userRepository;
    private final WorkspaceService workspaceService;
    private final CurrentUserService currentUserService;
    private final CurrencyConversionService currencyConversionService;
    private final PurchaseItemMapper purchaseItemMapper;

    @Transactional(readOnly = true)
    public List<PurchaseItemDto> getByWorkspace(Long workspaceId) {
        return getByWorkspace(workspaceId, null, null);
    }

    @Transactional(readOnly = true)
    public List<PurchaseItemDto> getByWorkspace(Long workspaceId, PurchaseItemStatus status, String titleQuery) {
        Long currentUserId = currentUserService.getCurrentUserId();
        workspaceService.getAccessibleWorkspace(workspaceId, currentUserId);
        List<PurchaseItem> items = hasFilters(status, titleQuery)
                ? purchaseItemRepository.search(workspaceId, status, titleQuery)
                : purchaseItemRepository.findByWorkspaceIdOrderByCreatedAtDesc(workspaceId);
        return items.stream()
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
    public boolean canApproveOnCreate(Long workspaceId) {
        Long currentUserId = currentUserService.getCurrentUserId();
        workspaceService.getAccessibleWorkspace(workspaceId, currentUserId);
        return workspaceService.isGlobalAdmin()
                || workspaceService.isWorkspaceAdmin(workspaceId, currentUserId);
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
        form.setProductUrl(item.getProductUrl());
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
        applyCreateForm(item, form, author, currentUserId);
        purchaseItemRepository.save(item);
        log.info(
                "Purchase item created: itemId={}, workspaceId={}, authorUserId={}, status={}",
                item.getId(),
                workspace.getId(),
                author.getId(),
                item.getStatus()
        );
        return toDto(item, currentUserId);
    }

    @Transactional
    public PurchaseItemDto update(Long id, PurchaseItemForm form) {
        Long currentUserId = currentUserService.getCurrentUserId();
        PurchaseItem item = getAccessibleItem(id, currentUserId);
        ensureCanEditContent(item, currentUserId);
        PurchaseItemStatus previousStatus = item.getStatus();

        if (!item.getWorkspace().getId().equals(form.getWorkspaceId())) {
            Workspace newWorkspace = workspaceService.getAccessibleWorkspace(form.getWorkspaceId(), currentUserId);
            item.setWorkspace(newWorkspace);
        }

        applyEditForm(item, form, getCurrentUserEntity(), currentUserId);
        if (previousStatus != item.getStatus()) {
            log.info(
                    "Purchase item status changed: itemId={}, workspaceId={}, actorUserId={}, from={}, to={}",
                    item.getId(),
                    item.getWorkspace().getId(),
                    currentUserId,
                    previousStatus,
                    item.getStatus()
            );
        }
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
        CurrencyConversionResult currentPrice = getCurrentBasePrice(
                item.getPriceAmount(),
                item.getPriceCurrency()
        );

        return purchaseItemMapper.toDto(item, canEdit, canModerateStatus, currentPrice);
    }

    private CurrencyConversionResult getCurrentBasePrice(BigDecimal amount, String currency) {
        try {
            return currencyConversionService.convertToBase(amount, currency);
        } catch (CurrencyConversionException ex) {
            return new CurrencyConversionResult(amount, currency);
        }
    }

    private boolean hasFilters(PurchaseItemStatus status, String titleQuery) {
        return status != null || (titleQuery != null && !titleQuery.isBlank());
    }

    private void applyCreateForm(PurchaseItem item, PurchaseItemForm form, User actingUser, Long currentUserId) {
        item.setTitle(form.getTitle());
        item.setDescription(form.getDescription());
        item.setProductUrl(normalizeProductUrl(form.getProductUrl()));
        item.setQuantity(form.getQuantity());
        item.setUnit(form.getUnit());
        item.setPriceAmount(form.getPriceAmount());
        item.setPriceCurrency(normalizeCurrency(form.getPriceCurrency()));
        item.setRejectionReason(null);
        item.setRejectedAt(null);
        item.setRejectedBy(null);

        if (form.isApproveImmediately()) {
            if (!canModerateStatus(item, currentUserId)) {
                throw new AccessDeniedException("Сразу утверждать позицию может только администратор workspace");
            }
            item.setStatus(PurchaseItemStatus.APPROVED);
            item.setApprovedAt(LocalDateTime.now());
            item.setApprovedBy(actingUser);
            return;
        }

        item.setStatus(PurchaseItemStatus.NEW);
        item.setApprovedAt(null);
        item.setApprovedBy(null);
    }

    private void applyEditForm(PurchaseItem item, PurchaseItemForm form, User actingUser, Long currentUserId) {
        item.setTitle(form.getTitle());
        item.setDescription(form.getDescription());
        item.setProductUrl(normalizeProductUrl(form.getProductUrl()));
        item.setQuantity(form.getQuantity());
        item.setUnit(form.getUnit());
        item.setPriceAmount(form.getPriceAmount());
        item.setPriceCurrency(normalizeCurrency(form.getPriceCurrency()));

        PurchaseItemStatus nextStatus = PurchaseItemStatus.NEW;
        if (canModerateStatus(item, currentUserId)) {
            nextStatus = form.getStatus();
        }

        item.setStatus(nextStatus);

        if (nextStatus == PurchaseItemStatus.APPROVED) {
            item.setApprovedAt(LocalDateTime.now());
            item.setApprovedBy(actingUser);
            item.setRejectedAt(null);
            item.setRejectedBy(null);
            item.setRejectionReason(null);
        } else if (nextStatus == PurchaseItemStatus.REJECTED) {
            item.setApprovedAt(null);
            item.setApprovedBy(null);
            item.setRejectedAt(LocalDateTime.now());
            item.setRejectedBy(actingUser);
            item.setRejectionReason(normalizeRejectionReason(form.getRejectionReason()));
        } else {
            item.setApprovedAt(null);
            item.setApprovedBy(null);
            item.setRejectedAt(null);
            item.setRejectedBy(null);
            item.setRejectionReason(null);
        }
    }

    private String normalizeProductUrl(String productUrl) {
        if (productUrl == null || productUrl.isBlank()) {
            return null;
        }
        return productUrl.trim();
    }

    private String normalizeRejectionReason(String rejectionReason) {
        if (rejectionReason == null || rejectionReason.isBlank()) {
            return null;
        }
        return rejectionReason.trim();
    }

    private String normalizeCurrency(String currency) {
        if (currency == null || currency.isBlank()) {
            return null;
        }
        return currency.trim().toUpperCase(Locale.ROOT);
    }
}
