package es.xpressaly.mapper;

import java.util.Collection;
import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import es.xpressaly.Model.Review;
import es.xpressaly.dto.ReviewApiDTO;

@Mapper(componentModel = "spring")
public interface ReviewAPIMapper {

    ReviewApiDTO toDTO(Review review);

    @Mapping(target = "user", ignore = true)
    @Mapping(target = "product", ignore = true)
    Review toDomain(ReviewApiDTO dto);

    List<ReviewApiDTO> toDTOs(Collection<Review> reviews);
}
