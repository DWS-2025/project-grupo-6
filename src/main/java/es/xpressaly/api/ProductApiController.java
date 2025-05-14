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
import es.xpressaly.dto.ProductDTO;
import es.xpressaly.Model.User;
import es.xpressaly.Service.UserService;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/products")
public class ProductApiController {

    @Autowired
    private ProductService productService;

    @Autowired
    private ReviewService reviewService;

    @Autowired
    private UserService userService;

    @GetMapping
    public ResponseEntity<Map<String, Object>> getProducts(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false, defaultValue = "default") String sort,
            @RequestParam(required = false, defaultValue = "0") double minPrice,
            @RequestParam(required = false) Double maxPrice) {
        try {
            Map<String, Object> response = new HashMap<>();
            
            // Calculate dynamic max price if not provided
            double dynamicMaxPrice = productService.getMaxPriceForFilter();
            double effectiveMaxPrice = maxPrice != null ? maxPrice : dynamicMaxPrice;
            
            // Add the dynamic max price to response for client-side slider
            response.put("maxPriceForFilter", dynamicMaxPrice);
            
            if (search != null && !search.isEmpty()) {
                // Modified: We no longer ignore price filters in text searches
                List<Product> products;
                
                // Use the price filter if specified
                if (minPrice > 0 || maxPrice != null) {
                    products = productService.searchProductsByPrice(search, minPrice, effectiveMaxPrice);
                } else {
                    products = productService.searchProducts(search);
                }
                
                List<Map<String, Object>> productList = products.stream()
                    .map(this::convertToMap)
                    .collect(Collectors.toList());
                
                response.put("products", productList);
                response.put("totalPages", 1);
                response.put("currentPage", 1);
                response.put("hasMore", false);
            } else {
                // Normal pagination with price filtering
                Page<Product> productPage;
                
                // Use price filter if specified
                if (minPrice > 0 || maxPrice != null) {
                    productPage = productService.getProductsByPageAndPrice(page, size, sort, minPrice, effectiveMaxPrice);
                } else {
                    productPage = productService.getProductsByPage(page, size, sort);
                }
                
                List<Map<String, Object>> productList = productPage.getContent().stream()
                    .map(this::convertToMap)
                    .collect(Collectors.toList());
                
                response.put("products", productList);
                response.put("totalPages", productPage.getTotalPages());
                response.put("currentPage", productPage.getNumber() + 1);
                response.put("hasMore", productPage.getNumber() + 1 < productPage.getTotalPages());
            }
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }
    
    // Method to convert a product to a map, excluding binary image data
    private Map<String, Object> convertToMap(Product product) {
        Map<String, Object> productMap = new HashMap<>();
        productMap.put("id", product.getId());
        productMap.put("name", product.getName());
        productMap.put("description", product.getDescription());
        productMap.put("price", product.getPrice());
        productMap.put("stock", product.getStock());
        // Check if the current user is an administrator
        try {
            User currentUser = userService.getUser();
            productMap.put("isAdmin", currentUser != null && currentUser.isAdmin());
        } catch (Exception e) {
            productMap.put("isAdmin", false);
        }
        // We don't include imageData to avoid overloading the JSON response
        return productMap;
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