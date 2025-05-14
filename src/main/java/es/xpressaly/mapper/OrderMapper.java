package es.xpressaly.mapper;

import es.xpressaly.Model.Order;
import es.xpressaly.dto.OrderDTO;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class OrderMapper {
    
    public OrderDTO toDTO(Order order) {
        return new OrderDTO(
            order.getId(),
            order.getAddress(),
            order.getTotal(),
            null // items would be allocated elsewhere if needed
        );
    }
    
    public Order toEntity(OrderDTO dto) {
        Order order = new Order();
        order.setId(dto.id());
        order.setAddress(dto.address());
        order.setTotal(dto.total());
        return order;
    }
}