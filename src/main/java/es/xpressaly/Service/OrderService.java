package es.xpressaly.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import es.xpressaly.Model.Order;
import es.xpressaly.Model.Product;
import es.xpressaly.Model.User;
import es.xpressaly.Model.UserRole;
import es.xpressaly.Repository.OrderRepository;
import es.xpressaly.Repository.ProductRepository;
import es.xpressaly.Repository.UserRepository;
import es.xpressaly.dto.OrderApiDTO;
import es.xpressaly.dto.OrderDTO;
import es.xpressaly.dto.ProductDTO;
import es.xpressaly.dto.ProductWebDTO;
import es.xpressaly.dto.UserWebDTO;
import es.xpressaly.mapper.OrderMapper;
import es.xpressaly.mapper.OrderApiMapper;
import es.xpressaly.mapper.ProductWebMapper;
import es.xpressaly.mapper.UserWebMapper;
import jakarta.transaction.Transactional;
import jakarta.persistence.EntityManager;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserWebMapper userWebMapper;

    @Autowired
    private OrderMapper orderWebMapper;

    @Autowired
    private ProductWebMapper productWebMapper;

    @Autowired
    private OrderApiMapper orderApiMapper;

    @Autowired
    private SanitizationService sanitizationService;

    @Autowired
    private UserService userService;

    @Autowired
    private EntityManager entityManager;

    private void checkUserPermissionForOrder(Order order) {
        UserWebDTO currentUser = userService.getUser();
        if (currentUser == null) {
            throw new SecurityException("User not authenticated");
        }
    
        if (order == null) {
            throw new IllegalArgumentException("Order cannot be null");
        }
    
        if (currentUser.role() != UserRole.ADMIN && (order.getUser() == null || !order.getUser().getId().equals(currentUser.id()))) {
            throw new SecurityException("Sorry, you don't have permission to access this order");
        }
    }

    //Sanitized
    public OrderDTO createOrderDTO(UserWebDTO user, String address) {
        UserWebDTO currentUser = userService.getUser();
        if (currentUser == null || (!currentUser.id().equals(user.id()) && currentUser.role() != UserRole.ADMIN)) {
            throw new SecurityException("Access Denied. You can only create orders for yourself.");
        }
        User userEntity = userWebMapper.toDomain(user);
        Order order = new Order();
        order.setUser(userEntity);
        order.setAddress(sanitizationService.sanitize(address));
        order.setTotal(0.0);
        
        // Set the user order number
        List<Order> userOrders = orderRepository.findByUser(userEntity);
        // The order number must be the next to the last existing order number
        int maxOrderNumber = 0;
        for (Order existingOrder : userOrders) {
            if (existingOrder.getUserOrderNumber() != null && existingOrder.getUserOrderNumber() > maxOrderNumber) {
                maxOrderNumber = existingOrder.getUserOrderNumber();
            }
        }
        order.setUserOrderNumber(maxOrderNumber + 1);
        
        return orderWebMapper.toDTO(order);
    }

    public List<OrderDTO> getOrdersByUserDTO(UserWebDTO user) {
        UserWebDTO currentUser = userService.getUser();
        if (currentUser == null || (!currentUser.id().equals(user.id()) && currentUser.role() != UserRole.ADMIN)) {
            throw new SecurityException("Access Denied. You can only view your own orders.");
        }
        User userEntity = userWebMapper.toDomain(user);
        List<Order> orders = orderRepository.findByUser(userEntity);

        // Make sure all orders have a userOrderNumber
        // The order number must be the next to the last existing order number
        int maxOrderNumber = 0;
        for (Order existingOrder : orders) {
            if (existingOrder.getUserOrderNumber() != null && existingOrder.getUserOrderNumber() > maxOrderNumber) {
                maxOrderNumber = existingOrder.getUserOrderNumber();
            }
        }
        
        for (int i = 0; i < orders.size(); i++) {
            Order order = orders.get(i);
            if (order.getUserOrderNumber() == null) {
                order.setUserOrderNumber(maxOrderNumber + i + 1);
                orderRepository.save(order);
            }
        }
        
        return orderWebMapper.toDTOs(orders);
    }

    //Sanitized
    public Order createOrder(UserWebDTO user, String address) {
        UserWebDTO currentUser = userService.getUser();
        if (currentUser == null || (!currentUser.id().equals(user.id()) && currentUser.role() != UserRole.ADMIN)) {
            throw new SecurityException("Access Denied. You can only create orders for yourself.");
        }
        User userEntity = userWebMapper.toDomain(user);
        Order order = new Order();
        order.setUser(userEntity);
        order.setAddress(sanitizationService.sanitize(address));
        order.setTotal(0.0);

        // Set the user's order number
        List<Order> userOrders = orderRepository.findByUser(userEntity);
        // The order number must be the next to the last existing order number
        int maxOrderNumber = 0;
        for (Order existingOrder : userOrders) {
            if (existingOrder.getUserOrderNumber() != null && existingOrder.getUserOrderNumber() > maxOrderNumber) {
                maxOrderNumber = existingOrder.getUserOrderNumber();
            }
        }
        order.setUserOrderNumber(maxOrderNumber + 1);
        
        return order;
    }

    public Order getOrderById(Long id) {
        Order order = orderRepository.findById(id).orElse(null);
        if (order != null) {
            checkUserPermissionForOrder(order);
        }
        return order;
    }

    public List<Order> getOrdersByUser(UserWebDTO user) {
        UserWebDTO currentUser = userService.getUser();
        if (currentUser == null || (!currentUser.id().equals(user.id()) && currentUser.role() != UserRole.ADMIN)) {
            throw new SecurityException("Access Denied. You can only view your own orders.");
        }
        User userEntity = userWebMapper.toDomain(user);
        List<Order> orders = orderRepository.findByUser(userEntity);

        // Make sure all orders have a userOrderNumber

        int maxOrderNumber = 0;
        for (Order existingOrder : orders) {
            if (existingOrder.getUserOrderNumber() != null && existingOrder.getUserOrderNumber() > maxOrderNumber) {
                maxOrderNumber = existingOrder.getUserOrderNumber();
            }
        }
        
        for (int i = 0; i < orders.size(); i++) {
            Order order = orders.get(i);
            if (order.getUserOrderNumber() == null) {
                order.setUserOrderNumber(maxOrderNumber + i + 1);
                orderRepository.save(order);
            }
        }
        
        return orders;
    }

    public Order addProductToOrder(ProductWebDTO productWebDTO, int amount, UserWebDTO currentUser, Order currentOrder) {
        if (currentOrder != null) {
            checkUserPermissionForOrder(currentOrder);
        }
        User user = userWebMapper.toDomain(currentUser);
        

        if (currentOrder == null) {
            currentOrder = createOrder(currentUser, user.getAddress());
            user.setCurrentOrder(currentOrder);
        }
        
        Product product = productRepository.findById(productWebDTO.id()).orElse(null);

        if (product == null) {
            throw new IllegalArgumentException("Product not found");
        }

        if (product.getStock() < amount) {
            throw new IllegalArgumentException("Insufficient stock");
        }


        int currentQuantity = currentOrder.getProductQuantity(product);
        // Update the quantity by adding the new quantity
        currentOrder.setProductQuantity(product, currentQuantity + amount);
        currentOrder.calculateTotal();

        // Ensure the order is updated on the user
        user.setCurrentOrder(currentOrder);
        currentOrder.addProduct(product);

        // Make sure the userOrderNumber is set
        if (currentOrder.getUserOrderNumber() == null) {
            List<Order> userOrders = orderRepository.findByUser(user);

            int maxOrderNumber = 0;
            for (Order existingOrder : userOrders) {
                if (existingOrder.getUserOrderNumber() != null && existingOrder.getUserOrderNumber() > maxOrderNumber) {
                    maxOrderNumber = existingOrder.getUserOrderNumber();
                }
            }
            currentOrder.setUserOrderNumber(maxOrderNumber + 1);
        }
        
        return currentOrder;
    }

    public Order removeProductFromOrder(Order order, ProductWebDTO productWebDTO) {
        checkUserPermissionForOrder(order);
        Product product = productWebMapper.toDomain(productWebDTO);
        order.removeProduct(product);
        return order;
    }

    public Order clearOrder(Order order) {
        checkUserPermissionForOrder(order);
        while (order.getProducts().size() > 0) {
            order.removeProduct(order.getProducts().get(0));
        }
        return order;
    }

    public ProductWebDTO findProductByIdWeb(Long id, Order order) {
        checkUserPermissionForOrder(order);
        Product product = order.findProductById(id);
        return productWebMapper.toDTO(product);
    }

    //Sanitizado
    public Order setAddress(Order order, String address) {
        checkUserPermissionForOrder(order);
        order.setAddress(sanitizationService.sanitize(address));
        return order;
    }

    public Order setUserOrderNumber(Order order, int userOrderNumber) {
        order.setUserOrderNumber(userOrderNumber);
        return order;
    }

    public void save(Order order) {
        checkUserPermissionForOrder(order);
        // Make sure the userOrderNumber is set
        if (order.getUserOrderNumber() == null && order.getUser() != null) {
            List<Order> userOrders = orderRepository.findByUser(order.getUser());

            int maxOrderNumber = 0;
            for (Order existingOrder : userOrders) {
                if (existingOrder.getUserOrderNumber() != null && existingOrder.getUserOrderNumber() > maxOrderNumber) {
                    maxOrderNumber = existingOrder.getUserOrderNumber();
                }
            }
            order.setUserOrderNumber(maxOrderNumber + 1);
        }
        
        orderRepository.save(order);
    }

    public Order setProductQuantity(Order order, ProductWebDTO productWebDTO, int amount) {
        checkUserPermissionForOrder(order);
        Product product = productWebMapper.toDomain(productWebDTO);
        order.setProductQuantity(product, amount);
        return order;
    }

    public Order calculateTotal(Order order) {
        checkUserPermissionForOrder(order);
        order.calculateTotal();
        return order;
    }

    public void delete(Order orderToDelete) {
        checkUserPermissionForOrder(orderToDelete);
        
        // Delete the order
        orderRepository.delete(orderToDelete);
        
        // Force flush to ensure deletion is committed to database immediately
        entityManager.flush();
        entityManager.clear(); // Clear persistence context to force fresh query
    }

    public Order findById(Long orderId) {
        Order order = orderRepository.findById(orderId).orElse(null);
        if (order != null) {
            checkUserPermissionForOrder(order);
        }
        return order;
    }

    public List<OrderApiDTO> getAllOrders() {
        UserWebDTO currentUser = userService.getUser();
        if (currentUser == null || currentUser.role() != UserRole.ADMIN) {
            throw new SecurityException("Access Denied: Only admins can view all orders.");
        }
        List<Order> orders = orderRepository.findAll();

        // Make sure all orders have a userOrderNumber
        for (Order order : orders) {
            if (order.getUserOrderNumber() == null && order.getUser() != null) {
                List<Order> userOrders = orderRepository.findByUser(order.getUser());
                // The order number must be the next to the last existing order number
                int maxOrderNumber = 0;
                for (Order existingOrder : userOrders) {
                    if (existingOrder.getUserOrderNumber() != null && existingOrder.getUserOrderNumber() > maxOrderNumber) {
                        maxOrderNumber = existingOrder.getUserOrderNumber();
                    }
                }
                order.setUserOrderNumber(maxOrderNumber + 1);
                orderRepository.save(order);
            }
        }
        
        return orders.stream()
            .map(orderApiMapper::toDTO)
            .collect(Collectors.toList());
    }

    public OrderApiDTO createOrderApiDTO(OrderApiDTO orderDto) {
        UserWebDTO currentUser = userService.getUser();
        if (currentUser == null) {
            throw new SecurityException("User not authenticated.");
        }
        if (orderDto.userId() != null && !currentUser.id().equals(orderDto.userId()) && currentUser.role() != UserRole.ADMIN) {
            throw new SecurityException("Access Denied. You can only create orders for yourself.");
        }
        Order order = new Order();
        order.setAddress(sanitizationService.sanitize(orderDto.address()));
        order.setTotal(orderDto.total());
        
        if (orderDto.userId() != null) {
            User user = userService.getUserEntityById(orderDto.userId());
            if (user != null) {
                order.setUser(user);
                // add order to user
                user.addOrder(order);
                

                List<Order> userOrders = orderRepository.findByUser(user);
                // The order number must be the next to the last existing order number
                int maxOrderNumber = 0;
                for (Order existingOrder : userOrders) {
                    if (existingOrder.getUserOrderNumber() != null && existingOrder.getUserOrderNumber() > maxOrderNumber) {
                        maxOrderNumber = existingOrder.getUserOrderNumber();
                    }
                }
                order.setUserOrderNumber(maxOrderNumber + 1);
            }
        } else {
            // If there is no user, set a temporary order number
            order.setUserOrderNumber(0);
        }
        
        if (orderDto.products() != null && !orderDto.products().isEmpty()) {
            for (es.xpressaly.dto.ProductDTO productDto : orderDto.products()) {
                if (productDto.id() != null) {
                    Product product = productRepository.findById(productDto.id())
                            .orElseThrow(() -> new RuntimeException("Producto no encontrado con ID: " + productDto.id()));

                    // Check that there is sufficient stock
                    int quantity = productDto.quantity() > 0 ? productDto.quantity() : 1;
                    if (product.getStock() < quantity) {
                        throw new RuntimeException("No hay suficiente stock para el producto: " + product.getName() + 
                            ". Stock disponible: " + product.getStock() + ", Cantidad solicitada: " + quantity);
                    }

                    // Set the quantity specified in the ProductDTO
                    order.setProductQuantity(product, quantity);
                }
            }
        }
        
        order.calculateTotal();
        Order savedOrder = orderRepository.save(order);
        return orderApiMapper.toDTO(savedOrder);
    }

    public OrderApiDTO getOrderByIdApi(Long id) {
        Order order = orderRepository.findById(id).orElse(null);
        if (order == null) {
            return null;
        }
        checkUserPermissionForOrder(order);

        // Make sure the userOrderNumber is set
        if (order.getUserOrderNumber() == null && order.getUser() != null) {
            List<Order> userOrders = orderRepository.findByUser(order.getUser());
            // El número de orden debe ser el siguiente al último número de orden existente
            int maxOrderNumber = 0;
            for (Order existingOrder : userOrders) {
                if (existingOrder.getUserOrderNumber() != null && existingOrder.getUserOrderNumber() > maxOrderNumber) {
                    maxOrderNumber = existingOrder.getUserOrderNumber();
                }
            }
            order.setUserOrderNumber(maxOrderNumber + 1);
            orderRepository.save(order);
        }
        
        return orderApiMapper.toDTO(order);
    }

    public void deleteOrderApi(Long id) {
        Order orderToDelete = orderRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Order not found"));
        delete(orderToDelete);
    }
}