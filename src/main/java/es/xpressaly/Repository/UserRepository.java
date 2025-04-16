package es.xpressaly.Repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import es.xpressaly.Model.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional <User> findByEmail(String email);
    boolean existsByEmail(String email);
    Optional<User> findByFirstName(String firstName);
}