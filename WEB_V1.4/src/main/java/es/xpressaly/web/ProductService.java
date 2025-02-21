import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ProductService {
    private final List<Product> products = new ArrayList<>();
    private Long nextId = 1L; // ID autoincremental

    public ProductService() {
        // Productos iniciales
        products.add(new Product(nextId++, "Laptop", "Laptop potente", 999.99, 10, "laptop.jpg", List.of("laptop1.jpg", "laptop2.jpg")));
        products.add(new Product(nextId++, "Smartphone", "Última generación", 799.99, 20, "phone.jpg", List.of("phone1.jpg", "phone2.jpg")));
    }

    // Obtener todos los productos
    public List<Product> getAllProducts() {
        return products;
    }

    // Agregar un nuevo producto
    public Product addProduct(Product product) {
        product.setId(nextId++); // Asigna un ID único
        products.add(product);
        return product;
    }

    // Modificar un producto existente
    public Product updateProduct(Long id, Product updatedProduct) {
        Optional<Product> existingProduct = products.stream().filter(p -> p.getId().equals(id)).findFirst();
        if (existingProduct.isPresent()) {
            Product product = existingProduct.get();
            product.setName(updatedProduct.getName());
            product.setDescription(updatedProduct.getDescription());
            product.setPrice(updatedProduct.getPrice());
            product.setStock(updatedProduct.getStock());
            product.setMainImage(updatedProduct.getMainImage());
            product.setSecondaryImages(updatedProduct.getSecondaryImages());
            return product;
        }
        return null; // Producto no encontrado
    }

    // Eliminar un producto por ID
    public boolean deleteProduct(Long id) {
        return products.removeIf(p -> p.getId().equals(id));
    }
}
