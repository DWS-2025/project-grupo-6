package es.web.xpressaly;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

@Service
public class ProductService {

    private List<Product> products = new ArrayList<>();
    private Long nextId = 1L;  // Generador de ID manual

    public ProductService() {
        addProduct(new Product("raton", "Muy versatil y recomendado", 50, 25, "/teclado.jpg"));
        addProduct(new Product("teclado", "Muy versatil y recomendado", 30, 2, "/teclado.jpg"));
        addProduct(new Product("movil", "Muy versatil y recomendado", 200, 67, "/teclado.jpg"));
        addProduct(new Product("television", "Muy versatil y recomendado", 70, 3, "/teclado.jpg"));
    }

    public List<Product> getAllProducts() {
        return products;
    }

    public Product getProductById(Long id) {
        return products.stream().filter(p -> p.getId().equals(id)).findFirst().orElse(null);
    }

    public void addProduct(Product product) {
        product.setId(nextId++);  // Asignamos un ID único
        products.add(product);
    }
    // Eliminar un producto
    public void deleteProduct(Long productId) {
        products.removeIf(p -> p.getId().equals(productId));  // Elimina el producto por su ID
    }
}
