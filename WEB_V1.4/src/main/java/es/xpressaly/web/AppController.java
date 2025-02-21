/*
package es.xpressaly.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import UserService;
//import org.springframework.web.bind.annotation.RequestMethod;


@Controller
public class AppController {
    @GetMapping("/")
    public String start(Model model) {
        return "App";
    }
    
    @RequestMapping("/register") // create new account
    public String register(Model model, @RequestParam String email, @RequestParam String password) {

        return "start_template";
    }

    @PostMapping("/signin") // sign with an existent account
    public String signin(Model model, @RequestParam String firstName,@RequestParam String lastName,@RequestParam String email,@RequestParam String password,@RequestParam String address,@RequestParam int phoneNumber,@RequestParam int edad) {
        createUser(firstName,lastName,email,password,address,phoneNumber,edad);
        return "start_template";
    }

    @GetMapping("/profile")
    public String openProfile() {
        return "profile_template";
    }
    
    
}









package es.xpressaly.web;

import es.xpressaly.model.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@Controller
public class AppController {

    // Simulación de una base de datos en memoria
    private static List<User> users = new ArrayList<>();
    private static List<Product> products = new ArrayList<>();
    private static List<Order> orders = new ArrayList<>();
    private static List<Cart> carts = new ArrayList<>();
    private static List<Review> reviews = new ArrayList<>();

    // Inicialización de datos de prueba
    static {
        // Añadir usuarios de prueba
        users.add(new User("1", "John Doe", "john.doe@example.com", "password123", "+34 123 456 789", 30, "Calle Falsa 123, Madrid"));
        users.add(new User("2", "Jane Smith", "jane.smith@example.com", "password456", "+34 987 654 321", 25, "Avenida Real 456, Barcelona"));

        // Añadir productos de prueba
        products.add(new Product("1", "Xiaomi Redmi Note 14", "Electrónica", 199.99, "Smartphone de 6+128GB, Pantalla de 6.67\" AMOLED FHD+ 120Hz"));
        products.add(new Product("2", "Cargador Rápido", "Electrónica", 19.99, "Cargador rápido de 33W"));
        products.add(new Product("3", "Auriculares Inalámbricos", "Electrónica", 49.99, "Auriculares Bluetooth con cancelación de ruido"));
    }

    // Ruta para la página de inicio
    @GetMapping("/")
    public String start(Model model) {
        model.addAttribute("products", products);
        return "start_template";
    }

    // Ruta para el registro de usuarios
    @PostMapping("/register")
    public String register(@RequestParam String email, @RequestParam String password) {
        User newUser = new User(
                String.valueOf(users.size() + 1), // ID autoincremental
                "Nombre por defecto", // Nombre por defecto
                email,
                password,
                "", // Teléfono vacío
                0, // Edad por defecto
                "" // Dirección vacía
        );
        users.add(newUser);
        return "redirect:/";
    }

    // Ruta para el inicio de sesión
    @PostMapping("/signin")
    public String signin(@RequestParam String email, @RequestParam String password, Model model) {
        User user = users.stream()
                .filter(u -> u.getEmail().equals(email) && u.getPassword().equals(password))
                .findFirst()
                .orElse(null);

        if (user != null) {
            model.addAttribute("user", user);
            return "redirect:/profile";
        } else {
            return "redirect:/";
        }
    }

    // Ruta para el perfil del usuario
    @GetMapping("/profile")
    public String profile(Model model) {
        // Simulación de un usuario logueado (podrías usar un sistema de sesiones más adelante)
        User user = users.get(0); // Obtener el primer usuario de prueba
        model.addAttribute("user", user);
        return "profile_template";
    }

    // Ruta para añadir un producto al carrito
    @PostMapping("/addToCart")
    public String addToCart(@RequestParam String productId, @RequestParam String userId) {
        Product product = products.stream()
                .filter(p -> p.getId().equals(productId))
                .findFirst()
                .orElse(null);

        if (product != null) {
            Cart cart = carts.stream()
                    .filter(c -> c.getUserId().equals(userId))
                    .findFirst()
                    .orElse(new Cart(userId));
            cart.addProduct(product);
            carts.add(cart);
        }
        return "redirect:/";
    }

    // Ruta para ver el carrito
    @GetMapping("/cart")
    public String viewCart(@RequestParam String userId, Model model) {
        Cart cart = carts.stream()
                .filter(c -> c.getUserId().equals(userId))
                .findFirst()
                .orElse(new Cart(userId));
        model.addAttribute("cart", cart);
        return "cart_template";
    }
}

*/


package es.xpressaly.web;

import es.xpressaly.model.Product;
import es.xpressaly.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class AppController {

    @Autowired // Inyectar el servicio
    private ProductService productService;

    // Ruta para la página de inicio
    @GetMapping("/")
    public String start(Model model) {
        model.addAttribute("products", productService.getAllProducts()); // Usar el servicio
        return "start_template";
    }

    // Ruta para añadir un producto al carrito
    @PostMapping("/addToCart")
    public String addToCart(@RequestParam Long productId, @RequestParam String userId) {
        Product product = productService.getAllProducts().stream()
                .filter(p -> p.getId().equals(productId))
                .findFirst()
                .orElse(null);

        if (product != null) {
            // Lógica para añadir al carrito (usar el servicio de carrito si lo tienes)
        }
        return "redirect:/";
    }
}