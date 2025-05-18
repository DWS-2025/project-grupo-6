package es.xpressaly.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.servlet.http.HttpSession;
import jakarta.transaction.Transactional;
import es.xpressaly.Model.UserRole;
import es.xpressaly.Service.OrderService;
import es.xpressaly.Service.ProductService;
import es.xpressaly.Service.UserService;
import es.xpressaly.dto.OrderDTO;
import es.xpressaly.dto.ProductWebDTO;
import es.xpressaly.dto.UserWebDTO;
import es.xpressaly.Model.Order;
import es.xpressaly.Model.Product;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

@Controller
@Transactional
public class OrderController {

    @Autowired
    private ProductService productService;

    @Autowired
    private UserService userService;

    @Autowired
    private OrderService orderService;
    
    @PersistenceContext
    private EntityManager entityManager;

    private static final String CURRENT_ORDER_SESSION_KEY = "currentOrder";
    
    public Order getCurrentOrder(HttpSession session) {
        Order order = (Order) session.getAttribute(CURRENT_ORDER_SESSION_KEY);
        UserWebDTO currentUser = userService.getUser();
        
        // If there is no order in the session or the user is null, it returns null
        if (order == null || currentUser == null || currentUser.id() == null) {
            return null;
        }
        
        // If the order exists but belongs to a different user, create a new one
        if (order.getUser() == null || order.getUser().getId() == null || !order.getUser().getId().equals(currentUser.id())) {
            String address = userService.getAddress(currentUser);
            order = orderService.createOrder(currentUser, address);
            setCurrentOrder(session, order);
        }
        
        return order;
    }

    public void setCurrentOrder(HttpSession session, Order order) {
        session.setAttribute(CURRENT_ORDER_SESSION_KEY, order);
    }

    public int getCartItemCount(HttpSession session) {
        Order currentOrder = getCurrentOrder(session);
        if (currentOrder == null || currentOrder.getProducts() == null) {
            return 0;
        }
        return currentOrder.getProducts().size();
    }

    @GetMapping("/view_order_products")
    public String showProducts(Model model, HttpSession session) {
        UserWebDTO currentUser = userService.getUser();
        Order currentOrder = getCurrentOrder(session);

        if (currentOrder == null || !currentOrder.getUser().getId().equals(currentUser.id())) {
            currentOrder = orderService.createOrder(currentUser, currentUser.address());
        }

        model.addAttribute("products", productService.getAllProductsWeb());
        model.addAttribute("order", currentOrder);
        model.addAttribute("isAdmin", currentUser.role() == UserRole.ADMIN);
        model.addAttribute("cartItemCount", getCartItemCount(session));
        return "Wellcome";
    }

    // Add product to order
    @PostMapping("/add-to-order")
    @ResponseBody
    public Map<String, Object> addToOrder(@RequestParam Long productId, HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        try {
            ProductWebDTO productWebDTO = productService.getProductByIdWeb(productId);
            UserWebDTO currentUser = userService.getUser();
            
            if (currentUser == null) {
                response.put("error", "User not logged in");
                return response;
            }
            
            Order currentOrder = getCurrentOrder(session);
            if (productWebDTO == null) {
                response.put("error", "Product not found");
                return response;
            }

            if (productWebDTO.stock() <= 0) {
                response.put("error", "We're sorry, we do not have enough stock at the moment, try later");
                return response;
            }

            if (currentOrder == null) {
                currentOrder = orderService.createOrder(currentUser, currentUser.address());
                setCurrentOrder(session, currentOrder);
            }

            int currentQuantity = getProductQuantity(currentOrder, productId);
            if (currentQuantity + 1 > productWebDTO.stock()) {
                response.put("error", "We're sorry, we do not have enough stock at the moment, try later");
                return response;
            }

            currentOrder = orderService.setProductQuantity(currentOrder, productWebDTO, currentQuantity + 1);
            currentOrder = orderService.addProductToOrder(productWebDTO, currentQuantity + 1, session);
            currentOrder = orderService.calculateTotal(currentOrder);
            setCurrentOrder(session, currentOrder);
            
            response.put("success", true);
            response.put("cartSize", getCartItemCount(session));
            return response;
        } catch (Exception e) {
            response.put("error", e.getMessage());
            return response;
        }
    }

    // View current order
    @GetMapping("/view-order")
    public String viewOrder(Model model, HttpSession session) {
        UserWebDTO currentUser = userService.getUser();
        
        // Check if user is logged in, redirect to cart prompt if not
        if (currentUser == null) {
            return "redirect:/cart-prompt";
        }
        
        Order currentOrder = getCurrentOrder(session);
        model.addAttribute("isAdmin", currentUser.role() == UserRole.ADMIN);
        model.addAttribute("cartItemCount", getCartItemCount(session));
        
        if (currentOrder == null || currentOrder.getProducts() == null || currentOrder.getProducts().isEmpty()) {
            model.addAttribute("message", "Your cart is empty");
            return "my_Order";
        }
        
        model.addAttribute("order", currentOrder);
        model.addAttribute("products", currentOrder.getProducts());
        model.addAttribute("quantities", currentOrder.getQuantities());
        model.addAttribute("total", currentOrder.getTotal());
        return "my_Order";
    }

    // Remove a product from the order
    @PostMapping("/remove-from-order")
    public String removeFromOrder(@RequestParam Long productId, Model model, HttpSession session) {
        UserWebDTO currentUser = userService.getUser();
        
        if (currentUser == null) {
            return "redirect:/cart-prompt";
        }
        
        Order currentOrder = getCurrentOrder(session);
        if (currentOrder != null) {
            ProductWebDTO productWebDTO = productService.getProductByIdWeb(productId);
            if (productWebDTO != null) {
                currentOrder = orderService.removeProductFromOrder(currentOrder, productWebDTO);
                setCurrentOrder(session, currentOrder);
            }
        }
        setCurrentOrder(session, currentOrder);
        model.addAttribute("order", currentOrder);
        model.addAttribute("cartItemCount", getCartItemCount(session));
        return "redirect:/view-order";
    }

    //Delete order
    @GetMapping("/delete-order")
    public String deleteOrder(Model model, HttpSession session) {
        UserWebDTO currentUser = userService.getUser();
        if (currentUser == null) {
            return "redirect:/cart-prompt";
        }

        Order currentOrder = getCurrentOrder(session);
        if (currentOrder == null || currentOrder.getProducts().isEmpty()) {
            model.addAttribute("message", "No active order to delete");
            return "redirect:/products";
        }

        currentOrder = orderService.clearOrder(currentOrder);
        setCurrentOrder(session, currentOrder);
        model.addAttribute("products", productService.getAllProductsWeb());
        model.addAttribute("order", currentOrder);
        model.addAttribute("isAdmin", currentUser.role() == UserRole.ADMIN);
        model.addAttribute("cartItemCount", getCartItemCount(session));
        return "Wellcome";
    }
    
    //Update products' amount
    @PostMapping("/update-amount")
    public String updateAmount(Model model, @RequestParam int amount, @RequestParam Long productId, HttpSession session) {
        UserWebDTO currentUser = userService.getUser();
        
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

        ProductWebDTO productWebDTO = productService.getProductByIdWeb(productId);
        if (productWebDTO == null) {
            model.addAttribute("error", "Product not found");
            return "redirect:/products";
        }

        ProductWebDTO productInStock = productWebDTO;
        ProductWebDTO productInOrder = orderService.findProductByIdWeb(productId, currentOrder);
        if (productInOrder == null) {
            model.addAttribute("error", "Product is not in the current order");
            return "redirect:/view-order";
        }

        if (amount > productInStock.stock()) {
            setCurrentOrder(session, currentOrder);
            model.addAttribute("errorMessage", "We're sorry, we do not have enough stock at the moment, try later");
            model.addAttribute("order", currentOrder);
            model.addAttribute("total", currentOrder.getTotal());
            model.addAttribute("isAdmin", currentUser.role() == UserRole.ADMIN);
            return "my_Order";
        }

        currentOrder = orderService.setProductQuantity(currentOrder, productInOrder, amount);
        currentOrder = orderService.calculateTotal(currentOrder);
        setCurrentOrder(session, currentOrder);
        model.addAttribute("order", currentOrder);
        model.addAttribute("total", currentOrder.getTotal());
        return "my_Order";
    }

    // Confirm order
    @PostMapping("/confirm-order")
    @Transactional
    public String confirmOrder(Model model, @RequestParam String address, HttpSession session) {
        try {
            UserWebDTO currentUser = userService.getUser();
            
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

            // Verificar stock antes de hacer cualquier cambio
            for (Product orderProduct : currentOrder.getProducts()) {
                ProductWebDTO productWebDTO = productService.getProductByIdWeb(orderProduct.getId());
                if (productWebDTO == null) {
                    model.addAttribute("error", "Some products do not have enough stock");
                    return "redirect:/view-order";
                }
                int quantity = getProductQuantity(currentOrder, orderProduct.getId());
                if (quantity > productWebDTO.stock()) {
                    model.addAttribute("error", "Some products do not have enough stock");
                    return "redirect:/view-order";
                }
            }

            // Actualizar stock y guardar cambios
            for (Product product : currentOrder.getProducts()) {
                ProductWebDTO productWebDTO = productService.getProductByIdWeb(product.getId());
                int quantity = getProductQuantity(currentOrder, product.getId());
                Product stockProduct = productService.getProductById(product.getId());
                stockProduct.setStock(stockProduct.getStock() - quantity);
                entityManager.merge(stockProduct);
            }

            // Configurar y guardar el pedido
            currentOrder = orderService.setAddress(currentOrder, address);
            List<Order> userOrders = orderService.getOrdersByUser(currentUser);
            int userOrderNumber = userOrders.size() + 1;
            currentOrder = orderService.setUserOrderNumber(currentOrder, userOrderNumber);
            
            // Guardar el pedido confirmado
            Order confirmedOrder = currentOrder;
            orderService.save(confirmedOrder);

            // Crear nuevo pedido vacío
            currentOrder = orderService.createOrder(currentUser, currentUser.address());
            setCurrentOrder(session, currentOrder);

            model.addAttribute("order", confirmedOrder);
            model.addAttribute("address", address);
            model.addAttribute("userOrderNumber", userOrderNumber);
            return "confirm_order";
        } catch (Exception e) {
            e.printStackTrace(); // Para ver el error en los logs
            model.addAttribute("error", "Error al confirmar el pedido: " + e.getMessage());
            return "redirect:/view-order";
        }
    }

    public int getProductQuantity(Order order, Long productId) {
        if (order == null || order.getProducts() == null) {
            return 0;
        }
        return order.getQuantities().getOrDefault(productId, 0);
    }
}

