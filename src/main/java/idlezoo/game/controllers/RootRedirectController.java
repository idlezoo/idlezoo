package idlezoo.game.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class RootRedirectController {
    private static final String ROOT_REDIRECT = "redirect:https://idlezoo.github.io";

    @GetMapping("/")
    public String index() {
        return ROOT_REDIRECT;
    }
}
