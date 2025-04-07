package es.xpressaly.mapper;

import es.xpressaly.Model.Order;
import es.xpressaly.dto.OrderDTO;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class OrderMapper {
    
    public OrderDTO toDTO(Order order) {
        OrderDTO dto = new OrderDTO();
        dto.setId(order.getId());
        dto.setAddress(order.getAddress());
        dto.setTotal(order.getTotal());
        return dto;
    }
    
    public Order toEntity(OrderDTO dto) {
        Order order = new Order();
        order.setId(dto.getId());
        order.setAddress(dto.getAddress());
        order.setTotal(dto.getTotal());
        return order;
    }
}