package org.abdrafikov.groupbuy.controller;

import jakarta.validation.Valid;
import org.abdrafikov.groupbuy.dto.WorkspaceForm;
import org.abdrafikov.groupbuy.dto.WorkspaceJoinForm;
import org.abdrafikov.groupbuy.exception.AccessDeniedException;
import org.abdrafikov.groupbuy.exception.ResourceNotFoundException;
import org.abdrafikov.groupbuy.service.WorkspaceService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/workspaces")
public class WorkspaceController {

    private final WorkspaceService workspaceService;

    public WorkspaceController(WorkspaceService workspaceService) {
        this.workspaceService = workspaceService;
    }

    @GetMapping
    public String list(Model model) {
        if (!model.containsAttribute("workspaceJoinForm")) {
            model.addAttribute("workspaceJoinForm", new WorkspaceJoinForm());
        }
        model.addAttribute("workspaces", workspaceService.getAllForCurrentUser());
        return "workspaces/list";
    }

    @PostMapping("/join")
    public String join(
            @Valid @ModelAttribute("workspaceJoinForm") WorkspaceJoinForm form,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute(
                    BindingResult.MODEL_KEY_PREFIX + "workspaceJoinForm",
                    bindingResult
            );
            redirectAttributes.addFlashAttribute("workspaceJoinForm", form);
            return "redirect:/workspaces";
        }

        try {
            workspaceService.joinByToken(form);
            redirectAttributes.addFlashAttribute("successMessage", "Вы вступили в workspace");
        } catch (ResourceNotFoundException | AccessDeniedException ex) {
            redirectAttributes.addFlashAttribute("joinErrorMessage", ex.getMessage());
            redirectAttributes.addFlashAttribute("workspaceJoinForm", form);
        }

        return "redirect:/workspaces";
    }

    @PostMapping("/{id}/leave")
    public String leave(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        workspaceService.leaveWorkspace(id);
        redirectAttributes.addFlashAttribute("successMessage", "Вы покинули workspace");
        return "redirect:/workspaces";
    }

    @GetMapping("/create")
    public String createPage(Model model) {
        model.addAttribute("workspaceForm", new WorkspaceForm());
        model.addAttribute("formAction", "/workspaces/create");
        model.addAttribute("pageTitle", "Создание workspace");
        return "workspaces/create";
    }

    @PostMapping("/create")
    public String create(
            @Valid @ModelAttribute("workspaceForm") WorkspaceForm form,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("formAction", "/workspaces/create");
            model.addAttribute("pageTitle", "Создание workspace");
            return "workspaces/create";
        }

        workspaceService.create(form);
        redirectAttributes.addFlashAttribute("successMessage", "Workspace создан");
        return "redirect:/workspaces";
    }

    @GetMapping("/{id}/edit")
    public String editPage(@PathVariable Long id, Model model) {
        model.addAttribute("workspaceForm", workspaceService.getForm(id));
        model.addAttribute("workspace", workspaceService.getById(id));
        model.addAttribute("formAction", "/workspaces/" + id + "/edit");
        model.addAttribute("pageTitle", "Редактирование workspace");
        return "workspaces/edit";
    }

    @PostMapping("/{id}/edit")
    public String update(
            @PathVariable Long id,
            @Valid @ModelAttribute("workspaceForm") WorkspaceForm form,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("workspace", workspaceService.getById(id));
            model.addAttribute("formAction", "/workspaces/" + id + "/edit");
            model.addAttribute("pageTitle", "Редактирование workspace");
            return "workspaces/edit";
        }

        workspaceService.update(id, form);
        redirectAttributes.addFlashAttribute("successMessage", "Workspace обновлен");
        return "redirect:/workspaces";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        workspaceService.delete(id);
        redirectAttributes.addFlashAttribute("successMessage", "Workspace удален");
        return "redirect:/workspaces";
    }
}
