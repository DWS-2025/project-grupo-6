package es.web.xpressaly;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.ui.Model;


@Controller

public class InitialPageController {
    @Autowired
    private UserService userService;
    @GetMapping ("/")
    public String Wellcome(Model model){
        User user = userService.getUser();  // Obtenemos el usuario único
        model.addAttribute("user", user.getFirstName());   // Lo pasamos al modelo
        return "index_template";
    }
}
