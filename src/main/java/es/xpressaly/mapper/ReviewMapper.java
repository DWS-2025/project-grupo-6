package es.xpressaly.mapper;

import es.xpressaly.Model.Review;
import es.xpressaly.dto.ReviewDTO;
import org.springframework.stereotype.Component;

@Component
public class ReviewMapper {
    
    public ReviewDTO toDTO(Review review) {
        return new ReviewDTO(
            review.getId(),
            review.getComment(),
            review.getRating(),
            review.getUser().getId(),
            review.getProduct().getId(),
            review.getUser().getFirstName() + " " + review.getUser().getLastName(),
            review.getProduct().getName()
        );
    }
    
    public Review toEntity(ReviewDTO dto) {
        Review review = new Review();
        review.setId(dto.id());
        review.setComment(dto.comment());
        review.setRating(dto.rating());
        return review;
    }
}