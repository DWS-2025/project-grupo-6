package es.xpressaly.dto;

import java.util.List;
import java.util.Map;

public class ProductDTO {
    private Long id;
    private String name;
    private String description;
    private double price;
    private int stock;
    private List<Map<String, Object>> reviews;

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public int getStock() { return stock; }
    public void setStock(int stock) { this.stock = stock; }

    public List<Map<String, Object>> getReviews() { return reviews; }
    public void setReviews(List<Map<String, Object>> reviews) { this.reviews = reviews; }
}