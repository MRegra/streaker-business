package com.streaker.repository;

import com.streaker.model.Streak;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface StreakRepository extends JpaRepository<Streak, UUID> {
    List<Streak> findByUserUuid(UUID userUuid);
}
