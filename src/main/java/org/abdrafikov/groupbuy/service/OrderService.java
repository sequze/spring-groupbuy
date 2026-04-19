package org.abdrafikov.groupbuy.service;

import org.abdrafikov.groupbuy.dto.OrderDto;
import org.abdrafikov.groupbuy.dto.OrderForm;
import org.abdrafikov.groupbuy.dto.OrderItemDto;
import org.abdrafikov.groupbuy.dto.OrderItemForm;
import org.abdrafikov.groupbuy.dto.OrderItemOptionDto;
import org.abdrafikov.groupbuy.exception.AccessDeniedException;
import org.abdrafikov.groupbuy.exception.ResourceNotFoundException;
import org.abdrafikov.groupbuy.model.Order;
import org.abdrafikov.groupbuy.model.OrderItem;
import org.abdrafikov.groupbuy.model.PurchaseItem;
import org.abdrafikov.groupbuy.model.User;
import org.abdrafikov.groupbuy.model.Workspace;
import org.abdrafikov.groupbuy.model.choices.OrderStatus;
import org.abdrafikov.groupbuy.model.choices.PurchaseItemStatus;
import org.abdrafikov.groupbuy.repository.OrderRepository;
import org.abdrafikov.groupbuy.repository.PurchaseItemRepository;
import org.abdrafikov.groupbuy.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final PurchaseItemRepository purchaseItemRepository;
    private final UserRepository userRepository;
    private final WorkspaceService workspaceService;
    private final CurrentUserService currentUserService;

    public OrderService(
            OrderRepository orderRepository,
            PurchaseItemRepository purchaseItemRepository,
            UserRepository userRepository,
            WorkspaceService workspaceService,
            CurrentUserService currentUserService
    ) {
        this.orderRepository = orderRepository;
        this.purchaseItemRepository = purchaseItemRepository;
        this.userRepository = userRepository;
        this.workspaceService = workspaceService;
        this.currentUserService = currentUserService;
    }

    @Transactional(readOnly = true)
    public List<OrderDto> getByWorkspace(Long workspaceId) {
        Long currentUserId = currentUserService.getCurrentUserId();
        workspaceService.getAccessibleWorkspace(workspaceId, currentUserId);
        return orderRepository.findByWorkspaceIdOrderByCreatedAtDesc(workspaceId).stream()
                .map(order -> toDto(order, currentUserId))
                .toList();
    }

    @Transactional(readOnly = true)
    public OrderDto getById(Long id) {
        Long currentUserId = currentUserService.getCurrentUserId();
        return toDto(getAccessibleOrder(id, currentUserId), currentUserId);
    }

    @Transactional(readOnly = true)
    public OrderForm getCreateForm(Long workspaceId) {
        Long currentUserId = currentUserService.getCurrentUserId();
        workspaceService.getAccessibleWorkspace(workspaceId, currentUserId);
        workspaceService.ensureWorkspaceAdmin(workspaceId, currentUserId);

        OrderForm form = new OrderForm();
        form.setWorkspaceId(workspaceId);
        form.setStatus(OrderStatus.DRAFT);
        form.setItems(buildAvailableItemForms(workspaceId, Map.of()));
        return form;
    }

    @Transactional(readOnly = true)
    public OrderForm getEditForm(Long id) {
        Long currentUserId = currentUserService.getCurrentUserId();
        Order order = getAccessibleOrder(id, currentUserId);
        ensureCanManage(order, currentUserId);

        Map<Long, Integer> selectedItems = new LinkedHashMap<>();
        order.getItems().stream()
                .sorted(Comparator.comparing(item -> item.getPurchaseItem().getTitle()))
                .forEach(item -> selectedItems.put(item.getPurchaseItem().getId(), item.getQuantitySnapshot()));

        OrderForm form = new OrderForm();
        form.setWorkspaceId(order.getWorkspace().getId());
        form.setTitle(order.getTitle());
        form.setDescription(order.getDescription());
        form.setStatus(order.getStatus());
        form.setItems(buildAvailableItemForms(order.getWorkspace().getId(), selectedItems));
        return form;
    }

    @Transactional(readOnly = true)
    public List<OrderItemOptionDto> getItemOptions(OrderForm form) {
        if (form.getWorkspaceId() == null) {
            return List.of();
        }

        Long currentUserId = currentUserService.getCurrentUserId();
        workspaceService.getAccessibleWorkspace(form.getWorkspaceId(), currentUserId);

        Map<Long, PurchaseItem> approvedItems = purchaseItemRepository
                .findByWorkspaceIdAndStatusOrderByCreatedAtDesc(form.getWorkspaceId(), PurchaseItemStatus.APPROVED).stream()
                .collect(LinkedHashMap::new, (map, item) -> map.put(item.getId(), item), Map::putAll);

        if (form.getItems() == null || form.getItems().isEmpty()) {
            return approvedItems.values().stream()
                    .map(this::toOptionDto)
                    .toList();
        }

        return form.getItems().stream()
                .map(OrderItemForm::getPurchaseItemId)
                .map(approvedItems::get)
                .filter(item -> item != null)
                .map(this::toOptionDto)
                .toList();
    }

    @Transactional
    public OrderDto create(OrderForm form) {
        Long currentUserId = currentUserService.getCurrentUserId();
        Workspace workspace = workspaceService.getAccessibleWorkspace(form.getWorkspaceId(), currentUserId);
        workspaceService.ensureWorkspaceAdmin(workspace.getId(), currentUserId);
        User currentUser = getCurrentUserEntity();

        Order order = new Order();
        order.setWorkspace(workspace);
        order.setCreatedBy(currentUser);
        applyCreateForm(order, form, workspace);
        orderRepository.save(order);
        return toDto(order, currentUserId);
    }

    @Transactional
    public OrderDto update(Long id, OrderForm form) {
        Long currentUserId = currentUserService.getCurrentUserId();
        Order order = getAccessibleOrder(id, currentUserId);
        ensureCanManage(order, currentUserId);

        applyForm(order, form, order.getWorkspace());
        return toDto(order, currentUserId);
    }

    @Transactional
    public void delete(Long id) {
        Long currentUserId = currentUserService.getCurrentUserId();
        Order order = getAccessibleOrder(id, currentUserId);
        ensureCanManage(order, currentUserId);
        orderRepository.delete(order);
    }

    @Transactional(readOnly = true)
    public Order getAccessibleOrder(Long id, Long currentUserId) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Заказ не найден"));
        workspaceService.getAccessibleWorkspace(order.getWorkspace().getId(), currentUserId);
        return order;
    }

    private void ensureCanManage(Order order, Long currentUserId) {
        if (workspaceService.isGlobalAdmin()) {
            return;
        }
        if (!workspaceService.isWorkspaceAdmin(order.getWorkspace().getId(), currentUserId)) {
            throw new AccessDeniedException("Изменять заказ может только администратор workspace");
        }
    }

    private User getCurrentUserEntity() {
        return userRepository.findById(currentUserService.getCurrentUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Текущий пользователь не найден"));
    }

    private OrderDto toDto(Order order, Long currentUserId) {
        boolean canManage = workspaceService.isWorkspaceAdmin(order.getWorkspace().getId(), currentUserId)
                || workspaceService.isGlobalAdmin();

        List<OrderItemDto> items = order.getItems().stream()
                .sorted(Comparator.comparing(OrderItem::getItemTitleSnapshot))
                .map(item -> OrderItemDto.builder()
                        .purchaseItemId(item.getPurchaseItem().getId())
                        .title(item.getItemTitleSnapshot())
                        .quantity(item.getQuantitySnapshot())
                        .unit(item.getPurchaseItem().getUnit())
                        .price(item.getPriceSnapshot())
                        .currency(item.getCurrencySnapshot())
                        .subtotal(item.getPriceSnapshot() == null ? null : item.getPriceSnapshot().multiply(BigDecimal.valueOf(item.getQuantitySnapshot())))
                        .build())
                .toList();

        return OrderDto.builder()
                .id(order.getId())
                .workspaceId(order.getWorkspace().getId())
                .workspaceName(order.getWorkspace().getName())
                .createdByDisplayName(order.getCreatedBy().getDisplayName())
                .title(order.getTitle())
                .description(order.getDescription())
                .status(order.getStatus())
                .totalAmount(order.getTotalAmount())
                .currency(order.getCurrency())
                .itemCount(items.size())
                .items(items)
                .canEdit(canManage)
                .canDelete(canManage)
                .build();
    }

    private List<OrderItemForm> buildAvailableItemForms(Long workspaceId, Map<Long, Integer> selectedItems) {
        List<PurchaseItem> purchaseItems = purchaseItemRepository
                .findByWorkspaceIdAndStatusOrderByCreatedAtDesc(workspaceId, PurchaseItemStatus.APPROVED);

        List<OrderItemForm> result = new ArrayList<>();
        for (PurchaseItem purchaseItem : purchaseItems) {
            OrderItemForm itemForm = new OrderItemForm();
            itemForm.setPurchaseItemId(purchaseItem.getId());
            itemForm.setSelected(selectedItems.containsKey(purchaseItem.getId()));
            itemForm.setQuantity(selectedItems.getOrDefault(purchaseItem.getId(), purchaseItem.getQuantity()));
            result.add(itemForm);
        }
        return result;
    }

    private OrderItemOptionDto toOptionDto(PurchaseItem purchaseItem) {
        return OrderItemOptionDto.builder()
                .purchaseItemId(purchaseItem.getId())
                .title(purchaseItem.getTitle())
                .unit(purchaseItem.getUnit())
                .priceLabel(formatPriceLabel(purchaseItem))
                .build();
    }

    private String formatPriceLabel(PurchaseItem purchaseItem) {
        BigDecimal price = purchaseItem.getBasePriceAmount() != null ? purchaseItem.getBasePriceAmount() : purchaseItem.getPriceAmount();
        String currency = purchaseItem.getBaseCurrency() != null ? purchaseItem.getBaseCurrency() : purchaseItem.getPriceCurrency();
        if (price == null) {
            return "Цена не указана";
        }
        return price + " " + (currency == null ? "" : currency);
    }

    private void applyForm(Order order, OrderForm form, Workspace workspace) {
        order.setTitle(form.getTitle());
        order.setDescription(form.getDescription());
        order.setStatus(form.getStatus());
        syncOrderItems(order, form, workspace);
        applyStatusDates(order, form.getStatus());
    }

    private void applyCreateForm(Order order, OrderForm form, Workspace workspace) {
        order.setTitle(form.getTitle());
        order.setDescription(form.getDescription());
        order.setStatus(OrderStatus.DRAFT);
        syncOrderItems(order, form, workspace);
        applyStatusDates(order, OrderStatus.DRAFT);
    }

    private void syncOrderItems(Order order, OrderForm form, Workspace workspace) {
        List<OrderItemForm> selectedForms = form.getItems() == null ? List.of() : form.getItems().stream()
                .filter(OrderItemForm::isSelected)
                .toList();

        if (selectedForms.isEmpty()) {
            throw new IllegalArgumentException("Нужно выбрать хотя бы одну позицию для заказа");
        }

        Map<Long, PurchaseItem> workspaceItems = purchaseItemRepository
                .findByWorkspaceIdAndStatusOrderByCreatedAtDesc(workspace.getId(), PurchaseItemStatus.APPROVED).stream()
                .collect(LinkedHashMap::new, (map, item) -> map.put(item.getId(), item), Map::putAll);

        order.getItems().clear();
        BigDecimal total = BigDecimal.ZERO;
        String currency = null;

        for (OrderItemForm itemForm : selectedForms) {
            PurchaseItem purchaseItem = workspaceItems.get(itemForm.getPurchaseItemId());
            if (purchaseItem == null) {
                throw new IllegalArgumentException("В заказ можно добавлять только APPROVED-позиции текущего workspace");
            }

            Integer quantity = itemForm.getQuantity();
            if (quantity == null || quantity < 1) {
                throw new IllegalArgumentException("Количество для выбранных позиций должно быть больше 0");
            }

            BigDecimal price = purchaseItem.getBasePriceAmount() != null ? purchaseItem.getBasePriceAmount() : purchaseItem.getPriceAmount();
            String itemCurrency = purchaseItem.getBaseCurrency() != null ? purchaseItem.getBaseCurrency() : purchaseItem.getPriceCurrency();
            if (price == null || itemCurrency == null || itemCurrency.isBlank()) {
                throw new IllegalArgumentException("У всех позиций заказа должна быть указана цена и валюта");
            }
            if (currency == null) {
                currency = itemCurrency.toUpperCase();
            } else if (!currency.equalsIgnoreCase(itemCurrency)) {
                throw new IllegalArgumentException("Все позиции заказа должны быть в одной валюте");
            }

            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setPurchaseItem(purchaseItem);
            orderItem.setItemTitleSnapshot(purchaseItem.getTitle());
            orderItem.setQuantitySnapshot(quantity);
            orderItem.setPriceSnapshot(price);
            orderItem.setCurrencySnapshot(currency);
            order.getItems().add(orderItem);

            total = total.add(price.multiply(BigDecimal.valueOf(quantity.longValue())));
        }

        order.setTotalAmount(total);
        order.setCurrency(currency);
    }

    private void applyStatusDates(Order order, OrderStatus status) {
        if (status == OrderStatus.SUBMITTED) {
            if (order.getSubmittedAt() == null) {
                order.setSubmittedAt(LocalDateTime.now());
            }
            order.setClosedAt(null);
        } else if (status == OrderStatus.CLOSED) {
            if (order.getSubmittedAt() == null) {
                order.setSubmittedAt(LocalDateTime.now());
            }
            order.setClosedAt(LocalDateTime.now());
        }
    }
}
