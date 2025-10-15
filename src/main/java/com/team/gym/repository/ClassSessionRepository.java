package com.team.gym.repository;

import com.team.gym.model.ClassSession;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ClassSessionRepository extends JpaRepository<ClassSession, Long> {
    List<ClassSession> findAllByOrderByStartAtAsc();
}
