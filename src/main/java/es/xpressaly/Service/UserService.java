package es.xpressaly.Service;

import org.hibernate.Hibernate;
import org.mapstruct.control.MappingControl.Use;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.core.Authentication;


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
import java.util.HashMap;
import java.util.Map;

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
    @Autowired
    private SanitizationService sanitizationService;


    private static Map<Long, Order> userCurrentOrders = new HashMap<>();

    //Sanitization function
    private void sanitizeUser(User user) {
        if (user.getFirstName() != null) {
            user.setFirstName(sanitizationService.sanitize(user.getFirstName()));
        }
        if (user.getLastName() != null) {
            user.setLastName(sanitizationService.sanitize(user.getLastName()));
        }
        if (user.getAddress() != null) {
            user.setAddress(sanitizationService.sanitize(user.getAddress()));
        }
    }


    // Method to obtain the user entity by ID
    public User getUserEntityById(Long userId) {
        UserWebDTO currentUser = getUser();
        if (currentUser == null || (!currentUser.id().equals(userId) && currentUser.role() != UserRole.ADMIN)) {
            throw new SecurityException("Access Denied");
        }
        User user = userRepository.findById(userId).orElse(null);
        if (user != null) {
            // Initialize collections
            if (user.getOrders() != null) {
                user.getOrders().size(); // Force initialization
                user.getOrders().forEach(order -> {
                    if (order.getProducts() != null) {
                        order.getProducts().size(); // Force initialization of products
                    }
                });
            }
            if (user.getReviews() != null) {
                user.getReviews().size(); // Force initialization
            }
            // Set currentOrder from static map
            user.setCurrentOrder(userCurrentOrders.get(userId));
        }
        return user;
    }

    // Method to obtain the user by ID
    public UserWebDTO getUserById(Long userId) {
        UserWebDTO currentUser = getUser();
        if (currentUser == null || (!currentUser.id().equals(userId) && currentUser.role() != UserRole.ADMIN)) {
            throw new SecurityException("Access Denied");
        }
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
        // At least 8 characters, one uppercase, one lowercase and one number
        return password != null && 
               password.length() >= 8 && 
               password.matches(".*[0-9].*") && 
               password.matches(".*[a-z].*") &&
               password.matches(".*[A-Z].*");
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
        sanitizeUser(newUser); //Sanitizacion --------------------------------------------------
        return userWebMapper.toDTO(userRepository.save(newUser));
    }

    // Method to get all users
    public List<UserWebDTO> getAllUsers() {
        UserWebDTO currentUser = getUser();
        if (currentUser == null || currentUser.role() != UserRole.ADMIN) {
            throw new SecurityException("Access Denied");
        }
        return userRepository.findAll().stream()
            .map(userWebMapper::toDTO)
            .collect(Collectors.toList());
    }

    public List<User> getAllUsersEntity() {
        UserWebDTO currentUser = getUser();
        if (currentUser == null || currentUser.role() != UserRole.ADMIN) {
            throw new SecurityException("Access Denied");
        }
        return userRepository.findAll();
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

    public User getUserEntity() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
    
        String email = authentication.getName();
        User user = userRepository.findByEmail(email).orElse(null);
        return user;
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





    // Method to update the user with validations
    public void updateUser(UserWebDTO userWebDTO) {
        UserWebDTO currentUser = getUser();
        if (currentUser == null) {
            throw new SecurityException("Not authenticated");
        }
        if (userWebDTO == null || userWebDTO.id() == null) {
            throw new IllegalArgumentException("Invalid user data");
        }
        if (!currentUser.id().equals(userWebDTO.id()) && currentUser.role() != UserRole.ADMIN) {
            throw new SecurityException("Access Denied");
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

        // En lugar de crear un nuevo objeto User, actualizamos el existente
        existingUser.setFirstName(userWebDTO.firstName());
        existingUser.setLastName(userWebDTO.lastName());
        existingUser.setEmail(userWebDTO.email());
        existingUser.setAddress(userWebDTO.address());
        existingUser.setPhoneNumber(userWebDTO.phoneNumber());
        existingUser.setAge(userWebDTO.age());
        
        // Comprobar si la contraseña ya está codificada o no
        if (userWebDTO.password() != null && !userWebDTO.password().isEmpty()) {
            if (!userWebDTO.password().startsWith("$2a$")) {
                // La contraseña no está codificada, codificarla
                existingUser.setPassword(passwordEncoder.encode(userWebDTO.password()));
            } else {
                existingUser.setPassword(userWebDTO.password());
            }
        }
        
        sanitizeUser(existingUser); //Sanitizacion ---------------------------------
        userRepository.save(existingUser);
        
        // Debug para verificar la actualización
        System.out.println("Usuario actualizado: " + existingUser.getEmail());
        System.out.println("Contraseña actualizada: " + (existingUser.getPassword() != null ? "Sí" : "No"));
    }

    public UserWebDTO findByEmail(String email) {
        UserWebDTO currentUser = getUser();
        if (currentUser == null || (!currentUser.email().equals(email) && currentUser.role() != UserRole.ADMIN)) {
            throw new SecurityException("Access Denied");
        }
        User user = userRepository.findByEmail(email).orElse(null);
        return user != null ? userWebMapper.toDTO(user) : null;
    }

    public void deleteOwnUser() {//invalidar sesion???
        UserWebDTO currentUser = getUser();
        if (currentUser != null) {
            userRepository.deleteById(currentUser.id());
            
        }
    }

    public void deleteUser(String email) {
        UserWebDTO currentUser = getUser();
        if (currentUser == null) {
            throw new SecurityException("Not authenticated");
        }
        User userToDelete = userRepository.findByEmail(email).orElse(null);
        if (userToDelete == null) {
            return;
        }
        if (!currentUser.id().equals(userToDelete.getId()) && currentUser.role() != UserRole.ADMIN) {
            throw new SecurityException("Access Denied");
        }
        userRepository.delete(userToDelete);
    }

    public String encodePassword(String rawPassword) {
        return passwordEncoder.encode(rawPassword);
    }

    // Método mejorado para obtener todos los usuarios
    @Transactional(readOnly = true)
    public List<UserWebDTO> getAllUsersInitialized() {
        UserWebDTO currentUser = getUser();
        if (currentUser == null || currentUser.role() != UserRole.ADMIN) {
            throw new SecurityException("Access Denied");
        }
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
        UserWebDTO currentUser = getUser();
        if (currentUser == null || (userWebDTO != null && !currentUser.id().equals(userWebDTO.id()) && currentUser.role() != UserRole.ADMIN)) {
            throw new SecurityException("Access Denied");
        }
        User user = userRepository.findById(userWebDTO.id()).orElse(null);
        if (user != null) {
            user.setPassword(password);
            user = userRepository.save(user);
        }
        return userWebMapper.toDTO(user);
    }

    public UserWebDTO setFirstName(UserWebDTO userWebDTO, String firstName) {
        UserWebDTO currentUser = getUser();
        if (currentUser == null || (userWebDTO != null && !currentUser.id().equals(userWebDTO.id()) && currentUser.role() != UserRole.ADMIN)) {
            throw new SecurityException("Access Denied");
        }
        User user = userRepository.findById(userWebDTO.id()).orElse(null);
        if (user != null) {
            user.setFirstName(firstName);
            user = userRepository.save(user);
        }
        return userWebMapper.toDTO(user);
    }

    public UserWebDTO setLastName(UserWebDTO userWebDTO, String lastName) {
        UserWebDTO currentUser = getUser();
        if (currentUser == null || (userWebDTO != null && !currentUser.id().equals(userWebDTO.id()) && currentUser.role() != UserRole.ADMIN)) {
            throw new SecurityException("Access Denied");
        }
        User user = userRepository.findById(userWebDTO.id()).orElse(null);
        if (user != null) {
            user.setLastName(lastName);
            user = userRepository.save(user);
        }
        return userWebMapper.toDTO(user);
    }

    public UserWebDTO setEmail(UserWebDTO userWebDTO, String email) {
        UserWebDTO currentUser = getUser();
        if (currentUser == null || (userWebDTO != null && !currentUser.id().equals(userWebDTO.id()) && currentUser.role() != UserRole.ADMIN)) {
            throw new SecurityException("Access Denied");
        }
        User user = userRepository.findById(userWebDTO.id()).orElse(null);
        if (user != null) {
            user.setEmail(email);
            user = userRepository.save(user);
        }
        return userWebMapper.toDTO(user);
    }

    public UserWebDTO setAddress(UserWebDTO userWebDTO, String address) {
        UserWebDTO currentUser = getUser();
        if (currentUser == null || (userWebDTO != null && !currentUser.id().equals(userWebDTO.id()) && currentUser.role() != UserRole.ADMIN)) {
            throw new SecurityException("Access Denied");
        }
        User user = userRepository.findById(userWebDTO.id()).orElse(null);
        if (user != null) {
            user.setAddress(address);
            user = userRepository.save(user);
        }
        return userWebMapper.toDTO(user);
    }

    public UserWebDTO setPhoneNumber(UserWebDTO userWebDTO, int phoneNumber) {
        UserWebDTO currentUser = getUser();
        if (currentUser == null || (userWebDTO != null && !currentUser.id().equals(userWebDTO.id()) && currentUser.role() != UserRole.ADMIN)) {
            throw new SecurityException("Access Denied");
        }
        User user = userRepository.findById(userWebDTO.id()).orElse(null);
        if (user != null) {
            user.setPhoneNumber(phoneNumber);
            user = userRepository.save(user);
        }
        return userWebMapper.toDTO(user);
    }

    public UserWebDTO setAge(UserWebDTO userWebDTO, int age) {
        UserWebDTO currentUser = getUser();
        if (currentUser == null || (userWebDTO != null && !currentUser.id().equals(userWebDTO.id()) && currentUser.role() != UserRole.ADMIN)) {
            throw new SecurityException("Access Denied");
        }
        User user = userRepository.findById(userWebDTO.id()).orElse(null);
        if (user != null) {
            user.setAge(age);
            user = userRepository.save(user);
        }
        return userWebMapper.toDTO(user);
    }

    public UserWebDTO setRole(UserWebDTO userWebDTO, UserRole role) {
        UserWebDTO currentUser = getUser();
        if (currentUser == null || currentUser.role() != UserRole.ADMIN) {
            throw new SecurityException("Access Denied: Only admins can change roles.");
        }
        User user = userWebMapper.toDomain(userWebDTO);
        user.setRole(role);
        return userWebMapper.toDTO(user);
    }

    public UserWebDTO addOrder(UserWebDTO userWebDTO, Order order) {
        UserWebDTO currentUser = getUser();
        if (currentUser == null || (userWebDTO != null && !currentUser.id().equals(userWebDTO.id()) && currentUser.role() != UserRole.ADMIN)) {
            throw new SecurityException("Access Denied");
        }
        User user = userWebMapper.toDomain(userWebDTO);
        user.addOrder(order);
        return userWebMapper.toDTO(user);
    }

    public UserWebDTO addReview(UserWebDTO userWebDTO, ReviewDTO reviewDTO) {
        UserWebDTO currentUser = getUser();
        if (currentUser == null || (userWebDTO != null && !currentUser.id().equals(userWebDTO.id()) && currentUser.role() != UserRole.ADMIN)) {
            throw new SecurityException("Access Denied");
        }
        User user = userWebMapper.toDomain(userWebDTO);
        Review review = reviewMapper.toDomain(reviewDTO);
        user.addReview(review);
        return userWebMapper.toDTO(user);
    }

    public String getAddress(UserWebDTO userWebDTO) {
        UserWebDTO currentUser = getUser();
        if (currentUser == null || (userWebDTO != null && !currentUser.id().equals(userWebDTO.id()) && currentUser.role() != UserRole.ADMIN)) {
            throw new SecurityException("Access Denied");
        }
        User user = userWebMapper.toDomain(userWebDTO);
        return user.getAddress();
    }

    public Order getCurrentOrder(User user) {
        if (user == null || user.getId() == null) {
            return null;
        }
        return userCurrentOrders.get(user.getId());
    }

    public void setCurrentOrder(User user, Order currentOrder) {
        if (user != null && user.getId() != null) {
            userCurrentOrders.put(user.getId(), currentOrder);
            user.setCurrentOrder(currentOrder);
        }
    }

    public List<UserDTO> getUsers() {
        UserWebDTO currentUser = getUser();
        if (currentUser == null || currentUser.role() != UserRole.ADMIN) {
            throw new SecurityException("Access Denied");
        }
        return userRepository.findAll().stream()
            .map(userMapper::toDTO)
            .collect(Collectors.toList());
    }

    public User getUserByFirstName(String userName) {
       UserWebDTO currentUser = getUser();
       if (currentUser == null || currentUser.role() != UserRole.ADMIN) {
            throw new SecurityException("Access Denied");
       }
       return UserRepository.findByFirstName(userName).orElse(null);
    }


    public UserDTO getUserApiById(Long id) {
        UserWebDTO currentUser = getUser();
        if (currentUser == null || (!currentUser.id().equals(id) && currentUser.role() != UserRole.ADMIN)) {
            throw new SecurityException("Access Denied");
        }
        User user=userRepository.findById(id).orElse(null);
        return userMapper.toDTO(user);
    }


    public UserDTO toDTO(UserWebDTO userWebDTO) {
        UserWebDTO currentUser = getUser();
        if (currentUser == null || (userWebDTO != null && !currentUser.id().equals(userWebDTO.id()) && currentUser.role() != UserRole.ADMIN)) {
            throw new SecurityException("Access Denied");
        }
        User user=userRepository.findById(userWebDTO.id()).orElse(null);
        Hibernate.initialize(user.getOrders());
        Hibernate.initialize(user.getReviews());
        UserDTO userDTO = userMapper.toDTO(user);
        return userDTO;
    }

    public User saveUser(User user) {
        UserWebDTO currentUser = getUser();
        if (currentUser == null || (user != null && !currentUser.id().equals(user.getId()) && currentUser.role() != UserRole.ADMIN)) {
            throw new SecurityException("Access Denied");
        }
        return userRepository.save(user);
    }

    public User getUserByEmail(String email) {
        UserWebDTO currentUser = getUser();
        if (currentUser == null || (!currentUser.email().equals(email) && currentUser.role() != UserRole.ADMIN)) {
            throw new SecurityException("Access Denied");
        }
        return userRepository.findByEmail(email).orElse(null);
    }

}