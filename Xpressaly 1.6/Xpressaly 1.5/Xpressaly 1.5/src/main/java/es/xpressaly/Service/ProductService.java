package es.xpressaly.Service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import es.xpressaly.Model.Product;

@Service
public class ProductService {

    private static final List<Product> products = new ArrayList<>();
    private static Long nextId = 1L;  // Generador de ID manual

    public ProductService() {
        if (products.isEmpty()) {
            initializeDefaultProducts();
        }
    }

    private void initializeDefaultProducts() {
        addProduct(new Product("Wireless mouse", "2.4G Wireless Bluetooth Mouse Ergonomic 800/1200/1600DPI 6 Silent Buttons for MacBook Tablet Laptops Computer PC", 50, 25, "/Images/wireless_mouse.jpg"));
        addProduct(new Product("Keyboard", "Subblim Business Slim Silent, Spanish Keyboard Layout (QWERTY), Plug & Play via USB, Flat Design, Silent Typing, Black", 40, 2, "/Images/keyboard.jpg"));
        addProduct(new Product("Phone", "Global Version realme C53 Smartphone 6.74'' 90Hz Screen 50MP AI Camera Powerful Octa-core Chipset 33W 5000mAh NFC Mobile Phone", 200, 67, "/Images/phone.jpg"));
        addProduct(new Product("Television", "24 LED television with HD resolution with Dolby system, flash memory", 150, 3, "/Images/television.jpg"));
    }

    public List<Product> getAllProducts() {
        return products;
    }

    public List<Product> getProductsByPage(int page, int pageSize) {
        int startIndex = (page - 1) * pageSize;
        int endIndex = Math.min(startIndex + pageSize, products.size());
        
        if (startIndex >= products.size()) {
            return new ArrayList<>();
        }
        
        return new ArrayList<>(products.subList(startIndex, endIndex));
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
        product.setId(nextId++);  // Asignamos un ID único
        products.add(product);
    }

    public void deleteProduct(Long productId) {
        products.removeIf(p -> p.getId().equals(productId));  // Elimina el producto por su ID
    }
}
