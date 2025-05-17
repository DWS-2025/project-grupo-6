package es.xpressaly.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import es.xpressaly.Model.Review;
import es.xpressaly.Service.ReviewService;
import es.xpressaly.dto.ReviewDTO;
import es.xpressaly.mapper.ReviewMapper;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/reviews")
public class ReviewController {

    @Autowired
    private ReviewService reviewService;

    @GetMapping
    public Collection<ReviewDTO> getAllReviews() {
        return reviewService.getAllReviews();          
    }

    @GetMapping("/{id}")
    public ReviewDTO getReviewById(@PathVariable Long id) {
        return reviewService.getReviewById(id);
    }

    @PostMapping
    public ReviewDTO createReview(@RequestBody ReviewDTO reviewDTO) {
        return reviewService.saveReview(reviewDTO);
    }

    @PutMapping("/{id}")
    public ReviewDTO updateReview(@PathVariable Long id, @RequestBody ReviewDTO reviewDTO) {
        ReviewDTO existingReview = reviewService.getReviewById(id);
        reviewService.setComment(reviewDTO.comment(), existingReview);
        reviewService.setRating(reviewDTO.rating(), existingReview);
        reviewService.saveReview(existingReview);
        return existingReview;
    }

    @DeleteMapping("/{productId}/reviews/{reviewId}")
    public void deleteReview(@PathVariable Long productId, @PathVariable Long reviewId) {
        reviewService.deleteReview(productId, reviewId);
    }
}