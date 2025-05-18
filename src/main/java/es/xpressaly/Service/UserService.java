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
import es.xpressaly.Model.Review;
import es.xpressaly.dto.OrderDTO;
import es.xpressaly.dto.ReviewDTO;
import es.xpressaly.dto.UserDTO;
import es.xpressaly.dto.UserWebDTO;
import es.xpressaly.mapper.OrderMapper;
import es.xpressaly.mapper.ReviewMapper;
import es.xpressaly.mapper.UserMapper;
import es.xpressaly.mapper.UserWebMapper;

import java.util.List;
import java.util.Optional;
import java.util.Arrays;
import java.util.regex.Pattern;
import java.util.ArrayList;
import java.util.stream.Collectors;

@Service
@Transactional
public class UserService {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private UserWebMapper userWebMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private ReviewMapper reviewMapper;
    @Autowired
    private OrderMapper orderMapper;
    //private User currentUser;

    // Method to obtain the user entity by ID
    public User getUserEntityById(Long userId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user != null) {
            // Inicializar las colecciones
            if (user.getOrders() != null) {
                user.getOrders().size(); // Forzar inicialización
                user.getOrders().forEach(order -> {
                    if (order.getProducts() != null) {
                        order.getProducts().size(); // Forzar inicialización de productos
                    }
                });
            }
            if (user.getReviews() != null) {
                user.getReviews().size(); // Forzar inicialización
            }
        }
        return user;
    }

    // Method to obtain the user by ID
    public UserWebDTO getUserById(Long userId) {
        User user = userRepository.findById(userId).orElse(null);
        return user != null ? userWebMapper.toDTO(user) : null;
    }

    // Method to authenticate a user
    public UserWebDTO authenticateUser(String email, String password) {
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
            return userWebMapper.toDTO(user);
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
    public UserWebDTO registerUser(String firstName, String lastName, String email, String password, String address, int phoneNumber, int age) {
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
        return userWebMapper.toDTO(userRepository.save(newUser));
    }

    // Method to get all users
    public List<UserWebDTO> getAllUsers() {
        return userRepository.findAll().stream()
            .map(userWebMapper::toDTO)
            .collect(Collectors.toList());
    }

    // Method to get the current user
    public UserWebDTO getUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
    
        String email = authentication.getName();
        User user = userRepository.findByEmail(email).orElse(null);
        return userWebMapper.toDTO(user);
    }

    public UserDTO getUserDTO(UserWebDTO userWebDTO) {
        User user = userWebMapper.toDomain(userWebDTO);
        return userMapper.toDTO(user);
    }
    
    @Transactional(readOnly = true)
    public UserWebDTO getUserWithReviews() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
    
        String email = authentication.getName();
        User user = userRepository.findUserWithReviews(email).orElse(null);
        
        if (user != null) {
            if (user.getReviews() != null) {
                user.getReviews().size();
            }
            if (user.getOrders() != null) {
                user.getOrders().size();
                
                List<Order> userOrders = orderRepository.findByUserOrderById(user);
                int orderCount = 1;
                for (Order order : userOrders) {
                    if (order.getUserOrderNumber() == null) {
                        order.setUserOrderNumber(orderCount++);
                        orderRepository.save(order);
                    }
                }
            }
            
            Hibernate.initialize(user.getOrders());
            if (user.getOrders() != null) {
                user.getOrders().forEach(order -> Hibernate.initialize(order.getProducts()));
            }
            
            return userWebMapper.toDTO(user);
        }
        return null;
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
    public void updateUser(UserWebDTO userWebDTO) {
        if (userWebDTO == null || userWebDTO.id() == null) {
            throw new IllegalArgumentException("Invalid user data");
        }

        User existingUser = userRepository.findById(userWebDTO.id())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (!isValidEmail(userWebDTO.email())) {
            throw new IllegalArgumentException("Invalid email format");
        }
        if (!isValidPhoneNumber(userWebDTO.phoneNumber())) {
            throw new IllegalArgumentException("Invalid phone number format");
        }
        if (!isValidAge(userWebDTO.age())) {
            throw new IllegalArgumentException("Age must be between 18 and 120");
        }
        if (userWebDTO.firstName() == null || userWebDTO.firstName().trim().isEmpty() || 
            userWebDTO.lastName() == null || userWebDTO.lastName().trim().isEmpty()) {
            throw new IllegalArgumentException("First name and last name cannot be empty");
        }
        if (userWebDTO.address() == null || userWebDTO.address().trim().isEmpty()) {
            throw new IllegalArgumentException("Address cannot be empty");
        }

        User updatedUser = userWebMapper.toDomain(userWebDTO);
        if (updatedUser.getPassword() != null && !updatedUser.getPassword().isEmpty()) {
            if (!passwordEncoder.matches(updatedUser.getPassword(), existingUser.getPassword())) {
                updatedUser.setPassword(passwordEncoder.encode(updatedUser.getPassword()));
            }
        } else {
            updatedUser.setPassword(existingUser.getPassword());
        }

        userRepository.save(updatedUser);
    }

    public UserWebDTO findByEmail(String email) {
        User user = userRepository.findByEmail(email).orElse(null);
        return user != null ? userWebMapper.toDTO(user) : null;
    }

    public void deleteOwnUser(HttpSession session) {
        UserWebDTO currentUser = getUser();
        if (currentUser != null) {
            userRepository.deleteById(currentUser.id());
            session.invalidate();
        }
    }

    public void deleteUser(String email) {
        User user = userRepository.findByEmail(email).orElse(null);
        if (user != null) {
            userRepository.delete(user);
        }
    }

    public String encodePassword(String rawPassword) {
        return passwordEncoder.encode(rawPassword);
    }

    // Método mejorado para obtener todos los usuarios
    @Transactional(readOnly = true)
    public List<UserWebDTO> getAllUsersInitialized() {
        // Usar la consulta optimizada que carga usuarios con sus reseñas y productos en una sola consulta
        List<User> usersWithReviews = userRepository.findAllUsersWithReviews();
        
        // Asegurarse de que todas las colecciones están inicializadas
        for (User user : usersWithReviews) {
            if (user.getReviews() != null) {
                // Forzar initialización
                user.getReviews().size();
            }
        }
        
        return usersWithReviews.stream()
            .map(userWebMapper::toDTO)
            .collect(Collectors.toList());
    }

    public UserWebDTO getUserWebDTO(User user) {
        return userWebMapper.toDTO(user);
    }

    public UserWebDTO setPassword(UserWebDTO userWebDTO, String password) {
        User user = userWebMapper.toDomain(userWebDTO);
        user.setPassword(password);
        return userWebMapper.toDTO(user);
    }

    public UserWebDTO setFirstName(UserWebDTO userWebDTO, String firstName) {
        User user = userWebMapper.toDomain(userWebDTO);
        user.setFirstName(firstName);
        return userWebMapper.toDTO(user);
    }

    public UserWebDTO setLastName(UserWebDTO userWebDTO, String lastName) {
        User user = userWebMapper.toDomain(userWebDTO);
        user.setLastName(lastName);
        return userWebMapper.toDTO(user);
    }

    public UserWebDTO setEmail(UserWebDTO userWebDTO, String email) {
        User user = userWebMapper.toDomain(userWebDTO);
        user.setEmail(email);
        return userWebMapper.toDTO(user);
    }

    public UserWebDTO setAddress(UserWebDTO userWebDTO, String address) {
        User user = userWebMapper.toDomain(userWebDTO);
        user.setAddress(address);
        return userWebMapper.toDTO(user);
    }

    public UserWebDTO setPhoneNumber(UserWebDTO userWebDTO, int phoneNumber) {
        User user = userWebMapper.toDomain(userWebDTO);
        user.setPhoneNumber(phoneNumber);
        return userWebMapper.toDTO(user);
    }

    public UserWebDTO setAge(UserWebDTO userWebDTO, int age) {
        User user = userWebMapper.toDomain(userWebDTO);
        user.setAge(age);
        return userWebMapper.toDTO(user);
    }

    public UserWebDTO setRole(UserWebDTO userWebDTO, UserRole role) {
        User user = userWebMapper.toDomain(userWebDTO);
        user.setRole(role);
        return userWebMapper.toDTO(user);
    }

    public UserWebDTO addOrder(UserWebDTO userWebDTO, Order order) {
        User user = userWebMapper.toDomain(userWebDTO);
        user.addOrder(order);
        return userWebMapper.toDTO(user);
    }

    public UserWebDTO addReview(UserWebDTO userWebDTO, ReviewDTO reviewDTO) {
        User user = userWebMapper.toDomain(userWebDTO);
        Review review = reviewMapper.toDomain(reviewDTO);
        user.addReview(review);
        return userWebMapper.toDTO(user);
    }

    public String getAddress(UserWebDTO userWebDTO) {
        User user = userWebMapper.toDomain(userWebDTO);
        return user.getAddress();
    }
}