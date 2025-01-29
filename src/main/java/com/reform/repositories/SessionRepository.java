package com.reform.repositories;

import com.reform.entities.Client;
import com.reform.entities.Session;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;

@Repository
public interface SessionRepository extends JpaRepository<Session, Long> {
    boolean existsByClientAndSessionDate(Client client, LocalDate sessionDate);

}
