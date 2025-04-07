package es.xpressaly.Controller;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import es.xpressaly.Model.Product;
import es.xpressaly.Model.User;
import es.xpressaly.Model.UserRole;
import es.xpressaly.Repository.OrderRepository;
import es.xpressaly.Repository.ProductRepository;
import es.xpressaly.Repository.ReviewRepository;
import es.xpressaly.Repository.UserRepository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;


@Controller
public class DataBaseUsage implements CommandLineRunner {
    
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private ReviewRepository reviewRepository;
    @Autowired
    private ProductRepository productRepository;

    @Override
    public void run(String... args) throws Exception {
        //Save some customers
        User user;
        user=new User("Juan", "Pérez", "juan.perez@email.com", "Password123", "Calle Ficticia 123", 213412398, 25);
        user.setRole(UserRole.ADMIN);
        userRepository.save(user);
        user=new User("Maria", "García", "maria.garcia@email.com", "Test4567", "Avenida Principal 456", 567814785, 30);
        user.setRole(UserRole.USER);
        userRepository.save(user);

        //Save initial products
            Product product;
            product = new Product("Wireless mouse", "2.4G Wireless Bluetooth Mouse Ergonomic 800/1200/1600DPI 6 Silent Buttons for MacBook Tablet Laptops Computer PC", 50, 25, "/Images/Wireless mouse.jpg");
            loadImageForProduct(product, "Wireless mouse.jpg");
            productRepository.save(product); 
            
            product = new Product("Keyboard", "Subblim Business Slim Silent, Spanish Keyboard Layout (QWERTY), Plug & Play via USB, Flat Design, Silent Typing, Black", 40, 2, "/Images/Keyboard.jpg");
            loadImageForProduct(product, "keyboard.jpg");
            productRepository.save(product);

            product = new Product("phone", "Global Version realme C53 Smartphone 6.74'' 90Hz Screen 50MP AI Camera Powerful Octa-core Chipset 33W 5000mAh NFC Mobile Phone", 200, 67, "/Images/phone.jpg");          
            loadImageForProduct(product, "phone.jpg");
            productRepository.save(product);

            product = new Product("Television", "24 LED television with HD resolution with Dolby system, flash memory", 150, 3, "/Images/Television.jpg");         
            loadImageForProduct(product, "television.jpg");
            productRepository.save(product);

            product = new Product("Smartwatch Fitness Pro", "Smartwatch with heart rate monitor, step counter, smartphone notifications, and water resistance. Ideal for athletes.", 120, 15, "/Images/Smartwatch Fitness Pro.jpg");          
            loadImageForProduct(product, "Smartwatch Fitness Pro.jpg");
            productRepository.save(product);

            product = new Product("Electric Scooter", "Foldable electric scooter with 25 km range, top speed of 25 km/h, and LED display.", 300, 5, "/Images/Electric Scooter.jpg");           
            loadImageForProduct(product, "Electric Scooter.jpg");
            productRepository.save(product);

            product = new Product("Fitness Tracker Band", "Activity tracker band with sleep monitor, step counter, notifications, and water resistance.", 50, 45, "/Images/Fitness Tracker Band.jpg");           
            loadImageForProduct(product, "Fitness Tracker Band.jpg");
            productRepository.save(product);

            product = new Product("Electric Kettle", "Stainless steel electric kettle with 1.7-liter capacity, automatic shut-off, and 360Â° swivel base.", 35, 50, "/Images/Electric Kettle.jpg");           
            loadImageForProduct(product, "Electric Kettle.jpg");
            productRepository.save(product);

            product = new Product("Robot Vacuum Cleaner", "Smart robot vacuum with laser navigation, room mapping, and app control.", 250, 12, "/Images/Robot Vacuum Cleaner.jpg");           
            loadImageForProduct(product, "Robot Vacuum Cleaner.jpg");
            productRepository.save(product);

            product = new Product("Air Purifier", "Air purifier with HEPA filter, 3 speed levels, and silent night mode for clean and healthy environments.", 130, 18, "/Images/Air Purifier.jpg");           
            loadImageForProduct(product, "Air Purifier.jpg");
            productRepository.save(product);

            product = new Product("Smart Bulb", "App-controllable smart bulb with 16 million colors, programmable, and compatible with Alexa and Google Home.", 20, 100, "/Images/Smart Bulb.jpg");            
            loadImageForProduct(product, "Smart Bulb.jpg");
            productRepository.save(product);

            product = new Product("Electric Toothbrush", "Rechargeable electric toothbrush with 3 cleaning modes, timer, and replaceable brush head.", 40, 70, "/Images/Electric Toothbrush.jpg");
            loadImageForProduct(product, "Electric Toothbrush.jpg");
            productRepository.save(product);

            product = new Product("Electric Blanket", "Electric blanket with 3 heat levels, automatic shut-off, and soft, washable fabric.", 55, 30, "/Images/Electric Blanket.jpg");
            loadImageForProduct(product, "Electric Blanket.jpg");
            productRepository.save(product);
    }
    
    /**
     * Loads an image from the static resources and sets it on the product
     * @param product The product to set the image on
     * @param imageName The name of the image file in the static/Images directory
     */
    private void loadImageForProduct(Product product, String imageName) {
        try {
            Resource resource = new ClassPathResource("static/Images/" + imageName);
            if (resource.exists()) {
                byte[] imageBytes = Files.readAllBytes(Paths.get(resource.getURI()));
                product.setImageData(imageBytes);
            } else {
                System.out.println("Image not found: " + imageName);
            }
        } catch (IOException e) {
            System.err.println("Error loading image: " + imageName + " - " + e.getMessage());
        }
    }
}


