package com.reform.repositories;

import com.reform.entities.Client;
import com.reform.entities.Token;
import com.reform.entities.TokenStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface TokenRepository extends JpaRepository<Token, Long> {

    @Query("SELECT COUNT(t) FROM Token t WHERE t.client = :client AND t.usedAt IS NULL")
    int countAvailableTokens(Client client);

    @Query("SELECT t FROM Token t WHERE t.expiresAt <= :date AND t.usedAt IS NULL")
    List<Token> findExpiringTokens(@Param("date") LocalDate date);

    @Query("SELECT t FROM Token t WHERE t.client = :client")
    List<Token> findByClient(@Param("client") Client client);

    List<Token> findTokensByClientAndStatus(Client client, TokenStatus status);
}
