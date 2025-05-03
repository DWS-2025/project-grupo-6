package es.xpressaly.mapper;

import es.xpressaly.Model.Product;
import es.xpressaly.dto.ProductDTO;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class ProductMapper {
    
    public ProductDTO toDTO(Product product) {
        return new ProductDTO(
            product.getId(),
            product.getName(),
            product.getDescription(),
            product.getPrice(),
            product.getStock(),
            null // reviews se asignarían en otro lugar si es necesario
        );
    }
    
    public Product toEntity(ProductDTO dto) {
        Product product = new Product();
        product.setId(dto.id());
        product.setName(dto.name());
        product.setDescription(dto.description());
        product.setPrice(dto.price());
        product.setStock(dto.stock());
        return product;
    }
}