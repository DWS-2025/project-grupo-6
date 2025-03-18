package es.xpressaly.Repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import es.xpressaly.Model.User;

public interface UserRepository extends JpaRepository<User,Long> {
    List<User> findByFirstName(String firstName);
    List<User> findByLastName(String lastName);
}
