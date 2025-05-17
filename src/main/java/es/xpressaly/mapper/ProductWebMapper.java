package es.xpressaly.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import es.xpressaly.Model.Product;
import es.xpressaly.dto.ProductWebDTO;
import java.util.List;

@Mapper(componentModel = "spring")
public interface ProductWebMapper {

    @Mapping(target = "rating", source = "rating")
    ProductWebDTO toDTO(Product product);

    List<ProductWebDTO> toDTOs(List<Product> products);

    @Mapping(target = "reviews", ignore = true)
    @Mapping(target = "amount", ignore = true)
    @Mapping(target = "rating", source = "rating")
    Product toDomain(ProductWebDTO dto);
} 