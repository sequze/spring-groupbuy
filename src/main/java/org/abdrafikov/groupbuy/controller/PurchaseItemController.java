package org.abdrafikov.groupbuy.controller;

import jakarta.validation.Valid;
import org.abdrafikov.groupbuy.dto.PurchaseItemDto;
import org.abdrafikov.groupbuy.dto.PurchaseItemForm;
import org.abdrafikov.groupbuy.model.choices.PurchaseItemStatus;
import org.abdrafikov.groupbuy.service.CommentService;
import org.abdrafikov.groupbuy.service.PurchaseItemService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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
    public String list(@RequestParam Long workspaceId, Model model) {
        model.addAttribute("workspaceId", workspaceId);
        model.addAttribute("purchaseItems", purchaseItemService.getByWorkspace(workspaceId));
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
            model.addAttribute("formAction", "/purchase-items/create");
            model.addAttribute("pageTitle", "Создание позиции закупки");
            return "purchase-items/create";
        }

        purchaseItemService.create(form);
        redirectAttributes.addFlashAttribute("successMessage", "Позиция закупки создана");
        return "redirect:/purchase-items?workspaceId=" + form.getWorkspaceId();
    }

    @GetMapping("/{id}/edit")
    public String editPage(@PathVariable Long id, Model model) {
        model.addAttribute("purchaseItemForm", purchaseItemService.getEditForm(id));
        model.addAttribute("purchaseItem", purchaseItemService.getById(id));
        model.addAttribute("canModerateStatus", purchaseItemService.canModerateStatus(id));
        populateForm(model);
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
            model.addAttribute("formAction", "/purchase-items/" + id + "/edit");
            model.addAttribute("pageTitle", "Редактирование позиции закупки");
            return "purchase-items/edit";
        }

        purchaseItemService.update(id, form);
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
}
