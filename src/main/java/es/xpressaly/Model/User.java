package es.xpressaly.Model;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.*;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(unique = true)
    private String firstName;
    private String lastName;
    private String email;
    private String password;
    private String address;
    private int age;
    private int phoneNumber;
    private String pdfPath;
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<Review> reviews;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<Order> orders;

    @Enumerated(EnumType.STRING)
    private UserRole role;

    @Transient
    private Order currentOrder;
    
    public User() {
        this.reviews = new ArrayList<>();
        this.orders = new ArrayList<>();
        this.role = UserRole.USER; // Default role is USER
        this.pdfPath = null;
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
        this.role = UserRole.USER; // Default role is USER
        this.pdfPath = null;
    } 
    
    public User(String firstName, String lastName, String email, String password, String address, int phoneNumber, int age, String pdfPath) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.password = password;
        this.address = address;
        this.phoneNumber = phoneNumber;
        this.age = age;
        this.reviews = new ArrayList<>();
        this.orders = new ArrayList<>();
        this.role = UserRole.USER; // Default role is USER
        this.pdfPath = pdfPath;
    }
    
    public List<Order> getOrders() { return orders; }
    public void setOrders(List<Order> orders) { this.orders = orders; }
    public void addOrder(Order order){ this.orders.add(order); }
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
    public UserRole getRole() { return role; }
    public void setRole(UserRole role) { this.role = role; }
    
    public List<Review> getReviews() { return reviews; }
    public void setReviews(List<Review> reviews) { this.reviews = reviews; }
    public void addReview(Review review) { this.reviews.add(review); }
    public List<UserRole> getRoles() {
        List<UserRole> roles = new ArrayList<>();
        roles.add(this.role);
        return roles;
    }
    public boolean isAdmin() {
        return this.role == UserRole.ADMIN;
    }

    public Order getCurrentOrder(){
        return currentOrder;
    }

    public void setCurrentOrder(Order currentOrder){
        this.currentOrder=currentOrder;
    }

    public String getPdfPath() {
        return pdfPath;
    }

    public void setPdfPath(String pdfPath) {
        this.pdfPath = pdfPath;
    }

    public double getAverageRating() {
        if (reviews == null || reviews.isEmpty()) {
            return 0.0;
        }
        return reviews.stream()
            .mapToDouble(Review::getRating)
            .average()
            .orElse(0.0);
    }
}
