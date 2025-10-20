package com.team.gym.repository;

import com.team.gym.model.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import com.team.gym.model.BookingStatus;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    long countByClassSessionId(Long classSessionId);
    List<Booking> findByUserIdOrderByClassSessionStartAtAsc(Long userId);

    boolean existsByUserIdAndClassSessionId(Long userId, Long classSessionId);


    Optional<Booking> findByUserIdAndClassSessionId(Long userId, Long classSessionId);

    long deleteByUserIdAndClassSessionId(Long userId, Long classSessionId);

}