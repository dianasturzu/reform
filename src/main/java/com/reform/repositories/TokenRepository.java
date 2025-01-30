package com.reform.repositories;

import com.reform.entities.Client;
import com.reform.entities.Token;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface TokenRepository extends JpaRepository<Token, Long> {

    @Query("SELECT t FROM Token t WHERE t.client = :client AND t.usedAt IS NULL AND (t.expiresAt IS NULL OR t.expiresAt >= :sessionDate)")
    List<Token> findUnexpiredTokens(Client client, LocalDate sessionDate);

    @Query("SELECT COUNT(t) FROM Token t WHERE t.client = :client AND t.usedAt IS NULL")
    int countAvailableTokens(Client client);
}
