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
}
