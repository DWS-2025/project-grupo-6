package es.xpressaly.dto;

import java.util.List;
import java.util.Map;

public record ProductDTO(
    Long id,
    String name,
    String description,
    double price,
    int stock,
    List<Map<String, Object>> reviews
) {
}