package es.xpressaly.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.core.io.ClassPathResource;

import es.xpressaly.Model.Order;
import es.xpressaly.Model.Product;
import es.xpressaly.Model.Review;
import es.xpressaly.Model.User;
import es.xpressaly.Model.UserRole;
import es.xpressaly.Repository.ProductRepository;
import es.xpressaly.Repository.ReviewRepository;
import es.xpressaly.Repository.UserRepository;
import es.xpressaly.Service.OrderService;
import es.xpressaly.Service.ProductService;
import es.xpressaly.Service.ReviewService;
import es.xpressaly.Service.UserService;
import es.xpressaly.dto.OrderDTO;
import es.xpressaly.dto.ProductDTO;
import es.xpressaly.dto.ProductWebDTO;
import es.xpressaly.dto.ReviewDTO;
import es.xpressaly.dto.UserWebDTO;
import es.xpressaly.mapper.ProductMapper;
import es.xpressaly.mapper.ProductWebMapper;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;
import java.util.Optional;

@Controller
public class ProductController {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductService productService;

    @Autowired
    private ReviewService reviewService;

    @Autowired
    private UserService userService;

    @Autowired
    private OrderController orderController;    

    @Autowired
    private OrderService orderService;

    // Show product list
    @GetMapping("/products")
    public String showProducts(Model model, @RequestParam(required = false, defaultValue = "default") String sort, HttpSession session) {
        try {
            UserWebDTO currentUser = userService.getUser();
            List<ProductWebDTO> products = productService.getAllProductsWeb();
            model.addAttribute("products", products);
            model.addAttribute("isAdmin", currentUser != null && currentUser.role() == UserRole.ADMIN);
            model.addAttribute("cartItemCount", orderController.getCartItemCount(session));
            return "Wellcome";
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "error";
        }
    }
    

    // Show form to create a product - Admin only
    @GetMapping("/create-product")
    public String createProductForm(Model model, HttpSession session) {
        UserWebDTO currentUser = userService.getUser();
        
        // More detailed verification of admin access
        if (currentUser == null) {
            return "redirect:/login";
        }
        
        if (currentUser.role() != UserRole.ADMIN) {
            return "redirect:/products";
        }
        
        model.addAttribute("isAdmin", true);
        model.addAttribute("cartItemCount", orderController.getCartItemCount(session));
        return "add_product";
    }

    // Manage the creation of the new product - Admin only
    @PostMapping("/add-product")
    public String addProduct(@RequestParam String name,
                             @RequestParam String description,
                             @RequestParam double price,
                             @RequestParam int stock,
                             @RequestParam MultipartFile mainImage,
                             @RequestParam(required = false) MultipartFile returnPolicy,
                             Model model) throws IOException {
        UserWebDTO currentUser = userService.getUser();
        if (currentUser == null || currentUser.role() != UserRole.ADMIN) {
            return "redirect:/products";
        }

        if (name == null || name.trim().isEmpty()) {
            model.addAttribute("error", "Product name is required");
            return "add_product";
        }
        if (description == null || description.trim().isEmpty()) {
            model.addAttribute("error", "Product description is required");
            return "add_product";
        }
        if (price <= 0) {
            model.addAttribute("error", "Price must be greater than 0");
            return "add_product";
        }
        if (stock < 0) {
            model.addAttribute("error", "Stock cannot be negative");
            return "add_product";
        }
        if (mainImage == null || mainImage.isEmpty()) {
            model.addAttribute("error", "Product image is required");
            return "add_product";
        }
        if (!mainImage.getContentType().startsWith("image/")) {
            model.addAttribute("error", "The file must be an image");
            return "add_product";
        }

        try {
            byte[] imageBytes = mainImage.getBytes();
            String relativeImagePath = "/Images/" + name + ".jpg";
            
            Product product = new Product(name, description, price, stock, relativeImagePath);
            product.setImageData(imageBytes);
            
            // Manejar el PDF de política de devoluciones si se proporciona
            if (returnPolicy != null && !returnPolicy.isEmpty()) {
                if (!returnPolicy.getContentType().equals("application/pdf")) {
                    model.addAttribute("error", "The return policy must be a PDF file");
                    return "add_product";
                }
                product.setReturnPolicyData(returnPolicy.getBytes());
                product.setReturnPolicyPath("/uploads/politicaDevolucion.pdf");
            }
            
            ProductWebDTO productWebDTO = toWebDTO(product);
            productService.addProduct(productWebDTO);
            return "redirect:/products";
        } catch (IOException e) {
            model.addAttribute("error", e.getMessage());
            return "add_product";
        }
    }

    // Add a review to a product
    @PostMapping("/add-review")
    public String addReview(@RequestParam Long productId,
                          @RequestParam String comment,
                          @RequestParam int rating,
                          Model model) {
        try {
            // Check if the user is logged in
            UserWebDTO user = userService.getUser();
            if (user == null) {
                return "redirect:/login";
            }
            
            // Check if the comment is empty
            if (comment == null || comment.trim().isEmpty() || comment.equals("<p><br></p>")) {
                model.addAttribute("error", "Comment is required");
                return "redirect:/product-details?id=" + productId;
            }
            
            // Check the rating
            if (rating < 1 || rating > 5) {
                model.addAttribute("error", "Rating must be between 1 and 5");
                return "redirect:/product-details?id=" + productId;
            }

            // Log for debugging
            System.out.println("Received HTML comment: " + comment);
            
            // Temporary solution: simple string replacement to filter potentially dangerous tags
            // This is NOT a complete security solution, just a temporary workaround
            String sanitizedComment = comment;
            // Filter potentially dangerous tags - THIS IS NOT A COMPLETE SECURITY SOLUTION
            sanitizedComment = sanitizedComment.replaceAll("(?i)<script.*?>.*?</script.*?>", "")
                .replaceAll("(?i)on\\w+\\s*=\\s*(['\"]).*?\\1", "")
                .replaceAll("(?i)<iframe.*?>.*?</iframe.*?>", "")
                .replaceAll("(?i)<frame.*?>.*?</frame.*?>", "")
                .replaceAll("(?i)<form.*?>.*?</form.*?>", "")
                .replaceAll("(?i)javascript:", "");
            
            // Extract plain text to verify the actual length
            String plainText = sanitizedComment.replaceAll("<[^>]*>", "").trim();
            System.out.println("Extracted plain text: " + plainText);
            System.out.println("Text length: " + plainText.length());
            
            // Check character limit (400)
            if (plainText.length() > 400) {
                model.addAttribute("error", "Review exceeds maximum of 400 characters");
                return "redirect:/product-details?id=" + productId + "&error=character_limit";
            }
            
            ProductWebDTO productWebDTO = productService.getProductByIdWeb(productId);

            if (productWebDTO == null) {
                return "redirect:/products";
            }

            // Crear ReviewDTO en lugar de Review
            ReviewDTO reviewDTO = new ReviewDTO(
                null, // id será generado por la base de datos
                sanitizedComment,
                rating,
                user,
                productWebDTO
            );

            reviewService.addReview(productId, reviewDTO);

            return "redirect:/product-details?id=" + productId;
        } catch (Exception e) {
            System.out.println("Error processing review: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("error", e.getMessage());
            return "redirect:/product-details?id=" + productId;
        }
    }

    // Delete a product - Admin only
    @PostMapping("/delete-product")
    public String deleteProduct(@RequestParam Long productId, 
                               HttpSession session, 
                               Model model,
                               @RequestParam(required = false) Boolean ajax) throws IOException {
        UserWebDTO currentUser = userService.getUser();
        if (currentUser == null || currentUser.role() != UserRole.ADMIN) {
            return "redirect:/products";
        }

        try {
            System.out.println("Attempting to delete product with ID: " + productId);
            
            // Remove from current order if present
            OrderDTO currentOrder = orderController.getCurrentOrder(session);
            ProductWebDTO productWebDTO = productService.getProductByIdWeb(productId);
            
            if (productWebDTO == null) {
                System.out.println("Product not found with ID: " + productId);
                model.addAttribute("error", "Product not found");
                return "redirect:/product-management";
            }
            
            if (currentOrder != null && productWebDTO != null) {
                OrderDTO updatedOrder = orderService.removeProductFromOrder(currentOrder, productWebDTO);
                orderController.setCurrentOrder(session, updatedOrder);
            }
            
            // Delete the product
            productService.deleteProduct(productId);
            System.out.println("Product deleted successfully");
            
            // Si es una solicitud AJAX, devolver un JSON simple
            if (ajax != null && ajax) {
                return "redirect:/product-management?success=true";
            }
            
            return "redirect:/product-management";
        } catch (Exception e) {
            System.out.println("Error deleting product: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("error", "Error deleting product: " + e.getMessage());
            return "redirect:/product-management";
        }
    }


    // Search products endpoint that queries the database
    @GetMapping("/search-products")
    public String searchProducts(@RequestParam String term, Model model, HttpSession session) {
        try {
            List<ProductWebDTO> searchResults = productService.searchProductsWeb(term);
            UserWebDTO currentUser = userService.getUser();
            model.addAttribute("products", searchResults);
            model.addAttribute("isAdmin", currentUser != null && currentUser.role() == UserRole.ADMIN);
            model.addAttribute("cartItemCount", orderController.getCartItemCount(session));
            model.addAttribute("searchTerm", term);
            return "Wellcome";
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "error";
        }
    }

    @GetMapping("/product-details")
    public String getProductDetails(@RequestParam Long id, Model model) {
        try {
            ProductWebDTO productWebDTO = productService.getProductByIdWeb(id);
            if (productWebDTO != null) {
                model.addAttribute("product", productWebDTO);
                model.addAttribute("rating", productWebDTO.rating());
                model.addAttribute("reviews", productWebDTO.reviews());
                model.addAttribute("isLoggedIn", userService.getUser() != null);
                
                // Añadir información del documento de política de devoluciones
                return "Product";
            }
            return "redirect:/products";
        } catch (Exception e) {
            return "redirect:/products";
        }
    }

    @PostMapping("/delete-review")
    public String deleteReview(@RequestParam Long productId, @RequestParam Long reviewId, Model model) {
        UserWebDTO currentUser = userService.getUser();
        if (currentUser == null) {
            return "redirect:/login";
        }

        try {
            reviewService.deleteReview(productId, reviewId);
            return "redirect:/product-details?id=" + productId;
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "redirect:/product-details?id=" + productId;
        }
    }

    // Endpoint for searching products with JSON results
    @GetMapping("/search-products-json")
    @ResponseBody
    public Map<String, Object> searchProductsJson(
            @RequestParam String term,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(required = false, defaultValue = "0") double minPrice,
            @RequestParam(required = false) Double maxPrice) {
        Map<String, Object> response = new HashMap<>();
        try {
            // We calculate the maximum effective price
            double dynamicMaxPrice = productService.getMaxPriceForFilter();
            double effectiveMaxPrice = maxPrice != null ? maxPrice : dynamicMaxPrice;
            
            // We get the results with price filter
            List<ProductDTO> searchResults;
            if (minPrice > 0 || maxPrice != null) {
                searchResults = productService.searchProductsByPrice(term, minPrice, effectiveMaxPrice);
            } else {
                searchResults = productService.searchProducts(term);
            }
            
            // Calculate pagination manually
            int total = searchResults.size();
            int start = (page - 1) * size;
            int end = Math.min(start + size, total);
            
            // Create sublist for current page
            List<ProductDTO> pageContent = start < end 
                ? searchResults.subList(start, end) 
                : List.of();
            
            // Convert products to maps without image data
            List<Map<String, Object>> productList = pageContent.stream()
                .map(this::convertToMap)
                .collect(Collectors.toList());
            
            // Pagination information
            response.put("products", productList);
            response.put("totalItems", total);
            response.put("totalPages", (int) Math.ceil((double) total / size));
            response.put("currentPage", page);
            response.put("hasMore", end < total);
            response.put("maxPriceForFilter", dynamicMaxPrice);
            
            return response;
        } catch (Exception e) {
            e.printStackTrace();
            response.put("error", e.getMessage());
            return response;
        }
    }
    
    // Method to convert a product to a map, excluding binary image data
    private Map<String, Object> convertToMap(ProductDTO productDTO) {
        Map<String, Object> productMap = new HashMap<>();
        productMap.put("id", productDTO.id());
        productMap.put("name", productDTO.name());
        productMap.put("description", productDTO.description());
        productMap.put("price", productDTO.price());
        productMap.put("stock", productDTO.stock());
        // Add isAdmin attribute for administrator button rendering if needed
        UserWebDTO currentUser = userService.getUser();
        productMap.put("isAdmin", currentUser != null && currentUser.role() == UserRole.ADMIN);
        // We don't include imageData to avoid overloading the JSON response
        return productMap;
    }

    @GetMapping("/get-cart-quantity")
    @ResponseBody
    public int getCartQuantity(@RequestParam Long productId, HttpSession session) {
        OrderDTO currentOrder = orderController.getCurrentOrder(session);
        if (currentOrder != null && currentOrder.products() != null) {
            return orderController.getProductQuantity(currentOrder, productId);
        }
        return 0;
    }

    // Product Management Page - Admin only
    @GetMapping("/product-management")
    public String productManagement(Model model, HttpSession session) {
        UserWebDTO currentUser = userService.getUser();
        
        // Verificación de acceso de administrador
        if (currentUser == null) {
            return "redirect:/login";
        }
        
        if (currentUser.role() != UserRole.ADMIN) {
            return "redirect:/products";
        }
        
        // Ya no cargamos todos los productos inicialmente
        // Los productos se cargarán dinámicamente a través de la API
        List<ProductDTO> products = productService.getAllProducts();
        model.addAttribute("products", products);
        model.addAttribute("isAdmin", true);
        model.addAttribute("cartItemCount", orderController.getCartItemCount(session));
        return "product-management";
    }

    // Show form to edit a product - Admin only
    @GetMapping("/edit-product/{id}")
    public String editProductForm(@PathVariable Long id, Model model, HttpSession session) {
        UserWebDTO currentUser = userService.getUser();
        
        //Verification of admin access
        if (currentUser == null) {
            return "redirect:/login";
        }
        
        if (currentUser.role() != UserRole.ADMIN) {
            return "redirect:/products";
        }
        
        ProductWebDTO productWebDTO = productService.getProductByIdWeb(id);
        if (productWebDTO == null) {
            return "redirect:/product-management";
        }
        
        
        model.addAttribute("product", productWebDTO);
        model.addAttribute("isAdmin", true);
        model.addAttribute("cartItemCount", orderController.getCartItemCount(session));
        return "edit-product";
    }

    // Update an existing product - Admin only
    @PostMapping("/update-product")
    public String updateProduct(@RequestParam Long productId,
                               @RequestParam String name,
                               @RequestParam String description,
                               @RequestParam double price,
                               @RequestParam int stock,
                               @RequestParam(required = false) MultipartFile mainImage,
                               @RequestParam(required = false) MultipartFile returnPolicy,
                               Model model) throws IOException {
        UserWebDTO currentUser = userService.getUser();
        if (currentUser == null || currentUser.role() != UserRole.ADMIN) {
            return "redirect:/products";
        }

        ProductWebDTO existingProductWebDTO = productService.getProductByIdWeb(productId);
        if (existingProductWebDTO == null) {
            return "redirect:/product-management";
        }

        // Validations
        if (name == null || name.trim().isEmpty()) {
            model.addAttribute("error", "Product name is required");
            model.addAttribute("product", existingProductWebDTO);
            return "edit-product";
        }
        if (description == null || description.trim().isEmpty()) {
            model.addAttribute("error", "Product description is required");
            model.addAttribute("product", existingProductWebDTO);
            return "edit-product";
        }
        if (price <= 0) {
            model.addAttribute("error", "Price must be greater than 0");
            model.addAttribute("product", existingProductWebDTO);
            return "edit-product";
        }
        if (stock < 0) {
            model.addAttribute("error", "Stock cannot be negative");
            model.addAttribute("product", existingProductWebDTO);
            return "edit-product";
        }

        try {
            Product product = toDomain(existingProductWebDTO);
            product.setName(name);
            product.setDescription(description);
            product.setPrice(price);
            product.setStock(stock);
            
            // Update image if provided
            if (mainImage != null && !mainImage.isEmpty()) {
                if (!mainImage.getContentType().startsWith("image/")) {
                    model.addAttribute("error", "The file must be an image");
                    model.addAttribute("product", existingProductWebDTO);
                    return "edit-product";
                }
                
                byte[] imageBytes = mainImage.getBytes();
                String relativeImagePath = "/Images/" + name + ".jpg";
                product.setImagePath(relativeImagePath);
                product.setImageData(imageBytes);
            }

            // Update return policy if provided
            if (returnPolicy != null && !returnPolicy.isEmpty()) {
                if (!returnPolicy.getContentType().equals("application/pdf")) {
                    model.addAttribute("error", "The return policy must be a PDF file");
                    model.addAttribute("product", existingProductWebDTO);
                    return "edit-product";
                }
                product.setReturnPolicyData(returnPolicy.getBytes());
                product.setReturnPolicyPath("/uploads/politicaDevolucion.pdf");
            }
            
            ProductWebDTO updatedProductWebDTO = toWebDTO(product);
            productService.updateProductWeb(updatedProductWebDTO);
            return "redirect:/product-management";
        } catch (IOException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("product", existingProductWebDTO);
            return "edit-product";
        }
    }

    private Product toDomain(ProductWebDTO productWebDTO) {
        return productService.toWebDomain(productWebDTO);
    }

    private ProductWebDTO toWebDTO(Product product) {
        return productService.toWebDTO(product);
    }

    @GetMapping("/download-return-policy/{id}")
    public ResponseEntity<byte[]> downloadReturnPolicy(@PathVariable Long id) throws IOException {
        Optional<Product> productOpt = productRepository.findById(id);
        if (productOpt.isPresent() && productOpt.get().getReturnPolicyData() != null) {
            Product product = productOpt.get();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", product.getReturnPolicyPath());
            return new ResponseEntity<>(product.getReturnPolicyData(), headers, HttpStatus.OK);
        } else {
            // Cargar el PDF por defecto desde resources
            ClassPathResource defaultPdf = new ClassPathResource("uploads/returnPolicy.pdf");
            byte[] defaultPdfBytes = defaultPdf.getInputStream().readAllBytes();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "returnPolicy.pdf");
            return new ResponseEntity<>(defaultPdfBytes, headers, HttpStatus.OK);
        }
    }
}


