package es.xpressaly.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import es.xpressaly.Model.Order;
import es.xpressaly.Model.User;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByUser(User user);
    //List<Order> findByUserOrderByCreatedAtDesc(User user);
}