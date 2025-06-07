package es.xpressaly.mapper;

import es.xpressaly.Model.Order;
import es.xpressaly.Model.Review;
import es.xpressaly.Model.User;
import es.xpressaly.dto.UserDTO;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.Collection;
import java.util.Collections;

@Mapper(componentModel = "spring", uses = {OrderMapper.class, ReviewMapper.class})
public interface UserMapper {
    
    @Mapping(target = "orders", source = "orders", qualifiedByName = "ordersToIds")
    @Mapping(target = "reviews", source = "reviews", qualifiedByName = "reviewsToIds")
    UserDTO toDTO(User user);
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "role", ignore = true)
    @Mapping(target = "currentOrder", ignore = true)
    @Mapping(target = "orders", ignore = true)
    @Mapping(target = "reviews", ignore = true)
    User toDomain(UserDTO userDTO);

    List<UserDTO> toDTOs(Collection<User> users);

    @Named("ordersToIds")
    default List<Long> mapOrdersToIds(List<Order> orders) {
        if (orders == null) {
            return Collections.emptyList();
        }
        return orders.stream().map(Order::getId).collect(Collectors.toList());
    }

    @Named("reviewsToIds")
    default List<Long> mapReviewsToIds(List<Review> reviews) {
        if (reviews == null) {
            return Collections.emptyList();
        }
        return reviews.stream().map(Review::getId).collect(Collectors.toList());
    }
}