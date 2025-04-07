package es.xpressaly.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.data.domain.Page;

import es.xpressaly.Model.Product;
import es.xpressaly.Model.Review;
import es.xpressaly.Service.ProductService;
import es.xpressaly.Service.ReviewService;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/products")
public class ProductApiController {

    @Autowired
    private ProductService productService;

    @Autowired
    private ReviewService reviewService;

    @GetMapping
    public ResponseEntity<Map<String, Object>> getProducts(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false, defaultValue = "default") String sort) {
        try {
            Map<String, Object> response = new HashMap<>();
            
            if (search != null && !search.isEmpty()) {
                List<Product> products = productService.searchProducts(search);
                response.put("products", products);
                response.put("totalPages", 1);
                response.put("currentPage", 1);
            } else {
                Page<Product> productPage = productService.getProductsByPage(page, size, sort);
                response.put("products", productPage.getContent());
                response.put("totalPages", productPage.getTotalPages());
                response.put("currentPage", productPage.getNumber() + 1);
                response.put("hasMore", productPage.getNumber() + 1 < productPage.getTotalPages());
            }
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> addProduct(
            @RequestParam String name,
            @RequestParam String description,
            @RequestParam double price,
            @RequestParam int stock,
            @RequestParam MultipartFile mainImage) {
        
        Map<String, Object> response = new HashMap<>();
        
        if (name == null || name.trim().isEmpty()) {
            response.put("error", "Product name is required");
            return ResponseEntity.badRequest().body(response);
        }
        if (description == null || description.trim().isEmpty()) {
            response.put("error", "Product description is required");
            return ResponseEntity.badRequest().body(response);
        }
        if (price <= 0) {
            response.put("error", "Price must be greater than 0");
            return ResponseEntity.badRequest().body(response);
        }
        if (stock < 0) {
            response.put("error", "Stock cannot be negative");
            return ResponseEntity.badRequest().body(response);
        }
        if (mainImage == null || mainImage.isEmpty()) {
            response.put("error", "Product image is required");
            return ResponseEntity.badRequest().body(response);
        }
        if (!mainImage.getContentType().startsWith("image/")) {
            response.put("error", "The file must be an image");
            return ResponseEntity.badRequest().body(response);
        }

        try {
            byte[] imageBytes = mainImage.getBytes();
            String relativeImagePath = "/Images/" + name + ".jpg";
            
            Product newProduct = new Product(name, description, price, stock, relativeImagePath);
            newProduct.setImageData(imageBytes);
            
            productService.addProduct(newProduct);
            response.put("success", "Product added successfully");
            return ResponseEntity.ok(response);
        } catch (IOException e) {
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/{productId}/reviews")
    public ResponseEntity<Map<String, Object>> addReview(
            @PathVariable Long productId,
            @RequestParam String comment,
            @RequestParam int rating) {
        
        Map<String, Object> response = new HashMap<>();
        
        if (comment == null || comment.trim().isEmpty()) {
            response.put("error", "Comment is required");
            return ResponseEntity.badRequest().body(response);
        }
        if (rating < 1 || rating > 5) {
            response.put("error", "Rating must be between 1 and 5");
            return ResponseEntity.badRequest().body(response);
        }

        try {
            Product product = productService.getProductById(productId);
            if (product == null) {
                response.put("error", "Product not found");
                return ResponseEntity.badRequest().body(response);
            }
            
            Review review = new Review(null, comment, rating);
            review.setProduct(product);
            
            reviewService.addReview(productId, review);
            response.put("success", "Review added successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @DeleteMapping("/{productId}")
    public ResponseEntity<Map<String, Object>> deleteProduct(@PathVariable Long productId) {
        Map<String, Object> response = new HashMap<>();
        try {
            productService.deleteProduct(productId);
            response.put("success", "Product deleted successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getProductDetails(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        try {
            Product product = productService.getProductWithReviews(id);
            if (product == null) {
                response.put("error", "Product not found");
                return ResponseEntity.badRequest().body(response);
            }

            response.put("product", product);
            response.put("reviews", product.getReviews());
            response.put("averageRating", productService.getAverageRating(product.getId()));
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @DeleteMapping("/{productId}/reviews/{reviewId}")
    public ResponseEntity<Map<String, Object>> deleteReview(
            @PathVariable Long productId,
            @PathVariable Long reviewId) {
        
        Map<String, Object> response = new HashMap<>();
        try {
            reviewService.deleteReview(productId, reviewId);
            response.put("success", "Review deleted successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}