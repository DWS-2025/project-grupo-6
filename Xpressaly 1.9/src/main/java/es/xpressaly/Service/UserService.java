package es.xpressaly.Service;

import org.springframework.stereotype.Service;

import es.xpressaly.Model.User;

@Service
public class UserService {

    private User user;

    public UserService() {
        // We create a unique user and save it in memory.
        user = new User("Juan", "Pérez", "juan.perez@email.com", "password123", "Calle Ficticia 123", 2134, 25);
        user.setId(1L);  // We assign a unique ID to the user.
    }

    // Method to obtain the unique user
    public User getUser() {
        return user;  // Returns the user that is in memory
    }

    // Method to update the user
    public void updateUser(User updatedUser) {
        // We update user data in memory
        this.user.setFirstName(updatedUser.getFirstName());
        this.user.setLastName(updatedUser.getLastName());
        this.user.setEmail(updatedUser.getEmail());
        this.user.setAddress(updatedUser.getAddress());
    }
}

