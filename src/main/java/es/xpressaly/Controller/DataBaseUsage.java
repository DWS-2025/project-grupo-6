package es.xpressaly.Controller;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.security.crypto.password.PasswordEncoder;

import es.xpressaly.Model.Product;
import es.xpressaly.Model.User;
import es.xpressaly.Model.UserRole;
import es.xpressaly.Model.Review;
import es.xpressaly.Model.Order;
import es.xpressaly.Model.OrderProduct;
import es.xpressaly.Repository.OrderRepository;
import es.xpressaly.Repository.ProductRepository;
import es.xpressaly.Repository.ReviewRepository;
import es.xpressaly.Repository.UserRepository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Random;


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
	@Autowired
	private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        // Save some customers
        User user;
        user=new User("Juan", "Pérez", "juan.perez@email.com", passwordEncoder.encode("Password123"), "Calle Ficticia 123", 213412398, 25);
        user.setRole(UserRole.ADMIN);
        User adminUser = userRepository.save(user);
        
        user=new User("Maria", "García", "maria.garcia@email.com", passwordEncoder.encode("Test4567"), "Avenida Principal 456", 567814785, 30);
        user.setRole(UserRole.USER);
        User mariaUser = userRepository.save(user);
        
        user=new User("Lucas", "López", "lucas.lopez@email.com", passwordEncoder.encode("Hola1234"), "Calle Real 666", 678542132, 20);
        user.setRole(UserRole.USER);
        User lucasUser = userRepository.save(user);
        
        // Save initial products
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
            
            product = new Product("Wireless Earbuds", "Bluetooth 5.0 earbuds with noise cancellation, touch controls and 24-hour battery life with charging case.", 75, 40, "/Images/Wireless Earbuds.jpg");
            loadImageForProduct(product, "Wireless Earbuds.jpg");
            productRepository.save(product);
            
            product = new Product("Digital Camera", "20MP digital camera with 4K video recording, 30x optical zoom and image stabilization.", 299, 15, "/Images/Digital Camera.jpg");
            loadImageForProduct(product, "Digital Camera.jpg");
            productRepository.save(product);
            
            product = new Product("Coffee Maker", "Programmable coffee maker with 12-cup capacity, auto shut-off and brew strength control.", 85, 25, "/Images/Coffee Maker.jpg");
            loadImageForProduct(product, "Coffee Maker.jpg");
            productRepository.save(product);
            
            product = new Product("Bluetooth Speaker", "Portable waterproof Bluetooth speaker with 360° sound, 20-hour battery life and built-in microphone.", 65, 30, "/Images/Bluetooth Speaker.jpg");
            loadImageForProduct(product, "Bluetooth Speaker.jpg");
            productRepository.save(product);
            
            product = new Product("External Hard Drive", "1TB portable external hard drive with USB 3.0 for fast data transfer and backup software included.", 79, 22, "/Images/External Hard Drive.jpg");
            loadImageForProduct(product, "External Hard Drive.jpg");
            productRepository.save(product);
            
            product = new Product("Gaming Headset", "Surround sound gaming headset with noise-cancelling microphone, RGB lighting and comfortable memory foam ear cups.", 95, 35, "/Images/Gaming Headset.jpg");
            loadImageForProduct(product, "Gaming Headset.jpg");
            productRepository.save(product);
            
            product = new Product("Tablet Stand", "Adjustable tablet stand with 360° rotation, compatible with all tablets and e-readers from 7 to 13 inches.", 25, 50, "/Images/Tablet Stand.jpg");
            loadImageForProduct(product, "Tablet Stand.jpg");
            productRepository.save(product);
            
            product = new Product("Wireless Charger", "10W fast wireless charging pad compatible with all Qi-enabled devices, LED indicator and anti-slip surface.", 30, 60, "/Images/Wireless Charger.jpg");
            loadImageForProduct(product, "Wireless Charger.jpg");
            productRepository.save(product);
            
            product = new Product("Smart Scale", "Bluetooth smart scale that measures weight, BMI, body fat percentage and other metrics with smartphone app integration.", 45, 20, "/Images/Smart Scale.jpg");
            loadImageForProduct(product, "Smart Scale.jpg");
            productRepository.save(product);
            
            product = new Product("Desk Lamp", "LED desk lamp with 5 brightness levels, 5 color modes, USB charging port and adjustable arm.", 35, 40, "/Images/Desk Lamp.jpg");
            loadImageForProduct(product, "Desk Lamp.jpg");
            productRepository.save(product);
            
            product = new Product("Security Camera", "Indoor wireless security camera with 1080p HD video, motion detection, night vision and two-way audio.", 60, 25, "/Images/Security Camera.jpg");
            loadImageForProduct(product, "Security Camera.jpg");
            productRepository.save(product);
            
            product = new Product("Digital Alarm Clock", "Digital alarm clock with large LED display, dual alarm settings, USB charging ports and battery backup.", 28, 45, "/Images/Digital Alarm Clock.jpg");
            loadImageForProduct(product, "Digital Alarm Clock.jpg");
            productRepository.save(product);
            
            product = new Product("Portable Projector", "Mini portable projector with 1080p support, built-in speakers, HDMI and USB connectivity for home theater.", 180, 12, "/Images/Portable Projector.jpg");
            loadImageForProduct(product, "Portable Projector.jpg");
            productRepository.save(product);
            
            product = new Product("Electric Fan", "Oscillating tower fan with 3 speed settings, remote control, timer function and quiet operation.", 70, 30, "/Images/Electric Fan.jpg");
            loadImageForProduct(product, "Electric Fan.jpg");
            productRepository.save(product);
            
            product = new Product("Laptop Cooling Pad", "Laptop cooling pad with 5 quiet fans, adjustable height settings and blue LED lights for laptops up to 17 inches.", 40, 35, "/Images/Laptop Cooling Pad.jpg");
            loadImageForProduct(product, "Laptop Cooling Pad.jpg");
            productRepository.save(product);
            
            product = new Product("Power Bank", "20000mAh power bank with fast charging, dual USB outputs, LED indicator and compact design.", 45, 50, "/Images/Power Bank.jpg");
            loadImageForProduct(product, "Power Bank.jpg");
            productRepository.save(product);
            
            product = new Product("Car Phone Mount", "Universal car phone mount with 360° rotation, one-touch locking and dashboard/windshield installation.", 18, 75, "/Images/Car Phone Mount.jpg");
            loadImageForProduct(product, "Car Phone Mount.jpg");
            productRepository.save(product);
            
            product = new Product("Smart Doorbell", "WiFi video doorbell with 1080p HD video, two-way talk, motion detection and cloud storage options.", 120, 20, "/Images/Smart Doorbell.jpg");
            loadImageForProduct(product, "Smart Doorbell.jpg");
            productRepository.save(product);
            
            product = new Product("Microwave Oven", "Compact 0.7 cubic feet microwave oven with 700W power, 10 power levels and 6 quick-set menu buttons.", 89, 15, "/Images/Microwave Oven.jpg");
            loadImageForProduct(product, "Microwave Oven.jpg");
            productRepository.save(product);
            
            product = new Product("Hair Dryer", "Professional hair dryer with ionic technology, 3 heat settings, 2 speed settings and concentrator attachment.", 50, 40, "/Images/Hair Dryer.jpg");
            loadImageForProduct(product, "Hair Dryer.jpg");
            productRepository.save(product);
            
            product = new Product("USB Hub", "7-port USB 3.0 hub with individual power switches, LED indicators and 5Gbps data transfer speed.", 35, 55, "/Images/USB Hub.jpg");
            loadImageForProduct(product, "USB Hub.jpg");
            productRepository.save(product);
            
            product = new Product("Water Bottle", "Insulated stainless steel water bottle that keeps drinks cold for 24 hours or hot for 12 hours, 24oz capacity.", 25, 80, "/Images/Water Bottle.jpg");
            loadImageForProduct(product, "Water Bottle.jpg");
            productRepository.save(product);
            
            product = new Product("Wireless Keyboard", "Slim wireless keyboard with multimedia keys, ergonomic design and long battery life.", 45, 30, "/Images/Wireless Keyboard.jpg");
            loadImageForProduct(product, "Wireless Keyboard.jpg");
            productRepository.save(product);
            
            product = new Product("Digital Food Scale", "Kitchen digital food scale with high precision sensors, tare function and multiple unit conversion.", 20, 60, "/Images/Digital Food Scale.jpg");
            loadImageForProduct(product, "Digital Food Scale.jpg");
            productRepository.save(product);
            
            product = new Product("Blender", "Countertop blender with 700W motor, 5 speed settings, pulse function and 48oz glass jar.", 70, 25, "/Images/Blender.jpg");
            loadImageForProduct(product, "Blender.jpg");
            productRepository.save(product);
            
            product = new Product("Indoor Plant Pot", "Set of 3 ceramic indoor plant pots with bamboo trays in different sizes, modern design.", 32, 40, "/Images/Indoor Plant Pot.jpg");
            loadImageForProduct(product, "Indoor Plant Pot.jpg");
            productRepository.save(product);
            
            product = new Product("Bike Light Set", "Rechargeable bike light set with front and rear lights, multiple lighting modes and water resistance.", 28, 45, "/Images/Bike Light Set.jpg");
            loadImageForProduct(product, "Bike Light Set.jpg");
            productRepository.save(product);
            
            product = new Product("Cordless Drill", "20V cordless drill with lithium battery, 2-speed gearbox, LED work light and 10 drill bits included.", 85, 20, "/Images/Cordless Drill.jpg");
            loadImageForProduct(product, "Cordless Drill.jpg");
            productRepository.save(product);
            
            product = new Product("Fitness Resistance Bands", "Set of 5 fitness resistance bands with different resistance levels, carrying bag and exercise guide.", 22, 65, "/Images/Fitness Resistance Bands.jpg");
            loadImageForProduct(product, "Fitness Resistance Bands.jpg");
            productRepository.save(product);
            
            product = new Product("Hand Mixer", "5-speed hand mixer with stainless steel attachments, eject button and compact storage design.", 38, 25, "/Images/Hand Mixer.jpg");
            loadImageForProduct(product, "Hand Mixer.jpg");
            productRepository.save(product);
            
            product = new Product("Essential Oil Diffuser", "Ultrasonic essential oil diffuser with 300ml capacity, 7 color LED lights and automatic shut-off.", 32, 40, "/Images/Essential Oil Diffuser.jpg");
            loadImageForProduct(product, "Essential Oil Diffuser.jpg");
            productRepository.save(product);
            
            product = new Product("Wireless Mouse Pad", "Qi wireless charging mouse pad with integrated charger for smartphones and wireless mice.", 42, 30, "/Images/Wireless Mouse Pad.jpg");
            loadImageForProduct(product, "Wireless Mouse Pad.jpg");
            productRepository.save(product);
            
            product = new Product("Smart Wi-Fi Plug", "Wi-Fi enabled smart plug compatible with voice assistants, scheduling features and energy monitoring.", 25, 50, "/Images/Smart Wi-Fi Plug.jpg");
            loadImageForProduct(product, "Smart Wi-Fi Plug.jpg");
            productRepository.save(product);
            
            product = new Product("Selfie Ring Light", "10-inch ring light with tripod stand, 3 light modes, 10 brightness levels and phone holder for photos and videos.", 30, 35, "/Images/Selfie Ring Light.jpg");
            loadImageForProduct(product, "Selfie Ring Light.jpg");
            productRepository.save(product);
            
            product = new Product("Rice Cooker", "Digital rice cooker with 6-cup capacity, steamer basket, keep warm function and non-stick inner pot.", 55, 20, "/Images/Rice Cooker.jpg");
            loadImageForProduct(product, "Rice Cooker.jpg");
            productRepository.save(product);
            
            product = new Product("Slow Cooker", "Programmable slow cooker with 6-quart capacity, digital timer, 3 cooking settings and removable dishwasher-safe stoneware.", 48, 15, "/Images/Slow Cooker.jpg");
            loadImageForProduct(product, "Slow Cooker.jpg");
            productRepository.save(product);
            
            product = new Product("Smart Plant Monitor", "Bluetooth plant monitor that tracks soil moisture, light exposure, temperature and provides care recommendations via app.", 35, 30, "/Images/Smart Plant Monitor.jpg");
            loadImageForProduct(product, "Smart Plant Monitor.jpg");
            productRepository.save(product);
        
        // Get all saved products for creating reviews and orders
        List<Product> allProducts = productRepository.findAll();
        
        // Create reviews for various products from all users
        createReviewsForUsers(adminUser, mariaUser, lucasUser, allProducts);
        
        // Update product ratings after adding all reviews
        calculateAndUpdateProductRatings(allProducts);
        
        // Create orders for each user
        createOrdersForUsers(adminUser, mariaUser, lucasUser, allProducts);
    }
    
    /**
     * Creates sample reviews for products from all three users
     * Distribution: Some products with no reviews, some with 1-2 reviews, and popular ones with 3 reviews
     */
    private void createReviewsForUsers(User adminUser, User mariaUser, User lucasUser, List<Product> products) {
        Review review;
        
        // === POPULAR PRODUCTS (3 reviews each) ===
        
        // Wireless mouse (products.get(0)) - Popular tech product
        review = new Review(adminUser, "Excellent wireless mouse, very comfortable and responsive. Great battery life!", 5);
        review.setProduct(products.get(0));
        reviewRepository.save(review);
        
        review = new Review(mariaUser, "Good mouse but the scroll wheel is a bit sensitive. Overall satisfied with the purchase.", 4);
        review.setProduct(products.get(0));
        reviewRepository.save(review);
        
        review = new Review(lucasUser, "Perfect for gaming and work. The ergonomic design really makes a difference during long sessions.", 5);
        review.setProduct(products.get(0));
        reviewRepository.save(review);
        
        // Smartwatch Fitness Pro (products.get(4)) - Popular fitness product
        review = new Review(mariaUser, "This smartwatch is amazing! Tracks everything I need for my fitness routine.", 5);
        review.setProduct(products.get(4));
        reviewRepository.save(review);
        
        review = new Review(lucasUser, "Great for tracking workouts and the heart rate monitor is very accurate. Highly recommend!", 5);
        review.setProduct(products.get(4));
        reviewRepository.save(review);
        
        review = new Review(adminUser, "Solid fitness tracker with good battery life. The app could be better but overall satisfied.", 4);
        review.setProduct(products.get(4));
        reviewRepository.save(review);
        
        // Wireless Earbuds (products.get(13)) - Popular audio product
        review = new Review(lucasUser, "Wireless earbuds have excellent noise cancellation. Battery lasts all day.", 5);
        review.setProduct(products.get(13));
        reviewRepository.save(review);
        
        review = new Review(mariaUser, "Great for workouts! They stay in place and the sound quality is amazing.", 5);
        review.setProduct(products.get(13));
        reviewRepository.save(review);
        
        review = new Review(adminUser, "Impressed with the audio quality and the charging case is very convenient. Worth the price.", 5);
        review.setProduct(products.get(13));
        reviewRepository.save(review);
        
        // Coffee Maker (products.get(15)) - Popular kitchen appliance
        review = new Review(mariaUser, "Coffee maker works great every morning. Easy to use and clean.", 5);
        review.setProduct(products.get(15));
        reviewRepository.save(review);
        
        review = new Review(adminUser, "Brews excellent coffee with perfect temperature. The programmable feature is very handy.", 5);
        review.setProduct(products.get(15));
        reviewRepository.save(review);
        
        review = new Review(lucasUser, "Great coffee maker! The auto shut-off and brew strength control are perfect features.", 4);
        review.setProduct(products.get(15));
        reviewRepository.save(review);
        
        // === PRODUCTS WITH 2 REVIEWS ===
        
        // Keyboard (products.get(1))
        review = new Review(adminUser, "Good keyboard for the price. Silent typing works as advertised.", 4);
        review.setProduct(products.get(1));
        reviewRepository.save(review);
        
        review = new Review(lucasUser, "Keys feel great and it's really quiet. Perfect for late night coding sessions!", 5);
        review.setProduct(products.get(1));
        reviewRepository.save(review);
        
        // Phone (products.get(2))
        review = new Review(lucasUser, "The phone has great features for the price. Camera quality is impressive!", 4);
        review.setProduct(products.get(2));
        reviewRepository.save(review);
        
        review = new Review(mariaUser, "Battery life is amazing! Can easily last a full day with heavy usage. Love the camera.", 5);
        review.setProduct(products.get(2));
        reviewRepository.save(review);
        
        // Electric Kettle (products.get(7))
        review = new Review(mariaUser, "Electric kettle heats water quickly and automatically shuts off. Very convenient!", 5);
        review.setProduct(products.get(7));
        reviewRepository.save(review);
        
        review = new Review(adminUser, "Fast heating and good capacity. The automatic shut-off is a great safety feature.", 5);
        review.setProduct(products.get(7));
        reviewRepository.save(review);
        
        // Gaming Headset (products.get(18))
        review = new Review(lucasUser, "Gaming headset is comfortable for long sessions. Great sound quality!", 5);
        review.setProduct(products.get(18));
        reviewRepository.save(review);
        
        review = new Review(adminUser, "Excellent surround sound and the microphone is very clear. RGB lighting is a nice touch.", 5);
        review.setProduct(products.get(18));
        reviewRepository.save(review);
        
        // Bluetooth Speaker (products.get(16))
        review = new Review(lucasUser, "Bluetooth speaker has amazing sound quality and is truly waterproof!", 5);
        review.setProduct(products.get(16));
        reviewRepository.save(review);
        
        review = new Review(mariaUser, "Perfect for pool parties! Sound is clear and loud, and I love the portability.", 5);
        review.setProduct(products.get(16));
        reviewRepository.save(review);
        
        // Digital Camera (products.get(14))
        review = new Review(lucasUser, "Digital camera takes professional-quality photos. Love the zoom feature!", 5);
        review.setProduct(products.get(14));
        reviewRepository.save(review);
        
        review = new Review(mariaUser, "Perfect for capturing family moments. The image stabilization works really well.", 5);
        review.setProduct(products.get(14));
        reviewRepository.save(review);
        
        // Smart Bulb (products.get(10))
        review = new Review(adminUser, "Smart bulb works perfectly with voice assistants. Easy setup and great color options.", 5);
        review.setProduct(products.get(10));
        reviewRepository.save(review);
        
        review = new Review(mariaUser, "Love being able to control the lights from my phone! The color changing is so much fun.", 5);
        review.setProduct(products.get(10));
        reviewRepository.save(review);
        
        // === PRODUCTS WITH 1 REVIEW ===
        
        // Television (products.get(3))
        review = new Review(adminUser, "Television picture quality is good for the price range.", 4);
        review.setProduct(products.get(3));
        reviewRepository.save(review);
        
        // Electric Scooter (products.get(5)) - Mixed review
        review = new Review(lucasUser, "Electric scooter is fun to ride, but battery life could be better.", 3);
        review.setProduct(products.get(5));
        reviewRepository.save(review);
        
        // Fitness Tracker Band (products.get(6))
        review = new Review(mariaUser, "Fitness tracker band is accurate and comfortable to wear all day.", 4);
        review.setProduct(products.get(6));
        reviewRepository.save(review);
        
        // Robot Vacuum Cleaner (products.get(8)) - Mixed review
        review = new Review(mariaUser, "Robot vacuum works well but sometimes gets stuck under furniture.", 3);
        review.setProduct(products.get(8));
        reviewRepository.save(review);
        
        // Air Purifier (products.get(9))
        review = new Review(mariaUser, "The air purifier has made a noticeable difference in our home's air quality.", 4);
        review.setProduct(products.get(9));
        reviewRepository.save(review);
        
        // Electric Toothbrush (products.get(11))
        review = new Review(mariaUser, "Much better than my old manual toothbrush. Teeth feel cleaner and the timer is helpful.", 5);
        review.setProduct(products.get(11));
        reviewRepository.save(review);
        
        // Electric Blanket (products.get(12))
        review = new Review(mariaUser, "So cozy and warm! Perfect for cold winter nights. The different heat settings are great.", 5);
        review.setProduct(products.get(12));
        reviewRepository.save(review);
        
        // External Hard Drive (products.get(17))
        review = new Review(lucasUser, "External hard drive is fast and reliable. Great for backing up files.", 4);
        review.setProduct(products.get(17));
        reviewRepository.save(review);
        
        // Wireless Charger (products.get(20))
        review = new Review(lucasUser, "Wireless charger works perfectly with my phone. No more tangled cables!", 4);
        review.setProduct(products.get(20));
        reviewRepository.save(review);
        
        // Security Camera (products.get(23))
        review = new Review(adminUser, "The security camera has good image quality, but the app could be better.", 4);
        review.setProduct(products.get(23));
        reviewRepository.save(review);
        
        // Portable Projector (products.get(25))
        review = new Review(lucasUser, "Amazing for movie nights! Picture quality is surprisingly good for the size.", 5);
        review.setProduct(products.get(25));
        reviewRepository.save(review);
        
        // Power Bank (products.get(28))
        review = new Review(lucasUser, "Power bank charges my devices multiple times. Very reliable for travel.", 4);
        review.setProduct(products.get(28));
        reviewRepository.save(review);
        
        // Smart Doorbell (products.get(29))
        review = new Review(adminUser, "Excellent smart doorbell! Clear video quality and motion detection works perfectly.", 5);
        review.setProduct(products.get(29));
        reviewRepository.save(review);
        
        // Hair Dryer (products.get(31))
        review = new Review(mariaUser, "Hair dryer is powerful and dries my hair quickly without damage.", 4);
        review.setProduct(products.get(31));
        reviewRepository.save(review);
        
        // USB Hub (products.get(32)) - Lower rating
        review = new Review(lucasUser, "Works as expected but the LED indicators are too bright at night.", 3);
        review.setProduct(products.get(32));
        reviewRepository.save(review);
        
        // Essential Oil Diffuser (products.get(40))
        review = new Review(mariaUser, "Essential oil diffuser creates a lovely atmosphere. Love the color changing lights!", 5);
        review.setProduct(products.get(40));
        reviewRepository.save(review);
        
        // Rice Cooker (products.get(44))
        review = new Review(mariaUser, "Perfect rice every time! The keep warm function is amazing for busy schedules.", 5);
        review.setProduct(products.get(44));
        reviewRepository.save(review);
        
        // Note: Many products (like Tablet Stand, Desk Lamp, Digital Alarm Clock, etc) 
        // will have NO REVIEWS, making the distribution more realistic
    }
    
    /**
     * Creates sample orders for all three users
     */
    private void createOrdersForUsers(User adminUser, User mariaUser, User lucasUser, List<Product> products) {
        Random random = new Random();
        
        // Order 1 for Juan Pérez (Admin) - Office setup
        Order order1 = new Order(adminUser, adminUser.getAddress());
        order1.setUserOrderNumber(1);
        addProductToOrder(order1, products.get(0), 1); // Wireless mouse
        addProductToOrder(order1, products.get(1), 1); // Keyboard
        addProductToOrder(order1, products.get(23), 1); // Security Camera
        addProductToOrder(order1, products.get(22), 1); // Desk Lamp
        order1.calculateTotal();
        orderRepository.save(order1);
        
        // Order 2 for Juan Pérez (Admin) - Smart home
        Order order2 = new Order(adminUser, adminUser.getAddress());
        order2.setUserOrderNumber(2);
        addProductToOrder(order2, products.get(10), 3); // Smart Bulb (3 units)
        addProductToOrder(order2, products.get(42), 2); // Smart Wi-Fi Plug (2 units)
        addProductToOrder(order2, products.get(29), 1); // Smart Doorbell
        order2.calculateTotal();
        orderRepository.save(order2);
        
        // Order 1 for Maria García - Fitness and wellness
        Order order3 = new Order(mariaUser, mariaUser.getAddress());
        order3.setUserOrderNumber(1);
        addProductToOrder(order3, products.get(4), 1); // Smartwatch Fitness Pro
        addProductToOrder(order3, products.get(6), 1); // Fitness Tracker Band
        addProductToOrder(order3, products.get(38), 1); // Fitness Resistance Bands
        addProductToOrder(order3, products.get(33), 1); // Water Bottle
        order3.calculateTotal();
        orderRepository.save(order3);
        
        // Order 2 for Maria García - Kitchen appliances
        Order order4 = new Order(mariaUser, mariaUser.getAddress());
        order4.setUserOrderNumber(2);
        addProductToOrder(order4, products.get(15), 1); // Coffee Maker
        addProductToOrder(order4, products.get(7), 1); // Electric Kettle
        addProductToOrder(order4, products.get(35), 1); // Blender
        addProductToOrder(order4, products.get(44), 1); // Rice Cooker
        order4.calculateTotal();
        orderRepository.save(order4);
        
        // Order 3 for Maria García - Home comfort
        Order order5 = new Order(mariaUser, mariaUser.getAddress());
        order5.setUserOrderNumber(3);
        addProductToOrder(order5, products.get(9), 1); // Air Purifier
        addProductToOrder(order5, products.get(40), 1); // Essential Oil Diffuser
        addProductToOrder(order5, products.get(36), 1); // Indoor Plant Pot
        addProductToOrder(order5, products.get(46), 1); // Smart Plant Monitor
        order5.calculateTotal();
        orderRepository.save(order5);
        
        // Order 1 for Lucas López - Tech enthusiast
        Order order6 = new Order(lucasUser, lucasUser.getAddress());
        order6.setUserOrderNumber(1);
        addProductToOrder(order6, products.get(2), 1); // Phone
        addProductToOrder(order6, products.get(13), 1); // Wireless Earbuds
        addProductToOrder(order6, products.get(18), 1); // Gaming Headset
        addProductToOrder(order6, products.get(27), 1); // Power Bank
        order6.calculateTotal();
        orderRepository.save(order6);
        
        // Order 2 for Lucas López - Photography and entertainment
        Order order7 = new Order(lucasUser, lucasUser.getAddress());
        order7.setUserOrderNumber(2);
        addProductToOrder(order7, products.get(14), 1); // Digital Camera
        addProductToOrder(order7, products.get(25), 1); // Portable Projector
        addProductToOrder(order7, products.get(43), 1); // Selfie Ring Light
        addProductToOrder(order7, products.get(19), 1); // Tablet Stand
        order7.calculateTotal();
        orderRepository.save(order7);
        
        // Order 3 for Lucas López - Storage and connectivity
        Order order8 = new Order(lucasUser, lucasUser.getAddress());
        order8.setUserOrderNumber(3);
        addProductToOrder(order8, products.get(17), 1); // External Hard Drive
        addProductToOrder(order8, products.get(32), 1); // USB Hub
        addProductToOrder(order8, products.get(20), 2); // Wireless Charger (2 units)
        addProductToOrder(order8, products.get(28), 1); // Car Phone Mount
        order8.calculateTotal();
        orderRepository.save(order8);
    }
    
    /**
     * Helper method to add a product to an order
     */
    private void addProductToOrder(Order order, Product product, int quantity) {
        order.setProductQuantity(product, quantity);
    }
    
    /**
     * Updates the average rating for all products based on their reviews
     */
    private void calculateAndUpdateProductRatings(List<Product> products) {
        for (Product product : products) {
            List<Review> productReviews = reviewRepository.findByProduct(product);
            
            if (productReviews.isEmpty()) {
                product.setRating(0.0);
            } else {
                double totalRating = productReviews.stream()
                    .mapToDouble(Review::getRating)
                    .sum();
                double averageRating = totalRating / productReviews.size();
                product.setRating(averageRating);
            }
            
            productRepository.save(product);
        }
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


