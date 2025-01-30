package com.reform.repositories;

import com.reform.entities.Instructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface InstructorRepository extends JpaRepository<Instructor, Long> {
    Optional<Instructor> findByName(String name);

    Optional<Instructor> findByGoogleCalendarId(String googleCalendarId);

    @Query("SELECT COUNT(s) FROM Session s WHERE s.instructor.id = :instructorId AND MONTH(s.sessionDate) = :month AND YEAR(s.sessionDate) = :year")
    int countSessionsForInstructor(Long instructorId, int month, int year);
}
