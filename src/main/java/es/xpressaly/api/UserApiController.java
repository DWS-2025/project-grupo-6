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
    

    @GetMapping("/{id}")
    public ResponseEntity<?> getProfile(@PathVariable Long id) {
        UserDTO user = userService.getUserApiById(id);
        if(user==null){
            return ResponseEntity.badRequest().body("User does not exist");
        }
        return ResponseEntity.ok(user);
    }

    @PutMapping("/update")
    public ResponseEntity<?> updateProfile(@RequestBody UserWebDTO userWebDTO) {
        try {
            // Verificar que el usuario existe
            UserWebDTO existingUser = userService.getUserById(userWebDTO.id());
            if(existingUser == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "User not found"));
            }

            // Input validation
            if (userWebDTO.firstName() == null || userWebDTO.firstName().trim().isEmpty() || userWebDTO.firstName().length() > 50) {
                return ResponseEntity.badRequest().body(Map.of("error", "First name is required and must not exceed 50 characters"));
            }
            if (userWebDTO.lastName() == null || userWebDTO.lastName().trim().isEmpty() || userWebDTO.lastName().length() > 50) {
                return ResponseEntity.badRequest().body(Map.of("error", "Last name is required and must not exceed 50 characters"));
            }
            if (userWebDTO.email() == null || !userWebDTO.email().matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
                return ResponseEntity.badRequest().body(Map.of("error", "Please enter a valid email address"));
            }
            if (userWebDTO.address() == null || userWebDTO.address().trim().isEmpty() || userWebDTO.address().length() > 200) {
                return ResponseEntity.badRequest().body(Map.of("error", "Address is required and must not exceed 200 characters"));
            }
            if (userWebDTO.age() < 18 || userWebDTO.age() > 120) {
                return ResponseEntity.badRequest().body(Map.of("error", "Age must be between 18 and 120"));
            }
            if (userWebDTO.password() != null && !userWebDTO.password().isEmpty()) {
                if (userWebDTO.password().length() < 8 || !userWebDTO.password().matches(".*[A-Z].*") || !userWebDTO.password().matches(".*[a-z].*") || !userWebDTO.password().matches(".*\\d.*")) {
                    return ResponseEntity.badRequest().body(Map.of("error", "Password must be at least 8 characters long and contain uppercase, lowercase, and numbers"));
                }
            }

            // Actualizar el usuario directamente
            userService.updateUser(userWebDTO);
            
            // Obtener el usuario actualizado
            UserDTO userDTO = userService.toDTO(userWebDTO);
            return ResponseEntity.ok(userDTO);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/create")
    public ResponseEntity<?> createUser(@RequestBody UserWebDTO userWebDTO) {
        try {
            UserWebDTO newUser = userService.registerUser(userWebDTO.firstName(),
                                                          userWebDTO.lastName(),
                                                          userWebDTO.email(),
                                                          userWebDTO.password(),
                                                          userWebDTO.address(),
                                                          userWebDTO.phoneNumber(),
                                                          userWebDTO.age()
                                                          );
            return ResponseEntity.ok(newUser);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        try {
            UserWebDTO user = userService.getUserById(id);
            userService.deleteUser(user.email());
            if (user == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "User not found"));
            }
            return ResponseEntity.ok(Map.of("message", "User deleted successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}