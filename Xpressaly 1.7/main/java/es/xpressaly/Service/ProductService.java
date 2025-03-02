package es.xpressaly.Service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import es.xpressaly.Model.Product;

@Service
public class ProductService {

    private static final List<Product> products = new ArrayList<>();
    private static Long nextId = 1L;  // Generador de ID manual

    public ProductService() {
        if (products.isEmpty()) {
            initializeDefaultProducts();
        }
    }

    private void initializeDefaultProducts() {
        addProduct(new Product("Wireless mouse", "2.4G Wireless Bluetooth Mouse Ergonomic 800/1200/1600DPI 6 Silent Buttons for MacBook Tablet Laptops Computer PC", 50, 25, "/Images/wireless_mouse.jpg"));
        addProduct(new Product("Keyboard", "Subblim Business Slim Silent, Spanish Keyboard Layout (QWERTY), Plug & Play via USB, Flat Design, Silent Typing, Black", 40, 2, "/Images/keyboard.jpg"));
        addProduct(new Product("Phone", "Global Version realme C53 Smartphone 6.74'' 90Hz Screen 50MP AI Camera Powerful Octa-core Chipset 33W 5000mAh NFC Mobile Phone", 200, 67, "/Images/phone.jpg"));
        addProduct(new Product("Television", "24 LED television with HD resolution with Dolby system, flash memory", 150, 3, "/Images/television.jpg"));
        addProduct(new Product("Smartwatch Fitness Pro", "Smartwatch con monitor de frecuencia cardíaca, contador de pasos, notificaciones de smartphone y resistencia al agua. Ideal para deportistas.", 120, 15, "/Images/smartwatch_fitness_pro.jpg"));
        addProduct(new Product("Noise-Cancelling Headphones", "Auriculares inalámbricos con cancelación de ruido, sonido envolvente y hasta 20 horas de autonomía. Compatible con Bluetooth 5.0.", 90, 30, "/Images/noise_cancelling_headphones.jpg"));
        addProduct(new Product("Portable Projector", "Proyector portátil con resolución 1080p, conexión HDMI y USB, ideal para películas y presentaciones en casa o la oficina.", 180, 10, "/Images/portable_projector.jpg"));
        addProduct(new Product("Electric Kettle", "Hervidor eléctrico de acero inoxidable con capacidad de 1.7 litros, apagado automático y base giratoria 360°.", 35, 50, "/Images/electric_kettle.jpg"));
        addProduct(new Product("Robot Vacuum Cleaner", "Aspiradora robot inteligente con navegación láser, mapeo de habitaciones y control desde app móvil.", 250, 12, "/Images/robot_vacuum_cleaner.jpg"));
        addProduct(new Product("Bluetooth Speaker", "Altavoz Bluetooth portátil con sonido estéreo, resistencia al agua y hasta 15 horas de reproducción continua.", 60, 40, "/Images/bluetooth_speaker.jpg"));
        addProduct(new Product("Gaming Chair", "Silla ergonómica para gamers con soporte lumbar, reposacabezas ajustable y base de metal resistente.", 150, 8, "/Images/gaming_chair.jpg"));
        addProduct(new Product("External Hard Drive", "Disco duro externo de 2TB, USB 3.0, compatible con PC y Mac, diseño compacto y resistente a golpes.", 80, 25, "/Images/external_hard_drive.jpg"));
        addProduct(new Product("Air Purifier", "Purificador de aire con filtro HEPA, 3 niveles de velocidad y modo nocturno silencioso para ambientes limpios y saludables.", 130, 18, "/Images/air_purifier.jpg"));
        addProduct(new Product("Electric Scooter", "Patinete eléctrico plegable con autonomía de 25 km, velocidad máxima de 25 km/h y pantalla LED.", 300, 5, "/Images/electric_scooter.jpg"));
        addProduct(new Product("Wireless Charging Pad", "Base de carga inalámbrica compatible con smartphones Qi, diseño compacto y carga rápida.", 25, 60, "/Images/wireless_charging_pad.jpg"));
        addProduct(new Product("Smart Bulb", "Bombilla inteligente controlable por app, 16 millones de colores, programable y compatible con Alexa y Google Home.", 20, 100, "/Images/smart_bulb.jpg"));
        addProduct(new Product("Laptop Cooling Pad", "Base refrigeradora para portátiles con 4 ventiladores silenciosos, ajuste de altura y luz LED azul.", 30, 35, "/Images/laptop_cooling_pad.jpg"));
        addProduct(new Product("Fitness Tracker Band", "Pulsera de actividad con monitor de sueño, contador de pasos, notificaciones y resistencia al agua.", 50, 45, "/Images/fitness-tracker-band.jpg"));
        addProduct(new Product("Electric Toothbrush", "Cepillo de dientes eléctrico recargable con 3 modos de limpieza, temporizador y cabezal intercambiable.", 40, 70, "/Images/electric_toothbrush.jpg"));
        addProduct(new Product("Desk Organizer", "Organizador de escritorio con compartimentos para bolígrafos, teléfono, tarjetas y cables. Diseño moderno en madera.", 25, 20, "/Images/desk_organizer.jpg"));
        addProduct(new Product("USB-C Hub", "Hub USB-C con puertos HDMI, USB 3.0, SD card reader y carga rápida. Compatible con MacBook y otros dispositivos USB-C.", 35, 50, "/Images/usb_c_hub.jpg"));
        addProduct(new Product("E-Reader", "Lector de libros electrónicos con pantalla antirreflejos, luz integrada y almacenamiento para miles de libros.", 110, 22, "/Images/e_reader.jpg"));
        addProduct(new Product("Mini Drone", "Dron mini con cámara HD, control por app, modo sin cabeza y tiempo de vuelo de 10 minutos. Ideal para principiantes.", 70, 15, "/Images/mini_drone.jpg"));
        addProduct(new Product("Electric Blanket", "Manta eléctrica con 3 niveles de calor, apagado automático y tejido suave y lavable.", 55, 30, "/Images/electric_blanket.jpg"));
    }

    public List<Product> getAllProducts() {
        return products;
    }

    public List<Product> getProductsByPage(int page, int pageSize) {
        int startIndex = (page - 1) * pageSize;
        int endIndex = Math.min(startIndex + pageSize, products.size());
        
        if (startIndex >= products.size()) {
            return new ArrayList<>();
        }
        
        return new ArrayList<>(products.subList(startIndex, endIndex));
    }

    public List<Product> searchProducts(String query) {
        if (query == null || query.trim().isEmpty()) {
            return products;
        }
        
        String searchQuery = query.toLowerCase();
        return products.stream()
            .filter(p -> p.getName().toLowerCase().contains(searchQuery) ||
                    p.getDescription().toLowerCase().contains(searchQuery))
            .toList();
    }
    public Product getProductById(Long id) {
        return products.stream().filter(p -> p.getId().equals(id)).findFirst().orElse(null);
    }

    public void addProduct(Product product) {
        product.setId(nextId++);  // Asignamos un ID único
        products.add(product);
    }

    public void deleteProduct(Long productId) {
        products.removeIf(p -> p.getId().equals(productId));  // Elimina el producto por su ID
    }
}
