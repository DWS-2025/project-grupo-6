package es.xpressaly.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import es.xpressaly.Model.Order;
import es.xpressaly.Model.Product;
import es.xpressaly.Model.User;
import es.xpressaly.Service.ProductService;
import es.xpressaly.Service.UserService;


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
            currentOrder = new Order(currentUser, currentUser.getAddress()); // Create new order with user's profile address
        } else {
            // Update the address to match the current user's address
            currentOrder.setAddress(currentUser.getAddress());
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

        // Check if product is out of stock
        if (product.getStock() <= 0) {
            model.addAttribute("errorMessage", "We're sorry, we do not have enough stock at the moment, try later");
            model.addAttribute("products", productService.getAllProducts());
            return "Wellcome";
        }

        if (currentOrder == null || !currentOrder.getUser().equals(currentUser)) {
            currentOrder = new Order(currentUser, currentUser.getAddress()); // Create new order with user's profile address
        } else {
            // Update the address to match the current user's address
            currentOrder.setAddress(currentUser.getAddress());
        }

        // Check if adding more would exceed available stock
        int currentAmount = 0;
        if(currentOrder.getProducts().contains(product)) {
            for (Product p : currentOrder.getProducts()) {
                if (p.equals(product)) {
                    currentAmount = p.getAmount();
                    if (currentAmount + 1 > product.getStock()) {
                        model.addAttribute("errorMessage", "We're sorry, we do not have enough stock at the moment, try later");
                        model.addAttribute("products", productService.getAllProducts());
                        return "Wellcome";
                    }
                    p.setAmount(currentAmount + 1); // Increase the quantity by 1
                    break; // Exit the loop once the product is found
                }
            }
        } else {
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
        Product productInStock = productService.getProductById(productId);
        Product productInOrder = currentOrder.findProductById(productId);

        if (productInOrder != null) {
            // Check if requested amount exceeds available stock
            if (amount > productInStock.getStock()) {
                model.addAttribute("errorMessage", "We're sorry, we do not have enough stock at the moment, try later");
                model.addAttribute("order", currentOrder);
                model.addAttribute("total", currentOrder.getTotal());
                return "my_Order";
            }
            productInOrder.setAmount(amount);
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
        currentOrder=new Order(userService.getUser(), userService.getUser().getAddress());
        model.addAttribute("order", confirmedOrder);
        model.addAttribute("address", address);
        return "confirm_order";
    }

    public Order getCurrentOrder() {
        return currentOrder;
    }
}

