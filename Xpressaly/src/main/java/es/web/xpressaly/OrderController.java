package es.web.xpressaly;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class OrderController {

    @Autowired
    private ProductService productService;

    @Autowired
    private UserService userService;

    private Order currentOrder; // Pedido actual del usuario

    // Ver productos y agregar al pedido
    @GetMapping("/view_order_products")
    public String showProducts(Model model) {
        User currentUser = userService.getUser();

        if (currentOrder == null || !currentOrder.getUser().equals(currentUser)) {
            currentOrder = new Order(currentUser, "Dirección por defecto"); // Crear nuevo pedido si no existe
        }

        model.addAttribute("products", productService.getAllProducts());
        model.addAttribute("order", currentOrder);
        return "products_template";
    }

    // Añadir producto al pedido
    @PostMapping("/add-to-order")
    public String addToOrder(@RequestParam Long productId, Model model) {
        Product product = productService.getProductById(productId);
        User currentUser = userService.getUser();

        if (currentOrder == null || !currentOrder.getUser().equals(currentUser)) {
            currentOrder = new Order(currentUser, "Dirección por defecto");
        }

        currentOrder.addProduct(product);

        model.addAttribute("products", productService.getAllProducts());
        model.addAttribute("order", currentOrder);
        return "redirect:/view_order_products";
    }

    // Ver el pedido actual
    @GetMapping("/view-order")
    public String viewOrder(Model model) {
        if (currentOrder == null) {
            model.addAttribute("message", "No tienes ningún pedido.");
            return "order_template";
        }

        model.addAttribute("order", currentOrder);
        return "order_template";
    }

    // Eliminar un producto del pedido
    @PostMapping("/remove-from-order")
    public String removeFromOrder(@RequestParam Long productId, Model model) {
        if (currentOrder != null) {
            Product product = productService.getProductById(productId);
            currentOrder.removeProduct(product);
        }

        model.addAttribute("order", currentOrder);
        return "redirect:/view-order";
    }
}

