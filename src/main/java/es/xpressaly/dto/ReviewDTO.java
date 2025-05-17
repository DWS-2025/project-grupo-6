package es.xpressaly.dto;

public record ReviewDTO(
    Long id,
    String comment,
    int rating,
    UserWebDTO user,
    ProductWebDTO product
) {
}