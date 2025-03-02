package es.xpressaly.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import es.xpressaly.Model.Order;
import es.xpressaly.Model.Product;
import es.xpressaly.Model.Review;
import es.xpressaly.Model.User;
import es.xpressaly.Service.ProductService;
import es.xpressaly.Service.ReviewService;
import es.xpressaly.Service.UserService;


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

    // Mostrar lista de productos
    @GetMapping("/products")
    public String showProducts(Model model) {
        // Inicialmente cargamos los primeros 15 productos (3 filas de 5 productos)
        List<Product> initialProducts = productService.getAllProducts().stream()
            .limit(1500)
            .collect(Collectors.toList());
        model.addAttribute("products", initialProducts);
        return "Wellcome";
    }

    // Mostrar formulario para crear un producto
    @GetMapping("/create-product")
    public String createProductForm() {
        return "add_product";
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
        Order currentOrder = orderController.getCurrentOrder();
        Product product=currentOrder.findProductById(productId);
        currentOrder.removeProduct(product);
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
        User user = userService.getUser();  // Obtener el usuario actual

        if (product == null || user == null) {
            return "redirect:/products";
        }

        // Crear la reseña con la lista vacía de imágenes
        Review review = new Review(user.getFirstName() + " " + user.getLastName(), comment, rating, new ArrayList<>());
        review.setProduct(product); // Set the product relationship

        // Añadir la reseña al producto y al usuario
        reviewService.addReview(productId, review);
        user.getReviews().add(review);

        // Volver a la vista de detalles del producto con la reseña añadida
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


