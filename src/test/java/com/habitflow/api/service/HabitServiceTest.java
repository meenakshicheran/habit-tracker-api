package com.habitflow.api.service;

import com.habitflow.api.dto.HabitRequest;
import com.habitflow.api.dto.HabitResponse;
import com.habitflow.api.model.Habit;
import com.habitflow.api.model.Streak;
import com.habitflow.api.model.User;
import com.habitflow.api.repository.HabitLogRepository;
import com.habitflow.api.repository.HabitRepository;
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
class HabitServiceTest {
    @Mock private HabitRepository habitRepository;
    @Mock private HabitLogRepository habitLogRepository;
    @Mock private StreakService streakService;
    @InjectMocks private HabitService habitService;

    private User user;
    private Habit habit;
    private Streak streak;

    @BeforeEach
    void setUp() {
        user = User.builder().id(1L).email("test@test.com").name("Test").build();
        habit = Habit.builder().id(1L).name("Exercise").user(user).isActive(true).build();
        streak = Streak.builder().currentStreak(0).longestStreak(0).build();
    }

    @Test
    void create_savesAndReturnsHabit() {
        HabitRequest req = new HabitRequest();
        req.setName("Exercise");
        req.setFrequency("DAILY");
        when(habitRepository.save(any())).thenReturn(habit);
        when(streakService.getOrCreate(any())).thenReturn(streak);
        when(habitLogRepository.existsByHabitAndCompletedDate(any(), any())).thenReturn(false);

        HabitResponse result = habitService.create(req, user);

        assertThat(result.getName()).isEqualTo("Exercise");
        verify(habitRepository).save(any(Habit.class));
    }

    @Test
    void findById_throwsWhenNotOwner() {
        User other = User.builder().id(2L).build();
        when(habitRepository.findById(1L)).thenReturn(Optional.of(habit));

        assertThatThrownBy(() -> habitService.findById(1L, other))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Habit not found");
    }

    @Test
    void findById_returnsHabitForOwner() {
        when(habitRepository.findById(1L)).thenReturn(Optional.of(habit));

        Habit result = habitService.findById(1L, user);

        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    void delete_setsIsActiveFalse() {
        when(habitRepository.findById(1L)).thenReturn(Optional.of(habit));
        when(habitRepository.save(any())).thenReturn(habit);

        habitService.delete(1L, user);

        assertThat(habit.getIsActive()).isFalse();
        verify(habitRepository).save(habit);
    }

    @Test
    void update_changesFields() {
        HabitRequest req = new HabitRequest();
        req.setName("Running");
        req.setFrequency("DAILY");
        req.setCategory("fitness");
        when(habitRepository.findById(1L)).thenReturn(Optional.of(habit));
        when(habitRepository.save(any())).thenReturn(habit);
        when(streakService.getOrCreate(any())).thenReturn(streak);
        when(habitLogRepository.existsByHabitAndCompletedDate(any(), any())).thenReturn(false);

        HabitResponse result = habitService.update(1L, req, user);

        assertThat(result.getName()).isEqualTo("Running");
        assertThat(result.getCategory()).isEqualTo("fitness");
    }

    @Test
    void logCompletion_skipsIfAlreadyLoggedToday() {
        when(habitRepository.findById(1L)).thenReturn(Optional.of(habit));
        when(habitLogRepository.existsByHabitAndCompletedDate(habit, LocalDate.now())).thenReturn(true);
        when(streakService.getOrCreate(any())).thenReturn(streak);

        habitService.logCompletion(1L, user, null);

        verify(habitLogRepository, never()).save(any());
        verify(streakService, never()).updateStreak(any());
    }

    @Test
    void getUserHabits_returnsOnlyActiveHabits() {
        when(habitRepository.findByUserAndIsActiveTrue(user)).thenReturn(List.of(habit));
        when(streakService.getOrCreate(any())).thenReturn(streak);
        when(habitLogRepository.existsByHabitAndCompletedDate(any(), any())).thenReturn(false);

        List<HabitResponse> result = habitService.getUserHabits(user);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Exercise");
    }
}
