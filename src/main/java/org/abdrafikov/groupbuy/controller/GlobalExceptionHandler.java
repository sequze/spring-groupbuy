package org.abdrafikov.groupbuy.controller;

import lombok.extern.slf4j.Slf4j;
import org.abdrafikov.groupbuy.exception.AccessDeniedException;
import org.abdrafikov.groupbuy.exception.ResourceNotFoundException;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public String handleNotFound(ResourceNotFoundException ex, Model model) {
        log.info("Application resource not found: {}", ex.getMessage());
        model.addAttribute("title", "Ресурс не найден");
        model.addAttribute("message", ex.getMessage());
        return "error/custom";
    }

    @ExceptionHandler(AccessDeniedException.class)
    public String handleAccessDenied(AccessDeniedException ex, Model model) {
        log.warn("Application access denied: {}", ex.getMessage());
        model.addAttribute("title", "Доступ запрещен");
        model.addAttribute("message", ex.getMessage());
        return "error/custom";
    }

    @ExceptionHandler(IllegalStateException.class)
    public String handleIllegalState(IllegalStateException ex, Model model) {
        log.error("Application illegal state error", ex);
        model.addAttribute("title", "Ошибка состояния приложения");
        model.addAttribute("message", ex.getMessage());
        return "error/custom";
    }
}
