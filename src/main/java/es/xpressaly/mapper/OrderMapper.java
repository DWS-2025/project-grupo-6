package es.xpressaly.mapper;

import es.xpressaly.Model.Order;
import es.xpressaly.Model.Product;
import es.xpressaly.dto.OrderDTO;
import es.xpressaly.dto.ProductDTO;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import java.util.Collection;
import java.util.List;

@Mapper(componentModel = "spring")
public interface OrderMapper {
    
    OrderDTO toDTO(Order order);

    List<OrderDTO> toDTOs(Collection<Order> orders);

    
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "userOrderNumber", ignore = true)
    Order toDomain(OrderDTO dto);

    @Mapping(target = "imageData", ignore = true)
    @Mapping(target = "amount", ignore = true)
    @Mapping(target = "reviews", ignore = true)
    @Mapping(target = "imagePath", ignore = true)
    Product toDomain(ProductDTO productDTO);
}