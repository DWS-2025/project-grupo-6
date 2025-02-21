public class Order {
    private int id;
    private int userId;
    private int productId;
    private float quantity;
    private LocalDate date;
    private String address;

    public Order(int id, int userId, int productId, float quantity, int date, String address) {
        this.id = id;
        this.userId = userId;
        this.productId = productId;
        this.quantity = quantity;
        this.date = LocalDate.parse(dateString);
        this.address = address;
    }

    // Getters
    public int getId() {
        return id;
    }

    public int getUserId() {
        return userId;
    }

    public int getProductId() {
        return productId;
    }

    public float getQuantity() {
        return quantity;
    }

    public LocalDate getDate() {
        return date;
    }

    public String getAddress() {
        return address;
    }

    // Setters
    public void setId(int id) {
        this.id = id;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public void setQuantity(float quantity) {
        this.quantity = quantity;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}


module.exports = Order;