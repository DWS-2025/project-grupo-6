package es.xpressaly.Model;

import java.util.ArrayList;
import java.util.List;

public class User {

    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String password;
    private String address;
    private int age;
    private int phoneNumber;
    private List<Review> reviews;
    private List<Order> orders;

    public User() {
        this.reviews = new ArrayList<>();
        this.orders = new ArrayList<>();
    }

    public User(String firstName, String lastName, String email, String password, String address, int phoneNumber, int age) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.password = password;
        this.address = address;
        this.phoneNumber = phoneNumber;
        this.age = age;
        this.reviews = new ArrayList<>();
        this.orders = new ArrayList<>();
    } 
    
    public List<Order> getOrders() { return orders; }
    public void setOrders(List<Order> orders) { this.orders = orders; }
    public void addOrder(Order order){
        this.orders.add(order);
    }
    public int getAge() { return age; }
    public void setAge(int age) { this.age = age; }
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public int getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(int phoneNumber) { this.phoneNumber = phoneNumber; }
    
    public List<Review> getReviews() { return reviews; }
    public void setReviews(List<Review> reviews) { this.reviews = reviews; }
    public void addReview(Review review) { this.reviews.add(review); }
}
