package es.xpressaly.dto;

import java.util.List;

public record OrderApiDTO(
    Long id,
    Long userId,
    String address,
    double total,
    List<ProductDTO> products
) {
}
