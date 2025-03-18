package es.xpressaly.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import es.xpressaly.Model.Review;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    
}
