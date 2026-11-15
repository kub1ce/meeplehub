package ru.meeplehub.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import ru.meeplehub.controller.dto.RegistrationForm;
import ru.meeplehub.service.UserService;

@Controller
public class RegistrationController {

    private final UserService userService;

    public RegistrationController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/registration")
    public String showRegistrationForm(Model model) {
        model.addAttribute(
                "registrationForm",
                new RegistrationForm()
        );

        return "registration";
    }

    @PostMapping("/registration")
    public String register(
            @ModelAttribute("registrationForm") RegistrationForm form,
            Model model) {

        try {
            userService.registerUser(
                    form.getUsername(),
                    form.getEmail(),
                    form.getDisplayName(),
                    form.getPassword()
            );

            return "redirect:/login?registered";

        } catch (IllegalArgumentException exception) {

            // Не возвращаем пароль в HTML после ошибки.
            form.setPassword(null);

            model.addAttribute(
                    "message",
                    exception.getMessage()
            );

            return "registration";
        }
    }
}