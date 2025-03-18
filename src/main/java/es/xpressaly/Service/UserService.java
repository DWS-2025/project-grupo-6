package es.xpressaly.Service;

import org.springframework.stereotype.Service;

import es.xpressaly.Model.User;
import es.xpressaly.Model.UserRole;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@Service
public class UserService {

    private User currentUser;
    private List<User> users;

    public UserService() {
        users = new ArrayList<>();
        // We create default users and save them in memory
        User defaultUser = new User("Juan", "Pérez", "juan.perez@email.com", "password123", "Calle Ficticia 123", 2134, 25);
        defaultUser.setId(1L);
        defaultUser.setRole(UserRole.ADMIN); // Set Juan as admin
        users.add(defaultUser);
        
        // Adding another test user
        User testUser = new User("Maria", "García", "maria.garcia@email.com", "test456", "Avenida Principal 456", 5678, 30);
        testUser.setId(2L);
        testUser.setRole(UserRole.USER); // Explicitly set Maria as regular user
        users.add(testUser);
    }

    // Method to obtain the user by ID
    public User getUserById(Long userId) {
        return users.stream()
                .filter(u -> u.getId().equals(userId))
                .findFirst()
                .orElse(null);
    }

    // Method to authenticate a user
    public User authenticateUser(String email, String password) {
        currentUser = users.stream()
                .filter(u -> u.getEmail().equals(email) && u.getPassword().equals(password))
                .findFirst()
                .orElse(null);
        this.setCurrentUser(currentUser);
        return currentUser;
    }

    // Validation methods
    private boolean isValidEmail(String email) {
        String emailRegex = "^[A-Za-z0-9+_.-]+@(.+)$";
        return Pattern.compile(emailRegex).matcher(email).matches();
    }

    private boolean isValidPassword(String password) {
        // At least 8 characters, containing at least one number and one letter
        return password != null && password.length() >= 8 && 
               password.matches(".*[0-9].*") && password.matches(".*[a-zA-Z].*");
    }

    private boolean isValidPhoneNumber(int phoneNumber) {
        // Phone number should be between 100000 and 999999999
        return String.valueOf(phoneNumber).length() >= 6 && 
               String.valueOf(phoneNumber).length() <= 9;
    }

    private boolean isValidAge(int age) {
        return age >= 18 && age <= 120;
    }

    // Method to register a new user with validations
    public User registerUser(String firstName, String lastName, String email, String password, String address, int phoneNumber, int age) {
        // Validate input data
        if (!isValidEmail(email)) {
            throw new IllegalArgumentException("Invalid email format");
        }
        if (!isValidPassword(password)) {
            throw new IllegalArgumentException("Password must be at least 8 characters long and contain both letters and numbers");
        }
        if (!isValidPhoneNumber(phoneNumber)) {
            throw new IllegalArgumentException("Invalid phone number format");
        }
        if (!isValidAge(age)) {
            throw new IllegalArgumentException("Age must be between 18 and 120");
        }
        if (firstName == null || firstName.trim().isEmpty() || lastName == null || lastName.trim().isEmpty()) {
            throw new IllegalArgumentException("First name and last name cannot be empty");
        }
        if (address == null || address.trim().isEmpty()) {
            throw new IllegalArgumentException("Address cannot be empty");
        }

        // Check if user already exists
        if (users.stream().anyMatch(u -> u.getEmail().equals(email))) {
            throw new IllegalArgumentException("Email already exists");
        }

        User newUser = new User(firstName, lastName, email, password, address, phoneNumber, age);
        newUser.setId((long) (users.size() + 1));
        users.add(newUser);
        return newUser;
    }

    // Method to get the current user
    public User getUser() {
        return currentUser;
    }

    public void setCurrentUser(User currentUser) {
        this.currentUser = currentUser;
    }

    // Method to update the user with validations
    public void updateUser(User updatedUser) {
        if (updatedUser == null || updatedUser.getId() == null) {
            throw new IllegalArgumentException("Invalid user data");
        }

        // Validate updated data
        if (!isValidEmail(updatedUser.getEmail())) {
            throw new IllegalArgumentException("Invalid email format");
        }
        if (updatedUser.getPassword() != null && !updatedUser.getPassword().isEmpty() && 
            !isValidPassword(updatedUser.getPassword())) {
            throw new IllegalArgumentException("Password must be at least 8 characters long and contain both letters and numbers");
        }
        if (!isValidPhoneNumber(updatedUser.getPhoneNumber())) {
            throw new IllegalArgumentException("Invalid phone number format");
        }
        if (!isValidAge(updatedUser.getAge())) {
            throw new IllegalArgumentException("Age must be between 18 and 120");
        }
        if (updatedUser.getFirstName() == null || updatedUser.getFirstName().trim().isEmpty() || 
            updatedUser.getLastName() == null || updatedUser.getLastName().trim().isEmpty()) {
            throw new IllegalArgumentException("First name and last name cannot be empty");
        }
        if (updatedUser.getAddress() == null || updatedUser.getAddress().trim().isEmpty()) {
            throw new IllegalArgumentException("Address cannot be empty");
        }

        users.stream()
                .filter(u -> u.getId().equals(updatedUser.getId()))
                .findFirst()
                .ifPresent(user -> {
                    // Check if email is being changed and if it's already in use by another user
                    if (!user.getEmail().equals(updatedUser.getEmail()) && 
                        users.stream().anyMatch(u -> !u.getId().equals(user.getId()) && 
                                                     u.getEmail().equals(updatedUser.getEmail()))) {
                        throw new IllegalArgumentException("Email already exists");
                    }
                    user.setFirstName(updatedUser.getFirstName());
                    user.setLastName(updatedUser.getLastName());
                    user.setEmail(updatedUser.getEmail());
                    user.setAddress(updatedUser.getAddress());
                    user.setPhoneNumber(updatedUser.getPhoneNumber());
                    user.setAge(updatedUser.getAge());
                    if (updatedUser.getPassword() != null && !updatedUser.getPassword().isEmpty()) {
                        user.setPassword(updatedUser.getPassword());
                    }
                });
    }
}