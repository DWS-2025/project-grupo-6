package es.xpressaly.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.servlet.http.HttpSession;
import jakarta.transaction.Transactional;
import es.xpressaly.Model.Order;
import es.xpressaly.Model.Product;
import es.xpressaly.Model.User;
import es.xpressaly.Model.UserRole;
import es.xpressaly.Repository.OrderRepository;
import es.xpressaly.Service.ProductService;
import es.xpressaly.Service.UserService;

import java.util.List;

@Controller
@Transactional
public class OrderController {

    @Autowired
    private ProductService productService;

    @Autowired
    private UserService userService;

    @Autowired
    private OrderRepository orderRepository; 
    
    @PersistenceContext
    private EntityManager entityManager;

    //private Order currentOrder; // User's current order
    private static final String CURRENT_ORDER_SESSION_KEY = "currentOrder";
    
    public Order getCurrentOrder(HttpSession session) {
        Order order = (Order) session.getAttribute(CURRENT_ORDER_SESSION_KEY);
        User currentUser = userService.getUser();
        
        // If there is no order in the session or the user is null, it returns null
        if (order == null || currentUser == null) {
            return null;
        }
        
        // Verify that the order belongs to the current user
        if (!order.getUser().getId().equals(currentUser.getId())) {
            // If the order belongs to another user, create a new order for the current user
            order = new Order(currentUser, currentUser.getAddress());
            setCurrentOrder(session, order);
        }
        
        return order;
    }

    public void setCurrentOrder(HttpSession session, Order order) {
        session.setAttribute(CURRENT_ORDER_SESSION_KEY, order);
    }
    // View products and add to order
    public int getCartItemCount(HttpSession session) {
        Order currentOrder = getCurrentOrder(session);
        if (currentOrder == null) {
            return 0;
        }
        return currentOrder.getProducts().stream()
                .mapToInt(product -> currentOrder.getProductQuantity(product))
                .sum();
    }

    @GetMapping("/view_order_products")
    public String showProducts(Model model, HttpSession session) {
        User currentUser = userService.getUser();
        Order currentOrder = getCurrentOrder(session);

        if (currentOrder == null || !currentOrder.getUser().equals(currentUser)) {
            currentOrder = new Order(currentUser, currentUser.getAddress()); // Create new order with user's profile address
        } else {
            // Update the address to match the current user's address
            currentOrder.setAddress(currentUser.getAddress());
        }

        model.addAttribute("products", productService.getAllProducts());
        model.addAttribute("order", currentOrder);
        model.addAttribute("isAdmin", currentUser.getRole() == UserRole.ADMIN);
        model.addAttribute("cartItemCount", getCartItemCount(session));
        return "Wellcome";
    }

    // Add product to order
    @PostMapping("/add-to-order")
    public String addToOrder(@RequestParam Long productId, Model model, HttpSession session) {
        Product product = productService.getProductById(productId);
        User currentUser = userService.getUser();
        
        // Check if user is logged in, redirect to cart prompt if not
        if (currentUser == null) {
            return "redirect:/cart-prompt";
        }
        
        Order currentOrder = getCurrentOrder(session);
        // Check if product exists
        if (product == null) {
            model.addAttribute("error", "Product not found");
            return "redirect:/products";
        }

        // Check if product is out of stock
        if (product.getStock() <= 0) {
            model.addAttribute("errorMessage", "We're sorry, we do not have enough stock at the moment, try later");
            model.addAttribute("products", productService.getAllProducts());
            model.addAttribute("cartItemCount", getCartItemCount(session));
            model.addAttribute("isAdmin", currentUser.getRole() == UserRole.ADMIN);
            return "Wellcome";
        }

        if (currentOrder == null) {
            currentOrder = new Order(currentUser, currentUser.getAddress());
            setCurrentOrder(session, currentOrder);
        } else {
            // Update the address to match the current user's address
            currentOrder.setAddress(currentUser.getAddress());
        }

        // Check if adding more would exceed available stock
        int currentQuantity = currentOrder.getProductQuantity(product);
        if (currentQuantity + 1 > product.getStock()) {
            setCurrentOrder(session, currentOrder);
            model.addAttribute("errorMessage", "We're sorry, we do not have enough stock at the moment, try later");
            model.addAttribute("products", productService.getAllProducts());
            model.addAttribute("cartItemCount", getCartItemCount(session));
            model.addAttribute("isAdmin", currentUser.getRole() == UserRole.ADMIN);
            return "Wellcome";
        }

        currentOrder.addProduct(product);
        setCurrentOrder(session, currentOrder);
        model.addAttribute("products", productService.getAllProducts());
        model.addAttribute("order", currentOrder);
        model.addAttribute("isAdmin", currentUser.getRole() == UserRole.ADMIN);
        model.addAttribute("cartItemCount", getCartItemCount(session));
        return "redirect:/view_order_products";
    }

    // View current order
    @GetMapping("/view-order")
    public String viewOrder(Model model, HttpSession session) {
        User currentUser = userService.getUser();
        
        // Check if user is logged in, redirect to cart prompt if not
        if (currentUser == null) {
            return "redirect:/cart-prompt";
        }
        
        Order currentOrder = getCurrentOrder(session);
        model.addAttribute("isAdmin", currentUser.getRole() == UserRole.ADMIN);
        model.addAttribute("cartItemCount", getCartItemCount(session));
        
        if (currentOrder == null) {
            model.addAttribute("message", "You don't have any orders.");
            return "my_Order";
        }
        currentOrder.calculateTotal();
        model.addAttribute("order", currentOrder);
        model.addAttribute("total", currentOrder.getTotal());
        return "my_Order";
    }

    // Remove a product from the order
    @PostMapping("/remove-from-order")
    public String removeFromOrder(@RequestParam Long productId, Model model, HttpSession session) {
        User currentUser = userService.getUser();
        
        // Check if user is logged in, redirect to cart prompt if not
        if (currentUser == null) {
            return "redirect:/cart-prompt";
        }
        
        Order currentOrder = getCurrentOrder(session);
        if (currentOrder != null) {
            Product product = productService.getProductById(productId);
            currentOrder.removeProduct(product);
        }
        setCurrentOrder(session, currentOrder);
        model.addAttribute("order", currentOrder);
        model.addAttribute("cartItemCount", getCartItemCount(session));
        return "redirect:/view-order";
    }

    //Delete order
    @GetMapping("/delete-order")
    public String deleteOrder(Model model, HttpSession session) {
        User currentUser = userService.getUser();
        Order currentOrder = getCurrentOrder(session);
        if (currentUser == null) {
            return "redirect:/cart-prompt";
        }

        if (currentOrder == null || !currentOrder.hasProducts()) {
            model.addAttribute("message", "No active order to delete");
            return "redirect:/products";
        }

        while (currentOrder.hasProducts()) {
            currentOrder.removeProduct(currentOrder.getProducts().get(0));
        }
        setCurrentOrder(session, currentOrder);
        model.addAttribute("products", productService.getAllProducts());
        model.addAttribute("order", currentOrder);
        model.addAttribute("isAdmin", currentUser.getRole() == UserRole.ADMIN);
        model.addAttribute("cartItemCount", getCartItemCount(session));
        return "Wellcome";
    }
    
    //Update products' amount
    @PostMapping("/update-amount")
    public String updateAmount(Model model, @RequestParam int amount, @RequestParam Long productId, HttpSession session) {
        User currentUser = userService.getUser();
        
        // Check if user is logged in, redirect to cart prompt if not
        if (currentUser == null) {
            return "redirect:/cart-prompt";
        }
        
        Order currentOrder = getCurrentOrder(session);
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
            setCurrentOrder(session, currentOrder);
            model.addAttribute("errorMessage", "We're sorry, we do not have enough stock at the moment, try later");
            model.addAttribute("order", currentOrder);
            model.addAttribute("total", currentOrder.getTotal());
            model.addAttribute("isAdmin", currentUser.getRole() == UserRole.ADMIN);
            return "my_Order";
        }

        currentOrder.setProductQuantity(productInOrder, amount);
        currentOrder.calculateTotal();
        setCurrentOrder(session, currentOrder);
        model.addAttribute("order", currentOrder);
        model.addAttribute("total", currentOrder.getTotal());
        return "my_Order";
    }

    // Confirm order
    @PostMapping("/confirm-order")
    public String confirmOrder(Model model, @RequestParam String address, HttpSession session) {
        try {
            User currentUser = userService.getUser();
            
            // Check if user is logged in, redirect to cart prompt if not
            if (currentUser == null) {
                return "redirect:/cart-prompt";
            }
            
            Order currentOrder = getCurrentOrder(session);
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
                int quantity = currentOrder.getProductQuantity(orderProduct);
                if (stockProduct == null || quantity > stockProduct.getStock()) {
                    model.addAttribute("error", "Some products do not have enough stock");
                    return "redirect:/view-order";
                }
            }

            // Update stock levels
            for (Product product : currentOrder.getProducts()) {
                Product stockProduct = productService.getProductById(product.getId());
                int quantity = currentOrder.getProductQuantity(product);
                stockProduct.setStock(stockProduct.getStock() - quantity);
                entityManager.merge(stockProduct);
            }

            currentOrder.setAddress(address);
            
            // Get all user orders sorted by ID
            List<Order> userOrders = orderRepository.findByUser(currentUser);
            int userOrderNumber = userOrders.size() + 1;
            
            // Assign the unique order number for this user
            currentOrder.setUserOrderNumber(userOrderNumber);
            
            currentUser.addOrder(currentOrder);

            Order confirmedOrder = currentOrder;
            orderRepository.save(confirmedOrder);
            currentOrder = new Order(currentUser, currentUser.getAddress());
            setCurrentOrder(session, currentOrder); // Reset the current order in the session
            model.addAttribute("order", confirmedOrder);
            model.addAttribute("address", address);
            model.addAttribute("userOrderNumber", userOrderNumber);
            return "confirm_order";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "redirect:/view-order";
        }
    }

    
}

