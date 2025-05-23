package es.xpressaly.dto;

import java.util.List;
import es.xpressaly.Model.Review;

public record ProductWebDTO(
    Long id,
    String name,
    String description,
    double price,
    int stock,
    String imagePath,
    byte[] imageData,
    String returnPolicyPath,
    byte[] returnPolicyData,
    List<Review> reviews,
    double rating
) {} 