package es.xpressaly.Service;

import org.springframework.stereotype.Service;

import es.xpressaly.Model.Product;
import es.xpressaly.Model.Review;
import es.xpressaly.Model.User;



@Service
public class ReviewService {

    private ProductService productService;
    private UserService userService;

    public ReviewService(ProductService productService, UserService userService) {
        this.productService = productService;
        this.userService = userService;
    }

    public void addReview(Long productId, Review review) {
        validateReview(review);
        Product product = productService.getProductById(productId);
        if (product != null) {
            product.addReview(review);
        } else {
            throw new IllegalArgumentException("The specified product does not exist");
        }
    }

    private void validateReview(Review review) {
        if (review.getComment() == null || review.getComment().trim().isEmpty()) {
            throw new IllegalArgumentException("Comment cannot be empty");
        }
        if (review.getComment().length() > 500) {
            throw new IllegalArgumentException("Comment cannot exceed 500 characters");
        }
        if (review.getRating() < 1 || review.getRating() > 5) {
            throw new IllegalArgumentException("Rating must be between 1 and 5 stars");
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
