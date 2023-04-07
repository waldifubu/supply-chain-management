package com.example.supplychainmanagement.repository;

import com.example.supplychainmanagement.entity.Product;
import com.example.supplychainmanagement.entity.Storage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface StorageRepository extends JpaRepository<Storage, Long> {
    @Query("select new Storage(s.id, s.product, s.inStock, s.updated, s.lastDelivery, s.nextDelivery) from Storage s where s.product = ?1")
    Optional<Storage> findByProduct(Product product);

    @Query("select s from Storage s where s.product = ?1")
    Optional<Storage> findByProductAdmin(Product product);
}
