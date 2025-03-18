package es.xpressaly.Service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import es.xpressaly.Model.Product;

@Service
public class ProductService {

    private static final List<Product> products = new ArrayList<>();
    private static Long nextId = 1L;  // Manual ID generator

    public ProductService() {
        if (products.isEmpty()) {
            initializeDefaultProducts();

        }
    }        
    private void initializeDefaultProducts() {
        addProduct(new Product("Wireless mouse", "2.4G Wireless Bluetooth Mouse Ergonomic 800/1200/1600DPI 6 Silent Buttons for MacBook Tablet Laptops Computer PC", 50, 25, "/Images/Wireless mouse.jpg"));
        addProduct(new Product("Keyboard", "Subblim Business Slim Silent, Spanish Keyboard Layout (QWERTY), Plug & Play via USB, Flat Design, Silent Typing, Black", 40, 2, "/Images/Keyboard.jpg"));
        addProduct(new Product("phone", "Global Version realme C53 Smartphone 6.74'' 90Hz Screen 50MP AI Camera Powerful Octa-core Chipset 33W 5000mAh NFC Mobile Phone", 200, 67, "/Images/phone.jpg"));
        addProduct(new Product("Television", "24 LED television with HD resolution with Dolby system, flash memory", 150, 3, "/Images/Television.jpg"));
        addProduct(new Product("Smartwatch Fitness Pro", "Smartwatch with heart rate monitor, step counter, smartphone notifications, and water resistance. Ideal for athletes.", 120, 15, "/Images/Smartwatch Fitness Pro.jpg"));
        addProduct(new Product("Noise-Cancelling Headphones", "Wireless headphones with noise cancellation, surround sound, and up to 20 hours of battery life. Compatible with Bluetooth 5.0.", 90, 30, "/Images/Noise Cancelling Headphones.jpg"));
        addProduct(new Product("Portable Projector", "Portable projector with 1080p resolution, HDMI and USB connectivity, ideal for movies and presentations at home or the office.", 180, 10, "/Images/Portable Projector.jpg"));
        addProduct(new Product("Electric Kettle", "Stainless steel electric kettle with 1.7-liter capacity, automatic shut-off, and 360Â° swivel base.", 35, 50, "/Images/Electric Kettle.jpg"));
        addProduct(new Product("Robot Vacuum Cleaner", "Smart robot vacuum with laser navigation, room mapping, and app control.", 250, 12, "/Images/Robot Vacuum Cleaner.jpg"));
        addProduct(new Product("Bluetooth Speaker", "Portable Bluetooth speaker with stereo sound, water resistance, and up to 15 hours of playback.", 60, 40, "/Images/Bluetooth Speaker.jpg"));
        addProduct(new Product("Gaming Chair", "Ergonomic gaming chair with lumbar support, adjustable headrest, and sturdy metal base.", 150, 8, "/Images/Gaming Chair.jpg"));
        addProduct(new Product("External Hard Drive", "2TB external hard drive, USB 3.0, compatible with PC and Mac, compact and shock-resistant design.", 80, 25, "/Images/External Hard Drive.jpg"));
        addProduct(new Product("Air Purifier", "Air purifier with HEPA filter, 3 speed levels, and silent night mode for clean and healthy environments.", 130, 18, "/Images/Air Purifier.jpg"));
        addProduct(new Product("Electric Scooter", "Foldable electric scooter with 25 km range, top speed of 25 km/h, and LED display.", 300, 5, "/Images/Electric Scooter.jpg"));
        addProduct(new Product("Wireless Charging Pad", "Qi-compatible wireless charging pad, compact design, and fast charging.", 25, 60, "/Images/Wireless Charging Pad.jpg"));
        addProduct(new Product("Smart Bulb", "App-controllable smart bulb with 16 million colors, programmable, and compatible with Alexa and Google Home.", 20, 100, "/Images/Smart Bulb.jpg"));
        addProduct(new Product("Laptop Cooling Pad", "Laptop cooling pad with 4 silent fans, height adjustment, and blue LED light.", 30, 35, "/Images/Laptop Cooling Pad.jpg"));
        addProduct(new Product("Fitness Tracker Band", "Activity tracker band with sleep monitor, step counter, notifications, and water resistance.", 50, 45, "/Images/Fitness Tracker Band.jpg"));
        addProduct(new Product("Electric Toothbrush", "Rechargeable electric toothbrush with 3 cleaning modes, timer, and replaceable brush head.", 40, 70, "/Images/Electric Toothbrush.jpg"));
        addProduct(new Product("Desk Organizer", "Desk organizer with compartments for pens, phone, cards, and cables. Modern wood design.", 25, 20, "/Images/Desk Organizer.jpg"));
        addProduct(new Product("USB-C Hub", "USB-C hub with HDMI, USB 3.0, SD card reader, and fast charging. Compatible with MacBook and other USB-C devices.", 35, 50, "/Images/USB-C Hub.jpg"));
        addProduct(new Product("E-Reader", "E-reader with anti-glare screen, built-in light, and storage for thousands of books.", 110, 22, "/Images/E-Reader.jpg"));
        addProduct(new Product("Mini Drone", "Mini drone with HD camera, app control, headless mode, and 10-minute flight time. Ideal for beginners.", 70, 15, "/Images/Mini Drone.jpg"));
        addProduct(new Product("Electric Blanket", "Electric blanket with 3 heat levels, automatic shut-off, and soft, washable fabric.", 55, 30, "/Images/Electric Blanket.jpg"));
    }

    public List<Product> getAllProducts() {
        return products;
    }

    public List<Product> getProductsByPage(int page, int pageSize, String sortOrder) {
        List<Product> sortedProducts = new ArrayList<>(products);
        
        // Apply sorting if specified
        if (sortOrder != null) {
            switch (sortOrder) {
                case "price_asc":
                    sortedProducts.sort((p1, p2) -> Double.compare(p1.getPrice(), p2.getPrice()));
                    break;
                case "price_desc":
                    sortedProducts.sort((p1, p2) -> Double.compare(p2.getPrice(), p1.getPrice()));
                    break;
                default:
                    // No sorting needed
                    break;
            }
        }

        int startIndex = (page - 1) * pageSize;
        int endIndex = Math.min(startIndex + pageSize, sortedProducts.size());
        
        if (startIndex >= sortedProducts.size()) {
            return new ArrayList<>();
        }
        
        return new ArrayList<>(sortedProducts.subList(startIndex, endIndex));
    }

    public List<Product> searchProducts(String query) {
        if (query == null || query.trim().isEmpty()) {
            return products;
        }
        
        String searchQuery = query.toLowerCase();
        return products.stream()
            .filter(p -> p.getName().toLowerCase().contains(searchQuery) ||
                    p.getDescription().toLowerCase().contains(searchQuery))
            .toList();
    }
    public Product getProductById(Long id) {
        return products.stream().filter(p -> p.getId().equals(id)).findFirst().orElse(null);
    }

    public void addProduct(Product product) {
        validateProduct(product);
        product.setId(nextId++);  // We assign a unique ID
        products.add(product);
    }

    private void validateProduct(Product product) {
        if (product.getName() == null || product.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Product name cannot be empty");
        }
        if (product.getName().length() > 100) {
            throw new IllegalArgumentException("Product name cannot exceed 100 characters");
        }
        if (product.getDescription() == null || product.getDescription().trim().isEmpty()) {
            throw new IllegalArgumentException("Product description cannot be empty");
        }
        if (product.getPrice() < 0) {
            throw new IllegalArgumentException("Product price cannot be negative");
        }
        if (product.getStock() < 0) {
            throw new IllegalArgumentException("Product stock cannot be negative");
        }
        if (product.getImagePath() == null || product.getImagePath().trim().isEmpty()) {
            throw new IllegalArgumentException("Product image path cannot be empty");
        }
    }

    public void deleteProduct(Long productId) {
        products.removeIf(p -> p.getId().equals(productId));  // Delete the product by its ID
    }
}
