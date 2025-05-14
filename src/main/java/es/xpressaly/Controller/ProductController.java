package es.xpressaly.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.bind.annotation.PathVariable;

import es.xpressaly.Model.Order;
import es.xpressaly.Model.Product;
import es.xpressaly.Model.Review;
import es.xpressaly.Model.User;
import es.xpressaly.Service.ProductService;
import es.xpressaly.Service.ReviewService;
import es.xpressaly.Service.UserService;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
//import java.nio.file.Files;
//import java.nio.file.Path;
//import java.nio.file.Paths;
//import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;
// Temporarily removed JSoup imports until server can be restarted with the dependency
// import org.jsoup.Jsoup;
// import org.jsoup.safety.Safelist;


@Controller
public class ProductController {

    @Autowired
    private ProductService productService;

    @Autowired
    private ReviewService reviewService;

    @Autowired
    private UserService userService;

    @Autowired
    private OrderController orderController;    

    // Show product list
    @GetMapping("/products")
    public String showProducts(Model model, @RequestParam(required = false, defaultValue = "default") String sort,HttpSession session) {
        try {
            // We no longer load all products initially
            // Products will be loaded dynamically through the API
            User currentUser = userService.getUser();
            model.addAttribute("products", List.of()); // Empty list
            model.addAttribute("isAdmin", currentUser != null && currentUser.isAdmin());
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
        User currentUser = userService.getUser();
        
        // Verificación más detallada de acceso de administrador
        if (currentUser == null) {
            return "redirect:/login";
        }
        
        if (!currentUser.isAdmin()) {
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
                             Model model) throws IOException {
        User currentUser = userService.getUser();
        if (currentUser == null || !currentUser.isAdmin()) {
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
            // Convert the image to a byte array instead of saving to filesystem
            byte[] imageBytes = mainImage.getBytes();
            String relativeImagePath = "/Images/" + name + ".jpg"; // Keep the path for backward compatibility
            
            Product newProduct = new Product(name, description, price, stock, relativeImagePath);
            newProduct.setImageData(imageBytes); // Set the image data
            
            productService.addProduct(newProduct);
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
            
            Product product = productService.getProductById(productId);
            User user = userService.getUser();

            if (product == null || user == null) {
                return "redirect:/products";
            }

            Review review = new Review(user, sanitizedComment, rating);
            review.setProduct(product);
            reviewService.addReview(productId, review);
            user.getReviews().add(review);

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
        User currentUser = userService.getUser();
        if (currentUser == null || !currentUser.isAdmin()) {
            return "redirect:/products";
        }

        try {
            System.out.println("Attempting to delete product with ID: " + productId);
            
            // Remove from current order if present
            Order currentOrder = orderController.getCurrentOrder(session);
            Product product = productService.getProductById(productId);
            
            if (product == null) {
                System.out.println("Product not found with ID: " + productId);
                model.addAttribute("error", "Product not found");
                return "redirect:/product-management";
            }
            
            if (currentOrder != null && product != null) {
                currentOrder.removeProduct(product);
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
            List<Product> searchResults = productService.searchProducts(term);
            User currentUser = userService.getUser();
            model.addAttribute("products", searchResults);
            model.addAttribute("isAdmin", currentUser != null && currentUser.isAdmin());
            model.addAttribute("cartItemCount", orderController.getCartItemCount(session));
            model.addAttribute("searchTerm", term);
            return "Wellcome";
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "error";
        }
    }

    @GetMapping("/product-details")
    public String productDetails(@RequestParam Long id, Model model, HttpSession session) {
        try {
            Product product = productService.getProductWithReviews(id);
            User currentUser = userService.getUser();
            
            if (product == null || currentUser == null) {
                return "redirect:/products";
            }

            // All these accesses are secure because the reviews are already loaded
            model.addAttribute("product", product);
            model.addAttribute("reviews", product.getReviews()); 
            model.addAttribute("averageRating", productService.getAverageRating(product.getId()));
            model.addAttribute("username", currentUser.getFirstName() + " " + currentUser.getLastName());
            model.addAttribute("isAdmin", currentUser.isAdmin());
            model.addAttribute("cartItemCount", orderController.getCartItemCount(session));

            return "Product";
        } catch (Exception e) {
            System.out.println(e);
            return "redirect:/products";
        }
    }

    @PostMapping("/delete-review")
    public String deleteReview(@RequestParam Long productId, @RequestParam Long reviewId, Model model) {
        User currentUser = userService.getUser();
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
            // Calculamos el precio máximo efectivo
            double dynamicMaxPrice = productService.getMaxPriceForFilter();
            double effectiveMaxPrice = maxPrice != null ? maxPrice : dynamicMaxPrice;
            
            // Obtenemos los resultados con filtro de precio
            List<Product> searchResults;
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
            List<Product> pageContent = start < end 
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
    private Map<String, Object> convertToMap(Product product) {
        Map<String, Object> productMap = new HashMap<>();
        productMap.put("id", product.getId());
        productMap.put("name", product.getName());
        productMap.put("description", product.getDescription());
        productMap.put("price", product.getPrice());
        productMap.put("stock", product.getStock());
        // Add isAdmin attribute for administrator button rendering if needed
        User currentUser = userService.getUser();
        productMap.put("isAdmin", currentUser != null && currentUser.isAdmin());
        // We don't include imageData to avoid overloading the JSON response
        return productMap;
    }

    @GetMapping("/get-cart-quantity")
    @ResponseBody
    public int getCartQuantity(@RequestParam Long productId, HttpSession session) {
        Order currentOrder = orderController.getCurrentOrder(session);
        if (currentOrder != null && currentOrder.hasProducts()) {
            for (Product product : currentOrder.getProducts()) {
                if (product.getId().equals(productId)) {
                    return product.getAmount();
                }
            }
        }
        return 0;
    }

    // Product Management Page - Admin only
    @GetMapping("/product-management")
    public String productManagement(Model model, HttpSession session) {
        User currentUser = userService.getUser();
        
        // Verificación de acceso de administrador
        if (currentUser == null) {
            return "redirect:/login";
        }
        
        if (!currentUser.isAdmin()) {
            return "redirect:/products";
        }
        
        // Ya no cargamos todos los productos inicialmente
        // Los productos se cargarán dinámicamente a través de la API
        model.addAttribute("products", List.of()); // Lista vacía
        model.addAttribute("isAdmin", true);
        model.addAttribute("cartItemCount", orderController.getCartItemCount(session));
        return "product-management";
    }

    // Show form to edit a product - Admin only
    @GetMapping("/edit-product/{id}")
    public String editProductForm(@PathVariable Long id, Model model, HttpSession session) {
        User currentUser = userService.getUser();
        
        // Verificación de acceso de administrador
        if (currentUser == null) {
            return "redirect:/login";
        }
        
        if (!currentUser.isAdmin()) {
            return "redirect:/products";
        }
        
        Product product = productService.getProductById(id);
        if (product == null) {
            return "redirect:/product-management";
        }
        
        model.addAttribute("product", product);
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
                               Model model) throws IOException {
        User currentUser = userService.getUser();
        if (currentUser == null || !currentUser.isAdmin()) {
            return "redirect:/products";
        }

        Product existingProduct = productService.getProductById(productId);
        if (existingProduct == null) {
            return "redirect:/product-management";
        }

        // Validations
        if (name == null || name.trim().isEmpty()) {
            model.addAttribute("error", "Product name is required");
            model.addAttribute("product", existingProduct);
            return "edit-product";
        }
        if (description == null || description.trim().isEmpty()) {
            model.addAttribute("error", "Product description is required");
            model.addAttribute("product", existingProduct);
            return "edit-product";
        }
        if (price <= 0) {
            model.addAttribute("error", "Price must be greater than 0");
            model.addAttribute("product", existingProduct);
            return "edit-product";
        }
        if (stock < 0) {
            model.addAttribute("error", "Stock cannot be negative");
            model.addAttribute("product", existingProduct);
            return "edit-product";
        }

        try {
            // Update product properties
            existingProduct.setName(name);
            existingProduct.setDescription(description);
            existingProduct.setPrice(price);
            existingProduct.setStock(stock);
            
            // Update image if provided
            if (mainImage != null && !mainImage.isEmpty()) {
                if (!mainImage.getContentType().startsWith("image/")) {
                    model.addAttribute("error", "The file must be an image");
                    model.addAttribute("product", existingProduct);
                    return "edit-product";
                }
                
                byte[] imageBytes = mainImage.getBytes();
                existingProduct.setImageData(imageBytes);
                existingProduct.setImagePath("/Images/" + name + ".jpg"); // Update path for consistency
            }
            
            productService.updateProduct(existingProduct);
            return "redirect:/product-management";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("product", existingProduct);
            return "edit-product";
        }
    }
}


