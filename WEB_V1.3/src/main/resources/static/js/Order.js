class Order {
    constructor(id, userId, productId, quantity, date, address) {
        this.id = id;
        this.userId = userId;
        this.productId = productId;
        this.quantity = quantity;
        this.date = date; 
        this.address = address;
    }
}

module.exports = Order;