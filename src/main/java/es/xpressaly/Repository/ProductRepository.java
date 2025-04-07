package es.xpressaly.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import es.xpressaly.Model.Product;
import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByNameContainingIgnoreCase(String name);
    List<Product> findByPriceLessThanEqual(double price);
    List<Product> findByStockGreaterThan(int stock);
    @Query("SELECT p FROM Product p LEFT JOIN FETCH p.reviews WHERE p.id = :id")
    Product findProductWithReviews(@Param("id") Long id);
    //consulta optimizada para el promedio de valoraciones
    @Query("SELECT AVG(r.rating) FROM Product p JOIN p.reviews r WHERE p.id = :productId")
    Double getAverageRatingByProductId(@Param("productId") Long productId);
    
    Page<Product> findAll(Pageable pageable);
    
    @Query("SELECT p FROM Product p ORDER BY p.price ASC")
    Page<Product> findAllByPriceAsc(Pageable pageable);
    
    @Query("SELECT p FROM Product p ORDER BY p.price DESC")
    Page<Product> findAllByPriceDesc(Pageable pageable);
}
