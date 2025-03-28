package es.xpressaly.Model;

import java.util.List;

public class Review {
    private static Long reviewIdCounter = 0L; // Review ID Generator
    private Long id;
    private String user;  // Instead of using a User entity, we just save the user name
    private String comment;
    private int rating;
    private List<String> images;

    public Review() {}

    public Review(String user, String comment, int rating, List<String> images) {
        this.id = ++reviewIdCounter;  // We assign a unique ID for each review
        this.user = user;
        this.comment = comment;
        this.rating = rating;
        this.images = images;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUser() { return user; }
    public void setUser(String user) { this.user = user; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }

    public int getRating() { return rating; }
    public void setRating(int rating) { this.rating = rating; }

    public List<String> getImages() { return images; }
    public void setImages(List<String> images) { this.images = images; }

    private Product product; // Add product relationship

    public Product getProduct() { return product; }
    public void setProduct(Product product) { this.product = product; }
}
