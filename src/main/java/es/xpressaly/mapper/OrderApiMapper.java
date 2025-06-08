package es.xpressaly.mapper;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Collections;

import org.mapstruct.Mapping;
import org.mapstruct.Mapper;

import es.xpressaly.Model.Order;
import es.xpressaly.dto.OrderApiDTO;
import es.xpressaly.dto.ProductDTO;

@Mapper(componentModel = "spring")
public interface OrderApiMapper {
    
    @Mapping(target = "userId", expression = "java(order.getUser().getId())")
    @Mapping(target = "products", expression = "java(getProducts(order))")
    OrderApiDTO toDTO(Order order);

    List<OrderApiDTO> toDTOs(Collection<Order> orders);
    
    @Mapping(target = "products", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "userOrderNumber", ignore = true) 
    @Mapping(target = "quantities", ignore = true)
    Order toDomain(OrderApiDTO dto);

    default List<ProductDTO> getProducts(Order order) {
        if (order == null || order.getProducts() == null) {
            return Collections.emptyList();
        }
        return order.getProducts().stream()
            .map(product -> new ProductDTO(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getStock(),
                order.getProductQuantity(product)
                // No incluir más relaciones
            ))
            .collect(Collectors.toList());
    }
}
