package es.xpressaly.Controller;

import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpServletRequest;

import es.xpressaly.Model.User;
import es.xpressaly.Model.UserRole;
import es.xpressaly.Model.Review;
import es.xpressaly.Service.UserService;
import es.xpressaly.Service.ReviewService;
import es.xpressaly.Service.PdfStorageService;
import es.xpressaly.dto.ReviewDTO;
import es.xpressaly.dto.UserWebDTO;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.nio.file.Path;

@Controller
public class UserController {
    @Autowired
    private UserService userService;

    @Autowired
    private OrderController orderController;

    @Autowired
    private ReviewService reviewService;

    @Autowired
    private PdfStorageService pdfStorageService;

    @GetMapping("/profile")
    public String showProfile(Model model) {
        try {
            User currentUser = userService.getUserEntity();
            if (currentUser == null) {
                return "redirect:/login";
            }
            
            User user = userService.getUserEntityById(currentUser.getId());
            if (user == null) {
                return "redirect:/login";
            }

            model.addAttribute("name", currentUser.getFirstName());
            model.addAttribute("surname", currentUser.getLastName());
            model.addAttribute("email", currentUser.getEmail());
            model.addAttribute("address", currentUser.getAddress());
            model.addAttribute("phone", currentUser.getPhoneNumber());
            model.addAttribute("age", currentUser.getAge());
            model.addAttribute("orders", user.getOrders());
            model.addAttribute("password", currentUser.getPassword());
            model.addAttribute("isAdmin", currentUser.getRole() == UserRole.ADMIN);
            model.addAttribute("cartItemCount", orderController.getCartItemCount());
            model.addAttribute("pdfPath", user.getPdfPath());
           
            // Get user's reviews and add product information
            List<Map<String, Object>> reviewsWithProducts = new ArrayList<>();
            for (Review review : user.getReviews()) {
                Map<String, Object> reviewMap = new HashMap<>();
                reviewMap.put("rating", review.getRating());
                reviewMap.put("comment", review.getComment());
                reviewMap.put("date", "Recently");
                reviewMap.put("userName", review.getUser().getFirstName() + " " + review.getUser().getLastName());
                reviewMap.put("productName", review.getProduct().getName());
                reviewMap.put("productId", review.getProduct().getId());
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
            
            // Debug messages
            System.out.println("Actualizando perfil para: " + email);
            System.out.println("¿Contraseña proporcionada? " + (password != null && !password.isEmpty()));
            
            // Password validation
            if (password != null && !password.isEmpty()) {
                if (confirmPassword == null || !password.equals(confirmPassword)) {
                    model.addAttribute("error", "Passwords do not match");
                    addUserDataToModel(model, user);
                    return "profile";
                }
                // Check password complexity
                if (password.length() < 8 || !password.matches(".*[A-Z].*") || !password.matches(".*[a-z].*") || !password.matches(".*\\d.*")) {
                    model.addAttribute("error", "Password must be at least 8 characters long and contain uppercase, lowercase, and numbers");
                    addUserDataToModel(model, user);
                    return "profile";
                }
                
                // Debuggear la contraseña
                System.out.println("Contraseña válida: " + password.length() + " caracteres");
                System.out.println("¿Tiene mayúsculas? " + password.matches(".*[A-Z].*"));
                System.out.println("¿Tiene minúsculas? " + password.matches(".*[a-z].*"));
                System.out.println("¿Tiene números? " + password.matches(".*\\d.*"));
            }

            // Crear usuario con datos actualizados
            UserWebDTO updatedUser;
            if (password != null && !password.isEmpty()) {
                // Si se proporciona una nueva contraseña, codificarla
                String hashedPassword = userService.encodePassword(password);
                System.out.println("Contraseña hasheada: " + hashedPassword);
                
                updatedUser = new UserWebDTO(
                    user.id(),
                    firstName,
                    lastName,
                    email,
                    hashedPassword,
                    address,
                    age,
                    phone,
                    user.reviews(),
                    user.orders(),
                    user.role(),
                    user.pdfPath()
                );
            } else {
                // Si no se proporciona una nueva contraseña, mantener la actual
                updatedUser = new UserWebDTO(
                    user.id(),
                    firstName,
                    lastName,
                    email,
                    user.password(),
                    address,
                    age,
                    phone,
                    user.reviews(),
                    user.orders(),
                    user.role(),
                    user.pdfPath()
                );
            }
            
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

    @PostMapping("/upload-pdf")
    public String uploadPdf(@RequestParam("pdfFile") MultipartFile file, RedirectAttributes redirectAttributes) {
        try {
            User currentUser = userService.getUserEntity();
            if (currentUser == null) {
                return "redirect:/login";
            }

            if (file.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Please select a file to upload.");
                return "redirect:/profile";
            }

            // Delete existing PDF if present
            if (currentUser.getPdfPath() != null && !currentUser.getPdfPath().isEmpty()) {
                try {
                    pdfStorageService.deleteFile(currentUser.getPdfPath(), currentUser.getId());
                } catch (IOException e) {
                    // Log the error but continue with new upload
                    System.err.println("Error deleting existing PDF: " + e.getMessage());
                }
            }

            String fileName = pdfStorageService.storeFile(file, currentUser.getId());
            currentUser.setPdfPath(fileName);
            userService.saveUser(currentUser);

            redirectAttributes.addFlashAttribute("success", "PDF uploaded successfully!");
            return "redirect:/profile";

        } catch (IOException ex) {
            redirectAttributes.addFlashAttribute("error", "Failed to upload PDF: " + ex.getMessage());
            return "redirect:/profile";
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("error", "An unexpected error occurred: " + ex.getMessage());
            return "redirect:/profile";
        }
    }

    @PostMapping("/delete-pdf")
    public String deletePdf(RedirectAttributes redirectAttributes) {
        try {
            User currentUser = userService.getUserEntity();
            if (currentUser == null) {
                return "redirect:/login";
            }

            String pdfPath = currentUser.getPdfPath();
            if (pdfPath != null && !pdfPath.isEmpty()) {
                pdfStorageService.deleteFile(pdfPath, currentUser.getId());
                currentUser.setPdfPath(null);
                userService.saveUser(currentUser);
                redirectAttributes.addFlashAttribute("success", "PDF deleted successfully.");
            }
            
        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("error", "Error deleting PDF: " + e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "An unexpected error occurred while deleting the PDF.");
        }
        
        return "redirect:/profile";
    }

    @GetMapping("/view-pdf")
    public ResponseEntity<Resource> viewPdf(HttpServletRequest request) {
        try {
            User currentUser = userService.getUserEntity();
            if (currentUser == null || currentUser.getPdfPath() == null || currentUser.getPdfPath().isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            // Extract filename from the stored path (e.g., /pdfs/user_123/unique_file.pdf)
            String storedPath = currentUser.getPdfPath();
            String storedFileName = storedPath.substring(storedPath.lastIndexOf("/") + 1);

            // Validate user has access to this file
            if (!storedPath.contains("/user_" + currentUser.getId() + "/")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            Path filePath = pdfStorageService.loadFileAsResource(storedFileName, currentUser.getId());
            Resource resource = new org.springframework.core.io.UrlResource(filePath.toUri());

            // Validate the resource exists and is readable
            if (!resource.exists() || !resource.isReadable()) {
                throw new IOException("Could not read PDF file");
            }

            String contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
            if (contentType == null) {
                contentType = "application/pdf";
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                    .body(resource);

        } catch (Exception ex) {
            return ResponseEntity.internalServerError().build();
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
        model.addAttribute("pdfPath", user.pdfPath());
        
        // Obtener la entidad User para acceder a las reviews
        User userEntity = userService.getUserEntityById(user.id());
        if (userEntity != null) {
            List<Map<String, Object>> reviewsWithProducts = new ArrayList<>();
            for (Review review : userEntity.getReviews()) {
                Map<String, Object> reviewMap = new HashMap<>();
                reviewMap.put("rating", review.getRating());
                reviewMap.put("comment", review.getComment());
                reviewMap.put("date", "Recently");
                reviewMap.put("userName", review.getUser().getFirstName() + " " + review.getUser().getLastName());
                reviewMap.put("productName", review.getProduct().getName());
                reviewMap.put("productId", review.getProduct().getId());
                reviewsWithProducts.add(reviewMap);
            }
            model.addAttribute("reviews", reviewsWithProducts);
        }
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
    public String deleteAccount() {
        UserWebDTO currentUser = userService.getUser();
        if (currentUser == null) {
            return "redirect:/login";
        }

        userService.deleteOwnUser();
        return "redirect:/login";
    }
    
    @GetMapping("/users-management")
    public String showUsersManagement(Model model) {
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
            model.addAttribute("cartItemCount", orderController.getCartItemCount());
            
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