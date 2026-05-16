package com.habitflow.api.controller;

import com.habitflow.api.model.User;
import com.habitflow.api.service.HabitService;
import com.habitflow.api.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {
    private final HabitService habitService;
    private final UserService userService;

    @GetMapping("/summary")
    public ResponseEntity<Map<String, Object>> summary(@AuthenticationPrincipal UserDetails principal) {
        User user = userService.findByEmail(principal.getUsername());
        var habits = habitService.getUserHabits(user);
        long completedToday = habits.stream().filter(h -> h.isCompletedToday()).count();
        int bestStreak = habits.stream().mapToInt(h -> h.getCurrentStreak()).max().orElse(0);
        double rate = habits.isEmpty() ? 0 : (completedToday * 100.0 / habits.size());
        return ResponseEntity.ok(Map.of(
                "totalHabits", habits.size(),
                "completedToday", completedToday,
                "completionRate", rate,
                "bestStreak", bestStreak
        ));
    }
}
