package es.xpressaly.dto;

public record ProductDTO(
    Long id,
    String name,
    String description,
    double price,
    int stock,
    int quantity,
    double rating
    //List<ReviewDTO> reviews
) {
}