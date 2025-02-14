package main.java.es.xpressaly.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AppController {
    @RequestMapping("/register") // create new account
    public String register(Model model, @RequestParam String name, @RequestParam String password) {

        return "start_template";
    }

    @RequestMapping("/signin") // sign with an existent account
    public String signin(Model model, @RequestParam String name, @RequestParam String password) {

    }
}