package es.xpressaly.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import es.xpressaly.Model.Order;
import es.xpressaly.Model.Product;
import es.xpressaly.Model.User;
import es.xpressaly.Model.UserRole;
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
    public int getCartItemCount() {
        if (currentOrder == null || currentOrder.getProducts() == null) {
            return 0;
        }
        return currentOrder.getProducts().stream()
                .mapToInt(Product::getAmount)
                .sum();
    }

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
        model.addAttribute("isAdmin", currentUser.getRole() == UserRole.ADMIN);
        model.addAttribute("cartItemCount", getCartItemCount());
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
            model.addAttribute("cartItemCount", getCartItemCount());
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
                        model.addAttribute("cartItemCount", getCartItemCount());
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
        model.addAttribute("isAdmin", currentUser.getRole() == UserRole.ADMIN);
        model.addAttribute("cartItemCount", getCartItemCount());
        return "redirect:/view_order_products";
    }

    // View current order
    @GetMapping("/view-order")
    public String viewOrder(Model model) {
        User currentUser = userService.getUser();
        model.addAttribute("isAdmin", currentUser.getRole() == UserRole.ADMIN);
        model.addAttribute("cartItemCount", getCartItemCount());
        
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
        model.addAttribute("cartItemCount", getCartItemCount());
        return "redirect:/view-order";
    }

    //Delete order
    @GetMapping("/delete-order")
    public String deleteOrder(Model model) {
        User currentUser = userService.getUser();
        while (currentOrder.hasProducts()) {
            currentOrder.removeProduct(currentOrder.getProducts().get(0));
        }
        model.addAttribute("products", productService.getAllProducts());
        model.addAttribute("order", currentOrder);
        model.addAttribute("isAdmin", currentUser.getRole() == UserRole.ADMIN);
        model.addAttribute("cartItemCount", getCartItemCount());
        return "Wellcome";
    }
    
    //Update products' amount
    @PostMapping("/update-amount")
    public String updateAmount(Model model, @RequestParam int amount, @RequestParam Long productId) {
        if (currentOrder == null) {
            model.addAttribute("error", "No active order");
            return "redirect:/products";
        }

        if (amount < 1) {
            model.addAttribute("error", "Quantity must be greater than 0");
            model.addAttribute("order", currentOrder);
            model.addAttribute("total", currentOrder.getTotal());
            return "my_Order";
        }

        Product productInStock = productService.getProductById(productId);
        if (productInStock == null) {
            model.addAttribute("error", "Product not found");
            return "redirect:/products";
        }

        Product productInOrder = currentOrder.findProductById(productId);
        if (productInOrder == null) {
            model.addAttribute("error", "Product is not in the current order");
            return "redirect:/view-order";
        }

        if (amount > productInStock.getStock()) {
            model.addAttribute("errorMessage", "We're sorry, we do not have enough stock at the moment, try later");
            model.addAttribute("order", currentOrder);
            model.addAttribute("total", currentOrder.getTotal());
            return "my_Order";
        }

        productInOrder.setAmount(amount);
        currentOrder.calculateTotal(currentOrder);
        model.addAttribute("order", currentOrder);
        model.addAttribute("total", currentOrder.getTotal());
        return "my_Order";
    }

    @PostMapping("/confirm-order")
    public String confirmOrder(Model model, @RequestParam String address) {
        if (currentOrder == null || currentOrder.getProducts().isEmpty()) {
            model.addAttribute("error", "No products in the order");
            return "redirect:/view-order";
        }

        if (address == null || address.trim().isEmpty()) {
            model.addAttribute("error", "Shipping address is required");
            return "redirect:/view-order";
        }

        // Verify stock availability before confirming
        for (Product orderProduct : currentOrder.getProducts()) {
            Product stockProduct = productService.getProductById(orderProduct.getId());
            if (stockProduct == null || orderProduct.getAmount() > stockProduct.getStock()) {
                model.addAttribute("error", "Some products do not have enough stock");
                return "redirect:/view-order";
            }
        }

        // Update stock levels
        for (Product product : productService.getAllProducts()) {
            if (currentOrder.getProducts().contains(product)) {
                product.setStock(product.getStock() - currentOrder.findProductById(product.getId()).getAmount());
            }
        }

        currentOrder.setAddress(address);
        userService.getUser().addOrder(currentOrder);
        Order confirmedOrder = currentOrder;
        currentOrder = new Order(userService.getUser(), userService.getUser().getAddress());
        model.addAttribute("order", confirmedOrder);
        model.addAttribute("address", address);
        return "confirm_order";
    }

    public Order getCurrentOrder() {
        return currentOrder;
    }
}

