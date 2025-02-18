class Review {
    constructor(id, userId, productId, comment, rating, images) {
        this.id = id;
        this.userId = userId;
        this.productId = productId;
        this.comment = comment;
        this.rating = rating; // Number between 1 and 5
        this.images = images; // Array of image URLs (maximum 3)
    }
}

module.exports = Review;