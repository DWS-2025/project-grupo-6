package es.xpressaly.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.jaxb.SpringDataJaxb.OrderDto;
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
import jakarta.servlet.http.HttpSession;
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

    public OrderDTO createOrder(UserWebDTO user, String address) {
        User userEntity = userWebMapper.toDomain(user);
        Order order = new Order();
        order.setUser(userEntity);
        order.setAddress(address);
        order.setTotal(0.0);
        return orderWebMapper.toDTO(order);
    }

    public Order getOrderById(Long id) {
        return orderRepository.findById(id).orElse(null);
    }

    public List<Order> getOrdersByUser(User user) {
        return orderRepository.findByUser(user);
    }

    public OrderDTO addProductToOrder(OrderDTO orderDTO, ProductWebDTO productWebDTO, int amount, HttpSession session) {
        Order order = orderWebMapper.toDomain(orderDTO);
        Product product = productWebMapper.toDomain(productWebDTO);

        if (product == null) {
            throw new IllegalArgumentException("Product not found");
        }

        if (product.getStock() < amount) {
            throw new IllegalArgumentException("Insufficient stock");
        }

        order.setProductQuantity(product, amount);
        order.calculateTotal();
        session.setAttribute("order", orderWebMapper.toDTO(order));
        productRepository.save(product);
        return orderWebMapper.toDTO(order);
    }

    public Order removeProductFromOrder(Long orderId, Long productId) {
        Order order = getOrderById(orderId);
        if (order == null) {
            throw new IllegalArgumentException("Order not found");
        }

        Product product = order.findProductById(productId);
        if (product == null) {
            throw new IllegalArgumentException("Product not found in order");
        }

        product.setStock(product.getStock() + product.getAmount());
        order.removeProduct(product);
        order.calculateTotal();

        productRepository.save(product);
        return order;
    }

    public Order updateOrderStatus(Long orderId) {
        Order order = getOrderById(orderId);
        if (order == null) {
            throw new IllegalArgumentException("Order not found");
        }

        return orderRepository.save(order);
    }

    public OrderDTO removeProductFromOrder(OrderDTO orderDTO, ProductWebDTO productWebDTO) {
        Order order = orderWebMapper.toDomain(orderDTO);
        Product product = productWebMapper.toDomain(productWebDTO);
        order.removeProduct(product);
        return orderWebMapper.toDTO(order);
    }

    public OrderDTO clearOrder(OrderDTO orderDTO) {
        Order order = orderWebMapper.toDomain(orderDTO);
        while (order.getProducts().size() > 0) {
            order.removeProduct(order.getProducts().get(0));
        }
        return orderWebMapper.toDTO(order);
    }

    public ProductWebDTO findProductByIdWeb(Long id, OrderDTO orderDTO) {
        Order order = orderWebMapper.toDomain(orderDTO);
        Product product = order.findProductById(id);
        return productWebMapper.toDTO(product);
    }

    public OrderDTO setAddress(OrderDTO orderDTO, String address) {
        Order order = orderWebMapper.toDomain(orderDTO);
        order.setAddress(address);
        return orderWebMapper.toDTO(order);
    }

    public List<OrderDTO> getOrdersByUser(UserWebDTO user) {
        User userEntity = userWebMapper.toDomain(user);
        Collection<Order> orders = orderRepository.findByUser(userEntity);
        return orderWebMapper.toDTOs(orders);
    }

    public OrderDTO setUserOrderNumber(OrderDTO orderDTO, int userOrderNumber) {
        Order order = orderWebMapper.toDomain(orderDTO);
        order.setUserOrderNumber(userOrderNumber);
        return orderWebMapper.toDTO(order);
    }

    public void save(OrderDTO orderDTO) {
        Order order = orderWebMapper.toDomain(orderDTO);
        orderRepository.save(order);
    }

    public OrderDTO setProductQuantity(OrderDTO orderDTO, ProductWebDTO productWebDTO, int amount) {
        Order order = orderWebMapper.toDomain(orderDTO);
        Product product = productWebMapper.toDomain(productWebDTO);
        order.setProductQuantity(product, amount);
        return orderWebMapper.toDTO(order);
    }

    public OrderDTO calculateTotal(OrderDTO orderDTO) {
        Order order = orderWebMapper.toDomain(orderDTO);
        order.calculateTotal();
        return orderWebMapper.toDTO(order);
    }
}