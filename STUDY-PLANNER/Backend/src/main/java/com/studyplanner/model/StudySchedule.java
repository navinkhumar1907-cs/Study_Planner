package com.studyplanner.model;

import jakarta.persistence.*;

@Entity
@Table(name = "study_schedules")
public class StudySchedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "subject_id")
    private Long subjectId;

    @Column(name = "subject_name", nullable = false)
    private String subjectName;

    @Column(name = "priority")
    private String priority;

    @Column(name = "start_time", nullable = false)
    private String startTime;

    @Column(name = "end_time", nullable = false)
    private String endTime;

    @Column(name = "status", nullable = false)
    private String status; // "Pending", "Completed", "Break"

    @Column(name = "is_break", nullable = false)
    private boolean isBreak;

    @Column(name = "session_order")
    private Integer sessionOrder;

    public StudySchedule() {
    }

    public StudySchedule(Long userId, Long subjectId, String subjectName, String priority,
                         String startTime, String endTime, String status, boolean isBreak, Integer sessionOrder) {
        this.userId = userId;
        this.subjectId = subjectId;
        this.subjectName = subjectName;
        this.priority = priority;
        this.startTime = startTime;
        this.endTime = endTime;
        this.status = status;
        this.isBreak = isBreak;
        this.sessionOrder = sessionOrder;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(Long subjectId) {
        this.subjectId = subjectId;
    }

    public String getSubjectName() {
        return subjectName;
    }

    public void setSubjectName(String subjectName) {
        this.subjectName = subjectName;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public boolean isBreak() {
        return isBreak;
    }

    public void setBreak(boolean aBreak) {
        isBreak = aBreak;
    }

    public Integer getSessionOrder() {
        return sessionOrder;
    }

    public void setSessionOrder(Integer sessionOrder) {
        this.sessionOrder = sessionOrder;
    }
}
