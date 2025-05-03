package es.xpressaly.dto;

import java.util.List;
import java.util.Map;

public record OrderDTO(
    Long id,
    String address,
    double total,
    List<Map<String, Object>> items
) {
}