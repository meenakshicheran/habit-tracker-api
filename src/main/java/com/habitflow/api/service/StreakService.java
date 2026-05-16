package com.habitflow.api.service;

import com.habitflow.api.model.Habit;
import com.habitflow.api.model.HabitLog;
import com.habitflow.api.model.Streak;
import com.habitflow.api.repository.HabitLogRepository;
import com.habitflow.api.repository.StreakRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StreakService {
    private final StreakRepository streakRepository;
    private final HabitLogRepository habitLogRepository;

    @Transactional
    public Streak updateStreak(Habit habit) {
        Streak streak = streakRepository.findByHabit(habit)
                .orElseGet(() -> Streak.builder().habit(habit).currentStreak(0).longestStreak(0).build());

        LocalDate today = LocalDate.now();
        LocalDate lastDate = streak.getLastCompletedDate();

        if (lastDate == null) {
            streak.setCurrentStreak(1);
        } else {
            long daysBetween = ChronoUnit.DAYS.between(lastDate, today);
            if (daysBetween == 1) {
                streak.setCurrentStreak(streak.getCurrentStreak() + 1);
            } else if (daysBetween > 1) {
                // gap breaks the streak
                streak.setCurrentStreak(1);
            }
            // daysBetween == 0: already logged today, no change
        }

        if (streak.getCurrentStreak() > streak.getLongestStreak()) {
            streak.setLongestStreak(streak.getCurrentStreak());
        }
        streak.setLastCompletedDate(today);
        return streakRepository.save(streak);
    }

    // Calculates streak from raw logs using sorted date traversal (O(n log n))
    public int calculateCurrentStreak(Habit habit) {
        List<HabitLog> logs = habitLogRepository.findByHabit(habit);
        if (logs.isEmpty()) return 0;

        List<LocalDate> dates = logs.stream()
                .map(HabitLog::getCompletedDate)
                .distinct()
                .sorted((a, b) -> b.compareTo(a))
                .toList();

        LocalDate today = LocalDate.now();
        if (!dates.get(0).equals(today) && !dates.get(0).equals(today.minusDays(1))) {
            return 0;
        }

        int streak = 1;
        for (int i = 1; i < dates.size(); i++) {
            long gap = ChronoUnit.DAYS.between(dates.get(i), dates.get(i - 1));
            if (gap == 1) {
                streak++;
            } else {
                break;
            }
        }
        return streak;
    }

    public Streak getOrCreate(Habit habit) {
        return streakRepository.findByHabit(habit)
                .orElseGet(() -> streakRepository.save(
                        Streak.builder().habit(habit).currentStreak(0).longestStreak(0).build()
                ));
    }
}
