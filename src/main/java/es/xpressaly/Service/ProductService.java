package es.xpressaly.Service;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import es.xpressaly.Model.Product;
import es.xpressaly.Model.Review;
import es.xpressaly.Repository.ProductRepository;
import jakarta.transaction.Transactional;

@Service
@Transactional
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public Page<Product> getProductsByPage(int page, int size, String sortOrder) {
        Pageable pageable = PageRequest.of(page - 1, size);
        
        if (sortOrder != null) {
            switch (sortOrder) {
                case "price_asc":
                    return productRepository.findAllByPriceAsc(pageable);
                case "price_desc":
                    return productRepository.findAllByPriceDesc(pageable);
                default:
                    return productRepository.findAll(pageable);
            }
        }
        
        return productRepository.findAll(pageable);
    }

    public Page<Product> getProductsByPageAndPrice(int page, int size, String sortOrder, double minPrice, double maxPrice) {
        Pageable pageable = PageRequest.of(page - 1, size);
        
        if (sortOrder != null) {
            switch (sortOrder) {
                case "price_asc":
                    return productRepository.findByPriceBetweenOrderByPriceAsc(minPrice, maxPrice, pageable);
                case "price_desc":
                    return productRepository.findByPriceBetweenOrderByPriceDesc(minPrice, maxPrice, pageable);
                default:
                    return productRepository.findByPriceBetween(minPrice, maxPrice, pageable);
            }
        }
        
        return productRepository.findByPriceBetween(minPrice, maxPrice, pageable);
    }

    public List<Product> searchProducts(String query) {
        if (query == null || query.trim().isEmpty()) {
            return productRepository.findAll();
        }
        
        String searchQuery = query.toLowerCase();
        return productRepository.findAll().stream()
            .filter(p -> p.getName().toLowerCase().contains(searchQuery) ||
                    p.getDescription().toLowerCase().contains(searchQuery))
            .toList();
    }

    public List<Product> searchProductsByPrice(String query, double minPrice, double maxPrice) {
        if (query == null || query.trim().isEmpty()) {
            return productRepository.findByPriceBetween(minPrice, maxPrice, PageRequest.of(0, Integer.MAX_VALUE))
                   .getContent();
        }
        
        String searchQuery = query.toLowerCase();
        return productRepository.findAll().stream()
            .filter(p -> (p.getName().toLowerCase().contains(searchQuery) ||
                         p.getDescription().toLowerCase().contains(searchQuery)) &&
                         p.getPrice() >= minPrice && p.getPrice() <= maxPrice)
            .toList();
    }

    public Product getProductById(Long id) {
        return productRepository.findById(id).orElse(null);
    }

    public void addProduct(Product product) {
        validateProduct(product);
        productRepository.save(product);
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
        productRepository.deleteById(productId);
    }

    public Product getProductWithReviews(Long id) {
        return productRepository.findProductWithReviews(id);
    }

    public double getAverageRating(Long productId) {
        Product product = getProductWithReviews(productId);
        return product.getReviews().stream()
            .mapToDouble(Review::getRating)
            .average()
            .orElse(0.0);
    }

    public double getMaxProductPrice() {
        List<Product> allProducts = productRepository.findAll();
        return allProducts.stream()
                .mapToDouble(Product::getPrice)
                .max()
                .orElse(1000.0); // Default value if no products exist
    }

    public double getMaxPriceForFilter() {
        double maxPrice = getMaxProductPrice();
        double minPrice = productRepository.findAll().stream()
                .mapToDouble(Product::getPrice)
                .min()
                .orElse(0.0);
                
        // Si la diferencia entre precio máximo y mínimo es pequeña, 
        // añadir un porcentaje en lugar de un valor fijo
        double priceDifference = maxPrice - minPrice;
        if (priceDifference < 50) {
            // Para rangos pequeños, añadir un 20% por encima y por debajo
            return maxPrice * 1.2;
        } else {
            // Para rangos grandes, mantener el comportamiento original
            return maxPrice + 100.0;
        }
    }
}
