package com.ecommerce.microcommerce.dao;

import com.ecommerce.microcommerce.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductDao extends JpaRepository<Product, Integer> {
    List<Product> findAll();
    Product findById(int id);
    List<Product> findByPrixGreaterThan(int prixLimit);
    Product save(Product product);

    @Override
    void deleteById(Integer integer);

    List<Product> findByNomLike(String recherche);
}
