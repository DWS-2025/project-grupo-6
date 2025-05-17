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
import es.xpressaly.dto.ReviewDTO;
import es.xpressaly.dto.UserWebDTO;
import es.xpressaly.mapper.ProductWebMapper;
import es.xpressaly.mapper.ReviewMapper;
import es.xpressaly.mapper.UserWebMapper;

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
    private ProductWebMapper productWebMapper;

    public ReviewService(ProductService productService, UserService userService, ReviewRepository reviewRepository) {
        this.productService = productService;
        this.userService = userService;
        this.reviewRepository = reviewRepository;
    }

    public Collection<ReviewDTO> getAllReviews() {
        return reviewToDTOs(reviewRepository.findAll());
    }
    
    public ReviewDTO saveReview(ReviewDTO reviewDTO) {
        Review review = reviewToDomain(reviewDTO);
        validateReview(review);
        return reviewToDTO(reviewRepository.save(review));
    }
    
    public void addReview(Long productId, ReviewDTO reviewDTO) {
        Review review = reviewToDomain(reviewDTO);
        validateReview(review);
        Product product = productWebMapper.toDomain(productService.getProductByIdWeb(productId));
        if (product != null) {
            // Asegurarse de que la lista de reviews esté inicializada
            if (product.getReviews() == null) {
                product.setReviews(new ArrayList<>());
            }
            
            // Verificar si el usuario ya tiene una review para este producto
            boolean userHasReview = product.getReviews().stream()
                .anyMatch(r -> r.getUser().getId().equals(review.getUser().getId()));
            
            if (userHasReview) {
                throw new IllegalArgumentException("Ya has dejado una review para este producto");
            }
            
            // Establecer la relación bidireccional
            review.setProduct(product);
            
            // Guardar la review primero
            reviewRepository.save(review);
            
            // Obtener todas las reviews del producto incluyendo la nueva
            List<Review> allReviews = reviewRepository.findByProduct(product);
            
            // Calcular el nuevo rating promedio
            double totalRating = allReviews.stream()
                .mapToDouble(Review::getRating)
                .sum();
            
            double averageRating = allReviews.isEmpty() ? 0.0 : totalRating / allReviews.size();
            product.setRating(averageRating);
            product.addReview(review);
            // Actualizar el producto con el nuevo rating
            productService.updateProductWeb(productWebMapper.toDTO(product));
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
        Product product = productWebMapper.toDomain(productService.getProductByIdWeb(productId));
        UserWebDTO user = userService.getUser();
        
        if (product != null && user != null) {
            Review review = reviewRepository.findById(reviewId).orElse(null);
            if (review != null) {
                // Allow admins to delete any review, but regular users can only delete their own
                if (user.role() == UserRole.ADMIN || review.getUser().getId().equals(user.id())) {
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
}
