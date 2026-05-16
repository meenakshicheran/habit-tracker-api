package com.habitflow.api.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class HabitRequest {
    @NotBlank(message = "Name is required")
    private String name;

    private String description;
    private String category;
    private String frequency = "DAILY";
    private String color;
    private Integer targetDays;
}
