package es.xpressaly.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import es.xpressaly.Model.Product;
import es.xpressaly.Model.Review;
import es.xpressaly.Model.User;
import es.xpressaly.Repository.ReviewRepository;
import es.xpressaly.dto.ProductDTO;
import es.xpressaly.dto.ReviewDTO;
import es.xpressaly.mapper.ProductMapper;
import es.xpressaly.mapper.ReviewMapper;

@Service
@Transactional
public class ReviewService {

    private final ProductService productService;
    private final UserService userService;
    private final ReviewRepository reviewRepository;
    @Autowired
    private ProductMapper productMapper;
    @Autowired
    private ReviewMapper reviewMapper;
    public ReviewService(ProductService productService, UserService userService, ReviewRepository reviewRepository) {
        this.productService = productService;
        this.userService = userService;
        this.reviewRepository = reviewRepository;
    }

    public Collection<ReviewDTO> getAllReviews() {
        return reviewToDTOs(reviewRepository.findAll());
    }
    
    public Review saveReview(Review review) {
        validateReview(review);
        return reviewRepository.save(review);
    }
    
    public void addReview(Long productId, Review review) {
        validateReview(review);
        Product product = productToDomain(productService.getProductByIdAPI(productId));
        if (product != null) {
            review.setProduct(product);
            product.addReview(review);
            reviewRepository.save(review);
        } else {
            throw new IllegalArgumentException("The specified product does not exist");
        }
    }

    private void validateReview(Review review) {
        if (review.getComment() == null || review.getComment().trim().isEmpty() 
                || review.getComment().equals("<p><br></p>")) {
            throw new IllegalArgumentException("Comment cannot be empty");
        }
        
        // Extract plain text without HTML tags to count characters correctly
        String plainText = review.getComment().replaceAll("<[^>]*>", "").trim();
        
        if (plainText.length() > 400) {
            throw new IllegalArgumentException("Comment cannot exceed 400 characters");
        }
        
        if (review.getRating() < 1 || review.getRating() > 5) {
            throw new IllegalArgumentException("Rating must be between 1 and 5 stars");
        }
        
        // Check if user is valid
        if (review.getUser() == null) {
            throw new IllegalArgumentException("User cannot be null");
        }
    }

    public Review getReviewById(Long id) {
        return reviewRepository.findById(id).orElse(null);
    }

    public List<Review> getReviewsByProduct(Product product) {
        return reviewRepository.findByProduct(product);
    }

    public void deleteReview(Long productId, Long reviewId) {
        Product product = productToDomain(productService.getProductByIdAPI(productId));
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
    
    private Product productToDomain(ProductDTO productDTO){
        return productMapper.toDomain(productDTO);
    }
    private Review reviewToDomain(ReviewDTO reviewDTO){
        return reviewMapper.toDomain(reviewDTO);
    }
    private ReviewDTO reviewToDTO(Review review){
        return reviewMapper.toDTO(review);
    }
    private Collection<ReviewDTO> reviewToDTOs(Collection<Review> reviews){
        return reviewMapper.toDTOs(reviews);
    }
}
