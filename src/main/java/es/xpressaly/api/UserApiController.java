package es.xpressaly.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import es.xpressaly.Model.User;
import es.xpressaly.Model.UserRole;
import es.xpressaly.Model.Review;
import es.xpressaly.Service.UserService;
import es.xpressaly.Service.ReviewService;
import es.xpressaly.dto.UserDTO;
import es.xpressaly.dto.UserWebDTO;
import es.xpressaly.security.jwt.UserLoginService;
import es.xpressaly.dto.ReviewDTO;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;


@RestController
@RequestMapping("/api/users")
public class UserApiController {
    @Autowired
    private UserService userService;

    @Autowired
    private ReviewService reviewService;    

    @GetMapping("/")
    public ResponseEntity<List<UserDTO>> getUsers() {
        return ResponseEntity.ok(userService.getUsers());
    }
    

    @GetMapping("/profile")
    public ResponseEntity<Map<String, Object>> getProfile() {
        try {
            UserWebDTO user = userService.getUser();
            if (user == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "User not found"));
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("name", user.firstName());
            response.put("surname", user.lastName());
            response.put("email", user.email());
            response.put("address", user.address());
            response.put("phone", user.phoneNumber());
            response.put("age", user.age());
            response.put("orders", user.orders());
            response.put("isAdmin", user.role() == UserRole.ADMIN);

            List<Map<String, Object>> reviewsWithProducts = new ArrayList<>();
            for (ReviewDTO review : user.reviews()) {
                Map<String, Object> reviewMap = new HashMap<>();
                reviewMap.put("rating", review.rating());
                reviewMap.put("comment", review.comment());
                reviewMap.put("date", "Recently");
                reviewMap.put("userName", review.user());
                reviewMap.put("productName", review.product().name());
                reviewMap.put("productId", review.product().id());
                reviewsWithProducts.add(reviewMap);
            }
            response.put("reviews", reviewsWithProducts);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/profile")
    public ResponseEntity<Map<String, Object>> updateProfile(
            @RequestParam String firstName,
            @RequestParam String lastName,
            @RequestParam String email,
            @RequestParam String address,
            @RequestParam int phone,
            @RequestParam int age,
            @RequestParam(required = false) String password,
            @RequestParam(required = false) String confirmPassword) {
        
        try {
            UserWebDTO user = userService.getUser();
            if(user == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "User not found"));
            }

            // Input validation
            if (firstName == null || firstName.trim().isEmpty() || firstName.length() > 50) {
                return ResponseEntity.badRequest().body(Map.of("error", "First name is required and must not exceed 50 characters"));
            }
            if (lastName == null || lastName.trim().isEmpty() || lastName.length() > 50) {
                return ResponseEntity.badRequest().body(Map.of("error", "Last name is required and must not exceed 50 characters"));
            }
            if (email == null || !email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
                return ResponseEntity.badRequest().body(Map.of("error", "Please enter a valid email address"));
            }
            if (address == null || address.trim().isEmpty() || address.length() > 200) {
                return ResponseEntity.badRequest().body(Map.of("error", "Address is required and must not exceed 200 characters"));
            }
            if (String.valueOf(phone).length() < 9 || String.valueOf(phone).length() > 15) {
                return ResponseEntity.badRequest().body(Map.of("error", "Please enter a valid phone number"));
            }
            if (age < 18 || age > 120) {
                return ResponseEntity.badRequest().body(Map.of("error", "Age must be between 18 and 120"));
            }
            if (password != null && !password.isEmpty()) {
                if (!password.equals(confirmPassword)) {
                    return ResponseEntity.badRequest().body(Map.of("error", "Passwords do not match"));
                }
                if (password.length() < 8 || !password.matches(".*[A-Z].*") || !password.matches(".*[a-z].*") || !password.matches(".*\\d.*")) {
                    return ResponseEntity.badRequest().body(Map.of("error", "Password must be at least 8 characters long and contain uppercase, lowercase, and numbers"));
                }
            }

            user = userService.setFirstName(user, firstName);
            user = userService.setLastName(user, lastName);
            user = userService.setEmail(user, email);
            user = userService.setAddress(user, address);
            user = userService.setPhoneNumber(user, phone);
            user = userService.setAge(user, age);
            
            if (password != null && !password.isEmpty() && password.equals(confirmPassword)) {
                user = userService.setPassword(user, userService.encodePassword(password));
            }
            
            userService.updateUser(user);
            return ResponseEntity.ok(Map.of("success", "Profile updated successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/reviews")
    public ResponseEntity<?> getMyReviews() {
        try {
            UserWebDTO currentUser = userService.getUser();
            if (currentUser == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "User not found"));
            }
            return ResponseEntity.ok(currentUser.reviews());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Error loading reviews"));
        }
    }
}