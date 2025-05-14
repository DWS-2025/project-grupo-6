package es.xpressaly.dto;

import java.util.List;

public record UserDTO(
    String firstName,
    String lastName,
    String email,
    String address,
    int phoneNumber,
    int age,
    boolean admin,
    List<OrderDTO> orders,
    List<ReviewDTO> reviews
) {
}