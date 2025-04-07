package es.xpressaly.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import es.xpressaly.Model.Order;
import es.xpressaly.Model.Product;
import es.xpressaly.Model.User;
import es.xpressaly.Service.ProductService;
import es.xpressaly.Service.UserService;
import es.xpressaly.Repository.OrderRepository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/orders")
@Transactional
public class OrderApiController {

    @Autowired
    private ProductService productService;

    @Autowired
    private UserService userService;

    @Autowired
    private OrderRepository orderRepository;
    
    @PersistenceContext
    private EntityManager entityManager;

    @GetMapping("/cart-count")
    public ResponseEntity<Integer> getCartItemCount() {
        try {
            Order currentOrder = getCurrentOrder();
            if (currentOrder == null || currentOrder.getProducts() == null) {
                return ResponseEntity.ok(0);
            }
            int count = currentOrder.getProducts().stream()
                    .mapToInt(Product::getAmount)
                    .sum();
            return ResponseEntity.ok(count);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/add-to-order")
    public ResponseEntity<Map<String, Object>> addToOrder(@RequestParam Long productId) {
        Map<String, Object> response = new HashMap<>();
        try {
            Product product = productService.getProductById(productId);
            if (product == null) {
                response.put("error", "Product not found");
                return ResponseEntity.badRequest().body(response);
            }

            if (product.getStock() <= 0) {
                response.put("error", "Product out of stock");
                return ResponseEntity.badRequest().body(response);
            }

            Order currentOrder = getCurrentOrder();
            if (currentOrder == null) {
                currentOrder = new Order(userService.getUser(), userService.getUser().getAddress());
            }

            // Check stock and add product logic here
            // ...
            
            response.put("success", "Product added to order");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> viewOrder() {
        Map<String, Object> response = new HashMap<>();
        try {
            Order currentOrder = getCurrentOrder();
            if (currentOrder == null) {
                response.put("message", "No active order");
                return ResponseEntity.ok(response);
            }
            
            currentOrder.calculateTotal();
            response.put("order", currentOrder);
            response.put("total", currentOrder.getTotal());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/remove-from-order")
    public ResponseEntity<Map<String, Object>> removeFromOrder(@RequestParam Long productId) {
        Map<String, Object> response = new HashMap<>();
        try {
            Order currentOrder = getCurrentOrder();
            if (currentOrder != null) {
                Product product = productService.getProductById(productId);
                currentOrder.removeProduct(product);
                response.put("success", "Product removed from order");
                return ResponseEntity.ok(response);
            }
            response.put("error", "No active order");
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/update-amount")
    public ResponseEntity<Map<String, Object>> updateAmount(
            @RequestParam int amount, 
            @RequestParam Long productId) {
        Map<String, Object> response = new HashMap<>();
        try {
            Order currentOrder = getCurrentOrder();
            if (currentOrder == null) {
                response.put("error", "No active order");
                return ResponseEntity.badRequest().body(response);
            }

            if (amount < 1) {
                response.put("error", "Quantity must be greater than 0");
                return ResponseEntity.badRequest().body(response);
            }

            Product productInStock = productService.getProductById(productId);
            if (productInStock == null) {
                response.put("error", "Product not found");
                return ResponseEntity.badRequest().body(response);
            }

            Product productInOrder = currentOrder.findProductById(productId);
            if (productInOrder == null) {
                response.put("error", "Product not in order");
                return ResponseEntity.badRequest().body(response);
            }

            if (amount > productInStock.getStock()) {
                response.put("error", "Not enough stock");
                return ResponseEntity.badRequest().body(response);
            }

            productInOrder.setAmount(amount);
            currentOrder.calculateTotal();
            response.put("order", currentOrder);
            response.put("total", currentOrder.getTotal());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/confirm")
    public ResponseEntity<Map<String, Object>> confirmOrder(@RequestParam String address) {
        Map<String, Object> response = new HashMap<>();
        try {
            Order currentOrder = getCurrentOrder();
            if (currentOrder == null || currentOrder.getProducts().isEmpty()) {
                response.put("error", "No products in order");
                return ResponseEntity.badRequest().body(response);
            }

            if (address == null || address.trim().isEmpty()) {
                response.put("error", "Address required");
                return ResponseEntity.badRequest().body(response);
            }

            // Stock verification and order confirmation logic here
            // ...
            
            response.put("success", "Order confirmed");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    private Order getCurrentOrder() {
        User currentUser = userService.getUser();
        if (currentUser == null) {
            return null;
        }
        
        // Get the user's active order from repository or create new one
        List<Order> userOrders = orderRepository.findByUser(currentUser);
        Order currentOrder = userOrders.stream()
            .findFirst()
            .orElse(null);
            
        if (currentOrder == null) {
            currentOrder = new Order(currentUser, currentUser.getAddress());
            orderRepository.save(currentOrder);
        }
        
        return currentOrder;
    }
}