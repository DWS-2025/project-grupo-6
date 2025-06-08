package es.xpressaly.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import es.xpressaly.Model.Order;
import es.xpressaly.Model.Product;
import es.xpressaly.Model.User;
import es.xpressaly.Service.OrderService;
import es.xpressaly.Service.ProductService;
import es.xpressaly.Service.UserService;
import es.xpressaly.dto.OrderApiDTO;
import es.xpressaly.dto.OrderDTO;
import es.xpressaly.dto.ProductDTO;
import es.xpressaly.dto.ProductWebDTO;
import es.xpressaly.dto.UserDTO;
import es.xpressaly.dto.UserWebDTO;
import es.xpressaly.Repository.OrderRepository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Collection;
import java.util.HashMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;


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

    @Autowired
    private OrderService orderService;
    
    @GetMapping("/all")
    public List<OrderApiDTO> getAllOrders() {     
        return orderService.getAllOrders();
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<OrderApiDTO> getOrderById(@PathVariable Long id) {     
        OrderApiDTO order = orderService.getOrderByIdApi(id);
        if (order == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(order);
    }

    @PostMapping("/create")
    public ResponseEntity<?> createOrder(@RequestBody OrderApiDTO orderDto) {
        try {
            // Validar que el usuario existe
            if (orderDto.userId() == null) {
                Map<String, Object> response = new HashMap<>();
                response.put("error", "El ID de usuario es obligatorio");
                return ResponseEntity.badRequest().body(response);
            }
            
            // Validar que la dirección no esté vacía
            if (orderDto.address() == null || orderDto.address().trim().isEmpty()) {
                Map<String, Object> response = new HashMap<>();
                response.put("error", "La dirección es obligatoria");
                return ResponseEntity.badRequest().body(response);
            }
            
            // Validar que haya al menos un producto
            if (orderDto.products() == null || orderDto.products().isEmpty()) {
                Map<String, Object> response = new HashMap<>();
                response.put("error", "La orden debe contener al menos un producto");
                return ResponseEntity.badRequest().body(response);
            }
            
            // Validar que todos los productos existan
            for (ProductDTO productDto : orderDto.products()) {
                if (productDto.id() == null) {
                    Map<String, Object> response = new HashMap<>();
                    response.put("error", "Todos los productos deben tener un ID válido");
                    return ResponseEntity.badRequest().body(response);
                }
            }
            
            OrderApiDTO createdOrder = orderService.createOrderApiDTO(orderDto);
            return ResponseEntity.ok(createdOrder);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(e.getMessage());
        }
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteOrder(@PathVariable Long id) {
        try{
            OrderApiDTO order = orderService.getOrderByIdApi(id);
            orderService.deleteOrderApi(id);
            return ResponseEntity.ok(order);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(e.getMessage());
        }
    }

    @GetMapping("/cart-count")
    public ResponseEntity<Integer> getCartItemCount() {
        try {
            OrderDTO currentOrder = getCurrentOrder();
            if (currentOrder == null || currentOrder.products() == null) {
                return ResponseEntity.ok(0);
            }
            int count = currentOrder.products().stream()
                    .mapToInt(product -> currentOrder.quantities().getOrDefault(product.id(), 0))
                    .sum();
            return ResponseEntity.ok(count);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    

    @GetMapping
    public ResponseEntity<Map<String, Object>> viewOrder() {
        Map<String, Object> response = new HashMap<>();
        try {
            OrderDTO currentOrder = getCurrentOrder();
            if (currentOrder == null) {
                response.put("message", "No active order");
                return ResponseEntity.ok(response);
            }
            
            // Calculate the total using the DTO data
            double total = currentOrder.products().stream()
                .mapToDouble(product -> product.price() * currentOrder.quantities().getOrDefault(product.id(), 0))
                .sum();
            
            response.put("order", currentOrder);
            response.put("total", total);
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
            OrderDTO currentOrder = getCurrentOrder();
            if (currentOrder != null) {
                ProductWebDTO product = productService.getProductByIdWeb(productId);
                if (product == null) {
                    response.put("error", "Product not found");
                    return ResponseEntity.badRequest().body(response);
                }

                // Get the Order entity to remove the product
                Order orderEntity = orderService.getOrderById(currentOrder.id());
                if (orderEntity != null) {
                    orderEntity = orderService.removeProductFromOrder(orderEntity, product);
                    orderService.save(orderEntity);
                    response.put("success", "Product removed from order");
                    return ResponseEntity.ok(response);
                }
            }
            response.put("error", "No active order");
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    

    @PostMapping("/confirm")
    public ResponseEntity<Map<String, Object>> confirmOrder(@RequestParam String address) {
        Map<String, Object> response = new HashMap<>();
        try {
            OrderDTO currentOrderDTO = getCurrentOrder();
            if (currentOrderDTO == null || currentOrderDTO.products().isEmpty()) {
                response.put("error", "No products in order");
                return ResponseEntity.badRequest().body(response);
            }

            if (address == null || address.trim().isEmpty()) {
                response.put("error", "Address required");
                return ResponseEntity.badRequest().body(response);
            }

            UserWebDTO currentUserDTO = userService.getUser();
            User currentUser = userService.getUserEntityById(currentUserDTO.id());
            
            // Get the Order entity
            Order currentOrder = orderService.getOrderById(currentOrderDTO.id());
            if (currentOrder == null) {
                response.put("error", "Order not found");
                return ResponseEntity.badRequest().body(response);
            }
            
            // Calculate the order number for this user
            int userOrderNumber = 1;
            List<Order> userOrders = orderRepository.findByUser(currentUser);
            if (!userOrders.isEmpty()) {
                userOrderNumber = userOrders.size() + 1;
            }
            currentOrder.setUserOrderNumber(userOrderNumber);
            
            // Validate stock and update product quantities
            for (ProductWebDTO orderProductDTO : currentOrderDTO.products()) {
                Product stockProduct = productService.getProductById(orderProductDTO.id());
                if (stockProduct == null || currentOrderDTO.quantities().get(orderProductDTO.id()) > stockProduct.getStock()) {
                    response.put("error", "Some products do not have enough stock");
                    return ResponseEntity.badRequest().body(response);
                }
                stockProduct.setStock(stockProduct.getStock() - currentOrderDTO.quantities().get(orderProductDTO.id()));
                entityManager.merge(stockProduct);
            }
            
            currentOrder.setAddress(address);
            currentUser.addOrder(currentOrder);
            orderRepository.save(currentOrder);
            
            response.put("success", "Order confirmed");
            response.put("orderNumber", userOrderNumber);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    private OrderDTO getCurrentOrder() {
        UserWebDTO currentUser = userService.getUser();
        if (currentUser == null) {
            return null;
        }
        
        List<OrderDTO> userOrders = orderService.getOrdersByUserDTO(currentUser);
        if (userOrders.isEmpty()) {          
            return orderService.createOrderDTO(currentUser, currentUser.address());
        } else {
            return userOrders.get(userOrders.size() - 1);
        }
    }
}