package org.abdrafikov.groupbuy.controller;

import org.abdrafikov.groupbuy.service.security.CustomUserDetails;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ProfileController {

    @GetMapping("/profile")
    public String profile(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        model.addAttribute("userId", userDetails.getId());
        model.addAttribute("email", userDetails.getUsername());
        model.addAttribute("displayName", userDetails.getDisplayName());

        return "profile";
    }
}
