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
    List<Long> orders, // Assuming orders are represented by their IDs
    List<Long> reviews // Assuming reviews are represented by their IDs
    //List<OrderDTO> orders,
    //List<ReviewDTO> reviews
) {
}