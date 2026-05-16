package com.habitflow.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.habitflow.api.dto.HabitRequest;
import com.habitflow.api.dto.HabitResponse;
import com.habitflow.api.model.User;
import com.habitflow.api.security.CustomUserDetailsService;
import com.habitflow.api.security.JwtUtil;
import com.habitflow.api.service.HabitService;
import com.habitflow.api.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(value = HabitController.class, excludeAutoConfiguration = UserDetailsServiceAutoConfiguration.class)
class HabitControllerTest {
    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @MockBean private HabitService habitService;
    @MockBean private UserService userService;
    // Mocked so JwtFilter can be wired (it needs these two); isValid() returns false
    // by default so the filter passes through without setting any auth
    @MockBean private JwtUtil jwtUtil;
    @MockBean private CustomUserDetailsService customUserDetailsService;

    private User user;
    private HabitResponse habitResponse;

    @BeforeEach
    void setUp() {
        user = User.builder().id(1L).email("test@test.com").name("Test").build();
        habitResponse = HabitResponse.builder()
                .id(1L).name("Exercise").frequency("DAILY")
                .currentStreak(3).longestStreak(7).completedToday(false)
                .build();
    }

    @Test
    @WithMockUser(username = "test@test.com")
    void getAll_returnsHabits() throws Exception {
        when(userService.findByEmail("test@test.com")).thenReturn(user);
        when(habitService.getUserHabits(user)).thenReturn(List.of(habitResponse));

        mockMvc.perform(get("/api/habits"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Exercise"))
                .andExpect(jsonPath("$[0].currentStreak").value(3));
    }

    @Test
    @WithMockUser(username = "test@test.com")
    void create_returnsCreatedHabit() throws Exception {
        HabitRequest req = new HabitRequest();
        req.setName("Exercise");
        req.setFrequency("DAILY");
        when(userService.findByEmail("test@test.com")).thenReturn(user);
        when(habitService.create(any(), eq(user))).thenReturn(habitResponse);

        mockMvc.perform(post("/api/habits")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Exercise"));
    }

    @Test
    @WithMockUser(username = "test@test.com")
    void delete_returns204() throws Exception {
        when(userService.findByEmail("test@test.com")).thenReturn(user);

        mockMvc.perform(delete("/api/habits/1").with(csrf()))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(username = "test@test.com")
    void complete_returnsUpdatedHabit() throws Exception {
        when(userService.findByEmail("test@test.com")).thenReturn(user);
        HabitResponse completed = HabitResponse.builder()
                .id(1L).name("Exercise").completedToday(true).currentStreak(4).longestStreak(7).build();
        when(habitService.logCompletion(eq(1L), eq(user), any())).thenReturn(completed);

        mockMvc.perform(post("/api/habits/1/complete")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.completedToday").value(true))
                .andExpect(jsonPath("$.currentStreak").value(4));
    }
}
