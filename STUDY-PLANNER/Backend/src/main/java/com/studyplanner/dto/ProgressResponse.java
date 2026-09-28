package com.studyplanner.dto;

public class ProgressResponse {
    private long totalSubjects;
    private int totalPlannedHours;
    private long totalSessions;
    private long completedSessions;
    private long pendingSessions;
    private double completionPercentage;

    public ProgressResponse() {}

    public ProgressResponse(long totalSubjects, int totalPlannedHours, long totalSessions,
                            long completedSessions, long pendingSessions, double completionPercentage) {
        this.totalSubjects = totalSubjects;
        this.totalPlannedHours = totalPlannedHours;
        this.totalSessions = totalSessions;
        this.completedSessions = completedSessions;
        this.pendingSessions = pendingSessions;
        this.completionPercentage = completionPercentage;
    }

    public long getTotalSubjects() { return totalSubjects; }
    public void setTotalSubjects(long totalSubjects) { this.totalSubjects = totalSubjects; }

    public int getTotalPlannedHours() { return totalPlannedHours; }
    public void setTotalPlannedHours(int totalPlannedHours) { this.totalPlannedHours = totalPlannedHours; }

    public long getTotalSessions() { return totalSessions; }
    public void setTotalSessions(long totalSessions) { this.totalSessions = totalSessions; }

    public long getCompletedSessions() { return completedSessions; }
    public void setCompletedSessions(long completedSessions) { this.completedSessions = completedSessions; }

    public long getPendingSessions() { return pendingSessions; }
    public void setPendingSessions(long pendingSessions) { this.pendingSessions = pendingSessions; }

    public double getCompletionPercentage() { return completionPercentage; }
    public void setCompletionPercentage(double completionPercentage) { this.completionPercentage = completionPercentage; }
}
