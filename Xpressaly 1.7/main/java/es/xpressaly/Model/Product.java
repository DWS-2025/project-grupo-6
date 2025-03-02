package es.xpressaly.Model;

import java.util.ArrayList;
import java.util.List;

public class Product {
    private static Long idCounter = 0L;
    private Long id;
    private String name;
    private String description;
    private double price;
    private int stock;
    private String mainImage;
    private List<Review> reviews; // Lista de comentarios para cada producto

    public Product(String name, String description, double price, int stock, String mainImage) {
        this.id = ++idCounter;
        this.name = name;
        this.description = description;
        this.price = price;
        this.stock = stock;
        this.mainImage = mainImage;
        this.reviews = new ArrayList<>();  // Inicializamos la lista de reviews
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; } // <-- Añadido para poder modificar el ID manualmente

    public String getName() { return name; }
    public String getDescription() { return description; }
    public double getPrice() { return price; }
    public int getStock() { return stock; }
    public void setStock(int stock){this.stock=stock;}
    public String getMainImage() { return mainImage; }
    public List<Review> getReviews() { return reviews; }

    public void addReview(Review review) {
        this.reviews.add(review);
    }

    public double getAverageRating() {
        if (reviews.isEmpty()) {
            return 0.0;
        }
        double avg = reviews.stream()
                .mapToInt(Review::getRating)
                .average()
                .orElse(0.0);
        return Math.round(avg * 10.0) / 10.0;
    }

    private int amount;public int getAmount(){return amount;}
    public void setAmount(int amount){this.amount=amount;}
}
