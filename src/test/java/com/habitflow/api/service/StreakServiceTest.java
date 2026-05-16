package com.habitflow.api.service;

import com.habitflow.api.model.Habit;
import com.habitflow.api.model.HabitLog;
import com.habitflow.api.model.Streak;
import com.habitflow.api.repository.HabitLogRepository;
import com.habitflow.api.repository.StreakRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StreakServiceTest {
    @Mock private StreakRepository streakRepository;
    @Mock private HabitLogRepository habitLogRepository;
    @InjectMocks private StreakService streakService;

    private Habit habit;

    @BeforeEach
    void setUp() {
        habit = Habit.builder().id(1L).name("Exercise").build();
    }

    @Test
    void updateStreak_startsAtOneWithNoHistory() {
        when(streakRepository.findByHabit(habit)).thenReturn(Optional.empty());
        when(streakRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        Streak result = streakService.updateStreak(habit);

        assertThat(result.getCurrentStreak()).isEqualTo(1);
        assertThat(result.getLongestStreak()).isEqualTo(1);
    }

    @Test
    void updateStreak_incrementsOnConsecutiveDay() {
        Streak existing = Streak.builder().habit(habit).currentStreak(3).longestStreak(5)
                .lastCompletedDate(LocalDate.now().minusDays(1)).build();
        when(streakRepository.findByHabit(habit)).thenReturn(Optional.of(existing));
        when(streakRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        Streak result = streakService.updateStreak(habit);

        assertThat(result.getCurrentStreak()).isEqualTo(4);
        assertThat(result.getLongestStreak()).isEqualTo(5);
    }

    @Test
    void updateStreak_resetsOnGap() {
        Streak existing = Streak.builder().habit(habit).currentStreak(7).longestStreak(7)
                .lastCompletedDate(LocalDate.now().minusDays(3)).build();
        when(streakRepository.findByHabit(habit)).thenReturn(Optional.of(existing));
        when(streakRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        Streak result = streakService.updateStreak(habit);

        assertThat(result.getCurrentStreak()).isEqualTo(1);
        assertThat(result.getLongestStreak()).isEqualTo(7);
    }

    @Test
    void updateStreak_updatesLongestWhenExceeded() {
        Streak existing = Streak.builder().habit(habit).currentStreak(4).longestStreak(4)
                .lastCompletedDate(LocalDate.now().minusDays(1)).build();
        when(streakRepository.findByHabit(habit)).thenReturn(Optional.of(existing));
        when(streakRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        Streak result = streakService.updateStreak(habit);

        assertThat(result.getCurrentStreak()).isEqualTo(5);
        assertThat(result.getLongestStreak()).isEqualTo(5);
    }

    @Test
    void calculateCurrentStreak_returnsZeroWithNoLogs() {
        when(habitLogRepository.findByHabit(habit)).thenReturn(List.of());
        assertThat(streakService.calculateCurrentStreak(habit)).isEqualTo(0);
    }

    @Test
    void calculateCurrentStreak_countsConsecutiveDays() {
        List<HabitLog> logs = List.of(
                HabitLog.builder().completedDate(LocalDate.now()).build(),
                HabitLog.builder().completedDate(LocalDate.now().minusDays(1)).build(),
                HabitLog.builder().completedDate(LocalDate.now().minusDays(2)).build()
        );
        when(habitLogRepository.findByHabit(habit)).thenReturn(logs);

        assertThat(streakService.calculateCurrentStreak(habit)).isEqualTo(3);
    }

    @Test
    void calculateCurrentStreak_stopsAtGap() {
        List<HabitLog> logs = List.of(
                HabitLog.builder().completedDate(LocalDate.now()).build(),
                HabitLog.builder().completedDate(LocalDate.now().minusDays(1)).build(),
                HabitLog.builder().completedDate(LocalDate.now().minusDays(3)).build()
        );
        when(habitLogRepository.findByHabit(habit)).thenReturn(logs);

        assertThat(streakService.calculateCurrentStreak(habit)).isEqualTo(2);
    }
}
