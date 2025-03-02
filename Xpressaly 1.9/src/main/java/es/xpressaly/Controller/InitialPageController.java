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
        // Get the unique user from the service
        User user = userService.getUser();

        // Pass the user name to the model to make it available in the view
        model.addAttribute("user", user.getFirstName() + " " + user.getLastName());

        // Return the "index" view (which corresponds to index.html)
        return "index";
    }
}
