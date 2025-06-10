package es.xpressaly.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import es.xpressaly.Model.Order;
import es.xpressaly.Model.User;
import es.xpressaly.Service.OrderService;
import es.xpressaly.Service.UserService;
import es.xpressaly.security.jwt.UserLoginService;
import es.xpressaly.dto.OrderDTO;
import es.xpressaly.dto.UserWebDTO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.util.HashMap;
import java.util.Map;

@Controller
public class AuthController {

    @Autowired
    private final UserService userService;
    @Autowired
    private final OrderService orderService;
    @Autowired
    private UserLoginService userLoginService;

    private final OrderController orderController= new OrderController();

    public AuthController(UserService userService, OrderService orderService) {
        this.userService = userService;
        this.orderService = orderService;
    }

    @GetMapping("/login")
    public String showLoginForm(Model model, HttpServletRequest request) {
        //userService.setCurrentUser(null);
        CsrfToken token = (CsrfToken) request.getAttribute("_csrf");
        model.addAttribute("token", token.getToken());
        return "login";
    }

    @PostMapping("/login")
    public String login(@RequestParam String email, 
                       @RequestParam String password,
                       
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
            
            UserWebDTO user = userService.authenticateUser(email, password);
            if (user != null) {
                Order newOrder=orderService.createOrder(user, userService.getAddress(user));
                User currentUser=userService.getUserEntity();
                userService.setCurrentOrder(currentUser,newOrder);
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
            
            UserWebDTO newUser = userService.registerUser(firstName, lastName, email, password, address, phoneNumber, age);
            if (newUser != null) {
                session.setAttribute("userId", newUser.id());
                session.setAttribute("userEmail", newUser.email());
                session.setAttribute("userRole", newUser.role());
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

    @PostMapping("/logout")
    public String logout(HttpServletResponse response) {
        // Invalidar los tokens JWT usando el servicio
        userLoginService.logout(response);
        
        return "redirect:/login";
    }

    @GetMapping("/cart-prompt")
    public String showCartPrompt() {
        return "cart_prompt";
    }
}