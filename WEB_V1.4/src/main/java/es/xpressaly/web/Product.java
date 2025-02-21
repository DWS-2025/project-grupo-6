@Entity
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String description;
    private double price;
    private int stock;
    private String mainImage;
    @ElementCollection
    private List<String> secondaryImages;

    public Product() {}

    public Product(String name, String description, double price, int stock, String mainImage, List<String> secondaryImages) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.stock = stock;
        this.mainImage = mainImage;
        this.secondaryImages = secondaryImages;
    }

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
    public String getMainImage() { return mainImage; }
    public void setMainImage(String mainImage) { this.mainImage = mainImage; }
    public List<String> getSecondaryImages() { return secondaryImages; }
    public void setSecondaryImages(List<String> secondaryImages) { this.secondaryImages = secondaryImages; }
}

