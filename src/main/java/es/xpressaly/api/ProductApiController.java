package es.xpressaly.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.PageImpl;

import es.xpressaly.Model.Product;
import es.xpressaly.Model.Review;
import es.xpressaly.Service.ProductService;
import es.xpressaly.Service.ReviewService;
import es.xpressaly.dto.ProductDTO;
import es.xpressaly.dto.ProductWebDTO;
import es.xpressaly.dto.UserWebDTO;
import es.xpressaly.dto.ReviewDTO;
import es.xpressaly.mapper.ProductMapper;
import es.xpressaly.Model.User;
import es.xpressaly.Model.UserRole;
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
    public ResponseEntity<Page<ProductDTO>> getProducts(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false, defaultValue = "default") String sort,
            @RequestParam(required = false, defaultValue = "0") double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false, defaultValue = "0") int minRating) {
        try {
            // Create the Pageable object
            Pageable pageable = PageRequest.of(page - 1, size, getSortDirection(sort));
            
            // Get the dynamic maximum price
            double dynamicMaxPrice = productService.getMaxPriceForFilter();
            double effectiveMaxPrice = maxPrice != null ? maxPrice : dynamicMaxPrice;
            
            // Get the page of products according to the filters
            Page<ProductDTO> productPage;
            boolean hasSearch = search != null && !search.isEmpty();
            boolean hasPrice = minPrice > 0 || maxPrice != null;
            boolean hasRating = minRating > 0;

            if (hasSearch && hasPrice && hasRating) {
                productPage = productService.searchProductsByPriceAndMinRating(search, minPrice, effectiveMaxPrice, minRating, pageable);
            } else if (hasSearch && hasRating) {
                productPage = productService.searchProductsByMinRating(search, minRating, pageable);
            } else if (hasPrice && hasRating) {
                productPage = productService.getProductsByPriceAndMinRating(minPrice, effectiveMaxPrice, minRating, pageable);
            } else if (hasSearch && hasPrice) {
                productPage = productService.searchProductsByPriceAndPageable(search, minPrice, effectiveMaxPrice, pageable);
            } else if (hasRating) {
                productPage = productService.getProductsByMinRating(minRating, pageable);
            } else if (hasSearch) {
                productPage = productService.searchProductsByPageable(search, pageable);
            } else if (hasPrice) {
                productPage = productService.getProductsByPageAndPrice(pageable, sort, minPrice, effectiveMaxPrice);
            } else {
                productPage = productService.getProductsByPage(pageable, sort);
            }
            
            return ResponseEntity.ok(productPage);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }

    // Auxiliary method to get the sorting direction
    private Sort getSortDirection(String sort) {
        if (sort == null || sort.equals("default")) {
            return Sort.by(Sort.Direction.DESC, "id");
        }
        
        String[] parts = sort.split("_");
        if (parts.length != 2) {
            return Sort.by(Sort.Direction.DESC, "id");
        }
        
        String field = parts[0];
        String direction = parts[1].toUpperCase();
        
        return Sort.by(Sort.Direction.valueOf(direction), field);
    }
    
    private Map<String, Object> convertToMap(ProductDTO productDTO) {
        Map<String, Object> productMap = new HashMap<>();
        productMap.put("id", productDTO.id());
        productMap.put("name", productDTO.name());
        productMap.put("description", productDTO.description());
        productMap.put("price", productDTO.price());
        productMap.put("stock", productDTO.stock());
        try {
            UserWebDTO currentUser = userService.getUser();
            productMap.put("isAdmin", currentUser != null && currentUser.role() == UserRole.ADMIN);
        } catch (Exception e) {
            productMap.put("isAdmin", false);
        }
        return productMap;
    }

    @PostMapping("/")
    public ResponseEntity<ProductWebDTO> addProduct(@RequestParam String name,
                                                    @RequestParam String description,
                                                    @RequestParam double price,
                                                    @RequestParam int stock,
                                                    @RequestParam MultipartFile imageData) {
        try {
            UserWebDTO currentUser = userService.getUser();
            if (currentUser == null || currentUser.role() != UserRole.ADMIN) {
                return new ResponseEntity<>(HttpStatus.FORBIDDEN);
            }
            // Validations
            if (name == null || name.trim().isEmpty()) {
                return ResponseEntity.badRequest().build();
            }
            if (description == null || description.trim().isEmpty()) {
                return ResponseEntity.badRequest().build();
            }
            if (price <= 0) {
                return ResponseEntity.badRequest().build();
            }
            if (stock < 0) {
                return ResponseEntity.badRequest().build();
            }
            String imagePath = "/Images/" + name + ".jpg";
            if (imagePath == null || imagePath.trim().isEmpty()) {
                return ResponseEntity.badRequest().build();
            }
            if (imageData == null || imageData.isEmpty()) {
                return ResponseEntity.badRequest().build();
            }

            // Verify that the image is valid
            try {
                BufferedImage image = ImageIO.read(new java.io.ByteArrayInputStream(imageData.getBytes()));
                if (image == null) {
                    return ResponseEntity.badRequest().build();
                }
            } catch (IOException e) {
                return ResponseEntity.badRequest().build();
            }
            ProductWebDTO productWebDTO= new ProductWebDTO(
                null, // ID se asignará automáticamente
                name,
                description,
                price,
                stock,
                imagePath,
                imageData.getBytes(), // Convertir MultipartFile a byte[]
                null, // returnPolicyPath no se usa en este endpoint
                null, // returnPolicyData no se usa en este endpoint
                null, // reviews no se usa en este endpoint
                0.0 // rating inicial
            );
            productService.addProduct(productWebDTO);
            return ResponseEntity.ok(productWebDTO);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{productId}")
    public ResponseEntity<ProductWebDTO> updateProduct(
            @PathVariable Long productId,
            @RequestParam String name,
            @RequestParam String description,
            @RequestParam double price,
            @RequestParam int stock,
            @RequestParam(required = false) MultipartFile imageData) {
        try {
            UserWebDTO currentUser = userService.getUser();
            if (currentUser == null || currentUser.role() != UserRole.ADMIN) {
                return new ResponseEntity<>(HttpStatus.FORBIDDEN);
            }
            if (name == null || name.trim().isEmpty()) {
                return ResponseEntity.badRequest().build();
            }
            if (description == null || description.trim().isEmpty()) {
                return ResponseEntity.badRequest().build();
            }
            if (price <= 0) {
                return ResponseEntity.badRequest().build();
            }
            if (stock < 0) {
                return ResponseEntity.badRequest().build();
            }

            ProductWebDTO existingProduct = productService.getProductByIdWeb(productId);
            if (existingProduct == null) {
                return ResponseEntity.notFound().build();
            }

            String imagePath = existingProduct.imagePath(); // Keep the existing image path
            byte[] imageBytes = existingProduct.imageData(); // Keep the existing image data

            if (imageData != null && !imageData.isEmpty()) {
                // Verify that the new image is valid
                try {
                    BufferedImage image = ImageIO.read(new java.io.ByteArrayInputStream(imageData.getBytes()));
                    if (image == null) {
                        return ResponseEntity.badRequest().build();
                    }
                    imageBytes = imageData.getBytes(); // Update the image data
                } catch (IOException e) {
                    return ResponseEntity.badRequest().build();
                }
            }

            ProductWebDTO updatedProduct = new ProductWebDTO(
                productId, // ID of the product to update
                name,
                description,
                price,
                stock,
                imagePath,
                imageBytes, // Use the updated image data
                null, // returnPolicyPath not used in this endpoint
                null, // returnPolicyData not used in this endpoint
                null, // reviews not used in this endpoint
                0.0 // initial rating
            );

            productService.updateProductWeb(updatedProduct);
            return ResponseEntity.ok(updatedProduct);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{productId}")
    public ResponseEntity<?> deleteProduct(@PathVariable Long productId) {
        try {
            UserWebDTO currentUser = userService.getUser();
            if (currentUser == null || currentUser.role() != UserRole.ADMIN) {
                return new ResponseEntity<>(HttpStatus.FORBIDDEN);
            }
            ProductDTO productDTO = productService.getProductDTO(productId);
            productService.deleteProduct(productId);
            return ResponseEntity.ok(productDTO);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/{id}")//works perfectly
    public ResponseEntity<ProductDTO> getProductDetails(@PathVariable Long id) {
        try {
            ProductDTO productDTO = productService.getProductWithReviews(id);
            if (productDTO == null) {
                return ResponseEntity.badRequest().build();
            }
            return ResponseEntity.ok(productDTO);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }


    @GetMapping("/management")
    public ResponseEntity<Map<String, Object>> getProductsForManagement(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        UserWebDTO currentUser = userService.getUser();
        if (currentUser == null || currentUser.role() != UserRole.ADMIN) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
        Map<String, Object> response = new HashMap<>();
        try {
            // Validate input parameters
            if (page < 1) {
                page = 1;
            }
            if (size < 1 || size > 100) {
                size = 20;
            }
            
            Pageable pageable = PageRequest.of(page - 1, size);
            Page<ProductWebDTO> productPage = productService.getProductsByPageWeb(pageable, "default");
            
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
            UserWebDTO currentUser = userService.getUser();
            productMap.put("isAdmin", currentUser != null && currentUser.role() == UserRole.ADMIN);
        } catch (Exception e) {
            productMap.put("isAdmin", false);
        }
        return productMap;
    }
    
    
    
    private Product toDomain(ProductDTO productDTO) {
        return productService.toDomain(productDTO);
    }

    @GetMapping("/debug")
    public ResponseEntity<Map<String, Object>> debugProducts() {
        Map<String, Object> response = new HashMap<>();
        try {
            List<Product> allProducts = productService.getAllProductsNormal();
            response.put("totalProducts", allProducts.size());
            response.put("products", allProducts);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}