package es.xpressaly.Model;

import java.util.ArrayList;
import java.util.List;

public class Order {

    private Long id;
    private static Long orderCounter = 0L;
    private User user;
    private List<Product> products;
    private String address;

    public Order(User user, String address) {
        this.id = ++orderCounter;
        this.user = user;
        this.products = new ArrayList<>();
        this.address = address;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public List<Product> getProducts() { return products; }
    public void setProducts(List<Product> products) { this.products = products; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public void addProduct(Product product) {
        this.products.add(product);
    }

    public void removeProduct(Product product) {
        this.products.remove(product);
    }

    public boolean hasProducts(){
        return this.products!=null&&!products.isEmpty();
    }

    private double total;public double getTotal(){return total;}
    public void setTotal(double total){this.total=total;} public void calculateTotal(Order order){
        double total=0;
        if(order.hasProducts()){
            for (Product product : order.getProducts()) {
                total += product.getPrice() * product.getAmount();
            }
        }
        order.setTotal(total);
    }

    public Product findProductById(Long id) {
        for (Product product : products) {
            if (product.getId().equals(id)) {
                return product;
            }
        }
        return null;
    }

}

