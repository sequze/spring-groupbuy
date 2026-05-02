package org.abdrafikov.groupbuy.controller;

import jakarta.validation.Valid;
import org.abdrafikov.groupbuy.dto.OrderForm;
import org.abdrafikov.groupbuy.model.choices.OrderStatus;
import org.abdrafikov.groupbuy.service.OrderService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping
    public String list(@RequestParam Long workspaceId, Model model) {
        model.addAttribute("workspaceId", workspaceId);
        model.addAttribute("orders", orderService.getByWorkspace(workspaceId));
        return "orders/list";
    }

    @GetMapping("/create")
    public String createPage(@RequestParam Long workspaceId, Model model) {
        OrderForm orderForm = orderService.getCreateForm(workspaceId);
        model.addAttribute("orderForm", orderForm);
        populateForm(model, orderForm);
        model.addAttribute("formAction", "/orders/create");
        model.addAttribute("pageTitle", "Создание заказа");
        return "orders/create";
    }

    @PostMapping("/create")
    public String create(
            @Valid @ModelAttribute("orderForm") OrderForm form,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            populateForm(model, form);
            model.addAttribute("formAction", "/orders/create");
            model.addAttribute("pageTitle", "Создание заказа");
            return "orders/create";
        }

        try {
            orderService.create(form);
        } catch (IllegalArgumentException ex) {
            bindingResult.reject("orderItems", ex.getMessage());
            populateForm(model, form);
            model.addAttribute("formAction", "/orders/create");
            model.addAttribute("pageTitle", "Создание заказа");
            return "orders/create";
        }

        redirectAttributes.addFlashAttribute("successMessage", "Заказ создан");
        return "redirect:/orders?workspaceId=" + form.getWorkspaceId();
    }

    @GetMapping("/{id}/edit")
    public String editPage(@PathVariable Long id, Model model) {
        OrderForm orderForm = orderService.getEditForm(id);
        model.addAttribute("orderForm", orderForm);
        model.addAttribute("order", orderService.getById(id));
        populateForm(model, orderForm);
        model.addAttribute("formAction", "/orders/" + id + "/edit");
        model.addAttribute("pageTitle", "Редактирование заказа");
        return "orders/edit";
    }

    @PostMapping("/{id}/edit")
    public String update(
            @PathVariable Long id,
            @Valid @ModelAttribute("orderForm") OrderForm form,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        Long workspaceId = orderService.getById(id).getWorkspaceId();
        form.setWorkspaceId(workspaceId);

        if (bindingResult.hasErrors()) {
            model.addAttribute("order", orderService.getById(id));
            populateForm(model, form);
            model.addAttribute("formAction", "/orders/" + id + "/edit");
            model.addAttribute("pageTitle", "Редактирование заказа");
            return "orders/edit";
        }

        try {
            orderService.update(id, form);
        } catch (IllegalArgumentException ex) {
            bindingResult.reject("orderItems", ex.getMessage());
            model.addAttribute("order", orderService.getById(id));
            populateForm(model, form);
            model.addAttribute("formAction", "/orders/" + id + "/edit");
            model.addAttribute("pageTitle", "Редактирование заказа");
            return "orders/edit";
        }

        redirectAttributes.addFlashAttribute("successMessage", "Заказ обновлен");
        return "redirect:/orders?workspaceId=" + workspaceId;
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, @RequestParam Long workspaceId, RedirectAttributes redirectAttributes) {
        orderService.delete(id);
        redirectAttributes.addFlashAttribute("successMessage", "Заказ удален");
        return "redirect:/orders?workspaceId=" + workspaceId;
    }

    private void populateForm(Model model, OrderForm orderForm) {
        model.addAttribute("statuses", OrderStatus.values());
        model.addAttribute("itemOptions", orderService.getItemOptions(orderForm));
    }
}
