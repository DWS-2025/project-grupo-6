package es.xpressaly.Service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.ArrayList;
import es.xpressaly.Model.Product;
import es.xpressaly.Model.Review;
import es.xpressaly.Model.User;
import es.xpressaly.Model.UserRole;
import es.xpressaly.Repository.ReviewRepository;
import es.xpressaly.dto.ProductWebDTO;
import es.xpressaly.dto.ReviewApiDTO;
import es.xpressaly.dto.ReviewDTO;
import es.xpressaly.dto.UserWebDTO;
import es.xpressaly.mapper.ProductWebMapper;
import es.xpressaly.mapper.ReviewAPIMapper;
import es.xpressaly.mapper.ReviewMapper;
import es.xpressaly.mapper.UserWebMapper;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@Service
@Transactional
public class ReviewService {

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private ProductService productService;

    @Autowired
    private ReviewMapper reviewMapper;

    @Autowired
    private ReviewAPIMapper reviewApiMapper;

    @Autowired
    private ProductWebMapper productWebMapper;

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private SanitizationService sanitizationService;


    public ReviewService(ProductService productService, UserService userService, ReviewRepository reviewRepository) {
        this.productService = productService;
        this.userService = userService;
        this.reviewRepository = reviewRepository;
    }
    //funcion para la sanitizacion
    private void sanitizeReview(Review review) {
        if (review.getComment() != null) {
            review.setComment(sanitizationService.sanitize(review.getComment()));
        }
    }


    public Collection<ReviewDTO> getAllReviews() {
        return reviewToDTOs(reviewRepository.findAll());
    }

    public Collection<ReviewApiDTO> getAllAPIReviews() {
        return reviewApiMapper.toDTOs(reviewRepository.findAll());
    }
    
    public ReviewDTO saveReview(ReviewDTO reviewDTO) {
        Review review = reviewToDomain(reviewDTO);
        sanitizeReview(review); //Sanitizacion
        validateReview(review);
        return reviewToDTO(reviewRepository.save(review));
    }
    
    public void addReview(Long productId, ReviewDTO reviewDTO) {
        User user = userService.getUserEntityById(reviewDTO.user().id());
        Product product = productService.getProductById(productId);
        Review review = new Review();
        review.setComment(reviewDTO.comment());
        review.setRating(reviewDTO.rating());
        review.setUser(user);
        review.setProduct(product);
        sanitizeReview(review); //Sanitizacion
        validateReview(review);
        
        if (product != null) {
            if (product.getReviews() == null) {
                product.setReviews(new ArrayList<>());
            }
            
            //Verify if the user has already reviewed the product
            boolean userHasReview = product.getReviews().stream()
                .anyMatch(r -> r.getUser().getId().equals(review.getUser().getId()));
            
            if (userHasReview) {
                throw new IllegalArgumentException("Ya has dejado una review para este producto");
            }
            
            review.setProduct(product);       
            product.addReview(review);
            reviewRepository.save(review);
            // Obtener todas las reviews del producto incluyendo la nueva
            List<Review> allReviews = reviewRepository.findByProduct(product);
            
            // Calcular el nuevo rating promedio
            double totalRating = allReviews.stream()
                .mapToDouble(Review::getRating)
                .sum();
            
            double averageRating = allReviews.isEmpty() ? 0.0 : totalRating / allReviews.size();
            product.setRating(averageRating);
            productService.updateProduct(product);
        } else {
            throw new IllegalArgumentException("El producto especificado no existe");
        }
    }

    private void validateReview(Review review) {
        if (review.getComment() == null || review.getComment().trim().isEmpty() 
                || review.getComment().equals("<p><br></p>")) {
            throw new IllegalArgumentException("El comentario no puede estar vacío");
        }
        
        // Extraer texto plano sin etiquetas HTML para contar caracteres correctamente
        String plainText = review.getComment().replaceAll("<[^>]*>", "").trim();
        
        if (plainText.length() > 400) {
            throw new IllegalArgumentException("El comentario no puede exceder los 400 caracteres");
        }
        
        if (review.getRating() < 1 || review.getRating() > 5) {
            throw new IllegalArgumentException("La calificación debe estar entre 1 y 5 estrellas");
        }
        
        // Verificar si el usuario es válido
        if (review.getUser() == null) {
            throw new IllegalArgumentException("El usuario no puede ser nulo");
        }
    }

    public ReviewDTO getReviewById(Long id) {
        return reviewToDTO(reviewRepository.findById(id).orElse(null));
    }

    public Collection<ReviewDTO> getReviewsByProduct(Product product) {
        return reviewToDTOs(reviewRepository.findByProduct(product));
    }

    public void deleteReview(Long productId, Long reviewId) {
        User user = userService.getUserEntity();
        
        if (user == null) {
            throw new IllegalArgumentException("User not logged in");
        }
        
        Review review = reviewRepository.findById(reviewId).orElse(null);
        if (review == null) {
            throw new IllegalArgumentException("Review not found");
        }
        
        // Check if the review belongs to the specified product
        if (!review.getProduct().getId().equals(productId)) {
            throw new IllegalArgumentException("Review does not belong to the specified product");
        }
        
        // Allow admins to delete any review, but regular users can only delete their own
        if (user.getRole() != UserRole.ADMIN && !review.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("Lo siento, no tienes permisos para eliminar esta reseña.");
        }
        
        // Delete the review from database first
        reviewRepository.deleteById(reviewId);
        
        // Force flush to ensure deletion is committed to database immediately
        entityManager.flush();
        entityManager.clear(); // Clear persistence context to force fresh query
        
        // Recalculate product rating after deletion
        Product product = productService.getProductById(productId);
        if (product != null) {
            List<Review> remainingReviews = reviewRepository.findByProduct(product);
            
            if (remainingReviews.isEmpty()) {
                product.setRating(0.0); // No reviews left
            } else {
                double totalRating = remainingReviews.stream()
                    .mapToDouble(Review::getRating)
                    .sum();
                double averageRating = totalRating / remainingReviews.size();
                product.setRating(averageRating);
            }
            
            // Save the updated product rating
            productService.updateProduct(product);
        }
    }
    
    public Review reviewToDomain(ReviewDTO reviewDTO){
        return reviewMapper.toDomain(reviewDTO);
    }
    public ReviewDTO reviewToDTO(Review review){
        return reviewMapper.toDTO(review);
    }
    public Collection<ReviewDTO> reviewToDTOs(Collection<Review> reviews){
        return reviewMapper.toDTOs(reviews);
    }

    public ReviewDTO setComment(String comment, ReviewDTO reviewDTO){
        Review review = reviewToDomain(reviewDTO);
        review.setComment(comment);
        return reviewToDTO(reviewRepository.save(review));
    }

    public ReviewDTO setRating(int rating, ReviewDTO reviewDTO){
        Review review = reviewToDomain(reviewDTO);
        review.setRating(rating);
        return reviewToDTO(review);
    }

    public ReviewApiDTO getReviewAPIById(Long id) {
        return reviewApiMapper.toDTO(reviewRepository.findById(id).orElse(null));
    }

    public ReviewApiDTO saveAPIReview(ReviewApiDTO reviewApiDTO) {
        Review review = reviewApiMapper.toDomain(reviewApiDTO);
        
        // Verificar usuario
        User user = userService.getUserByFirstName(reviewApiDTO.userName());
        if (user == null) {
            throw new IllegalArgumentException("User not found");
        }
        review.setUser(user);
        
        // Verificar producto
        Product product = productService.getProductByName(reviewApiDTO.productName());
        if (product == null) {
            throw new IllegalArgumentException("Product not found");
        }
        review.setProduct(product);
        
        validateReview(review);
        
        // Verificar si el usuario ya tiene una review para este producto
        boolean userHasReview = product.getReviews().stream()
            .anyMatch(r -> r.getUser().getId().equals(user.getId()));
        
        if (userHasReview) {
            throw new IllegalArgumentException("You have already submitted a review for this product");
        }
        
        // Si no tiene review previa, proceder
        product.addReview(review);
        review = reviewRepository.save(review);
        
        return reviewApiMapper.toDTO(review);
    }

    public ReviewApiDTO deleteAPIReview(Long reviewId) {
        Review review = reviewRepository.findById(reviewId).orElse(null);
        if(review== null) {
            throw new IllegalArgumentException("Review not found");
        }
        reviewRepository.delete(review);
        return reviewApiMapper.toDTO(review);
    }

}
