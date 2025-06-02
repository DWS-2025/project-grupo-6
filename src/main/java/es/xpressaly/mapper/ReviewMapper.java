package es.xpressaly.mapper;

import es.xpressaly.Model.Product;
import es.xpressaly.Model.Review;
import es.xpressaly.Model.User;
import es.xpressaly.dto.ProductDTO;
import es.xpressaly.dto.ReviewDTO;
import es.xpressaly.dto.UserDTO;

import java.util.Collection;
import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;


@Mapper(componentModel = "spring")
public interface ReviewMapper {
    
    @Mapping(target = "user", ignore = true)
    ReviewDTO toDTO(Review review);

    @Mapping(target = "user", ignore = true)
    @Mapping(target = "product", ignore = true)
    Review toDomain(ReviewDTO dto);

    List<ReviewDTO> toDTOs(Collection<Review> reviews);

    @Mapping(target = "orders", ignore = true)
    @Mapping(target = "reviews", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "role", ignore = true)
    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "currentOrder", ignore = true)
    User toDomain(UserDTO userDTO);

    @Mapping(target = "amount", ignore = true)
    @Mapping(target = "reviews", ignore = true)
    @Mapping(target = "imageData", ignore = true)
    @Mapping(target = "imagePath", ignore = true)
    @Mapping(target = "rating", ignore = true)
    @Mapping(target = "returnPolicyData", ignore = true)
    @Mapping(target = "returnPolicyPath", ignore = true)
    Product toDomain(ProductDTO productDTO);
}