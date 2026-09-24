package com.example.postgresstack.web;

import com.example.postgresstack.rls.RlsService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class RlsController {

    private final RlsService service;

    public RlsController(RlsService service) {
        this.service = service;
    }

    @GetMapping("/rls")
    public String page(@RequestParam(defaultValue = "1") long user, Model model) {
        model.addAttribute("users", service.users());
        model.addAttribute("uid", user);
        model.addAttribute("notes", service.notesFor(user));
        model.addAttribute("flash", "");
        model.addAttribute("spoofResult", "");
        return "rls";
    }

    @GetMapping("/rls/notes")
    public String notes(@RequestParam long user, Model model) {
        model.addAttribute("users", service.users());
        model.addAttribute("uid", user);
        model.addAttribute("notes", service.notesFor(user));
        model.addAttribute("flash", "");
        model.addAttribute("spoofResult", "");
        return "partials/rls :: notes";
    }

    @PostMapping("/rls/notes")
    public String add(@RequestParam long user,
                      @RequestParam String content,
                      @RequestParam(required = false) String ownerOverride,
                      Model model) {
        RlsService.AddResult result = service.addNote(user, content, ownerOverride);
        boolean spoofed = ownerOverride != null && !ownerOverride.isBlank()
            && Long.parseLong(ownerOverride) != user;
        model.addAttribute("users", service.users());
        model.addAttribute("uid", user);
        model.addAttribute("notes", result.notes());
        if (spoofed) {
            model.addAttribute("flash", "You tried to insert a row with owner_id=" + ownerOverride
                + " while authenticated as user " + user + "."
                + " The WITH CHECK policy rejected it: " + result.inserted() + " rows inserted.");
            model.addAttribute("spoofResult", "rejected");
        } else {
            model.addAttribute("flash", "Inserted " + result.inserted() + " row as user " + user
                + " — RLS let it through because the policy matched.");
            model.addAttribute("spoofResult", "ok");
        }
        return "partials/rls :: notes";
    }
}