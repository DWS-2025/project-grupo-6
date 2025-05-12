package es.xpressaly.Service;

import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.core.Authentication;
import jakarta.servlet.http.HttpSession;

import es.xpressaly.Model.User;
import es.xpressaly.Model.UserRole;
import es.xpressaly.Repository.UserRepository;
import es.xpressaly.Repository.OrderRepository;
import es.xpressaly.Model.Order;

import java.util.List;
import java.util.Optional;
import java.util.Arrays;
import java.util.regex.Pattern;

@Service
@Transactional
public class UserService {

    @Autowired
    private UserRepository userRepository;
    @Autowired
	private PasswordEncoder passwordEncoder;
    @Autowired
    private OrderRepository orderRepository;

    //private User currentUser;

    // Method to obtain the user by ID
    public User getUserById(Long userId) {
        return userRepository.findById(userId).orElse(null);
    }

    // Method to authenticate a user
    public User authenticateUser(String email, String password) {
        // First try to find the user with the exact email
        User user = userRepository.findByEmail(email).orElse(null);
        
        // If not found and it might be a temporary user (email ends with .temp)
        if (user == null && !email.endsWith(".temp")) {
            // Check if there's a temporary version of this user
            user = userRepository.findByEmail(email + ".temp").orElse(null);
        }
        
        if (user != null && user.getPassword().equals(password)) {
            // Initialize collections to prevent LazyInitializationException
            if (user.getOrders() != null) {
                user.getOrders().size(); // Force initialization of orders collection
            }
            if (user.getReviews() != null) {
                user.getReviews().size(); // Force initialization of reviews collection
            }
            //this.setCurrentUser(user);
            return user;
        }
        // Debug information to help troubleshoot login issues
        if (user != null) {
            System.out.println("Login attempt for user: " + email);
            System.out.println("Password match failed. Input: " + password + ", Stored: " + user.getPassword());
        } else {
            System.out.println("No user found with email: " + email);
        }
        return null;
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
        String phoneStr = String.valueOf(phoneNumber);
        // Validar que el número tenga entre 6 y 9 dígitos exactamente
        return phoneStr.length() >= 6 && phoneStr.length() <= 9;
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
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email already exists");
        }

        User newUser = new User(firstName, lastName, email, passwordEncoder.encode(password), address, phoneNumber, age);
        newUser.setRole(UserRole.USER); // Set default role
        return userRepository.save(newUser);
    }

    // Method to get all users
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    // Method to get the current user
    public User getUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
    
        String email = authentication.getName();
        return userRepository.findByEmail(email).orElse(null);
    }
    @Transactional(readOnly = true)
    public User getUserWithReviews(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
    
        String email = authentication.getName();
        User user = userRepository.findUserWithReviews(email).orElse(null);
        // Inicialización explícita para evitar LazyInitializationException
        if (user.getReviews() != null) {
            user.getReviews().size(); // Fuerza la inicialización
        }
        if (user.getOrders() != null) {
            user.getOrders().size(); // Fuerza la inicialización
            
            // Aseguramos que todos los pedidos tengan un número de pedido asignado
            List<Order> userOrders = orderRepository.findByUserOrderById(user);
            int orderCount = 1;
            for (Order order : userOrders) {
                if (order.getUserOrderNumber() == null) {
                    order.setUserOrderNumber(orderCount++);
                    orderRepository.save(order);
                }
            }
        }
        if (user != null) {
            // Initialize orders and their products in a separate query
            Hibernate.initialize(user.getOrders());
            if (user.getOrders() != null) {
                user.getOrders().forEach(order -> Hibernate.initialize(order.getProducts()));
            }
        }
        return user;
    }

    /*public void setCurrentUser(User currentUser) {
        this.currentUser = currentUser;
    }*/

    // List of example user emails that should be preserved
    private final List<String> exampleUserEmails = Arrays.asList(
        "juan.perez@email.com",
        "maria.garcia@email.com"
    );

    // Method to check if a user is an example user
    private boolean isExampleUser(User user) {
        return user != null && exampleUserEmails.contains(user.getEmail());
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

        // Check if email is being changed and if it's already in use by another user
        User existingUser = userRepository.findById(updatedUser.getId()).orElse(null);
        if (existingUser != null && !existingUser.getEmail().equals(updatedUser.getEmail()) && 
            userRepository.existsByEmail(updatedUser.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }

        userRepository.save(updatedUser);
    }

    public User findByEmail(String email) {
        return userRepository.findByEmail(email).orElse(null);
    }

    public void deleteUser(HttpSession session) {
        User currentUser = getUser();
        if (currentUser != null) {
            userRepository.delete(currentUser);
            session.invalidate();
        }
    }
}