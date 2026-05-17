package org.abdrafikov.groupbuy.controller;

import jakarta.validation.Valid;
import org.abdrafikov.groupbuy.dto.PurchaseItemDto;
import org.abdrafikov.groupbuy.dto.PurchaseItemForm;
import org.abdrafikov.groupbuy.model.choices.PurchaseItemStatus;
import org.abdrafikov.groupbuy.service.CommentService;
import org.abdrafikov.groupbuy.service.PurchaseItemService;
import org.abdrafikov.groupbuy.service.currency.CurrencyConversionException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/purchase-items")
public class PurchaseItemController {

    private final PurchaseItemService purchaseItemService;
    private final CommentService commentService;

    public PurchaseItemController(PurchaseItemService purchaseItemService, CommentService commentService) {
        this.purchaseItemService = purchaseItemService;
        this.commentService = commentService;
    }

    @GetMapping
    public String list(
            @RequestParam Long workspaceId,
            @RequestParam(required = false) PurchaseItemStatus status,
            @RequestParam(required = false) String q,
            Model model
    ) {
        model.addAttribute("workspaceId", workspaceId);
        model.addAttribute("selectedStatus", status);
        model.addAttribute("searchQuery", q);
        model.addAttribute("statuses", PurchaseItemStatus.values());
        model.addAttribute("purchaseItems", purchaseItemService.getByWorkspace(workspaceId, status, q));
        return "purchase-items/list";
    }

    @GetMapping("/{id}")
    public String details(@PathVariable Long id, Model model) {
        PurchaseItemDto purchaseItem = purchaseItemService.getById(id);
        model.addAttribute("purchaseItem", purchaseItem);
        model.addAttribute("comments", commentService.getByPurchaseItem(id));
        return "purchase-items/details";
    }

    @GetMapping("/create")
    public String createPage(@RequestParam Long workspaceId, Model model) {
        model.addAttribute("purchaseItemForm", purchaseItemService.getCreateForm(workspaceId));
        populateForm(model);
        model.addAttribute("canApproveImmediately", purchaseItemService.canApproveOnCreate(workspaceId));
        model.addAttribute("formAction", "/purchase-items/create");
        model.addAttribute("pageTitle", "Создание позиции закупки");
        return "purchase-items/create";
    }

    @PostMapping("/create")
    public String create(
            @Valid @ModelAttribute("purchaseItemForm") PurchaseItemForm form,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            populateForm(model);
            model.addAttribute("canApproveImmediately", canApproveImmediately(form.getWorkspaceId()));
            model.addAttribute("formAction", "/purchase-items/create");
            model.addAttribute("pageTitle", "Создание позиции закупки");
            return "purchase-items/create";
        }

        try {
            purchaseItemService.create(form);
        } catch (CurrencyConversionException ex) {
            bindingResult.rejectValue("priceCurrency", "currency.conversion", ex.getMessage());
            populateForm(model);
            model.addAttribute("canApproveImmediately", canApproveImmediately(form.getWorkspaceId()));
            model.addAttribute("formAction", "/purchase-items/create");
            model.addAttribute("pageTitle", "Создание позиции закупки");
            return "purchase-items/create";
        }
        redirectAttributes.addFlashAttribute("successMessage", "Позиция закупки создана");
        return "redirect:/purchase-items?workspaceId=" + form.getWorkspaceId();
    }

    @GetMapping("/{id}/edit")
    public String editPage(@PathVariable Long id, Model model) {
        model.addAttribute("purchaseItemForm", purchaseItemService.getEditForm(id));
        model.addAttribute("purchaseItem", purchaseItemService.getById(id));
        model.addAttribute("canModerateStatus", purchaseItemService.canModerateStatus(id));
        populateForm(model);
        populateEditStatusOptions(model, id);
        model.addAttribute("formAction", "/purchase-items/" + id + "/edit");
        model.addAttribute("pageTitle", "Редактирование позиции закупки");
        return "purchase-items/edit";
    }

    @PostMapping("/{id}/edit")
    public String update(
            @PathVariable Long id,
            @Valid @ModelAttribute("purchaseItemForm") PurchaseItemForm form,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("purchaseItem", purchaseItemService.getById(id));
            model.addAttribute("canModerateStatus", purchaseItemService.canModerateStatus(id));
            populateForm(model);
            populateEditStatusOptions(model, id);
            model.addAttribute("formAction", "/purchase-items/" + id + "/edit");
            model.addAttribute("pageTitle", "Редактирование позиции закупки");
            return "purchase-items/edit";
        }

        try {
            purchaseItemService.update(id, form);
        } catch (CurrencyConversionException ex) {
            bindingResult.rejectValue("priceCurrency", "currency.conversion", ex.getMessage());
            model.addAttribute("purchaseItem", purchaseItemService.getById(id));
            model.addAttribute("canModerateStatus", purchaseItemService.canModerateStatus(id));
            populateForm(model);
            populateEditStatusOptions(model, id);
            model.addAttribute("formAction", "/purchase-items/" + id + "/edit");
            model.addAttribute("pageTitle", "Редактирование позиции закупки");
            return "purchase-items/edit";
        }
        redirectAttributes.addFlashAttribute("successMessage", "Позиция закупки обновлена");
        return "redirect:/purchase-items/" + id;
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, @RequestParam Long workspaceId, RedirectAttributes redirectAttributes) {
        purchaseItemService.delete(id);
        redirectAttributes.addFlashAttribute("successMessage", "Позиция закупки удалена");
        return "redirect:/purchase-items?workspaceId=" + workspaceId;
    }

    private void populateForm(Model model) {
        model.addAttribute("workspaces", purchaseItemService.getWorkspaceOptions());
        model.addAttribute("statuses", PurchaseItemStatus.values());
    }

    private void populateEditStatusOptions(Model model, Long id) {
        if (purchaseItemService.canModerateStatus(id)) {
            model.addAttribute("editStatuses", List.of(
                    PurchaseItemStatus.NEW,
                    PurchaseItemStatus.APPROVED,
                    PurchaseItemStatus.REJECTED
            ));
        } else {
            model.addAttribute("editStatuses", List.of(PurchaseItemStatus.NEW));
        }
    }

    private boolean canApproveImmediately(Long workspaceId) {
        return workspaceId != null && purchaseItemService.canApproveOnCreate(workspaceId);
    }
}
