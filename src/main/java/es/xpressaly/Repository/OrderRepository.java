package es.xpressaly.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import es.xpressaly.Model.Order;

public interface OrderRepository extends JpaRepository<Order, Long> {
    
}
