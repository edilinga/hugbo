package com.team.gym.repository;

import com.team.gym.model.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    long countByClassSessionId(Long classSessionId);
    List<Booking> findByUserIdOrderByClassSessionStartAtAsc(Long userId);

    boolean existsByUserIdAndClassSessionId(Long userId, Long classSessionId);

}