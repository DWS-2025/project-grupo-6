package es.xpressaly.Model;

import java.util.ArrayList;
import java.util.List;
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

    @ManyToMany
    @JoinTable(
        name = "order_products",
        joinColumns = @JoinColumn(name = "order_id"),
        inverseJoinColumns = @JoinColumn(name = "product_id")
    )
    private List<Product> products;

    @Column(nullable = false)
    private String address;

    @Column(nullable = false)
    private double total;
    
    @Column(name = "user_order_number")
    private Integer userOrderNumber;
    

    // Default constructor required by JPA
    public Order() {
        this.products = new ArrayList<>();
    }

    public Order(User user, String address) {
        this.user = user;
        this.products = new ArrayList<>();
        this.address = address;
        this.total = 0.0;   
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public List<Product> getProducts() { return products; }
    
    public void setProducts(List<Product> products) { this.products = products; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public void addProduct(Product product) {
        this.products.add(product);
    }

    public void removeProduct(Product product) {
        this.products.remove(product);
    }

    public boolean hasProducts(){
        return this.products!=null&&!products.isEmpty();
    }

    public double getTotal() { return total; }
    public void setTotal(double total) { this.total = total; }
    
    public Integer getUserOrderNumber() { return userOrderNumber; }
    public void setUserOrderNumber(Integer userOrderNumber) { this.userOrderNumber = userOrderNumber; }
    
    public void calculateTotal() {
        double calculatedTotal = 0;
        if (this.hasProducts()) {
            for (Product product : this.getProducts()) {
                calculatedTotal += product.getPrice() * product.getAmount();
            }
        }
        this.setTotal(calculatedTotal);
    }

    public Product findProductById(Long id) {
        for (Product product : products) {
            if (product.getId().equals(id)) {
                return product;
            }
        }
        return null;
    }
}

