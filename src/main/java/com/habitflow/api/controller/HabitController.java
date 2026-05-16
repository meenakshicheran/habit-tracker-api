package com.habitflow.api.controller;

import com.habitflow.api.dto.HabitRequest;
import com.habitflow.api.dto.HabitResponse;
import com.habitflow.api.model.User;
import com.habitflow.api.service.HabitService;
import com.habitflow.api.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/habits")
@RequiredArgsConstructor
public class HabitController {
    private final HabitService habitService;
    private final UserService userService;

    @GetMapping
    public ResponseEntity<List<HabitResponse>> getAll(@AuthenticationPrincipal UserDetails principal) {
        User user = userService.findByEmail(principal.getUsername());
        return ResponseEntity.ok(habitService.getUserHabits(user));
    }

    @PostMapping
    public ResponseEntity<HabitResponse> create(@Valid @RequestBody HabitRequest request,
                                                @AuthenticationPrincipal UserDetails principal) {
        User user = userService.findByEmail(principal.getUsername());
        return ResponseEntity.ok(habitService.create(request, user));
    }

    @PutMapping("/{id}")
    public ResponseEntity<HabitResponse> update(@PathVariable Long id,
                                                @Valid @RequestBody HabitRequest request,
                                                @AuthenticationPrincipal UserDetails principal) {
        User user = userService.findByEmail(principal.getUsername());
        return ResponseEntity.ok(habitService.update(id, request, user));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id,
                                       @AuthenticationPrincipal UserDetails principal) {
        User user = userService.findByEmail(principal.getUsername());
        habitService.delete(id, user);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/complete")
    public ResponseEntity<HabitResponse> complete(@PathVariable Long id,
                                                  @RequestBody(required = false) Map<String, String> body,
                                                  @AuthenticationPrincipal UserDetails principal) {
        User user = userService.findByEmail(principal.getUsername());
        String note = body != null ? body.get("note") : null;
        return ResponseEntity.ok(habitService.logCompletion(id, user, note));
    }
}
