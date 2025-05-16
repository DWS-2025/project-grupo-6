package es.xpressaly.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import es.xpressaly.Model.Product;
import es.xpressaly.dto.ProductWebDTO;
import java.util.List;

@Mapper(componentModel = "spring")
public interface ProductWebMapper {

    @Mapping(target = "averageRating", ignore = true)
    ProductWebDTO toDTO(Product product);

    List<ProductWebDTO> toDTOs(List<Product> products);

    @Mapping(target = "reviews", ignore = true)
    @Mapping(target = "amount", ignore = true)
    Product toDomain(ProductWebDTO dto);
} 