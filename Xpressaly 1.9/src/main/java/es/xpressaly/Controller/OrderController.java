package es.xpressaly.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import es.xpressaly.Model.Order;
import es.xpressaly.Model.OrderUpdateResponse;
import es.xpressaly.Model.Product;
import es.xpressaly.Model.User;
import es.xpressaly.Service.ProductService;
import es.xpressaly.Service.UserService;

import org.springframework.http.ResponseEntity;


@Controller
public class OrderController {

    @Autowired
    private ProductService productService;

    @Autowired
    private UserService userService;

    private Order currentOrder; // User's current order

    // View products and add to order
    @GetMapping("/view_order_products")
    public String showProducts(Model model) {
        User currentUser = userService.getUser();

        if (currentOrder == null || !currentOrder.getUser().equals(currentUser)) {
            currentOrder = new Order(currentUser, "Default address"); // Create new order if it does not exist
        }

        model.addAttribute("products", productService.getAllProducts());
        model.addAttribute("order", currentOrder);
        return "Wellcome";
    }

    // Add product to order
    @PostMapping("/add-to-order")
    public String addToOrder(@RequestParam Long productId, Model model) {
        Product product = productService.getProductById(productId);
        User currentUser = userService.getUser();

        if (currentOrder == null || !currentOrder.getUser().equals(currentUser)) {
            currentOrder = new Order(currentUser, "Default address");
        }

        if(currentOrder.getProducts().contains(product)){
            for (Product p : currentOrder.getProducts()) {
                if (p.equals(product)) {
                p.setAmount(p.getAmount() + 1); // Increase the quantity by 1
                break; // Exit the loop once the product is found
                }
            }
        }else{
            product.setAmount(1);
            currentOrder.addProduct(product);
        }
        
        model.addAttribute("products", productService.getAllProducts());
        model.addAttribute("order", currentOrder);
        return "redirect:/view_order_products";
    }

    // View current order
    @GetMapping("/view-order")
    public String viewOrder(Model model) {
        if (currentOrder == null) {
            model.addAttribute("message", "You don't have any orders.");
            return "my_Order";
        }
        currentOrder.calculateTotal(currentOrder);
        model.addAttribute("order", currentOrder);
        model.addAttribute("total", currentOrder.getTotal());
        return "my_Order";
    }

    // Remove a product from the order
    @PostMapping("/remove-from-order")
    public String removeFromOrder(@RequestParam Long productId, Model model) {
        if (currentOrder != null) {
            Product product = productService.getProductById(productId);
            currentOrder.removeProduct(product);
        }
        model.addAttribute("order", currentOrder);
        return "redirect:/view-order";
    }

    //Delete order
    @GetMapping("/delete-order")
    public String deleteOrder(Model model) {
        while (currentOrder.hasProducts()) {
            currentOrder.removeProduct(currentOrder.getProducts().get(0));
        }
        model.addAttribute("products", productService.getAllProducts());
        model.addAttribute("order", currentOrder);
        return "Wellcome";
    }
    
    //Update products' amount
    @PostMapping("/update-amount")
    public String updateAmount(Model model, @RequestParam int amount,@RequestParam Long productId) {

        Product product = currentOrder.findProductById(productId);

        if (product != null) {
            product.setAmount(amount);;
        }
        currentOrder.calculateTotal(currentOrder);
        model.addAttribute("order", currentOrder);
        model.addAttribute("total", currentOrder.getTotal());
        return "my_Order";
    }
    
    //Confirmed payment
    @PostMapping("/confirm-order")
    public String confirmOrder(Model model, @RequestParam String address) {

        for (Product product : productService.getAllProducts()) {
            if(currentOrder.getProducts().contains(product)){
                product.setStock(product.getStock()-currentOrder.findProductById(product.getId()).getAmount());
            }

        }

        currentOrder.setAddress(address);
        userService.getUser().addOrder(currentOrder);
        Order confirmedOrder = currentOrder;
        currentOrder=new Order(userService.getUser(), "Default address");
        model.addAttribute("order", confirmedOrder);
        model.addAttribute("address", address);
        return "confirm_order";
    }

    public Order getCurrentOrder() {
        return currentOrder;
    }
}

