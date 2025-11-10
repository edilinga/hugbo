package com.team.gym.repository;

import com.team.gym.model.ClassSession;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;

public interface ClassSessionRepository extends JpaRepository<ClassSession, Long> {

    // (UC4)
    List<ClassSession> findAllByOrderByStartAtAsc();

    // (UC10)
    Page<ClassSession> findByStartAtBetween(Instant from, Instant to, Pageable pageable);
}
