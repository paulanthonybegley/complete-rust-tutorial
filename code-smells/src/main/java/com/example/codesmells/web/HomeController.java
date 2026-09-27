package com.example.codesmells.web;

import com.example.codesmells.domain.AttemptStore;
import com.example.codesmells.domain.SmellRegistry;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    private final SmellRegistry registry;
    private final AttemptStore attempts;

    public HomeController(SmellRegistry registry, AttemptStore attempts) {
        this.registry = registry;
        this.attempts = attempts;
    }

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("smells", registry.all());
        model.addAttribute("total", registry.all().size());
        model.addAttribute("leaderboard", attempts.leaderboard());
        return "home";
    }
}