package es.xpressaly.Controller;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import es.xpressaly.Model.User;
import es.xpressaly.Service.UserService;

import org.springframework.ui.Model;


@Controller
public class InitialPageController {

    @Autowired
    private UserService userService;

    @GetMapping("/")
    public String welcome(Model model) {

        return "login";
    }
}
