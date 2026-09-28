package com.studyplanner.dto;

import java.util.List;

public class GenerateScheduleRequest {
    private Long userId;
    private int studyHours;
    private String startTime; // "09:00" or "09:00 AM"
    private int breakMinutes = 15;
    private List<SubjectDto> subjects;

    public GenerateScheduleRequest() {}

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public int getStudyHours() { return studyHours; }
    public void setStudyHours(int studyHours) { this.studyHours = studyHours; }

    public String getStartTime() { return startTime; }
    public void setStartTime(String startTime) { this.startTime = startTime; }

    public int getBreakMinutes() { return breakMinutes; }
    public void setBreakMinutes(int breakMinutes) { this.breakMinutes = breakMinutes; }

    public List<SubjectDto> getSubjects() { return subjects; }
    public void setSubjects(List<SubjectDto> subjects) { this.subjects = subjects; }

    public static class SubjectDto {
        private Long id;
        private String subjectName;
        private String priority; // High, Medium, Low

        public SubjectDto() {}

        public SubjectDto(Long id, String subjectName, String priority) {
            this.id = id;
            this.subjectName = subjectName;
            this.priority = priority;
        }

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }

        public String getSubjectName() { return subjectName; }
        public void setSubjectName(String subjectName) { this.subjectName = subjectName; }

        public String getPriority() { return priority; }
        public void setPriority(String priority) { this.priority = priority; }
    }
}
