package com.habitflow.api.service;

import com.habitflow.api.dto.HabitRequest;
import com.habitflow.api.dto.HabitResponse;
import com.habitflow.api.model.Habit;
import com.habitflow.api.model.HabitLog;
import com.habitflow.api.model.Streak;
import com.habitflow.api.model.User;
import com.habitflow.api.repository.HabitLogRepository;
import com.habitflow.api.repository.HabitRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class HabitService {
    private final HabitRepository habitRepository;
    private final HabitLogRepository habitLogRepository;
    private final StreakService streakService;

    public HabitResponse create(HabitRequest request, User user) {
        Habit habit = Habit.builder()
                .user(user)
                .name(request.getName())
                .description(request.getDescription())
                .category(request.getCategory())
                .frequency(request.getFrequency())
                .color(request.getColor())
                .targetDays(request.getTargetDays())
                .build();
        return toResponse(habitRepository.save(habit));
    }

    public List<HabitResponse> getUserHabits(User user) {
        return habitRepository.findByUserAndIsActiveTrue(user).stream()
                .map(h -> toResponse(h))
                .toList();
    }

    public Habit findById(Long id, User user) {
        return habitRepository.findById(id)
                .filter(h -> h.getUser().getId().equals(user.getId()))
                .orElseThrow(() -> new IllegalArgumentException("Habit not found"));
    }

    public HabitResponse update(Long id, HabitRequest request, User user) {
        Habit habit = findById(id, user);
        habit.setName(request.getName());
        habit.setDescription(request.getDescription());
        habit.setCategory(request.getCategory());
        habit.setFrequency(request.getFrequency());
        habit.setColor(request.getColor());
        habit.setTargetDays(request.getTargetDays());
        return toResponse(habitRepository.save(habit));
    }

    public void delete(Long id, User user) {
        Habit habit = findById(id, user);
        habit.setIsActive(false);
        habitRepository.save(habit);
    }

    @Transactional
    public HabitResponse logCompletion(Long id, User user, String note) {
        Habit habit = findById(id, user);
        LocalDate today = LocalDate.now();
        if (!habitLogRepository.existsByHabitAndCompletedDate(habit, today)) {
            HabitLog log = HabitLog.builder()
                    .habit(habit)
                    .user(user)
                    .completedDate(today)
                    .note(note)
                    .build();
            habitLogRepository.save(log);
            streakService.updateStreak(habit);
        }
        return toResponse(habit);
    }

    public HabitResponse toResponse(Habit habit) {
        Streak streak = streakService.getOrCreate(habit);
        boolean completedToday = habitLogRepository
                .existsByHabitAndCompletedDate(habit, LocalDate.now());
        return HabitResponse.builder()
                .id(habit.getId())
                .name(habit.getName())
                .description(habit.getDescription())
                .category(habit.getCategory())
                .frequency(habit.getFrequency())
                .color(habit.getColor())
                .targetDays(habit.getTargetDays())
                .isActive(habit.getIsActive())
                .createdAt(habit.getCreatedAt())
                .currentStreak(streak.getCurrentStreak())
                .longestStreak(streak.getLongestStreak())
                .completedToday(completedToday)
                .build();
    }
}
