package es.xpressaly.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import es.xpressaly.Model.Order;
import es.xpressaly.Model.Product;
import es.xpressaly.Model.Review;
import es.xpressaly.Model.User;
import es.xpressaly.Service.ProductService;
import es.xpressaly.Service.ReviewService;
import es.xpressaly.Service.UserService;

import java.io.IOException;
//import java.nio.file.Files;
//import java.nio.file.Path;
//import java.nio.file.Paths;
//import java.util.ArrayList;
import java.util.List;


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
    public String showProducts(Model model) {
        try {
            List<Product> initialProducts = productService.getProductsByPage(1, 20000, "default");
            User currentUser = userService.getUser();
            model.addAttribute("products", initialProducts);
            model.addAttribute("isAdmin", currentUser != null && currentUser.isAdmin());
            model.addAttribute("cartItemCount", orderController.getCartItemCount());
            return "Wellcome";
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "error";
        }
    }

    @GetMapping("/api/products")
    @ResponseBody
    public List<Product> getProducts(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(required = false) String search,
            @RequestParam(required = false, defaultValue = "default") String sort) {
        //opcion 1:
        if (search != null && !search.isEmpty()) {
            return productService.searchProducts(search);
        }
        return productService.getProductsByPage(page, 20000, sort);
    }

    // Show form to create a product - Admin only
    @GetMapping("/create-product")
    public String createProductForm(Model model) {
        User currentUser = userService.getUser();
        if (currentUser == null || !currentUser.isAdmin()) {
            return "redirect:/products";
        }
        model.addAttribute("isAdmin", true);
        model.addAttribute("cartItemCount", orderController.getCartItemCount());
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
            if (comment == null || comment.trim().isEmpty()) {
                model.addAttribute("error", "Comment is required");
                return "redirect:/product-details?id=" + productId;
            }
            if (rating < 1 || rating > 5) {
                model.addAttribute("error", "Rating must be between 1 and 5");
                return "redirect:/product-details?id=" + productId;
            }

            Product product = productService.getProductById(productId);
            User user = userService.getUser();

            if (product == null || user == null) {
                return "redirect:/products";
            }

            Review review = new Review(user, comment, rating);
            review.setProduct(product);
            reviewService.addReview(productId, review);
            user.getReviews().add(review);

            return "redirect:/product-details?id=" + productId;
        }catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "redirect:/product-details?id=" + productId;
        }
    }

    // Delete a product - Admin only
    @PostMapping("/delete-product")
    public String deleteProduct(@RequestParam Long productId) throws IOException {
        User currentUser = userService.getUser();
        if (currentUser == null || !currentUser.isAdmin()) {
            return "redirect:/products";
        }

        Order currentOrder = orderController.getCurrentOrder();
        Product product = productService.getProductById(productId);
        if (currentOrder != null) {
            currentOrder.removeProduct(product);
        }
        
        // No need to delete image from filesystem as it's now stored in the database
        
        productService.deleteProduct(productId);
        return "redirect:/products";
    }

    @GetMapping("/product-details")
    public String productDetails(@RequestParam Long id, Model model) {
        Product product = productService.getProductById(id);
        User currentUser = userService.getUser();

        try {
            if (product == null || currentUser == null) {
                System.out.println("Producto o usuario no encontrado");
                return "redirect:/products";
            }

            model.addAttribute("product", product);
            model.addAttribute("reviews", product.getReviews());
            model.addAttribute("username", currentUser.getFirstName() + " " + currentUser.getLastName());
            model.addAttribute("isAdmin", currentUser.isAdmin());
            model.addAttribute("cartItemCount", orderController.getCartItemCount());
            model.addAttribute("averageRating", product.getAverageRating());

            return "Product";
        }catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "redirect:/products";
        }
    }

    @PostMapping("/delete-review")
    public String deleteReview(@RequestParam Long productId, @RequestParam Long reviewId) {
        reviewService.deleteReview(productId, reviewId);
        return "redirect:/product-details?id=" + productId;
    }
}


