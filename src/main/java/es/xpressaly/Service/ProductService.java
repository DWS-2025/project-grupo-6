package es.xpressaly.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import es.xpressaly.Model.Product;
import es.xpressaly.Model.Review;
import es.xpressaly.Repository.ProductRepository;
import es.xpressaly.dto.ProductDTO;
import es.xpressaly.dto.ProductWebDTO;
import es.xpressaly.mapper.ProductMapper;
import es.xpressaly.mapper.ProductWebMapper;

@Service
@Transactional
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private ProductWebMapper productWebMapper;

    @Autowired
    private SanitizationService sanitizationService;

    public List<ProductDTO> getAllProducts() {
        return productRepository.findAll().stream()
            .map(productMapper::toDTO)
            .collect(Collectors.toList());
    }

    private void sanitizeProduct(Product product) {
        if (product.getName() != null)
            product.setName(sanitizationService.sanitize(product.getName()));
        if (product.getDescription() != null)
            product.setDescription(sanitizationService.sanitize(product.getDescription()));
        if (product.getImagePath() != null)
            product.setImagePath(sanitizationService.sanitize(product.getImagePath()));
    }



    public Page<ProductDTO> getProductsByPage(Pageable pageable, String sort) {
        try {
            System.out.println("Fetching products with pageable: " + pageable);
            Page<Product> productPage = productRepository.findAll(pageable);
            System.out.println("Found " + productPage.getTotalElements() + " products");
            
            if (productPage == null || productPage.isEmpty()) {
                System.out.println("No products found");
                return Page.empty(pageable);
            }
            
            return productPage.map(productMapper::toDTO);
        } catch (Exception e) {
            e.printStackTrace();
            return Page.empty(pageable);
        }
    }

    public Page<ProductDTO> getProductsByPageAndPrice(Pageable pageable, String sort, double minPrice, double maxPrice) {
        return productRepository.findByPriceBetween(minPrice, maxPrice, pageable)
            .map(productMapper::toDTO);
    }

    public Page<ProductDTO> getProductsByMinRating(int minRating, Pageable pageable) {
        return productRepository.findByMinRating(minRating, pageable)
            .map(productMapper::toDTO);
    }

    public Page<ProductDTO> getProductsByPriceAndMinRating(double minPrice, double maxPrice, int minRating, Pageable pageable) {
        return productRepository.findByPriceBetweenAndMinRating(minPrice, maxPrice, minRating, pageable)
            .map(productMapper::toDTO);
    }

    public Page<ProductDTO> searchProductsByMinRating(String searchTerm, int minRating, Pageable pageable) {
        return productRepository.findByNameContainingAndMinRating(searchTerm, minRating, pageable)
            .map(productMapper::toDTO);
    }

    public Page<ProductDTO> searchProductsByPriceAndMinRating(String searchTerm, double minPrice, double maxPrice, int minRating, Pageable pageable) {
        return productRepository.findByNameContainingAndPriceBetweenAndMinRating(searchTerm, minPrice, maxPrice, minRating, pageable)
            .map(productMapper::toDTO);
    }

    public List<ProductDTO> searchProducts(String term) {
        return productRepository.findByNameContainingIgnoreCase(term).stream()
            .map(productMapper::toDTO)
            .collect(Collectors.toList());
    }

    public List<ProductDTO> searchProductsByPrice(String term, double minPrice, double maxPrice) {
        return productRepository.findByNameContainingIgnoreCaseAndPriceBetween(term, minPrice, maxPrice).stream()
            .map(productMapper::toDTO)
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Product getProductById(Long id) {
        Product product = productRepository.findById(id).orElse(null);
        if (product != null) {
            // Forzar la inicialización de las colecciones lazy
            Hibernate.initialize(product.getReviews());
            if (product.getReviews() != null) {
                product.getReviews().forEach(review -> {
                    Hibernate.initialize(review.getUser());
                });
            }
        }
        return product;
    }

    public ProductDTO getProductDTO(Long id) {
        Product product = productRepository.findById(id).orElse(null);
        return product != null ? productMapper.toDTO(product) : null;
    }

    public ProductWebDTO getProductByIdWeb(Long id) {
        Product product = productRepository.findById(id).orElse(null);
        return product != null ? productWebMapper.toDTO(product) : null;
    }

    public ProductDTO getProductWithReviews(Long id) {
        return productRepository.findById(id)
            .map(productMapper::toDTO)
            .orElse(null);
    }

    public void addProduct(ProductWebDTO productWebDTO) {
        Product product = productWebMapper.toDomain(productWebDTO);
        sanitizeProduct(product); //sanitized with the function above
        validateProduct(product);
        productRepository.save(product);
    }

    private void validateProduct(Product product) {
        if (product.getName() == null || product.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Product name cannot be empty");
        }
        if (product.getName().length() > 100) {
            throw new IllegalArgumentException("Product name cannot exceed 100 characters");
        }
        if (product.getDescription() == null || product.getDescription().trim().isEmpty()) {
            throw new IllegalArgumentException("Product description cannot be empty");
        }
        if (product.getPrice() < 0) {
            throw new IllegalArgumentException("Product price cannot be negative");
        }
        if (product.getStock() < 0) {
            throw new IllegalArgumentException("Product stock cannot be negative");
        }
        if (product.getImagePath() == null || product.getImagePath().trim().isEmpty()) {
            throw new IllegalArgumentException("Product image path cannot be empty");
        }
    }

    public void deleteProduct(Long id) {
        productRepository.deleteById(id);
    }

    public void updateProduct(ProductDTO productDTO) {
        Product product = productMapper.toDomain(productDTO);
        validateProduct(product);
        productRepository.save(product);
    }

    public void updateProductWeb(ProductWebDTO productWebDTO) {
        Product product = productWebMapper.toDomain(productWebDTO);
        sanitizeProduct(product); //sanitized with the function above
        validateProduct(product);
        productRepository.save(product);
    }
    public void updateProduct(Product product) {
        validateProduct(product);
        productRepository.save(product);
    }

    public double getAverageRating(Long productId) {
        Optional<Product> productOpt = productRepository.findById(productId);
        if (productOpt.isPresent()) {
            Product product = productOpt.get();
            List<Review> reviews = product.getReviews();
            if (reviews.isEmpty()) {
                return 0.0;
            }
            return reviews.stream()
                .mapToDouble(Review::getRating)
                .average()
                .orElse(0.0);
        }
        return 0.0;
    }

    public double getMaxProductPrice() {
        List<Product> allProducts = productRepository.findAll();
        return allProducts.stream()
                .mapToDouble(Product::getPrice)
                .max()
                .orElse(1000.0);
    }

    public double getMaxPriceForFilter() {
        return productRepository.findMaxPrice();
    }

    private Pageable createPageable(int page, int size, String sort) {
        Sort sortObj;
        switch (sort) {
            case "price_asc":
                sortObj = Sort.by("price").ascending();
                break;
            case "price_desc":
                sortObj = Sort.by("price").descending();
                break;
            case "name_asc":
                sortObj = Sort.by("name").ascending();
                break;
            case "name_desc":
                sortObj = Sort.by("name").descending();
                break;
            default:
                sortObj = Sort.by("id").ascending();
        }
        return PageRequest.of(page - 1, size, sortObj);
    }

    @Transactional
    public List<ProductDTO> getAllProductsWithReviews() {
        List<Product> products = productRepository.findAll();
        List<Product> productsWithReviews = new ArrayList<>();
        
        for (Product product : products) {
            Product productWithReviews = productRepository.findProductWithReviews(product.getId());
            if (productWithReviews != null) {
                if (productWithReviews.getReviews() != null) {
                    Hibernate.initialize(productWithReviews.getReviews());
                    productWithReviews.getReviews().forEach(review -> {
                        if (review.getUser() != null) {
                            Hibernate.initialize(review.getUser());
                        }
                    });
                }
                productsWithReviews.add(productWithReviews);
            }
        }
        
        return productMapper.toDTOs(productsWithReviews);
    }

    public List<ProductWebDTO> getAllProductsWeb() {
        return productRepository.findAll().stream()
            .map(productWebMapper::toDTO)
            .collect(Collectors.toList());
    }

    public Page<ProductWebDTO> getProductsByPageWeb(Pageable pageable, String sort) {
        return productRepository.findAll(pageable)
            .map(productWebMapper::toDTO);
    }

    public Page<ProductWebDTO> getProductsByPageAndPriceWeb(Pageable pageable, String sort, double minPrice, double maxPrice) {
        return productRepository.findByPriceBetween(minPrice, maxPrice, pageable)
            .map(productWebMapper::toDTO);
    }

    public Page<ProductWebDTO> getProductsByMinRatingWeb(int minRating, Pageable pageable) {
        return productRepository.findByMinRating(minRating, pageable)
            .map(productWebMapper::toDTO);
    }

    public Page<ProductWebDTO> getProductsByPriceAndMinRatingWeb(double minPrice, double maxPrice, int minRating, Pageable pageable) {
        return productRepository.findByPriceBetweenAndMinRating(minPrice, maxPrice, minRating, pageable)
            .map(productWebMapper::toDTO);
    }

    public Page<ProductWebDTO> searchProductsByPageableWeb(String search, Pageable pageable) {
        return productRepository.findByNameContaining(search, pageable)
            .map(productWebMapper::toDTO);
    }

    public Page<ProductWebDTO> searchProductsByPriceAndPageableWeb(String search, double minPrice, double maxPrice, Pageable pageable) {
        return productRepository.findByNameContainingAndPriceBetween(search, minPrice, maxPrice, pageable)
            .map(productWebMapper::toDTO);
    }

    public ProductWebDTO getProductWithReviewsWeb(Long id) {
        return productRepository.findById(id)
            .map(productWebMapper::toDTO)
            .orElse(null);
    }

    public Page<ProductWebDTO> searchProductsByMinRatingWeb(String searchTerm, int minRating, Pageable pageable) {
        return productRepository.findByNameContainingAndMinRating(searchTerm, minRating, pageable)
            .map(productWebMapper::toDTO);
    }

    public Page<ProductWebDTO> searchProductsByPriceAndMinRatingWeb(String searchTerm, double minPrice, double maxPrice, int minRating, Pageable pageable) {
        return productRepository.findByNameContainingAndPriceBetweenAndMinRating(searchTerm, minPrice, maxPrice, minRating, pageable)
            .map(productWebMapper::toDTO);
    }

    public Product toDomain(ProductDTO productDTO) {
        return productMapper.toDomain(productDTO);
    }

    public Product toWebDomain(ProductWebDTO productWebDTO) {
        return productWebMapper.toDomain(productWebDTO);
    }

    public ProductWebDTO toWebDTO(Product product) {
        return productWebMapper.toDTO(product);
    }

    public ProductDTO toDTO(Product product) {
        return productMapper.toDTO(product);
    }


    public ProductDTO getProductDTO(ProductWebDTO productWebDTO) {
        Product product = productWebMapper.toDomain(productWebDTO);
        return productMapper.toDTO(product);
    }

    public ProductWebDTO setStock(ProductWebDTO productWebDTO, int stock) {
        Product product = productWebMapper.toDomain(productWebDTO);
        product.setStock(stock);
        return productWebMapper.toDTO(product);
    }

    public Page<ProductDTO> searchProductsByPriceAndPageable(String search, double minPrice, double maxPrice, Pageable pageable) {
        return productRepository.findByNameContainingAndPriceBetween(search, minPrice, maxPrice, pageable)
            .map(productMapper::toDTO);
    }

    public Page<ProductDTO> searchProductsByPageable(String search, Pageable pageable) {
        return productRepository.findByNameContaining(search, pageable)
            .map(productMapper::toDTO);
    }

    public List<Product> getAllProductsNormal() {
        return productRepository.findAll();
    }

    public Product getProductByName(String productName) {
        return productRepository.findByName(productName).orElse(null);
    }
}
