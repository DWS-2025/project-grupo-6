package es.xpressaly.Controller;

import org.hibernate.Hibernate;
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
import jakarta.servlet.http.HttpSession;
import es.xpressaly.Service.ReviewService;
import es.xpressaly.dto.ReviewDTO;
import es.xpressaly.dto.UserWebDTO;

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
    public String showProfile(Model model, HttpSession session) {
        try {
            UserWebDTO currentUser = userService.getUser();
            if (currentUser == null) {
                return "redirect:/login";
            }
            
            //User user = userService.getUser();
            model.addAttribute("name", currentUser.firstName());
            model.addAttribute("surname", currentUser.lastName());
            model.addAttribute("email", currentUser.email());
            model.addAttribute("address", currentUser.address());
            model.addAttribute("phone", currentUser.phoneNumber());
            model.addAttribute("age", currentUser.age());
            model.addAttribute("orders", currentUser.orders());
            model.addAttribute("password", currentUser.password());
            model.addAttribute("isAdmin", currentUser.role() == UserRole.ADMIN);
            model.addAttribute("cartItemCount", orderController.getCartItemCount(session));
           
            // Get user's reviews and add product information
            List<Map<String, Object>> reviewsWithProducts = new ArrayList<>();
            for (ReviewDTO review : currentUser.reviews()) {
                Map<String, Object> reviewMap = new HashMap<>();
                reviewMap.put("rating", review.rating());
                reviewMap.put("comment", review.comment());
                reviewMap.put("date", "Recently");
                reviewMap.put("userName", review.user());
                reviewMap.put("productName", review.product().name());
                reviewMap.put("productId", review.product().id());
                reviewsWithProducts.add(reviewMap);
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
            UserWebDTO user = userService.getUser();

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
            
            // Password validation
            if (password != null && !password.isEmpty()) {
                if (confirmPassword == null || !password.equals(confirmPassword)) {
                    model.addAttribute("error", "Passwords do not match");
                    addUserDataToModel(model, user);
                    return "profile";
                }
                // Check password complexity
                if (password.length() < 8) {
                    model.addAttribute("error", "Password must be at least 8 characters long and contain uppercase, lowercase, and numbers");
                    addUserDataToModel(model, user);
                    return "profile";
                }
            }

            UserWebDTO updatedUser = new UserWebDTO(
                user.id(),
                firstName,
                lastName,
                email,
                password != null && !password.isEmpty() ? password : user.password(),
                address,
                age,
                phone,
                user.reviews(),
                user.orders(),
                user.role()
            );
            
            // Save the updated user
            userService.updateUser(updatedUser);
            model.addAttribute("success", "Profile updated successfully");
            return "redirect:/profile";

        } catch (Exception e) {
            System.out.println("Error updating profile: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("error", e.getMessage());
            addUserDataToModel(model, userService.getUser());
            return "profile";
        }
    }

    private void addUserDataToModel(Model model, UserWebDTO user) {
        model.addAttribute("name", user.firstName());
        model.addAttribute("surname", user.lastName());
        model.addAttribute("email", user.email());
        model.addAttribute("address", user.address());
        model.addAttribute("phone", user.phoneNumber());
        model.addAttribute("age", user.age());
        model.addAttribute("password", user.password());
        
        List<Map<String, Object>> reviewsWithProducts = new ArrayList<>();
        for (ReviewDTO review : user.reviews()) {
            // Verify if the review still exists in the database
            if (reviewService.getReviewById(review.id()) != null) {
                Map<String, Object> reviewMap = new HashMap<>();
                reviewMap.put("rating", review.rating());
                reviewMap.put("comment", review.comment());
                reviewMap.put("date", "Recently");
                reviewMap.put("userName", review.user());
                reviewMap.put("productName", review.product().name());
                reviewMap.put("productId", review.product().id());
                reviewsWithProducts.add(reviewMap);
            }
        }
        model.addAttribute("reviews", reviewsWithProducts);
    }

    @GetMapping("/reviews")
    public String showMyReviews(Model model) {
        try {
            UserWebDTO currentUser = userService.getUser();
            if (currentUser == null) {
                return "redirect:/login";
            }
            model.addAttribute("myReviews", currentUser.reviews());
            return "myReviews";
        } catch (Exception e) {
            model.addAttribute("error", "Error loading reviews. Please try again.");
            return "redirect:/profile";
        }
    }

    @PostMapping("/delete-account")
    public String deleteAccount(HttpSession session) {
        UserWebDTO currentUser = userService.getUser();
        if (currentUser == null) {
            return "redirect:/login";
        }

        userService.deleteOwnUser(session);
        return "redirect:/login";
    }
    
    @GetMapping("/users-management")
    public String showUsersManagement(Model model, HttpSession session) {
        try {
            UserWebDTO currentUser = userService.getUser();
            if (currentUser == null) {
                return "redirect:/login";
            }
            
            if (currentUser.role() != UserRole.ADMIN) {
                return "redirect:/";
            }
            
            List<UserWebDTO> users = userService.getAllUsersInitialized();
            
            model.addAttribute("users", users);
            model.addAttribute("isAdmin", currentUser.role() == UserRole.ADMIN);
            model.addAttribute("userIsAdmin", currentUser.role() == UserRole.ADMIN);
            model.addAttribute("cartItemCount", orderController.getCartItemCount(session));
            
            return "users-management";
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "Error al cargar la gestión de usuarios: " + e.getMessage());
            return "redirect:/";
        }
    }

    @PostMapping("/delete-user")
    public String deleteUser(@RequestParam String email, Model model) {
        try {
            userService.deleteUser(email);
            return "redirect:/users-management";
        } catch (Exception e) {
            model.addAttribute("error", "Error deleting user: " + e.getMessage());
            return "redirect:/users-management";
        }
    }

    @PostMapping("/edit-user")
    public String editUser(@RequestParam String originalEmail,
                         @RequestParam String firstName,
                         @RequestParam String lastName,
                         @RequestParam String email,
                         @RequestParam String address,
                         @RequestParam(required = false, defaultValue = "0") int phoneNumber,
                         @RequestParam(required = false, defaultValue = "0") int age,
                         @RequestParam(required = false) String password,
                         Model model) {
        
        try {
            // Verificar si el usuario actual es administrador
            UserWebDTO currentUser = userService.getUser();
            if (currentUser == null || currentUser.role() != UserRole.ADMIN) {
                model.addAttribute("error", "You do not have permission to edit users");
                return "redirect:/users-management";
            }
            
            // Buscar el usuario a editar por su email original
            UserWebDTO userToEdit = userService.findByEmail(originalEmail);
            if (userToEdit == null) {
                model.addAttribute("error", "User not found");
                return "redirect:/users-management";
            }
            
            // Validar datos de entrada
            if (firstName == null || firstName.trim().isEmpty() || firstName.length() > 50) {
                model.addAttribute("error", "First name is required and must not exceed 50 characters");
                return "redirect:/users-management";
            }
            if (lastName == null || lastName.trim().isEmpty() || lastName.length() > 50) {
                model.addAttribute("error", "Last name is required and must not exceed 50 characters");
                return "redirect:/users-management";
            }
            if (email == null || !email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
                model.addAttribute("error", "Please enter a valid email address");
                return "redirect:/users-management";
            }
            if (address == null || address.trim().isEmpty() || address.length() > 200) {
                model.addAttribute("error", "Address is required and must not exceed 200 characters");
                return "redirect:/users-management";
            }
            
            // Validación de teléfono y edad si se proporcionaron
            if (phoneNumber > 0 && (String.valueOf(phoneNumber).length() < 9 || String.valueOf(phoneNumber).length() > 15)) {
                model.addAttribute("error", "Please enter a valid phone number");
                return "redirect:/users-management";
            }
            if (age > 0 && (age < 18 || age > 120)) {
                model.addAttribute("error", "Age must be between 18 and 120");
                return "redirect:/users-management";
            }
            
            // Validación de contraseña si se proporcionó una nueva
            if (password != null && !password.isEmpty()) {
                // Verificar longitud mínima
                if (password.length() < 8) {
                    model.addAttribute("error", "Password must be at least 8 characters long");
                    return "redirect:/users-management";
                }
                
                // Verificar que contenga al menos una mayúscula
                if (!password.matches(".*[A-Z].*")) {
                    model.addAttribute("error", "Password must contain at least one uppercase letter");
                    return "redirect:/users-management";
                }
                
                // Verificar que contenga al menos una minúscula
                if (!password.matches(".*[a-z].*")) {
                    model.addAttribute("error", "Password must contain at least one lowercase letter");
                    return "redirect:/users-management";
                }
                
                // Verificar que contenga al menos un número
                if (!password.matches(".*[0-9].*")) {
                    model.addAttribute("error", "Password must contain at least one number");
                    return "redirect:/users-management";
                }
                
                // Hashear la contraseña antes de guardarla
                //userToEdit.setPassword(userService.encodePassword(password));
                userToEdit = userService.setPassword(userToEdit, password);
            }
            
            // Actualizar los datos del usuario
            userToEdit = userService.setFirstName(userToEdit, firstName);
            userToEdit = userService.setLastName(userToEdit, lastName);
            userToEdit = userService.setEmail(userToEdit, email);
            userToEdit = userService.setAddress(userToEdit, address);
            
            if (phoneNumber > 0) {
                userToEdit = userService.setPhoneNumber(userToEdit, phoneNumber);
            }
            
            if (age > 0) {
                userToEdit = userService.setAge(userToEdit, age);
            }
            
            // Guardar los cambios
            userService.updateUser(userToEdit);
            
            return "redirect:/users-management";
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "Error updating user: " + e.getMessage());
            return "redirect:/users-management";
        }
    }
}