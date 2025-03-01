package es.web.xpressaly;

import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class ReviewService {

    private ProductService productService;

    public ReviewService(ProductService productService) {
        this.productService = productService;
    }

    public void addReview(Long productId, Review review) {
        Product product = productService.getProductById(productId);
        if (product != null) {
            product.addReview(review);  // Añadir la reseña al producto
        }
    }

    public void deleteReview(Long productId, Long reviewId) {
        Product product = productService.getProductById(productId);
        if (product != null) {
            product.getReviews().removeIf(review -> review.getId().equals(reviewId));  // Eliminar la reseña por ID
        }
    }
}

