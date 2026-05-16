package com.habitflow.api.repository;

import com.habitflow.api.model.Habit;
import com.habitflow.api.model.HabitLog;
import com.habitflow.api.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface HabitLogRepository extends JpaRepository<HabitLog, Long> {
    List<HabitLog> findByHabit(Habit habit);
    List<HabitLog> findByUser(User user);
    List<HabitLog> findByHabitAndCompletedDateBetween(Habit habit, LocalDate start, LocalDate end);
    Optional<HabitLog> findByHabitAndCompletedDate(Habit habit, LocalDate date);
    boolean existsByHabitAndCompletedDate(Habit habit, LocalDate date);
}
