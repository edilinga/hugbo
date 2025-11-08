package com.team.gym.repository;

import com.team.gym.model.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import com.team.gym.model.BookingStatus;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    long countByClassSessionIdAndStatus(Long classSessionId, BookingStatus status);
    List<Booking> findByUserIdOrderByClassSessionStartAtAsc(Long userId);

    boolean existsByUserIdAndClassSessionId(Long userId, Long classSessionId);

    Optional<Booking> findByUserIdAndClassSessionId(Long userId, Long classSessionId);

    // Fyrir biðlista – ná í "fyrsta á lista" eftir createdAt
    List<Booking> findByClassSessionIdAndStatusOrderByCreatedAtAsc(
            Long classSessionId,
            BookingStatus status
    );

    long deleteByUserIdAndClassSessionId(Long userId, Long classSessionId);

}