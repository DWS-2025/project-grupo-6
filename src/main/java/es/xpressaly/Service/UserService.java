package es.xpressaly.Service;

import org.springframework.stereotype.Service;

import es.xpressaly.Model.User;
import java.util.ArrayList;
import java.util.List;

@Service
public class UserService {

    private User currentUser;
    private List<User> users;

    public UserService() {
        users = new ArrayList<>();
        // We create default users and save them in memory
        User defaultUser = new User("Juan", "Pérez", "juan.perez@email.com", "password123", "Calle Ficticia 123", 2134, 25);
        defaultUser.setId(1L);
        users.add(defaultUser);
        
        // Adding another test user
        User testUser = new User("Maria", "García", "maria.garcia@email.com", "test456", "Avenida Principal 456", 5678, 30);
        testUser.setId(2L);
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

    // Method to register a new user
    public User registerUser(String firstName, String lastName, String email, String password, String address, int phoneNumber, int age) {
        // Check if user already exists
        if (users.stream().anyMatch(u -> u.getEmail().equals(email))) {
            return null;
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

    // Method to update the user
    public void updateUser(User updatedUser) {
        users.stream()
                .filter(u -> u.getId().equals(updatedUser.getId()))
                .findFirst()
                .ifPresent(user -> {
                    user.setFirstName(updatedUser.getFirstName());
                    user.setLastName(updatedUser.getLastName());
                    user.setEmail(updatedUser.getEmail());
                    user.setAddress(updatedUser.getAddress());
                });
    }
}