package com.reform.repositories;

import com.reform.entities.Instructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface InstructorRepository extends JpaRepository<Instructor, Long> {
    Optional<Instructor> findByName(String name);

    Optional<Instructor> findByGoogleCalendarId(String googleCalendarId);

}
