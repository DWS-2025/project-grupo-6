package es.xpressaly.mapper;

import es.xpressaly.Model.Order;
import es.xpressaly.Model.Product;
import es.xpressaly.Model.Review;
import es.xpressaly.Model.User;
import es.xpressaly.dto.OrderDTO;
import es.xpressaly.dto.ProductWebDTO;
import es.xpressaly.dto.ReviewDTO;
import es.xpressaly.dto.UserWebDTO;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import java.util.Collection;
import java.util.List;

@Mapper(componentModel = "spring")
public interface UserWebMapper {
    
    @Mapping(target = "orders", ignore = true)
    UserWebDTO toDTO(User user);
    
    List<UserWebDTO> toDTOs(Collection<User> users);
    
    @Mapping(target = "roles", ignore = true)
    User toDomain(UserWebDTO userWebDTO);

    @Mapping(target = "userOrderNumber", ignore = true)
    Order toDomain(OrderDTO orderDTO);

    @Mapping(target = "amount", ignore = true)
    @Mapping(target = "imagePath", ignore = true)
    @Mapping(target = "imageData", ignore = true)
    @Mapping(target = "reviews", ignore = true)
    @Mapping(target = "rating", ignore = true)
    Product toDomain(ProductWebDTO productWebDTO);


    @Mapping(target = "user", ignore = true)
    @Mapping(target = "product", ignore = true)
    Review toDomain(ReviewDTO reviewDTO);
} 