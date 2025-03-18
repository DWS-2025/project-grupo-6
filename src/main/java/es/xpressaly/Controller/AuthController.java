package es.xpressaly.Controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import es.xpressaly.Model.User;
import es.xpressaly.Service.UserService;
import jakarta.servlet.http.HttpSession;

import java.util.HashMap;
import java.util.Map;

@Controller
public class AuthController {


    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/login")
    public String showLoginForm() {
        userService.setCurrentUser(null);
        return "login";
    }

    @PostMapping("/login")
    public String login(@RequestParam String email, 
                       @RequestParam String password,
                       HttpSession session,
                       Model model) {
        // Validate input
        if (email == null || email.trim().isEmpty()) {
            model.addAttribute("error", "Email is required");
            return "login";
        }
        if (password == null || password.trim().isEmpty()) {
            model.addAttribute("error", "Password is required");
            return "login";
        }
        
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
    public @ResponseBody Map<String, Object> register(@RequestParam String firstName,
                          @RequestParam String lastName,
                          @RequestParam String email,
                          @RequestParam String password,
                          @RequestParam String address,
                          @RequestParam int phoneNumber,
                          @RequestParam int age,
                          HttpSession session,
                          Model model) {
        Map<String, Object> response = new HashMap<>();

        // Validate input
        if (firstName == null || firstName.trim().isEmpty()) {
            response.put("error", "First name is required");
            return response;
        }
        if (lastName == null || lastName.trim().isEmpty()) {
            response.put("error", "Last name is required");
            return response;
        }
        if (email == null || email.trim().isEmpty()) {
            response.put("error", "Email is required");
            return response;
        }
        if (password == null || password.trim().isEmpty()) {
            response.put("error", "Password is required");
            return response;
        }
        if (!password.matches("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z]).{8,}$")) {
            response.put("error", "Password must be at least 8 characters long and contain uppercase, lowercase, and numbers");
            return response;
        }
        if (address == null || address.trim().isEmpty()) {
            response.put("error", "Address is required");
            return response;
        }
        if (phoneNumber <= 0) {
            response.put("error", "Invalid phone number");
            return response;
        }
        if (age < 18) {
            response.put("error", "You must be 18 or older to register");
            return response;
        }
        
        try {
            User newUser = userService.registerUser(firstName, lastName, email, password, address, phoneNumber, age);
            if (newUser != null) {
                session.setAttribute("userId", newUser.getId());
                session.setAttribute("userEmail", newUser.getEmail());
                userService.setCurrentUser(newUser);
                response.put("success", true);
                return response;
            }
            response.put("error", "Email already exists");
            return response;
        } catch (IllegalArgumentException e) {
            response.put("error", e.getMessage());
            return response;
        }
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }
}