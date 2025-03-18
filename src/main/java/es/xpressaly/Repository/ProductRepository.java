package es.xpressaly.Repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import es.xpressaly.Model.Product;

public interface ProductRepository extends JpaRepository<Product, Long>{
    //metodos para buscar por precio mayor y precio menor
    List<Product> findByGreaterPrice(List<Product> products);
    List<Product> findByLowerPrice(List<Product> products);
}
