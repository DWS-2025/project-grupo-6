package es.xpressaly.dto;

import java.util.List;
import java.util.Map;

public class UserDTO {
    private String firstName;
    private String lastName;
    private String email;
    private String address;
    private int phoneNumber;
    private int age;
    private boolean isAdmin;
    private List<Map<String, Object>> orders;
    private List<Map<String, Object>> reviews;

    // Getters and Setters
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public int getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(int phoneNumber) { this.phoneNumber = phoneNumber; }

    public int getAge() { return age; }
    public void setAge(int age) { this.age = age; }

    public boolean isAdmin() { return isAdmin; }
    public void setAdmin(boolean admin) { isAdmin = admin; }

    public List<Map<String, Object>> getOrders() { return orders; }
    public void setOrders(List<Map<String, Object>> orders) { this.orders = orders; }

    public List<Map<String, Object>> getReviews() { return reviews; }
    public void setReviews(List<Map<String, Object>> reviews) { this.reviews = reviews; }
}