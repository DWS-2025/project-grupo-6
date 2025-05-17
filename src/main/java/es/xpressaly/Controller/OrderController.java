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
import es.xpressaly.Service.OrderService;
import es.xpressaly.Service.ProductService;
import es.xpressaly.Service.UserService;
import es.xpressaly.dto.OrderDTO;
import es.xpressaly.dto.ProductWebDTO;
import es.xpressaly.dto.UserWebDTO;
import es.xpressaly.mapper.ProductWebMapper;

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

    //private Order currentOrder; // User's current order
    private static final String CURRENT_ORDER_SESSION_KEY = "currentOrder";
    
    public OrderDTO getCurrentOrder(HttpSession session) {
        OrderDTO order = (OrderDTO) session.getAttribute(CURRENT_ORDER_SESSION_KEY);
        UserWebDTO currentUser = userService.getUser();
        
        // If there is no order in the session or the user is null, it returns null
        if (order == null || currentUser == null || currentUser.id() == null) {
            return null;
        }
        
        // If the order exists but belongs to a different user, create a new one
        if (order.user() == null || order.user().id() == null || !order.user().id().equals(currentUser.id())) {
            String address = userService.getAddress(currentUser);
            order = orderService.createOrder(currentUser, address);
            setCurrentOrder(session, order);
        }
        
        return order;
    }

    public void setCurrentOrder(HttpSession session, OrderDTO order) {
        session.setAttribute(CURRENT_ORDER_SESSION_KEY, order);
    }
    // View products and add to order
    public int getCartItemCount(HttpSession session) {
        OrderDTO currentOrder = getCurrentOrder(session);
        if (currentOrder == null || currentOrder.products() == null) {
            return 0;
        }
        return currentOrder.products().size();
    }

    @GetMapping("/view_order_products")
    public String showProducts(Model model, HttpSession session) {
        UserWebDTO currentUser = userService.getUser();
        OrderDTO currentOrder = getCurrentOrder(session);

        if (currentOrder == null || !currentOrder.user().id().equals(currentUser.id())) {
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
            
            OrderDTO currentOrder = getCurrentOrder(session);
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
            currentOrder = orderService.addProductToOrder(currentOrder, productWebDTO, currentQuantity + 1, session);
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
        
        OrderDTO currentOrder = getCurrentOrder(session);
        model.addAttribute("isAdmin", currentUser.role() == UserRole.ADMIN);
        model.addAttribute("cartItemCount", getCartItemCount(session));
        
        if (currentOrder == null || currentOrder.products() == null || currentOrder.products().isEmpty()) {
            model.addAttribute("message", "Your cart is empty");
            return "my_Order";
        }
        
        model.addAttribute("order", currentOrder);
        model.addAttribute("products", currentOrder.products());
        model.addAttribute("quantities", currentOrder.quantities());
        model.addAttribute("total", currentOrder.total());
        return "my_Order";
    }

    // Remove a product from the order
    @PostMapping("/remove-from-order")
    public String removeFromOrder(@RequestParam Long productId, Model model, HttpSession session) {
        UserWebDTO currentUser = userService.getUser();
        
        if (currentUser == null) {
            return "redirect:/cart-prompt";
        }
        
        OrderDTO currentOrder = getCurrentOrder(session);
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

        OrderDTO currentOrder = getCurrentOrder(session);
        if (currentOrder == null || currentOrder.products().isEmpty()) {
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
        
        OrderDTO currentOrder = getCurrentOrder(session);
        if (currentOrder == null) {
            model.addAttribute("error", "No active order");
            return "redirect:/products";
        }

        if (amount < 1) {
            model.addAttribute("error", "Quantity must be greater than 0");
            model.addAttribute("order", currentOrder);
            model.addAttribute("total", currentOrder.total());
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
            model.addAttribute("total", currentOrder.total());
            model.addAttribute("isAdmin", currentUser.role() == UserRole.ADMIN);
            return "my_Order";
        }

        currentOrder = orderService.setProductQuantity(currentOrder, productInOrder, amount);
        currentOrder = orderService.calculateTotal(currentOrder);
        setCurrentOrder(session, currentOrder);
        model.addAttribute("order", currentOrder);
        model.addAttribute("total", currentOrder.total());
        return "my_Order";
    }

    // Confirm order
    @PostMapping("/confirm-order")
    public String confirmOrder(Model model, @RequestParam String address, HttpSession session) {
        try {
            UserWebDTO currentUser = userService.getUser();
            
            if (currentUser == null) {
                return "redirect:/cart-prompt";
            }
            
            OrderDTO currentOrder = getCurrentOrder(session);
            if (currentOrder == null || currentOrder.products().isEmpty()) {
                model.addAttribute("error", "No products in the order");
                return "redirect:/view-order";
            }

            if (address == null || address.trim().isEmpty()) {
                model.addAttribute("error", "Shipping address is required");
                return "redirect:/view-order";
            }

            for (ProductWebDTO orderProduct : currentOrder.products()) {
                ProductWebDTO productWebDTO = productService.getProductByIdWeb(orderProduct.id());
                if (productWebDTO == null) {
                    model.addAttribute("error", "Some products do not have enough stock");
                    return "redirect:/view-order";
                }
                ProductWebDTO stockProduct = productWebDTO;
                int quantity = getProductQuantity(currentOrder, orderProduct.id());
                if (quantity > stockProduct.stock()) {
                    model.addAttribute("error", "Some products do not have enough stock");
                    return "redirect:/view-order";
                }
            }

            for (ProductWebDTO product : currentOrder.products()) {
                ProductWebDTO productWebDTO = productService.getProductByIdWeb(product.id());
                ProductWebDTO stockProduct = productWebDTO;
                int quantity = getProductQuantity(currentOrder, product.id());
                stockProduct = productService.setStock(stockProduct, stockProduct.stock() - quantity);
                entityManager.merge(stockProduct);
            }

            currentOrder = orderService.setAddress(currentOrder, address);
            
            List<OrderDTO> userOrders = orderService.getOrdersByUser(currentUser);
            int userOrderNumber = userOrders.size() + 1;
            
            currentOrder=orderService.setUserOrderNumber(currentOrder, userOrderNumber);
            
            currentUser = userService.addOrder(currentUser, currentOrder);

            OrderDTO confirmedOrder = currentOrder;
            orderService.save(confirmedOrder);
            currentOrder = orderService.createOrder(currentUser, currentUser.address());
            setCurrentOrder(session, currentOrder);
            model.addAttribute("order", confirmedOrder);
            model.addAttribute("address", address);
            model.addAttribute("userOrderNumber", userOrderNumber);
            return "confirm_order";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "redirect:/view-order";
        }
    }

    public int getProductQuantity(OrderDTO order, Long productId) {
        if (order == null || order.products() == null) {
            return 0;
        }
        return order.quantities().getOrDefault(productId, 0);
    }
}

