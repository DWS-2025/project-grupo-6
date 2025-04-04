package es.xpressaly.Model;

import java.util.List;
import jakarta.persistence.*;

@Entity
@Table(name = "order_update_responses")
public class OrderUpdateResponse {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private boolean success;

    @Column(nullable = false)
    private double total;

    @ManyToMany
    @JoinTable(
        name = "order_update_response_products",
        joinColumns = @JoinColumn(name = "response_id"),
        inverseJoinColumns = @JoinColumn(name = "product_id")
    )
    private List<Product> products;

    // Default constructor required by JPA
    public OrderUpdateResponse() {}

    // Constructor
    public OrderUpdateResponse(boolean success, double total, List<Product> products) {
        this.success = success;
        this.total = total;
        this.products = products;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public double getTotal() {
        return total;
    }

    public void setTotal(double total) {
        this.total = total;
    }

    public List<Product> getProducts() {
        return products;
    }

    public void setProducts(List<Product> products) {
        this.products = products;
    }
}