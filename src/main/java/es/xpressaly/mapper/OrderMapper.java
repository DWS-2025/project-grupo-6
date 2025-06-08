package es.xpressaly.mapper;

import es.xpressaly.Model.Order;
import es.xpressaly.Model.Product;
import es.xpressaly.Model.User;
import es.xpressaly.Model.OrderProduct;
import es.xpressaly.dto.OrderDTO;
import es.xpressaly.dto.ProductDTO;
import es.xpressaly.dto.ProductWebDTO;
import es.xpressaly.dto.UserWebDTO;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.AfterMapping;
import org.mapstruct.MappingTarget;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;

@Mapper(componentModel = "spring")
public interface OrderMapper {
    
    @Mapping(target = "quantities", expression = "java(getProductQuantities(order))")
    @Mapping(target = "products", expression = "java(getProducts(order))")
    OrderDTO toDTO(Order order);

    List<OrderDTO> toDTOs(Collection<Order> orders);

    @Mapping(target = "products", ignore = true)
    @Mapping(target = "userOrderNumber", ignore = true)
    Order toDomain(OrderDTO dto);

    @Mapping(target = "imageData", ignore = true)
    @Mapping(target = "amount", ignore = true)
    @Mapping(target = "reviews", ignore = true)
    @Mapping(target = "imagePath", ignore = true)
    @Mapping(target = "rating", ignore = true)
    Product toDomain(ProductWebDTO productWebDTO);

    @Mapping(target = "orders", ignore = true)
    @Mapping(target = "role", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "address", ignore = true)
    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "reviews", ignore = true)
    @Mapping(target = "currentOrder", ignore = true)
    User toDomain(UserWebDTO userWebDTO);

    default Map<Long, Integer> getProductQuantities(Order order) {
        Map<Long, Integer> quantities = new HashMap<>();
        if (order != null && order.getProducts() != null) {
            for (Product product : order.getProducts()) {
                quantities.put(product.getId(), order.getProductQuantity(product));
            }
        }
        return quantities;
    }

    default List<ProductWebDTO> getProducts(Order order) {
        List<ProductWebDTO> products = new ArrayList<>();
        if (order != null && order.getProducts() != null) {
            for (Product product : order.getProducts()) {
                ProductWebDTO productDTO = toProductWebDTO(product);
                products.add(productDTO);
            }
        }
        return products;
    }

    private ProductWebDTO toProductWebDTO(Product product) {
        return new ProductWebDTO(
            product.getId(),
            product.getName(),
            product.getDescription(),
            product.getPrice(),
            product.getStock(),
            product.getImagePath(),
            product.getImageData(),
            product.getReturnPolicyPath(),
            product.getReturnPolicyData(),
            product.getReviews(),
            product.getRating()
        );
    }
}