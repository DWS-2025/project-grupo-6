package es.xpressaly.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PostMapping;

import es.xpressaly.Model.Product;
import es.xpressaly.Model.Review;
import es.xpressaly.Model.User;
import es.xpressaly.Model.UserRole;
import es.xpressaly.Service.ProductService;
import es.xpressaly.Service.ReviewService;
import es.xpressaly.Service.UserService;

import jakarta.servlet.http.HttpSession;
import java.util.List;
import java.util.stream.Collectors;

@Controller
public class ReviewManagementController {

    @Autowired
    private ReviewService reviewService;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private ProductService productService;
    
    @Autowired
    private OrderController orderController;

    @GetMapping("/review-management")
    public String reviewManagement(Model model, HttpSession session) {
        
        User currentUser = userService.getUser();
        
        // Verificar que sea administrador
        if (currentUser == null) {
            return "redirect:/login";
        }
        
        if (currentUser.getRole() != UserRole.ADMIN) {
            return "redirect:/products";
        }
        
        // Obtener todos los usuarios con sus reviews inicializadas
        List<User> users = userService.getAllUsersInitialized();
        
        // Obtener todos los productos con sus reviews inicializadas
        List<Product> allProducts = productService.getAllProductsWithReviews();
        
        // Filtrar productos que tienen al menos una reseña
        List<Product> productsWithReviews = allProducts.stream()
                .filter(product -> product.getReviews() != null && !product.getReviews().isEmpty())
                .collect(Collectors.toList());
        
        model.addAttribute("users", users);
        model.addAttribute("products", productsWithReviews);
        model.addAttribute("isAdmin", true);
        model.addAttribute("cartItemCount", orderController.getCartItemCount(session));
        
        return "review-management";
    }
    
    @PostMapping("/delete-review-admin")
    public String deleteReviewAdmin(
            @RequestParam Long productId, 
            @RequestParam Long reviewId) {
        
        User currentUser = userService.getUser();
        if (currentUser == null || !currentUser.isAdmin()) {
            return "redirect:/login";
        }

        try {
            reviewService.deleteReview(productId, reviewId);
            return "redirect:/review-management";
        } catch (Exception e) {
            return "redirect:/review-management";
        }
    }
} 