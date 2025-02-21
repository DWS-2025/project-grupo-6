/*@Entity
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    private User user;
    @ManyToOnes
    private Product product;
    private int quantity;
    private Date date;
    private String address;

    public Order() {}

    public Order(User user, Product products, int quantity, Date date, String address) {
        this.user = user;
        this.products = products;
        this.quantity = quantity;
        this.date = date;
        this.address = address;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public Product getProduct() { return product; }
    public void setProduct(Product product) { this.product = product; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public Date getDate() { return date; }
    public void setDate(Date date) { this.date = date; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
}*/
import javax.persistence.*;
import java.util.Date;
import java.util.List;

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
    private List<OrderProduct> orderProducts; //Correct relationship

    @Temporal(TemporalType.TIMESTAMP)
    private Date date;

    private String address;

    public Order() {}

    public Order(User user, Date date, String address) {
        this.user = user;
        this.date = date;
        this.address = address;
    }

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public List<OrderProduct> getOrderProducts() { return orderProducts; }
    public void setOrderProducts(List<OrderProduct> orderProducts) { this.orderProducts = orderProducts; }
    public Date getDate() { return date; }
    public void setDate(Date date) { this.date = date; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
}
