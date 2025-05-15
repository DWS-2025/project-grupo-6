package es.xpressaly.dto;

import java.util.List;


public record OrderDTO(
    Long id,
    String address,
    double total,
    List<ProductDTO> products
) {
}