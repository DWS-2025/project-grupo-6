@Entity
public class Review {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    private User user;
    @ManyToOne
    private Product product;
    private String comment;
    private int rating;
    @ElementCollection
    private List<String> images;

    public Review() {}

    public Review(User user, Product product, String comment, int rating, List<String> images) {
        this.user = user;
        this.product = product;
        this.comment = comment;
        this.rating = rating;
        this.images = images;
    }
    
    //Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public Product getProduct() { return product; }
    public void setProduct(Product product) { this.product = product; }
    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }
    public int getRating() { return rating; }
    public void setRating(int rating) { this.rating = rating; }
    public List<String> getImages() { return images; }
    public void setImages(List<String> images) { this.images = images; }

    
}

module.exports = Review;