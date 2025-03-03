package es.xpressaly.Service;

import org.springframework.stereotype.Service;

import es.xpressaly.Model.Product;
import es.xpressaly.Model.Review;
import es.xpressaly.Model.User;

import java.util.List;

@Service
public class ReviewService {

    private ProductService productService;
    private UserService userService;

    public ReviewService(ProductService productService, UserService userService) {
        this.productService = productService;
        this.userService = userService;
    }

    public void addReview(Long productId, Review review) {
        Product product = productService.getProductById(productId);
        if (product != null) {
            product.addReview(review);  // Add the review to the product
        }
    }

    public void deleteReview(Long productId, Long reviewId) {
        Product product = productService.getProductById(productId);
        User user = userService.getUser();
        
        if (product != null && user != null) {
            // Remove from product's reviews
            product.getReviews().removeIf(review -> review.getId().equals(reviewId));
            
            // Remove from user's reviews
            user.getReviews().removeIf(review -> review.getId().equals(reviewId));
        }
    }
}
