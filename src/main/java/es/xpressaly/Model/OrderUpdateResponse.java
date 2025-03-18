package es.xpressaly.Model;

import java.util.List;

public class OrderUpdateResponse {
    private boolean success;
    private double total;
    private List<Product> products;

    // Constructor
    public OrderUpdateResponse(boolean success, double total, List<Product> products) {
        this.success = success;
        this.total = total;
        this.products = products;
    }

    // Getters and Setters
    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public double getTotal() {
        return total;
    }

    public void setTotal(double total) {
        this.total = total;
    }

    public List<Product> getProducts() {
        return products;
    }

    public void setProducts(List<Product> products) {
        this.products = products;
    }
}