package es.xpressaly.dto;

import java.util.List;
import java.util.Map;

public record UserDTO(
    String firstName,
    String lastName,
    String email,
    String address,
    int phoneNumber,
    int age,
    boolean isAdmin,
    List<Map<String, Object>> orders,
    List<Map<String, Object>> reviews
) {
}