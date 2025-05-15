package es.xpressaly.Repository;

import java.util.Optional;
import java.util.List;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import es.xpressaly.Model.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional <User> findByEmail(String email);
    boolean existsByEmail(String email);
    Optional<User> findByFirstName(String firstName);
    @Query("SELECT DISTINCT u FROM User u " +
           "LEFT JOIN FETCH u.reviews r " +
           "LEFT JOIN FETCH r.product " +
           "WHERE u.email = :email")
    Optional<User> findUserWithReviews(@Param("email") String email);
    
    @Query("SELECT DISTINCT u FROM User u " +
           "LEFT JOIN FETCH u.reviews r " +
           "LEFT JOIN FETCH r.product")
    List<User> findAllUsersWithReviews();
}