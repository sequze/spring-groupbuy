package org.abdrafikov.groupbuy.controller;

import jakarta.validation.Valid;
import org.abdrafikov.groupbuy.dto.CommentForm;
import org.abdrafikov.groupbuy.service.CommentService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/comments")
public class CommentController {

    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @GetMapping
    public String list(@RequestParam Long workspaceId, Model model) {
        model.addAttribute("workspaceId", workspaceId);
        model.addAttribute("comments", commentService.getByWorkspace(workspaceId));
        return "comments/list";
    }

    @GetMapping("/create")
    public String createPage(@RequestParam Long workspaceId, @RequestParam Long purchaseItemId, Model model) {
        model.addAttribute("commentForm", commentService.getCreateForm(purchaseItemId));
        populateForm(model, workspaceId);
        model.addAttribute("workspaceId", workspaceId);
        model.addAttribute("formAction", "/comments/create?workspaceId=" + workspaceId);
        model.addAttribute("pageTitle", "Создание комментария");
        return "comments/create";
    }

    @PostMapping("/create")
    public String create(
            @RequestParam Long workspaceId,
            @Valid @ModelAttribute("commentForm") CommentForm form,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            populateForm(model, workspaceId);
            model.addAttribute("workspaceId", workspaceId);
            model.addAttribute("formAction", "/comments/create?workspaceId=" + workspaceId);
            model.addAttribute("pageTitle", "Создание комментария");
            return "comments/create";
        }

        commentService.create(form);
        redirectAttributes.addFlashAttribute("successMessage", "Комментарий создан");
        return "redirect:/comments?workspaceId=" + workspaceId;
    }

    @GetMapping("/{id}/edit")
    public String editPage(@PathVariable Long id, @RequestParam Long workspaceId, Model model) {
        model.addAttribute("commentForm", commentService.getEditForm(id));
        model.addAttribute("comment", commentService.getById(id));
        populateForm(model, workspaceId);
        model.addAttribute("workspaceId", workspaceId);
        model.addAttribute("formAction", "/comments/" + id + "/edit?workspaceId=" + workspaceId);
        model.addAttribute("pageTitle", "Редактирование комментария");
        return "comments/edit";
    }

    @PostMapping("/{id}/edit")
    public String update(
            @PathVariable Long id,
            @RequestParam Long workspaceId,
            @Valid @ModelAttribute("commentForm") CommentForm form,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("comment", commentService.getById(id));
            populateForm(model, workspaceId);
            model.addAttribute("workspaceId", workspaceId);
            model.addAttribute("formAction", "/comments/" + id + "/edit?workspaceId=" + workspaceId);
            model.addAttribute("pageTitle", "Редактирование комментария");
            return "comments/edit";
        }

        commentService.update(id, form);
        redirectAttributes.addFlashAttribute("successMessage", "Комментарий обновлен");
        return "redirect:/comments?workspaceId=" + workspaceId;
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, @RequestParam Long workspaceId, RedirectAttributes redirectAttributes) {
        commentService.delete(id);
        redirectAttributes.addFlashAttribute("successMessage", "Комментарий удален");
        return "redirect:/comments?workspaceId=" + workspaceId;
    }

    private void populateForm(Model model, Long workspaceId) {
        model.addAttribute("purchaseItems", commentService.getPurchaseItemOptions(workspaceId));
    }
}
