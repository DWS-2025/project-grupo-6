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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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

    private static final Path IMAGES_FOLDER = Paths.get(System.getProperty("user.dir"), "Xpressaly 1.14 FINAL/src/main/resources/static/Images");//Directory where images are stored
    // Show product list
    @GetMapping("/products")
    public String showProducts(Model model) {
        // Load the first page of products with default sorting
        List<Product> initialProducts = productService.getProductsByPage(1, 20000, "default");
        model.addAttribute("products", initialProducts);
        return "Wellcome";
    }

    @GetMapping("/api/products")
    @ResponseBody
    public List<Product> getProducts(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(required = false) String search,
            @RequestParam(required = false, defaultValue = "default") String sort) {
        
        // If search query is provided, filter products first
        if (search != null && !search.isEmpty()) {
            return productService.searchProducts(search);
        }
        
        // Otherwise return paginated and sorted results
        return productService.getProductsByPage(page, 20000, sort);
    }

    // Show form to create a product
    @GetMapping("/create-product")
    public String createProductForm() {
        return "add_product";
    }

    // Manage the creation of the new product
    @PostMapping("/add-product")
    public String addProduct(@RequestParam String name,
                             @RequestParam String description,
                             @RequestParam double price,
                             @RequestParam int stock,
                             @RequestParam MultipartFile mainImage) throws IOException {
        
        Files.createDirectories(IMAGES_FOLDER);
        Path imagePath = IMAGES_FOLDER.resolve(name+".jpg");
        mainImage.transferTo(imagePath);

        String relativeImagePath = "/Images/" + name + ".jpg";
        Product newProduct = new Product(name, description, price, stock, relativeImagePath);
        productService.addProduct(newProduct);

        return "redirect:/products";
    }

    // Delete a product
    @PostMapping("/delete-product")
    public String deleteProduct(@RequestParam Long productId) throws IOException {
        Order currentOrder = orderController.getCurrentOrder();
        Product product = productService.getProductById(productId);
        if(currentOrder!=null){//if orderr's not empty, delete the product
            currentOrder.removeProduct(product);
        }
        Path imagePath=IMAGES_FOLDER.resolve(product.getName()+".jpg");
        Files.delete(imagePath);
        productService.deleteProduct(productId);
        return "redirect:/products";
    }

    @GetMapping("/product-details")
    public String productDetails(@RequestParam Long id, Model model) {
        Product product = productService.getProductById(id);
        User currentUser = userService.getUser();

        if (product == null) {
            return "redirect:/products";
        }

        model.addAttribute("product", product);
        model.addAttribute("reviews", product.getReviews());
        model.addAttribute("username", currentUser.getFirstName() + " " + currentUser.getLastName());

        return "Product";
    }

    @PostMapping("/add-review")
    public String addReview(@RequestParam Long productId,
                            @RequestParam String comment,
                            @RequestParam int rating,
                            Model model) {

        Product product = productService.getProductById(productId);
        User user = userService.getUser();  // Get current user

        if (product == null || user == null) {
            return "redirect:/products";
        }

        // Create the review with the empty list of images
        Review review = new Review(user.getFirstName() + " " + user.getLastName(), comment, rating, new ArrayList<>());
        review.setProduct(product); // Set the product relationship

        // Add the review to the product and the user
        reviewService.addReview(productId, review);
        user.getReviews().add(review);

        // Return to the product details view with the review added
        model.addAttribute("product", product);
        model.addAttribute("reviews", product.getReviews());
        model.addAttribute("username", user.getFirstName() + " " + user.getLastName());

        return "redirect:/product-details?id=" + productId;
    }


    @PostMapping("/delete-review")
    public String deleteReview(@RequestParam Long productId, @RequestParam Long reviewId) {
        reviewService.deleteReview(productId, reviewId);
        return "redirect:/product-details?id=" + productId;
    }
}


