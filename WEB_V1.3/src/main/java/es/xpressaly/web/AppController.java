package es.xpressaly.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
//import org.springframework.web.bind.annotation.RequestMethod;


@Controller
public class AppController {
    @GetMapping("/")
    public String start(Model model) {
        return "App";
    }
    
    @RequestMapping("/register") // create new account
    public String register(Model model, @RequestParam String email, @RequestParam String password) {

        return "start_template";
    }

    @PostMapping("/signin") // sign with an existent account
    public String signin(Model model, @RequestParam String email, @RequestParam String password) {
        return "start_template";
    }

    @GetMapping("/profile")
    public String openProfile() {
        return "profile_template";
    }
    
}