package es.xpressaly.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import es.xpressaly.Model.Order;
import es.xpressaly.Model.Product;
import es.xpressaly.Model.User;
import es.xpressaly.Repository.OrderRepository;
import es.xpressaly.Repository.ProductRepository;
import es.xpressaly.dto.OrderDTO;
import es.xpressaly.dto.ProductWebDTO;
import es.xpressaly.dto.UserWebDTO;
import es.xpressaly.mapper.OrderMapper;
import es.xpressaly.mapper.ProductWebMapper;
import es.xpressaly.mapper.UserWebMapper;
import jakarta.transaction.Transactional;

import java.util.Collection;
import java.util.List;

@Service
@Transactional
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserWebMapper userWebMapper;

    @Autowired
    private OrderMapper orderWebMapper;

    @Autowired
    private ProductWebMapper productWebMapper;

    public OrderDTO createOrderDTO(UserWebDTO user, String address) {
        User userEntity = userWebMapper.toDomain(user);
        Order order = new Order();
        order.setUser(userEntity);
        order.setAddress(address);
        order.setTotal(0.0);
        return orderWebMapper.toDTO(order);
    }

    public List<OrderDTO> getOrdersByUserDTO(UserWebDTO user) {
        User userEntity = userWebMapper.toDomain(user);
        List<Order> orders = orderRepository.findByUser(userEntity);
        return orderWebMapper.toDTOs(orders);
    }

    public Order createOrder(UserWebDTO user, String address) {
        User userEntity = userWebMapper.toDomain(user);
        Order order = new Order();
        order.setUser(userEntity);
        order.setAddress(address);
        order.setTotal(0.0);
        return order;
    }

    public Order getOrderById(Long id) {
        return orderRepository.findById(id).orElse(null);
    }

    public List<Order> getOrdersByUser(UserWebDTO user) {
        User userEntity = userWebMapper.toDomain(user);
        return orderRepository.findByUser(userEntity);
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

    public Order setAddress(Order order, String address) {
        order.setAddress(address);
        return order;
    }

    public Order setUserOrderNumber(Order order, int userOrderNumber) {
        order.setUserOrderNumber(userOrderNumber);
        return order;
    }

    public void save(Order order) {
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
}