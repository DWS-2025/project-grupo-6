package es.xpressaly.Controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import es.xpressaly.Model.User;
import es.xpressaly.Service.UserService;
import jakarta.servlet.http.HttpSession;

@Controller
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/login")
    public String showLoginForm() {
        return "login";
    }

    @PostMapping("/login")
    public String login(@RequestParam String email, 
                       @RequestParam String password,
                       HttpSession session,
                       Model model) {
        User user = userService.authenticateUser(email, password);
        if (user != null) {
            session.setAttribute("userId", user.getId());
            session.setAttribute("userEmail", user.getEmail());
            return "redirect:/products";
        }
        model.addAttribute("error", "Invalid email or password");
        userService.setCurrentUser(user);
        return "login";
    }

    @GetMapping("/register")
    public String showRegistrationForm() {
        return "register";
    }

    @PostMapping("/register")
    public String register(@RequestParam String firstName,
                          @RequestParam String lastName,
                          @RequestParam String email,
                          @RequestParam String password,
                          @RequestParam String address,
                          @RequestParam int phoneNumber,
                          @RequestParam int age,
                          HttpSession session,
                          Model model) {
        User newUser = userService.registerUser(firstName, lastName, email, password, address, phoneNumber, age);
        if (newUser != null) {
            session.setAttribute("userId", newUser.getId());
            session.setAttribute("userEmail", newUser.getEmail());
            userService.setCurrentUser(newUser);
            return "redirect:/products";
        }
        model.addAttribute("error", "Email already exists");
        return "register";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }
}