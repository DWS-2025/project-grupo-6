package es.xpressaly.Service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import es.xpressaly.Model.Product;
import es.xpressaly.Model.Review;
import es.xpressaly.Model.User;
import es.xpressaly.Repository.ReviewRepository;

@Service
@Transactional
public class ReviewService {

    private final ProductService productService;
    private final UserService userService;
    private final ReviewRepository reviewRepository;

    public ReviewService(ProductService productService, UserService userService, ReviewRepository reviewRepository) {
        this.productService = productService;
        this.userService = userService;
        this.reviewRepository = reviewRepository;
    }

    public List<Review> getAllReviews() {
        return reviewRepository.findAll();
    }
    
    public Review saveReview(Review review) {
        validateReview(review);
        return reviewRepository.save(review);
    }
    
    public void addReview(Long productId, Review review) {
        validateReview(review);
        Product product = productService.getProductById(productId);
        if (product != null) {
            review.setProduct(product);
            product.addReview(review);
            reviewRepository.save(review);
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

    public Review getReviewById(Long reviewId) {
        return reviewRepository.findById(reviewId).orElse(null);
    }

    public void deleteReview(Long productId, Long reviewId) {
        Product product = productService.getProductById(productId);
        User user = userService.getUser();
        
        if (product != null && user != null) {
            Review review = reviewRepository.findById(reviewId).orElse(null);
            if (review != null) {
                // Allow admins to delete any review, but regular users can only delete their own
                if (user.isAdmin() || review.getUser().getId().equals(user.getId())) {
                    // Remove review from product's review list
                    product.getReviews().remove(review);
                    
                    // Remove review from review author's list
                    User reviewAuthor = review.getUser();
                    if (reviewAuthor != null) {
                        reviewAuthor.getReviews().remove(review);
                    }
                    
                    // Delete the review from database
                    reviewRepository.deleteById(reviewId);
                } else {
                    throw new IllegalArgumentException("You can only delete your own reviews");
                }
            }
        }
    }
}
