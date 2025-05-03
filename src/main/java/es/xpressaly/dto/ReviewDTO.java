package es.xpressaly.dto;

public record ReviewDTO(
    Long id,
    String comment,
    int rating,
    Long userId,
    Long productId,
    String userName,
    String productName
) {
}