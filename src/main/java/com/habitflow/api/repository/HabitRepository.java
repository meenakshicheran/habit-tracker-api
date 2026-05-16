package com.habitflow.api.repository;

import com.habitflow.api.model.Habit;
import com.habitflow.api.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface HabitRepository extends JpaRepository<Habit, Long> {
    List<Habit> findByUserAndIsActiveTrue(User user);
    List<Habit> findByUser(User user);
}
