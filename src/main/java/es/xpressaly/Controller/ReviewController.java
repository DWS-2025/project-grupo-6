package es.xpressaly.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import es.xpressaly.Model.Review;
import es.xpressaly.Service.ReviewService;
import es.xpressaly.dto.ReviewDTO;
import es.xpressaly.mapper.ReviewMapper;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/reviews")
public class ReviewController {

    @Autowired
    private ReviewService reviewService;

    @Autowired
    private ReviewMapper reviewMapper;

    @GetMapping
    public List<ReviewDTO> getAllReviews() {
        return reviewService.getAllReviews().stream()
                .map(reviewMapper::toDTO)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public ReviewDTO getReviewById(@PathVariable Long id) {
        return reviewMapper.toDTO(reviewService.getReviewById(id));
    }

    @PostMapping
    public ReviewDTO createReview(@RequestBody ReviewDTO reviewDTO) {
        Review review = reviewMapper.toDomain(reviewDTO);
        return reviewMapper.toDTO(reviewService.saveReview(review));
    }

    @PutMapping("/{id}")
    public ReviewDTO updateReview(@PathVariable Long id, @RequestBody ReviewDTO reviewDTO) {
        Review existingReview = reviewService.getReviewById(id);
        existingReview.setComment(reviewDTO.comment());
        existingReview.setRating(reviewDTO.rating());
        return reviewMapper.toDTO(reviewService.saveReview(existingReview));
    }

    @DeleteMapping("/{productId}/reviews/{reviewId}")
    public void deleteReview(@PathVariable Long productId, @PathVariable Long reviewId) {
        reviewService.deleteReview(productId, reviewId);
    }
}