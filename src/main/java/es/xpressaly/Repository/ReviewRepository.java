package es.xpressaly.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import es.xpressaly.Model.Review;
import es.xpressaly.Model.Product;
import es.xpressaly.Model.User;
import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findByProduct(Product product);
    List<Review> findByRating(int rating);
    List<Review> findByUser(User user);
    //List<Review> findByProductOrderByCreatedAtDesc(Product product);
}