package com.example.codesmells.web;

import com.example.codesmells.domain.AttemptStore;
import com.example.codesmells.domain.Smell;
import com.example.codesmells.domain.SmellRegistry;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.ui.Model;

@Controller
public class QuizController {

    private final SmellRegistry registry;
    private final AttemptStore attempts;

    public QuizController(SmellRegistry registry, AttemptStore attempts) {
        this.registry = registry;
        this.attempts = attempts;
    }

    @PostMapping("/lab/{slug}/check")
    public String check(@PathVariable String slug,
                        @RequestParam(name = "player", defaultValue = "anonymous") String player,
                        @RequestParam(name = "identify") String identify,
                        @RequestParam(name = "refactor") String refactor,
                        Model model) {
        Smell smell = registry.bySlug(slug);
        boolean identifyCorrect = identify.equals(smell.identifyAnswer());
        boolean refactorCorrect = refactor.equals(smell.refactorAnswer());

        attempts.insert(smell.slug(), player.strip().isBlank() ? "anonymous" : player.strip(),
                identify, refactor, identifyCorrect, refactorCorrect);

        model.addAttribute("smell", smell);
        model.addAttribute("identifyCorrect", identifyCorrect);
        model.addAttribute("refactorCorrect", refactorCorrect);
        model.addAttribute("identifyGiven", identify);
        model.addAttribute("refactorGiven", refactor);
        model.addAttribute("allCorrect", identifyCorrect && refactorCorrect);
        model.addAttribute("leaderboard", attempts.leaderboard());
        model.addAttribute("labStats", attempts.labStats());
        return "partials/check";
    }
}