package com.studyplanner.controller;

import com.studyplanner.dto.GenerateScheduleRequest;
import com.studyplanner.dto.LoginRequest;
import com.studyplanner.dto.ProgressResponse;
import com.studyplanner.dto.RegisterRequest;
import com.studyplanner.model.StudySchedule;
import com.studyplanner.model.Subject;
import com.studyplanner.model.User;
import com.studyplanner.service.StudyPlannerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class StudyPlannerController {

    @Autowired
    private StudyPlannerService studyPlannerService;

    // ==================== HEALTH ====================
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> res = new HashMap<>();
        res.put("status", "UP");
        res.put("message", "Study Planner API is running smoothly!");
        return ResponseEntity.ok(res);
    }

    // ==================== USER AUTH ====================
    @PostMapping("/users/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest req) {
        try {
            User user = studyPlannerService.register(req);
            Map<String, Object> res = new HashMap<>();
            res.put("success", true);
            res.put("message", "Registration successful! You can now log in.");
            res.put("userId", user.getId());
            res.put("name", user.getName());
            res.put("email", user.getEmail());
            return ResponseEntity.status(HttpStatus.CREATED).body(res);
        } catch (IllegalArgumentException e) {
            Map<String, Object> err = new HashMap<>();
            err.put("success", false);
            err.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(err);
        } catch (Exception e) {
            Map<String, Object> err = new HashMap<>();
            err.put("success", false);
            err.put("message", "Registration failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(err);
        }
    }

    @PostMapping("/users/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest req) {
        try {
            User user = studyPlannerService.login(req);
            Map<String, Object> res = new HashMap<>();
            res.put("success", true);
            res.put("message", "Login successful!");
            res.put("userId", user.getId());
            res.put("name", user.getName());
            res.put("email", user.getEmail());
            return ResponseEntity.ok(res);
        } catch (IllegalArgumentException e) {
            Map<String, Object> err = new HashMap<>();
            err.put("success", false);
            err.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(err);
        } catch (Exception e) {
            Map<String, Object> err = new HashMap<>();
            err.put("success", false);
            err.put("message", "Login failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(err);
        }
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<?> getUser(@PathVariable Long id) {
        return studyPlannerService.getUserById(id)
                .map(u -> {
                    Map<String, Object> res = new HashMap<>();
                    res.put("userId", u.getId());
                    res.put("name", u.getName());
                    res.put("email", u.getEmail());
                    return ResponseEntity.ok(res);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // ==================== SUBJECTS ====================
    @GetMapping("/subjects/user/{userId}")
    public ResponseEntity<List<Subject>> getSubjects(@PathVariable Long userId) {
        List<Subject> subjects = studyPlannerService.getSubjects(userId);
        return ResponseEntity.ok(subjects);
    }

    @PostMapping("/subjects/user/{userId}")
    public ResponseEntity<?> addSubject(@PathVariable Long userId, @RequestBody Subject subject) {
        try {
            Subject created = studyPlannerService.addSubject(userId, subject);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (IllegalArgumentException e) {
            Map<String, Object> err = new HashMap<>();
            err.put("success", false);
            err.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(err);
        }
    }

    @PutMapping("/subjects/{id}")
    public ResponseEntity<?> updateSubject(@PathVariable Long id, @RequestBody Subject subject) {
        try {
            Subject updated = studyPlannerService.updateSubject(id, subject);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            Map<String, Object> err = new HashMap<>();
            err.put("success", false);
            err.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(err);
        }
    }

    @DeleteMapping("/subjects/{id}")
    public ResponseEntity<?> deleteSubject(@PathVariable Long id) {
        try {
            studyPlannerService.deleteSubject(id);
            Map<String, Object> res = new HashMap<>();
            res.put("success", true);
            res.put("message", "Subject deleted successfully.");
            return ResponseEntity.ok(res);
        } catch (Exception e) {
            Map<String, Object> err = new HashMap<>();
            err.put("success", false);
            err.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(err);
        }
    }

    // ==================== SCHEDULES ====================
    @PostMapping("/schedules/generate")
    public ResponseEntity<?> generateSchedule(@RequestBody GenerateScheduleRequest req) {
        try {
            List<StudySchedule> schedule = studyPlannerService.generateSchedule(req);
            Map<String, Object> res = new HashMap<>();
            res.put("success", true);
            res.put("message", "Schedule generated successfully!");
            res.put("schedule", schedule);
            return ResponseEntity.ok(res);
        } catch (IllegalArgumentException e) {
            Map<String, Object> err = new HashMap<>();
            err.put("success", false);
            err.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(err);
        } catch (Exception e) {
            Map<String, Object> err = new HashMap<>();
            err.put("success", false);
            err.put("message", "Failed to generate schedule: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(err);
        }
    }

    @GetMapping("/schedules/user/{userId}")
    public ResponseEntity<List<StudySchedule>> getSchedule(@PathVariable Long userId) {
        List<StudySchedule> schedule = studyPlannerService.getSchedule(userId);
        return ResponseEntity.ok(schedule);
    }

    @PutMapping("/schedules/{id}/complete")
    public ResponseEntity<?> toggleComplete(@PathVariable Long id) {
        try {
            StudySchedule updated = studyPlannerService.toggleSessionCompletion(id);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            Map<String, Object> err = new HashMap<>();
            err.put("success", false);
            err.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(err);
        }
    }

    @DeleteMapping("/schedules/user/{userId}")
    public ResponseEntity<?> clearSchedule(@PathVariable Long userId) {
        studyPlannerService.clearSchedule(userId);
        Map<String, Object> res = new HashMap<>();
        res.put("success", true);
        res.put("message", "Schedule cleared successfully.");
        return ResponseEntity.ok(res);
    }

    // ==================== PROGRESS ====================
    @GetMapping("/schedules/progress/{userId}")
    public ResponseEntity<ProgressResponse> getProgress(@PathVariable Long userId) {
        ProgressResponse progress = studyPlannerService.getProgress(userId);
        return ResponseEntity.ok(progress);
    }
}
