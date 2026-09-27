package com.example.codesmells.web;

import com.example.codesmells.domain.AttemptStore;
import com.example.codesmells.domain.Diff;
import com.example.codesmells.domain.Smell;
import com.example.codesmells.domain.SmellRegistry;
import com.example.codesmells.domain.SmellSnippets;
import com.example.codesmells.domain.SniffMetrics;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class LabController {

    private final SmellRegistry registry;
    private final SmellSnippets snippets;
    private final AttemptStore attempts;

    public LabController(SmellRegistry registry, SmellSnippets snippets, AttemptStore attempts) {
        this.registry = registry;
        this.snippets = snippets;
        this.attempts = attempts;
    }

    @GetMapping("/lab/{slug}")
    public String lab(@PathVariable String slug, Model model) {
        Smell smell = registry.bySlug(slug);
        String before = snippets.before(slug);
        String after = snippets.after(slug);
        var diff = Diff.lcs(before, after);

        model.addAttribute("smell", smell);
        model.addAttribute("index", registry.all().indexOf(smell));
        model.addAttribute("previous", registry.all().stream()
                .filter(s -> registry.all().indexOf(s) == registry.all().indexOf(smell) - 1)
                .findFirst().orElse(null));
        model.addAttribute("next", registry.all().stream()
                .filter(s -> registry.all().indexOf(s) == registry.all().indexOf(smell) + 1)
                .findFirst().orElse(null));
        model.addAttribute("beforeLines", before.split("\\R"));
        model.addAttribute("afterLines", after.split("\\R"));
        model.addAttribute("beforeMetrics", SniffMetrics.of(before));
        model.addAttribute("afterMetrics", SniffMetrics.of(after));
        model.addAttribute("diff", diff);
        model.addAttribute("changedLines", Diff.changedLines(diff));
        model.addAttribute("labStats", attempts.labStats().stream()
                .filter(s -> s.lab().equals(slug)).findFirst().orElse(null));
        return "lab";
    }

    @GetMapping("/fragments/lab-nav/{slug}")
    public String labNav(@PathVariable String slug, Model model) {
        Smell smell = registry.bySlug(slug);
        model.addAttribute("smell", smell);
        model.addAttribute("previous", registry.all().stream()
                .filter(s -> registry.all().indexOf(s) == registry.all().indexOf(smell) - 1)
                .findFirst().orElse(null));
        model.addAttribute("next", registry.all().stream()
                .filter(s -> registry.all().indexOf(s) == registry.all().indexOf(smell) + 1)
                .findFirst().orElse(null));
        return "partials/lab-nav";
    }
}