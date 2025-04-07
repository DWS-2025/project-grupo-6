package es.xpressaly.mapper;

import es.xpressaly.Model.Review;
import es.xpressaly.dto.ReviewDTO;
import org.springframework.stereotype.Component;

@Component
public class ReviewMapper {
    
    public ReviewDTO toDTO(Review review) {
        ReviewDTO dto = new ReviewDTO();
        dto.setId(review.getId());
        dto.setComment(review.getComment());
        dto.setRating(review.getRating());
        dto.setUserId(review.getUser().getId());
        dto.setProductId(review.getProduct().getId());
        dto.setUserName(review.getUser().getFirstName() + " " + review.getUser().getLastName());
        dto.setProductName(review.getProduct().getName());
        return dto;
    }
    
    public Review toEntity(ReviewDTO dto) {
        Review review = new Review();
        review.setId(dto.getId());
        review.setComment(dto.getComment());
        review.setRating(dto.getRating());
        return review;
    }
}