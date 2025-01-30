package com.reform.repositories;

import com.reform.entities.Product;
import com.reform.entities.TokenType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    Optional<Product> findByTokenQtyAndTokenType(int tokenQty, TokenType tokenType);

    Optional<Product> findByName(String subscriptionName);

    @Query("SELECT p FROM Product p WHERE p.name = :productName AND :subscriptionStartDate BETWEEN p.availableFrom AND p.availableUntil")
    Optional<Product> findValidProductForDate(@Param("productName") String productName, @Param("subscriptionStartDate") LocalDate subscriptionStartDate);

}
