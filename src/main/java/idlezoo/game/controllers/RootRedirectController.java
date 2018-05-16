package idlezoo.game.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class RootRedirectController {
    @GetMapping("/")
    public String index() {
        return "redirect:https://idlezoo.github.io";
    }
}
