package es.web.xpressaly;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.List;

@Controller
public class ProductController {

    @Autowired
    private ProductService productService;

    @Autowired
    private ReviewService reviewService;

    @Autowired
    private UserService userService;

    // Mostrar lista de productos
    @GetMapping("/products")
    public String showProducts(Model model) {
        model.addAttribute("products", productService.getAllProducts());
        return "products_template";
    }

    // Mostrar formulario para crear un producto
    @GetMapping("/create-product")
    public String createProductForm() {
        return "create_product";
    }

    // Manejar la creación del nuevo producto
    @PostMapping("/add-product")
    public String addProduct(@RequestParam String name,
                             @RequestParam String description,
                             @RequestParam double price,
                             @RequestParam int stock,
                             @RequestParam String mainImage) {

        Product newProduct = new Product(name, description, price, stock, mainImage);
        productService.addProduct(newProduct);

        return "redirect:/products";
    }

    // Eliminar un producto
    @PostMapping("/delete-product")
    public String deleteProduct(@RequestParam Long productId) {
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

        return "product_details";
    }

    @PostMapping("/add-review")
    public String addReview(@RequestParam Long productId,
                            @RequestParam String comment,
                            @RequestParam int rating,
                            Model model) {

        Product product = productService.getProductById(productId);
        User user = userService.getUser();  // Obtener el usuario actual

        if (product == null || user == null) {
            return "redirect:/products";
        }

        // Crear la reseña con la lista vacía de imágenes
        Review review = new Review(user.getFirstName() + " " + user.getLastName(), comment, rating, new ArrayList<>());

        // Añadir la reseña al producto
        reviewService.addReview(productId, review);

        // Volver a la vista de detalles del producto con la reseña añadida
        model.addAttribute("product", product);
        model.addAttribute("reviews", product.getReviews());
        model.addAttribute("username", user.getFirstName() + " " + user.getLastName());

        return "product_details";
    }

    @PostMapping("/delete-review")
    public String deleteReview(@RequestParam Long productId, @RequestParam Long reviewId) {
        reviewService.deleteReview(productId, reviewId);
        return "redirect:/product-details?id=" + productId;
    }
}


