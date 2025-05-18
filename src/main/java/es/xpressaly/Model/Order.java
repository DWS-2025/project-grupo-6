package es.xpressaly.Model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import jakarta.persistence.*;

@Entity
@Table(name = "orders")
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderProduct> orderProducts = new ArrayList<>();

    @Column(nullable = false)
    private String address;

    @Column(nullable = false)
    private double total;
    
    @Column(name = "user_order_number")
    private Integer userOrderNumber;

    // Default constructor required by JPA
    public Order() {
    }

    public Order(User user, String address) {
        this.user = user;
        this.address = address;
        this.total = 0.0;   
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public List<Product> getProducts() {
        List<Product> products = new ArrayList<>();
        for (OrderProduct op : orderProducts) {
            Product p = op.getProduct();
            p.setAmount(op.getQuantity());
            products.add(p);
        }
        return products;
    }
    
    public void setProducts(List<Product> products) {
        orderProducts.clear();
        for (Product product : products) {
            addProduct(product);
        }
    }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public void addProduct(Product product) {
        OrderProduct existingOrderProduct = findOrderProduct(product);
        if (existingOrderProduct != null) {
            existingOrderProduct.setQuantity(existingOrderProduct.getQuantity() + 1);
        } else {
            OrderProduct orderProduct = new OrderProduct(this, product, 1);
            orderProducts.add(orderProduct);
        }
    }

    public void removeProduct(Product product) {
        OrderProduct orderProduct = findOrderProduct(product);
        if (orderProduct != null) {
            orderProducts.remove(orderProduct);
        }
    }

    private OrderProduct findOrderProduct(Product product) {
        for (OrderProduct op : orderProducts) {
            if (op.getProduct().getId().equals(product.getId())) {
                return op;
            }
        }
        return null;
    }

    public boolean hasProducts() {
        return !orderProducts.isEmpty();
    }

    public double getTotal() { return total; }
    public void setTotal(double total) { this.total = total; }
    
    public Integer getUserOrderNumber() { return userOrderNumber; }
    public void setUserOrderNumber(Integer userOrderNumber) { this.userOrderNumber = userOrderNumber; }
    
    public void calculateTotal() {
        double calculatedTotal = 0;
        for (OrderProduct op : orderProducts) {
            calculatedTotal += op.getProduct().getPrice() * op.getQuantity();
        }
        this.setTotal(calculatedTotal);
    }

    public Product findProductById(Long id) {
        for (OrderProduct op : orderProducts) {
            if (op.getProduct().getId().equals(id)) {
                return op.getProduct();
            }
        }
        return null;
    }

    public int getProductQuantity(Product product) {
        OrderProduct op = findOrderProduct(product);
        return op != null ? op.getQuantity() : 0;
    }

    public void setProductQuantity(Product product, int quantity) {
        OrderProduct op = findOrderProduct(product);
        if (op != null) {
            if (quantity > 0) {
                op.setQuantity(quantity);
            } else {
                orderProducts.remove(op);
            }
        } else if (quantity > 0) {
            OrderProduct newOp = new OrderProduct(this, product, quantity);
            orderProducts.add(newOp);
        }
    }

    public Map<Long, Integer> getQuantities() {
        Map<Long, Integer> quantities = new HashMap<>();
        for (OrderProduct op : orderProducts) {
            quantities.put(op.getProduct().getId(), op.getQuantity());
        }
        return quantities;
    }
}

