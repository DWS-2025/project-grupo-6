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
import es.xpressaly.dto.ProductWebDTO;
import es.xpressaly.mapper.ProductWebMapper;

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

    @Autowired
    private ProductWebMapper productWebMapper;

    @GetMapping("/review-management")
    public String reviewManagement(Model model, HttpSession session) {
        User currentUser = userService.getUser();
        
        if (currentUser == null) {
            return "redirect:/login";
        }
        
        if (currentUser.getRole() != UserRole.ADMIN) {
            return "redirect:/products";
        }
        
        List<User> users = userService.getAllUsersInitialized();
        List<ProductWebDTO> allProducts = productService.getAllProductsWeb();
        
        List<ProductWebDTO> productsWithReviews = allProducts.stream()
                .filter(product -> product.reviews() != null && !product.reviews().isEmpty())
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