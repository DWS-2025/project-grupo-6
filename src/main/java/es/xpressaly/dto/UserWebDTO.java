package es.xpressaly.dto;

import java.util.List;
import es.xpressaly.Model.UserRole;
import es.xpressaly.dto.ReviewDTO;
import es.xpressaly.dto.OrderDTO;


public record UserWebDTO(
    Long id,
    String firstName,
    String lastName,
    String email,
    String password,
    String address,
    int age,
    int phoneNumber,
    List<ReviewDTO> reviews,
    List<OrderDTO> orders,
    UserRole role
) {} 