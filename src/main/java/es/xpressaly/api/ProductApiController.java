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
import es.xpressaly.dto.ProductWebDTO;
import es.xpressaly.mapper.ProductMapper;
import es.xpressaly.Model.User;
import es.xpressaly.Service.UserService;

import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Collection;
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

    
    @GetMapping("/all")
    public List<ProductDTO> getAllProductsAPI() {
        return productService.getAllProducts();
    }

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
            
            double dynamicMaxPrice = productService.getMaxPriceForFilter();
            double effectiveMaxPrice = maxPrice != null ? maxPrice : dynamicMaxPrice;
            
            response.put("maxPriceForFilter", dynamicMaxPrice);
            
            if (search != null && !search.isEmpty()) {
                List<ProductDTO> products;
                
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
                Page<ProductDTO> productPage;
                
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
    
    private Map<String, Object> convertToMap(ProductDTO productDTO) {
        Map<String, Object> productMap = new HashMap<>();
        productMap.put("id", productDTO.id());
        productMap.put("name", productDTO.name());
        productMap.put("description", productDTO.description());
        productMap.put("price", productDTO.price());
        productMap.put("stock", productDTO.stock());
        try {
            User currentUser = userService.getUser();
            productMap.put("isAdmin", currentUser != null && currentUser.isAdmin());
        } catch (Exception e) {
            productMap.put("isAdmin", false);
        }
        return productMap;
    }

    @PostMapping
    public ResponseEntity<ProductWebDTO> addProduct(@RequestBody ProductWebDTO productWebDTO) {
        try {
            // Validaciones
            if (productWebDTO.name() == null || productWebDTO.name().trim().isEmpty()) {
                return ResponseEntity.badRequest().build();
            }
            if (productWebDTO.description() == null || productWebDTO.description().trim().isEmpty()) {
                return ResponseEntity.badRequest().build();
            }
            if (productWebDTO.price() <= 0) {
                return ResponseEntity.badRequest().build();
            }
            if (productWebDTO.stock() < 0) {
                return ResponseEntity.badRequest().build();
            }
            if (productWebDTO.imagePath() == null || productWebDTO.imagePath().trim().isEmpty()) {
                return ResponseEntity.badRequest().build();
            }
            if (productWebDTO.imageData() == null || productWebDTO.imageData().length == 0) {
                return ResponseEntity.badRequest().build();
            }

            // Verificar que la imagen sea válida
            try {
                BufferedImage image = ImageIO.read(new java.io.ByteArrayInputStream(productWebDTO.imageData()));
                if (image == null) {
                    return ResponseEntity.badRequest().build();
                }
            } catch (IOException e) {
                return ResponseEntity.badRequest().build();
            }

            productService.addProduct(productWebDTO);
            return ResponseEntity.ok(productWebDTO);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /*@PostMapping("/{productId}/reviews")
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
    }*/

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
            ProductDTO productDTO = productService.getProductWithReviews(id);
            if (productDTO == null) {
                response.put("error", "Product not found");
                return ResponseEntity.badRequest().body(response);
            }

            Product product = toDomain(productDTO);
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

    @GetMapping("/management")
    public ResponseEntity<Map<String, Object>> getProductsForManagement(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Validar parámetros de entrada
            if (page < 1) {
                page = 1;
            }
            if (size < 1 || size > 100) {
                size = 20;
            }
            
            Page<ProductWebDTO> productPage = productService.getProductsByPageWeb(page, size, "default");
            
            if (productPage == null) {
                response.put("error", "No se pudieron cargar los productos");
                return ResponseEntity.badRequest().body(response);
            }
            
            List<Map<String, Object>> productList = productPage.getContent().stream()
                .map(this::convertWebToMap)
                .collect(Collectors.toList());
            
            response.put("products", productList);
            response.put("totalPages", productPage.getTotalPages());
            response.put("currentPage", productPage.getNumber() + 1);
            response.put("hasMore", productPage.getNumber() + 1 < productPage.getTotalPages());
            response.put("totalElements", productPage.getTotalElements());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            response.put("error", "Error al cargar los productos: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    private Map<String, Object> convertWebToMap(ProductWebDTO productWebDTO) {
        Map<String, Object> productMap = new HashMap<>();
        productMap.put("id", productWebDTO.id());
        productMap.put("name", productWebDTO.name());
        productMap.put("description", productWebDTO.description());
        productMap.put("price", productWebDTO.price());
        productMap.put("stock", productWebDTO.stock());
        try {
            User currentUser = userService.getUser();
            productMap.put("isAdmin", currentUser != null && currentUser.isAdmin());
        } catch (Exception e) {
            productMap.put("isAdmin", false);
        }
        return productMap;
    }
    
    
    
    private Product toDomain(ProductDTO productDTO) {
        return productService.toDomain(productDTO);
    }
}