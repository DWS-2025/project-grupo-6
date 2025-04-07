package es.xpressaly.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import es.xpressaly.Model.User;
import es.xpressaly.Model.UserRole;
import es.xpressaly.Model.Review;
import es.xpressaly.Service.UserService;
import es.xpressaly.Service.ReviewService;


import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;

@Controller
public class UserController {
    @Autowired
    private UserService userService;

    @Autowired
    private OrderController orderController;

    @Autowired
    private ReviewService reviewService;

    

    @GetMapping("/profile")
    public String showProfile(Model model) {

        try {

            User user = userService.getUser();
            user.getOrders().size();
            model.addAttribute("name", user.getFirstName());
            model.addAttribute("surname", user.getLastName());
            model.addAttribute("email", user.getEmail());
            model.addAttribute("address", user.getAddress());
            model.addAttribute("phone", user.getPhoneNumber());
            model.addAttribute("age", user.getAge());
            model.addAttribute("orders", user.getOrders());
            System.out.println("Número de órdenes: " + user.getOrders().size()); // Debug
            user.getOrders().forEach(order -> {
                System.out.println(
                    "Order ID: " + order.getId() + 
                    ", Address: " + order.getAddress() + 
                    ", Total: " + order.getTotal()
                );
            });
            model.addAttribute("password", user.getPassword());
            model.addAttribute("isAdmin", user.getRole() == UserRole.ADMIN);
            model.addAttribute("cartItemCount", orderController.getCartItemCount());

            // Get user's reviews and add product information
            List<Map<String, Object>> reviewsWithProducts = new ArrayList<>();
            for (Review review : user.getReviews()) {
                // Verify if the review still exists in the database
                if (reviewService.getReviewById(review.getId()) != null) {
                    Map<String, Object> reviewMap = new HashMap<>();
                    reviewMap.put("rating", review.getRating());
                    reviewMap.put("comment", review.getComment());
                    reviewMap.put("date", "Recently"); // You can add actual date if available
                    reviewMap.put("userName", review.getUser()); // Store the user name
                    reviewMap.put("productName", review.getProduct().getName()); // Store the product name
                    reviewMap.put("productId", review.getProduct().getId()); // Add the product ID
                    reviewsWithProducts.add(reviewMap);
                }
            }
            model.addAttribute("reviews", reviewsWithProducts);
            return "profile";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "redirect:/login";
        }
    }

    @PostMapping("/update-profile")
    public String updateProfile(@RequestParam String firstName,
                          @RequestParam String lastName,
                          @RequestParam String email,
                          @RequestParam String address,
                          @RequestParam int phone,
                          @RequestParam int age,
                          @RequestParam(required = false) String password,
                          @RequestParam(required = false) String confirmPassword,
                          Model model) {

        try{                    
            User user = userService.getUser();

            if(user == null ){
                return "redirect:/login";
            }

            // Input validation
            if (firstName == null || firstName.trim().isEmpty() || firstName.length() > 50) {
                model.addAttribute("error", "First name is required and must not exceed 50 characters");
                addUserDataToModel(model, user);
                return "profile";
            }
            if (lastName == null || lastName.trim().isEmpty() || lastName.length() > 50) {
                model.addAttribute("error", "Last name is required and must not exceed 50 characters");
                addUserDataToModel(model, user);
                return "profile";
            }
            if (email == null || !email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
                model.addAttribute("error", "Please enter a valid email address");
                addUserDataToModel(model, user);
                return "profile";
            }
            if (address == null || address.trim().isEmpty() || address.length() > 200) {
                model.addAttribute("error", "Address is required and must not exceed 200 characters");
                addUserDataToModel(model, user);
                return "profile";
            }
            if (String.valueOf(phone).length() < 9 || String.valueOf(phone).length() > 15) {
                model.addAttribute("error", "Please enter a valid phone number");
                addUserDataToModel(model, user);
                return "profile";
            }
            if (age < 18 || age > 120) {
                model.addAttribute("error", "Age must be between 18 and 120");
                addUserDataToModel(model, user);
                return "profile";
            }
            if (password != null && !password.isEmpty()) {
                if (!password.equals(confirmPassword)) {
                    model.addAttribute("error", "Passwords do not match");
                    addUserDataToModel(model, user);
                    return "profile";
                }
                if (password.length() < 8 || !password.matches(".*[A-Z].*") || !password.matches(".*[a-z].*") || !password.matches(".*\\d.*")) {
                    model.addAttribute("error", "Password must be at least 8 characters long and contain uppercase, lowercase, and numbers");
                    addUserDataToModel(model, user);
                    return "profile";
                }
            }

            user.setFirstName(firstName);
            user.setLastName(lastName);
            user.setEmail(email);
            user.setAddress(address);
            user.setPhoneNumber(phone);
            user.setAge(age);
            
            if (password != null && !password.isEmpty() && password.equals(confirmPassword)) {
                user.setPassword(password);
            }
            
            userService.updateUser(user);
            model.addAttribute("success", "Profile updated successfully");
            return "redirect:/profile";

        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            addUserDataToModel(model, userService.getUser());
            return "profile";
        }
    }

    private void addUserDataToModel(Model model, User user) {
        model.addAttribute("name", user.getFirstName());
        model.addAttribute("surname", user.getLastName());
        model.addAttribute("email", user.getEmail());
        model.addAttribute("address", user.getAddress());
        model.addAttribute("phone", user.getPhoneNumber());
        model.addAttribute("age", user.getAge());
        model.addAttribute("password", user.getPassword());
        
        List<Map<String, Object>> reviewsWithProducts = new ArrayList<>();
        for (Review review : user.getReviews()) {
            // Verify if the review still exists in the database
            if (reviewService.getReviewById(review.getId()) != null) {
                Map<String, Object> reviewMap = new HashMap<>();
                reviewMap.put("rating", review.getRating());
                reviewMap.put("comment", review.getComment());
                reviewMap.put("date", "Recently");
                reviewMap.put("userName", review.getUser());
                reviewMap.put("productName", review.getProduct().getName());
                reviewMap.put("productId", review.getProduct().getId());
                reviewsWithProducts.add(reviewMap);
            }
        }
        model.addAttribute("reviews", reviewsWithProducts);
    }

    @GetMapping("/reviews")
    public String showMyReviews(Model model) {
        try {
            User currentUser = userService.getUser();
            if (currentUser == null) {
                return "redirect:/login";
            }
            model.addAttribute("myReviews", currentUser.getReviews());
            return "myReviews";
        } catch (Exception e) {
            model.addAttribute("error", "Error loading reviews. Please try again.");
            return "redirect:/profile";
        }
    }
    
}