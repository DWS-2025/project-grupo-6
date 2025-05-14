package es.xpressaly.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import es.xpressaly.Model.Order;
import es.xpressaly.Model.User;
import es.xpressaly.Service.UserService;
import jakarta.servlet.http.HttpSession;

import java.util.HashMap;
import java.util.Map;

@Controller
public class AuthController {

    private final UserService userService;
    
    private final OrderController orderController= new OrderController();

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/login")
    public String showLoginForm() {
        //userService.setCurrentUser(null);
        return "login";
    }

    @PostMapping("/login")
    public String login(@RequestParam String email, 
                       @RequestParam String password,
                       HttpSession session,
                       Model model) {
        try {
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
                Order newOrder = new Order(user, user.getAddress());
                orderController.setCurrentOrder(session, newOrder);
                session.setAttribute("userId", user.getId());
                session.setAttribute("userEmail", user.getEmail());
                session.setAttribute("userRole", user.getRole());
                //userService.setCurrentUser(user);
                return "redirect:/products";
            }
            model.addAttribute("error", "Invalid email or password");
            return "login";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "login";
        }
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
                          @RequestParam(name = "phoneNumber") String phoneNumberStr,
                          @RequestParam int age,
                          HttpSession session,
                          Model model) {

        try {
            // Validate input
            if (firstName == null || firstName.trim().isEmpty()) {
                model.addAttribute("error", "First name is required");
                return "register";
            }
            if (lastName == null || lastName.trim().isEmpty()) {
                model.addAttribute("error", "Last name is required");
                return "register";
            }
            if (email == null || email.trim().isEmpty()) {
                model.addAttribute("error", "Email is required");
                return "register";
            }
            if (password == null || password.trim().isEmpty()) {
                model.addAttribute("error", "Password is required");
                return "register";
            }
            if (!password.matches("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z]).{8,}$")) {
                model.addAttribute("error", "Password must be at least 8 characters long and contain uppercase, lowercase, and numbers");
                return "register";
            }
            if (address == null || address.trim().isEmpty()) {
                model.addAttribute("error", "Address is required");
                return "register";
            }
            
            // Phone validation: must be numeric and between 6 and 9 digits long
            if (!phoneNumberStr.matches("^[0-9]{6,9}$")) {
                model.addAttribute("error", "Phone number must contain between 6 and 9 digits");
                return "register";
            }
            int phoneNumber = Integer.parseInt(phoneNumberStr);
            
            if (age < 18 || age > 120) {
                model.addAttribute("error", "You must be 18 or older to register");
                return "register";
            }
            
            User newUser = userService.registerUser(firstName, lastName, email, password, address, phoneNumber, age);
            if (newUser != null) {
                session.setAttribute("userId", newUser.getId());
                session.setAttribute("userEmail", newUser.getEmail());
                session.setAttribute("userRole", newUser.getRole());
                //userService.setCurrentUser(newUser);
                return "redirect:/login";
            }
            model.addAttribute("error", "Email already exists");
            return "register";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            return "register";
        } catch (Exception e) {
            model.addAttribute("error", "An unexpected error occurred: " + e.getMessage());
            return "register";
        }
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        session.removeAttribute("currentOrder");
        return "redirect:/login";
    }
}