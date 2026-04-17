package org.abdrafikov.groupbuy.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class WorkspaceController {

    @GetMapping("/workspaces")
    public String workspacesPage() {
        return "workspaces/list";
    }
}