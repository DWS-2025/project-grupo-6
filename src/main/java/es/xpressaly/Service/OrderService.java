package es.xpressaly.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import es.xpressaly.Model.Order;
import es.xpressaly.Model.Product;
import es.xpressaly.Model.User;
import es.xpressaly.Repository.OrderRepository;
import es.xpressaly.Repository.ProductRepository;
import jakarta.transaction.Transactional;
import java.util.List;

@Service
@Transactional
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ProductRepository productRepository;

    public Order createOrder(User user, String address) {
        Order order = new Order(user, address);
        return orderRepository.save(order);
    }

    public Order getOrderById(Long id) {
        return orderRepository.findById(id).orElse(null);
    }

    public List<Order> getOrdersByUser(User user) {
        return orderRepository.findByUser(user);
    }

    public Order addProductToOrder(Long orderId, Long productId, int amount) {
        Order order = getOrderById(orderId);
        if (order == null) {
            throw new IllegalArgumentException("Order not found");
        }

        Product product = productRepository.findById(productId).orElse(null);
        if (product == null) {
            throw new IllegalArgumentException("Product not found");
        }

        if (product.getStock() < amount) {
            throw new IllegalArgumentException("Insufficient stock");
        }

        product.setAmount(amount);
        product.setStock(product.getStock() - amount);
        order.addProduct(product);
        order.calculateTotal();

        productRepository.save(product);
        return orderRepository.save(order);
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
        return orderRepository.save(order);
    }

    public Order updateOrderStatus(Long orderId) {
        Order order = getOrderById(orderId);
        if (order == null) {
            throw new IllegalArgumentException("Order not found");
        }

        return orderRepository.save(order);
    }

}