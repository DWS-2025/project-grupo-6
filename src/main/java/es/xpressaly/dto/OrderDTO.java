package es.xpressaly.dto;

import java.util.List;
import java.util.Map;

public record OrderDTO(
    Long id,
    UserWebDTO user,
    String address,
    double total,
    List<ProductWebDTO> products,
    Map<Long, Integer> quantities
) {
}