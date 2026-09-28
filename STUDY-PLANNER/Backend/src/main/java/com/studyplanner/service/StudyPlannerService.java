package com.studyplanner.service;

import com.studyplanner.dto.GenerateScheduleRequest;
import com.studyplanner.dto.LoginRequest;
import com.studyplanner.dto.ProgressResponse;
import com.studyplanner.dto.RegisterRequest;
import com.studyplanner.model.StudySchedule;
import com.studyplanner.model.Subject;
import com.studyplanner.model.User;
import com.studyplanner.repository.ScheduleRepository;
import com.studyplanner.repository.SubjectRepository;
import com.studyplanner.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class StudyPlannerService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SubjectRepository subjectRepository;

    @Autowired
    private ScheduleRepository scheduleRepository;

    private static final DateTimeFormatter TIME_FORMATTER_12 = DateTimeFormatter.ofPattern("hh:mm a", Locale.ENGLISH);

    // ==================== USER MANAGEMENT ====================

    public User register(RegisterRequest req) {
        if (req.getName() == null || req.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Full name is required.");
        }
        if (req.getEmail() == null || !req.getEmail().matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            throw new IllegalArgumentException("Please provide a valid email address.");
        }
        if (req.getPassword() == null || req.getPassword().length() < 4) {
            throw new IllegalArgumentException("Password must be at least 4 characters long.");
        }
        if (!req.getPassword().equals(req.getConfirmPassword())) {
            throw new IllegalArgumentException("Password and confirm password do not match.");
        }
        if (userRepository.existsByEmail(req.getEmail().trim().toLowerCase())) {
            throw new IllegalArgumentException("An account with this email already exists.");
        }

        User user = new User(req.getName().trim(), req.getEmail().trim().toLowerCase(), req.getPassword());
        return userRepository.save(user);
    }

    public User login(LoginRequest req) {
        if (req.getEmail() == null || req.getEmail().trim().isEmpty() ||
            req.getPassword() == null || req.getPassword().isEmpty()) {
            throw new IllegalArgumentException("Email and password are required.");
        }

        return userRepository.findByEmailAndPassword(req.getEmail().trim().toLowerCase(), req.getPassword())
                .orElseThrow(() -> new IllegalArgumentException("Invalid email or password."));
    }

    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    // ==================== SUBJECT MANAGEMENT ====================

    public List<Subject> getSubjects(Long userId) {
        return subjectRepository.findByUserId(userId);
    }

    public Subject addSubject(Long userId, Subject subject) {
        if (subject.getSubjectName() == null || subject.getSubjectName().trim().isEmpty()) {
            throw new IllegalArgumentException("Subject name is required.");
        }
        String priority = subject.getPriority();
        if (priority == null || (!priority.equalsIgnoreCase("High") &&
                                 !priority.equalsIgnoreCase("Medium") &&
                                 !priority.equalsIgnoreCase("Low"))) {
            priority = "Medium";
        } else {
            priority = priority.substring(0, 1).toUpperCase() + priority.substring(1).toLowerCase();
        }
        subject.setUserId(userId);
        subject.setSubjectName(subject.getSubjectName().trim());
        subject.setPriority(priority);
        return subjectRepository.save(subject);
    }

    public Subject updateSubject(Long id, Subject updated) {
        Subject existing = subjectRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Subject not found with id: " + id));

        if (updated.getSubjectName() != null && !updated.getSubjectName().trim().isEmpty()) {
            existing.setSubjectName(updated.getSubjectName().trim());
        }
        if (updated.getPriority() != null) {
            String p = updated.getPriority().trim();
            if (p.equalsIgnoreCase("High") || p.equalsIgnoreCase("Medium") || p.equalsIgnoreCase("Low")) {
                existing.setPriority(p.substring(0, 1).toUpperCase() + p.substring(1).toLowerCase());
            }
        }
        return subjectRepository.save(existing);
    }

    public void deleteSubject(Long id) {
        subjectRepository.deleteById(id);
    }

    // ==================== SCHEDULING LOGIC ====================

    @Transactional
    public List<StudySchedule> generateSchedule(GenerateScheduleRequest req) {
        Long userId = req.getUserId();
        if (userId == null) {
            throw new IllegalArgumentException("User ID is required.");
        }
        if (req.getStudyHours() <= 0) {
            throw new IllegalArgumentException("Available study hours must be greater than zero.");
        }
        if (req.getStartTime() == null || req.getStartTime().trim().isEmpty()) {
            throw new IllegalArgumentException("Valid starting time is required.");
        }

        // If subjects are passed in request, synchronize/use them; otherwise fetch from repository
        List<Subject> subjects = new ArrayList<>();
        if (req.getSubjects() != null && !req.getSubjects().isEmpty()) {
            for (GenerateScheduleRequest.SubjectDto dto : req.getSubjects()) {
                if (dto.getSubjectName() != null && !dto.getSubjectName().trim().isEmpty()) {
                    Subject s = new Subject(userId, dto.getSubjectName().trim(),
                            normalizePriority(dto.getPriority()));
                    if (dto.getId() != null) {
                        s.setId(dto.getId());
                    } else {
                        s = subjectRepository.save(s);
                    }
                    subjects.add(s);
                }
            }
        } else {
            subjects = subjectRepository.findByUserId(userId);
        }

        if (subjects.isEmpty()) {
            throw new IllegalArgumentException("Please add at least one subject before generating schedule.");
        }

        int totalHours = req.getStudyHours();
        int breakMinutes = req.getBreakMinutes() > 0 ? req.getBreakMinutes() : 15;

        // Priority Distribution:
        // Priority weights: High = 3, Medium = 2, Low = 1
        List<Subject> allocatedSessions = distributeSessions(subjects, totalHours);

        // Parse starting time
        LocalTime currentTime = parseTime(req.getStartTime().trim());

        // Remove old schedule
        scheduleRepository.deleteByUserId(userId);

        List<StudySchedule> scheduleList = new ArrayList<>();
        int order = 1;

        for (int i = 0; i < allocatedSessions.size(); i++) {
            Subject subj = allocatedSessions.get(i);
            LocalTime sessionStart = currentTime;
            LocalTime sessionEnd = currentTime.plusHours(1);

            StudySchedule studySession = new StudySchedule(
                    userId,
                    subj.getId(),
                    subj.getSubjectName(),
                    subj.getPriority(),
                    formatTime12(sessionStart),
                    formatTime12(sessionEnd),
                    "Pending",
                    false,
                    order++
            );
            scheduleList.add(studySession);
            currentTime = sessionEnd;

            // Add break after every 1-hour study session (except optionally after the very last one,
            // or include after every session per requirement: "add a break after every 1-hour study session")
            if (i < allocatedSessions.size() - 1) {
                LocalTime breakStart = currentTime;
                LocalTime breakEnd = currentTime.plusMinutes(breakMinutes);

                StudySchedule breakSession = new StudySchedule(
                        userId,
                        null,
                        "Break (" + breakMinutes + " mins)",
                        null,
                        formatTime12(breakStart),
                        formatTime12(breakEnd),
                        "Break",
                        true,
                        order++
                );
                scheduleList.add(breakSession);
                currentTime = breakEnd;
            }
        }

        return scheduleRepository.saveAll(scheduleList);
    }

    private List<Subject> distributeSessions(List<Subject> subjects, int totalHours) {
        int n = subjects.size();
        List<Subject> result = new ArrayList<>();

        if (totalHours <= 0) return result;

        Map<Subject, Integer> sessionCounts = new LinkedHashMap<>();
        for (Subject s : subjects) {
            sessionCounts.put(s, 0);
        }

        // Map weights
        Map<Subject, Integer> weights = new HashMap<>();
        for (Subject s : subjects) {
            int w = 2; // default Medium
            if ("High".equalsIgnoreCase(s.getPriority())) w = 4;
            else if ("Low".equalsIgnoreCase(s.getPriority())) w = 1;
            weights.put(s, w);
        }

        int remainingHours = totalHours;

        // Step 1: If totalHours >= n, give each subject at least 1 session
        if (remainingHours >= n) {
            for (Subject s : subjects) {
                sessionCounts.put(s, 1);
            }
            remainingHours -= n;
        } else {
            // Sort by priority weight descending and pick top remainingHours
            List<Subject> sorted = new ArrayList<>(subjects);
            sorted.sort((a, b) -> Integer.compare(weights.get(b), weights.get(a)));
            for (int i = 0; i < remainingHours; i++) {
                Subject s = sorted.get(i);
                sessionCounts.put(s, 1);
            }
            remainingHours = 0;
        }

        // Step 2: Distribute remaining hours according to priority weights
        if (remainingHours > 0) {
            // Priority order: High, then Medium, then Low
            List<Subject> sortedByPriority = new ArrayList<>(subjects);
            sortedByPriority.sort((a, b) -> Integer.compare(weights.get(b), weights.get(a)));

            while (remainingHours > 0) {
                for (Subject s : sortedByPriority) {
                    if (remainingHours <= 0) break;
                    // Give slot according to weight ratio
                    sessionCounts.put(s, sessionCounts.get(s) + 1);
                    remainingHours--;
                }
            }
        }

        // Step 3: Create interleaved session list for balanced studying
        // e.g. alternate subjects so the student does not get fatigued by doing 3 hours of one subject back-to-back
        Map<Subject, Integer> remaining = new HashMap<>(sessionCounts);
        Subject lastChosen = null;

        for (int i = 0; i < totalHours; i++) {
            Subject best = null;
            int maxCount = -1;

            // Prefer subject with highest remaining count that is not the same as lastChosen (if alternatives exist)
            for (Subject s : subjects) {
                int count = remaining.get(s);
                if (count > 0) {
                    if (s != lastChosen && count > maxCount) {
                        maxCount = count;
                        best = s;
                    }
                }
            }

            // If no different subject available, pick whatever remains
            if (best == null) {
                for (Subject s : subjects) {
                    int count = remaining.get(s);
                    if (count > 0 && count > maxCount) {
                        maxCount = count;
                        best = s;
                    }
                }
            }

            if (best != null) {
                result.add(best);
                remaining.put(best, remaining.get(best) - 1);
                lastChosen = best;
            }
        }

        return result;
    }

    public List<StudySchedule> getSchedule(Long userId) {
        return scheduleRepository.findByUserIdOrderBySessionOrderAsc(userId);
    }

    public StudySchedule toggleSessionCompletion(Long id) {
        StudySchedule schedule = scheduleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Schedule item not found: " + id));

        if (schedule.isBreak()) {
            return schedule; // breaks are informational
        }

        if ("Completed".equalsIgnoreCase(schedule.getStatus())) {
            schedule.setStatus("Pending");
        } else {
            schedule.setStatus("Completed");
        }
        return scheduleRepository.save(schedule);
    }

    @Transactional
    public void clearSchedule(Long userId) {
        scheduleRepository.deleteByUserId(userId);
    }

    // ==================== PROGRESS ====================

    public ProgressResponse getProgress(Long userId) {
        long totalSubjects = subjectRepository.countByUserId(userId);
        long totalSessions = scheduleRepository.countByUserIdAndIsBreakFalse(userId);
        long completedSessions = scheduleRepository.countByUserIdAndIsBreakFalseAndStatus(userId, "Completed");
        long pendingSessions = Math.max(0, totalSessions - completedSessions);
        int totalPlannedHours = (int) totalSessions;

        double percentage = 0.0;
        if (totalSessions > 0) {
            percentage = Math.round(((double) completedSessions / totalSessions) * 1000.0) / 10.0;
        }

        return new ProgressResponse(totalSubjects, totalPlannedHours, totalSessions,
                completedSessions, pendingSessions, percentage);
    }

    // ==================== UTILITIES ====================

    private String normalizePriority(String priority) {
        if (priority == null) return "Medium";
        priority = priority.trim();
        if (priority.equalsIgnoreCase("High")) return "High";
        if (priority.equalsIgnoreCase("Low")) return "Low";
        return "Medium";
    }

    private LocalTime parseTime(String timeStr) {
        timeStr = timeStr.trim();
        // Support 24-hour "HH:mm" (e.g. "09:00", "14:30")
        try {
            return LocalTime.parse(timeStr, DateTimeFormatter.ofPattern("HH:mm"));
        } catch (Exception ignored) {}

        // Support "H:mm"
        try {
            return LocalTime.parse(timeStr, DateTimeFormatter.ofPattern("H:mm"));
        } catch (Exception ignored) {}

        // Support "hh:mm a" (e.g. "09:00 AM", "09:00 am")
        try {
            return LocalTime.parse(timeStr.toUpperCase(), DateTimeFormatter.ofPattern("hh:mm a", Locale.ENGLISH));
        } catch (Exception ignored) {}

        // Support "h:mm a" (e.g. "9:00 AM")
        try {
            return LocalTime.parse(timeStr.toUpperCase(), DateTimeFormatter.ofPattern("h:mm a", Locale.ENGLISH));
        } catch (Exception ignored) {}

        // Fallback default 09:00
        return LocalTime.of(9, 0);
    }

    private String formatTime12(LocalTime time) {
        return time.format(TIME_FORMATTER_12);
    }
}
