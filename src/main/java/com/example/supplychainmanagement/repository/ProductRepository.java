package com.example.supplychainmanagement.repository;

import com.example.supplychainmanagement.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

//    @Query("select p from Product p where p.articleNo = ?1")
    Product findProductByArticleNo(long articleNo);
}

