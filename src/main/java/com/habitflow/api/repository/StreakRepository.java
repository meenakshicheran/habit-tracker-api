package com.habitflow.api.repository;

import com.habitflow.api.model.Habit;
import com.habitflow.api.model.Streak;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface StreakRepository extends JpaRepository<Streak, Long> {
    Optional<Streak> findByHabit(Habit habit);
}
