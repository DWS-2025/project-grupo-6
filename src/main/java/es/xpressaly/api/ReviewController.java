package es.xpressaly.api;

import java.security.Principal;
import java.util.Map;

import es.xpressaly.Service.UserService;
import es.xpressaly.dto.UserWebDTO;
import org.apache.tomcat.util.net.openssl.ciphers.Authentication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import es.xpressaly.Model.Review;
import es.xpressaly.Model.User;
import es.xpressaly.Model.UserRole;
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

    @Autowired
    private UserService userService;

    @GetMapping("/all")
    public Collection<ReviewApiDTO> getAllReviews() {
        return reviewService.getAllAPIReviews();
    }

    @GetMapping("/{id}")
    public ReviewApiDTO getReviewById(@PathVariable Long id) {
        return reviewService.getReviewAPIById(id);
    }

    @PostMapping
    public ResponseEntity<ReviewApiDTO> createReview(@RequestBody ReviewApiDTO reviewApiDTO) {
        try {
            ReviewApiDTO createdReview = reviewService.saveAPIReview(reviewApiDTO);
            return new ResponseEntity<ReviewApiDTO>(createdReview, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
    }

    @DeleteMapping("/{reviewId}")
    @PreAuthorize("hasRole('ADMIN') or @reviewSecurityService.isReviewOwner(#reviewId, authentication.name)")
    public ResponseEntity<?> deleteReview(@PathVariable Long reviewId) {
        try {
            // Obtener username del contexto de seguridad (forma estándar)
            UserWebDTO user=userService.getUser();
            if(user==null){
                return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
            }
            String username = user.email();
            User userReview=userService.getUserByEmail(username);
            
            if(user.id()!=userReview.getId()&&user.role()!=UserRole.ADMIN){
                return new ResponseEntity<>(HttpStatus.FORBIDDEN);
            }

            ReviewApiDTO deletedReview = reviewService.deleteAPIReview(reviewId, username);
            return ResponseEntity.ok(deletedReview);
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                    Map.of("error", "Acceso denegado"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
}