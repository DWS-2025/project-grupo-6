package es.xpressaly.api;

import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import es.xpressaly.Model.Review;
import es.xpressaly.Service.ReviewService;
import es.xpressaly.dto.ReviewApiDTO;
import es.xpressaly.dto.ReviewDTO;
import es.xpressaly.mapper.ReviewMapper;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/reviews")
public class ReviewController {

    @Autowired
    private ReviewService reviewService;

    @GetMapping("/all")
    public Collection<ReviewApiDTO> getAllReviews() {
        return reviewService.getAllAPIReviews();          
    }

    @GetMapping("/{id}")
    public ReviewApiDTO getReviewById(@PathVariable Long id) {
        return reviewService.getReviewAPIById(id);
    }

    @PostMapping
    public ResponseEntity<?> createReview(@RequestBody ReviewApiDTO reviewApiDTO) {
        try {
            ReviewApiDTO createdReview = reviewService.saveAPIReview(reviewApiDTO);
            return ResponseEntity.ok(createdReview);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(
                Map.of(
                    "error", e.getMessage(),
                    "timestamp", LocalDateTime.now()
                )
            );
        }
    }

    @DeleteMapping("/{reviewId}")
    @PreAuthorize("hasRole('ADMIN') or @reviewSecurityService.isReviewOwner(#reviewId, authentication.name)")
    public ReviewApiDTO deleteReview(@PathVariable Long reviewId) {
        return reviewService.deleteAPIReview(reviewId);
    }
}