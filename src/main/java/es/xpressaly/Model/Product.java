package es.xpressaly.Model;

import java.util.ArrayList;
import java.util.List;
import jakarta.persistence.*;
import jakarta.transaction.Transactional;

@Entity
@Table(name = "products")
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(length = 1000)
    private String description;

    @Column(nullable = false)
    private double price;

    @Column(nullable = false)
    private int stock;

    private String imagePath;
    
    @Lob
    @Column(name = "image_data", columnDefinition = "MEDIUMBLOB")
    private byte[] imageData;

    @Column(name = "rating", nullable = false)
    private double rating = 0.0;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Review> reviews; // List of comments for each product

    private String returnPolicyPath; // Path to Return Policy PDF
    @Lob
    @Column(name = "return_policy_data", columnDefinition = "MEDIUMBLOB")
    private byte[] returnPolicyData; // PDF Data

    public Product() {
        // default constructor
        this.reviews = new ArrayList<>();  // Initialize reviews list in default constructor
        this.rating = 0.0; // Initialize rating
    }

    public Product(String name, String description, double price, int stock, String imagePath ) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.stock = stock;
        this.imagePath = imagePath;
        this.reviews = new ArrayList<>();  // We initialize the list of reviews
        this.rating = 0.0; // Initialize rating
    }
    
    public void setName(String name) { this.name = name; }
    public void setDescription(String description) { this.description = description; }
    public void setPrice(double price) { this.price = price; }
    public void setRating(double rating) { this.rating = rating; }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public String getDescription() { return description; }
    public double getPrice() { return price; }
    public int getStock() { return stock; }
    public void setStock(int stock){this.stock=stock;}
    public String getImagePath() { return imagePath; }
    public void setImagePath(String imagePath) { this.imagePath = imagePath; }
    public byte[] getImageData() { return imageData; }
    public void setImageData(byte[] imageData) { this.imageData = imageData; }
    public List<Review> getReviews() { return reviews; }
    public void setReviews(List<Review> reviews) { this.reviews = reviews; }
    public double getRating() { return rating; }

    public void addReview(Review review) {
        this.reviews.add(review);
    }

    @Transient // This field is not stored in the database
    private int amount;
    public int getAmount(){return amount;}
    public void setAmount(int amount){this.amount=amount;}

    public String getReturnPolicyPath() {
        return returnPolicyPath;
    }

    public void setReturnPolicyPath(String returnPolicyPath) {
        this.returnPolicyPath = returnPolicyPath;
    }

    public byte[] getReturnPolicyData() {
        return returnPolicyData;
    }

    public void setReturnPolicyData(byte[] returnPolicyData) {
        this.returnPolicyData = returnPolicyData;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Product product = (Product) obj;
        return id != null && id.equals(product.getId());
    }
    
    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}
