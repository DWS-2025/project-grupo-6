package es.xpressaly.dto;

public record ProductDTO(
    Long id,
    String name,
    String description,
    double price,
    int stock,
    byte[] imageData
    //List<ReviewDTO> reviews
) {
}