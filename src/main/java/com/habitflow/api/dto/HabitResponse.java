package com.habitflow.api.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class HabitResponse {
    private Long id;
    private String name;
    private String description;
    private String category;
    private String frequency;
    private String color;
    private Integer targetDays;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private Integer currentStreak;
    private Integer longestStreak;
    private boolean completedToday;
}
