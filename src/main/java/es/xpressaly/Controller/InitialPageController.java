package es.xpressaly.Controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.ui.Model;


@Controller
public class InitialPageController {

    

    @GetMapping("/")
    public String welcome(Model model) {

        return "login";
    }
}
