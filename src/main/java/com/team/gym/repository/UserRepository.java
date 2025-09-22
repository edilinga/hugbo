package com.team.gym.repository;

import com.team.gym.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByEmail(String email);
    boolean existsBySsn(String ssn);
    Optional<User> findByEmail(String email);
}
