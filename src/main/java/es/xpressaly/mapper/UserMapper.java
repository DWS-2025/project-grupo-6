package es.xpressaly.mapper;

import es.xpressaly.Model.Order;
import es.xpressaly.Model.Review;
import es.xpressaly.Model.User;
import es.xpressaly.dto.OrderDTO;
import es.xpressaly.dto.ReviewDTO;
import es.xpressaly.dto.UserDTO;

import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import java.util.Collection;

@Mapper(componentModel = "spring")
public interface UserMapper {
    
    UserDTO toDTO(User user);
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "role", ignore = true)
    User toDomain(UserDTO userDTO);

    List<UserDTO> toDTOs(Collection<User> users);
    
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "products", ignore = true)
    @Mapping(target = "userOrderNumber", ignore = true)
    Order toDomain(OrderDTO orderDTO);

    @Mapping(target = "user", ignore = true)
    @Mapping(target = "product", ignore = true)
    Review toDomain(ReviewDTO reviewDTO);
}