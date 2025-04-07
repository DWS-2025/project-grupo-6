package es.xpressaly.dto;

import java.util.List;
import java.util.Map;

public class OrderDTO {
    private Long id;
    private String address;
    private double total;
    private List<Map<String, Object>> items;

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public double getTotal() { return total; }
    public void setTotal(double total) { this.total = total; }

    public List<Map<String, Object>> getItems() { return items; }
    public void setItems(List<Map<String, Object>> items) { this.items = items; }
}