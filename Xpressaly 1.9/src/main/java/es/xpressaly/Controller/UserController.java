package es.xpressaly.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import es.xpressaly.Model.User;
import es.xpressaly.Model.Review;
import es.xpressaly.Service.UserService;
import es.xpressaly.Service.ProductService;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;

@Controller
public class UserController {
    @Autowired
    private UserService userService;

    @Autowired
    private ProductService productService;

    @GetMapping("/profile")
    public String showProfile(Model model) {
        User user = userService.getUser();
        model.addAttribute("name", user.getFirstName());
        model.addAttribute("surname", user.getLastName());
        model.addAttribute("email", user.getEmail());
        model.addAttribute("address", user.getAddress());
        model.addAttribute("phone", user.getPhoneNumber());
        model.addAttribute("age", user.getAge());
        model.addAttribute("orders", user.getOrders());

        // Get user's reviews and add product information
        List<Map<String, Object>> reviewsWithProducts = new ArrayList<>();
        for (Review review : user.getReviews()) {
            Map<String, Object> reviewMap = new HashMap<>();
            reviewMap.put("rating", review.getRating());
            reviewMap.put("comment", review.getComment());
            reviewMap.put("date", "Recently"); // You can add actual date if available
            reviewMap.put("userName", review.getUser()); // Store the user name
            reviewMap.put("productName", review.getProduct().getName()); // Store the product name
            reviewsWithProducts.add(reviewMap);
        }
        model.addAttribute("reviews", reviewsWithProducts);
        return "profile";
    }

    @PostMapping("/update-profile")
    public String updateProfile(@RequestParam String firstName,
                          @RequestParam String lastName,
                          @RequestParam String email,
                          @RequestParam String address,
                          @RequestParam int phone,
                          @RequestParam int age) {
        User user = userService.getUser();
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setEmail(email);
        user.setAddress(address);
        user.setPhoneNumber(phone);
        user.setAge(age);
        userService.updateUser(user);
        return "redirect:/profile";
    }

    @GetMapping("/reviews")
    public String showMyReviews(Model model) {
        User currentUser = userService.getUser();
        model.addAttribute("myReviews", currentUser.getReviews());
        return "myReviews";
    }
    
}