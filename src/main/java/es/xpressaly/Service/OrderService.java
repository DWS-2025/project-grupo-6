package es.xpressaly.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import es.xpressaly.Model.Order;
import es.xpressaly.Model.Product;
import es.xpressaly.Model.User;
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

    //Sanitizado
    public OrderDTO createOrderDTO(UserWebDTO user, String address) {
        User userEntity = userWebMapper.toDomain(user);
        Order order = new Order();
        order.setUser(userEntity);
        order.setAddress(sanitizationService.sanitize(address));
        order.setTotal(0.0);
        
        // Establecer el número de orden del usuario
        List<Order> userOrders = orderRepository.findByUser(userEntity);
        // El número de orden debe ser el siguiente al último número de orden existente
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
        User userEntity = userWebMapper.toDomain(user);
        List<Order> orders = orderRepository.findByUser(userEntity);
        
        // Asegurarse de que todos los pedidos tienen userOrderNumber
        // El número de orden debe ser el siguiente al último número de orden existente
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
    //Sanitizado
    public Order createOrder(UserWebDTO user, String address) {
        User userEntity = userWebMapper.toDomain(user);
        Order order = new Order();
        order.setUser(userEntity);
        order.setAddress(sanitizationService.sanitize(address));
        order.setTotal(0.0);
        
        // Establecer el número de orden del usuario
        List<Order> userOrders = orderRepository.findByUser(userEntity);
        // El número de orden debe ser el siguiente al último número de orden existente
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
        return orderRepository.findById(id).orElse(null);
    }

    public List<Order> getOrdersByUser(UserWebDTO user) {
        User userEntity = userWebMapper.toDomain(user);
        List<Order> orders = orderRepository.findByUser(userEntity);
        
        // Asegurarse de que todos los pedidos tienen userOrderNumber
        // El número de orden debe ser el siguiente al último número de orden existente
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
        User user = userWebMapper.toDomain(currentUser);
        
        // Si el order es null, crear uno nuevo
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

        // Obtener la cantidad actual del producto
        int currentQuantity = currentOrder.getProductQuantity(product);
        // Actualizar la cantidad sumando la nueva cantidad
        currentOrder.setProductQuantity(product, currentQuantity + amount);
        currentOrder.calculateTotal();
        
        // Asegurar que el order se actualiza en el usuario
        user.setCurrentOrder(currentOrder);
        currentOrder.addProduct(product);
        
        // Asegurarse de que el userOrderNumber está establecido
        if (currentOrder.getUserOrderNumber() == null) {
            List<Order> userOrders = orderRepository.findByUser(user);
            // El número de orden debe ser el siguiente al último número de orden existente
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
        Product product = productWebMapper.toDomain(productWebDTO);
        order.removeProduct(product);
        return order;
    }

    public Order clearOrder(Order order) {
        while (order.getProducts().size() > 0) {
            order.removeProduct(order.getProducts().get(0));
        }
        return order;
    }

    public ProductWebDTO findProductByIdWeb(Long id, Order order) {
        Product product = order.findProductById(id);
        return productWebMapper.toDTO(product);
    }

    //Sanitizado
    public Order setAddress(Order order, String address) {
        order.setAddress(sanitizationService.sanitize(address));
        return order;
    }

    public Order setUserOrderNumber(Order order, int userOrderNumber) {
        order.setUserOrderNumber(userOrderNumber);
        return order;
    }

    public void save(Order order) {
        // Asegurarse de que el userOrderNumber está establecido
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
        }
        
        orderRepository.save(order);
    }

    public Order setProductQuantity(Order order, ProductWebDTO productWebDTO, int amount) {
        Product product = productWebMapper.toDomain(productWebDTO);
        order.setProductQuantity(product, amount);
        return order;
    }

    public Order calculateTotal(Order order) {
        order.calculateTotal();
        return order;
    }

    public void delete(Order orderToDelete) {
        orderRepository.delete(orderToDelete);
    }

    public Order findById(Long orderId) {
        return orderRepository.findById(orderId).orElse(null);
    }

    public List<OrderApiDTO> getAllOrders() {
        List<Order> orders = orderRepository.findAll();
        
        // Asegurarse de que todos los pedidos tienen userOrderNumber
        for (Order order : orders) {
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
        }
        
        return orders.stream()
            .map(orderApiMapper::toDTO)
            .collect(Collectors.toList());
    }

    public OrderApiDTO createOrderApiDTO(OrderApiDTO orderDto) {
        Order order = new Order();
        order.setAddress(sanitizationService.sanitize(orderDto.address()));
        order.setTotal(orderDto.total());
        
        if (orderDto.userId() != null) {
            User user = userService.getUserEntityById(orderDto.userId());
            if (user != null) {
                order.setUser(user);
                // Añadir la orden al usuario
                user.addOrder(order);
                
                // Establecer el número de orden del usuario
                List<Order> userOrders = orderRepository.findByUser(user);
                // El número de orden debe ser el siguiente al último número de orden existente
                int maxOrderNumber = 0;
                for (Order existingOrder : userOrders) {
                    if (existingOrder.getUserOrderNumber() != null && existingOrder.getUserOrderNumber() > maxOrderNumber) {
                        maxOrderNumber = existingOrder.getUserOrderNumber();
                    }
                }
                order.setUserOrderNumber(maxOrderNumber + 1);
            }
        } else {
            // Si no hay usuario, establecer un número de orden temporal
            order.setUserOrderNumber(0);
        }
        
        if (orderDto.products() != null && !orderDto.products().isEmpty()) {
            for (es.xpressaly.dto.ProductDTO productDto : orderDto.products()) {
                if (productDto.id() != null) {
                    Product product = productRepository.findById(productDto.id())
                            .orElseThrow(() -> new RuntimeException("Producto no encontrado con ID: " + productDto.id()));
                    
                    // Verificar que hay suficiente stock
                    int quantity = productDto.quantity() > 0 ? productDto.quantity() : 1;
                    if (product.getStock() < quantity) {
                        throw new RuntimeException("No hay suficiente stock para el producto: " + product.getName() + 
                            ". Stock disponible: " + product.getStock() + ", Cantidad solicitada: " + quantity);
                    }
                    
                    // Establecer la cantidad especificada en el ProductDTO
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
        
        // Asegurarse de que el userOrderNumber está establecido
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
        orderRepository.deleteById(id);
    }
}