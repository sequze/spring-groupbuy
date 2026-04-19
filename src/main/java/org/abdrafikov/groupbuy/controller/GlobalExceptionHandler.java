package org.abdrafikov.groupbuy.controller;

import org.abdrafikov.groupbuy.exception.AccessDeniedException;
import org.abdrafikov.groupbuy.exception.ResourceNotFoundException;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public String handleNotFound(ResourceNotFoundException ex, Model model) {
        model.addAttribute("title", "Ресурс не найден");
        model.addAttribute("message", ex.getMessage());
        return "error/custom";
    }

    @ExceptionHandler(AccessDeniedException.class)
    public String handleAccessDenied(AccessDeniedException ex, Model model) {
        model.addAttribute("title", "Доступ запрещен");
        model.addAttribute("message", ex.getMessage());
        return "error/custom";
    }

    @ExceptionHandler(IllegalStateException.class)
    public String handleIllegalState(IllegalStateException ex, Model model) {
        model.addAttribute("title", "Ошибка состояния приложения");
        model.addAttribute("message", ex.getMessage());
        return "error/custom";
    }
}
