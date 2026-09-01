package com.lms.dto;

import java.sql.Timestamp;

public class QuizDTO {
    private int id;
    private int courseId;
    private String title;
    private int totalQuestions;
    private int passScore;
    private int timeLimitMin;
    private Timestamp createdAt;
    private Integer chapterId;

    // Latest/Best Attempt Info (Can be null if student hasn't taken it yet)
    private Integer lastAttemptScore;       // score
    private Boolean lastAttemptPassed;      // is_passed
    private Timestamp lastAttemptStartedAt; // started_at
    private Timestamp lastAttemptSubmittedAt; // submitted_at
    private int attemptCount;               // number of times student attempted

    public QuizDTO() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getCourseId() {
        return courseId;
    }

    public void setCourseId(int courseId) {
        this.courseId = courseId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getTotalQuestions() {
        return totalQuestions;
    }

    public void setTotalQuestions(int totalQuestions) {
        this.totalQuestions = totalQuestions;
    }

    public int getPassScore() {
        return passScore;
    }

    public void setPassScore(int passScore) {
        this.passScore = passScore;
    }

    public int getTimeLimitMin() {
        return timeLimitMin;
    }

    public void setTimeLimitMin(int timeLimitMin) {
        this.timeLimitMin = timeLimitMin;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public Integer getLastAttemptScore() {
        return lastAttemptScore;
    }

    public void setLastAttemptScore(Integer lastAttemptScore) {
        this.lastAttemptScore = lastAttemptScore;
    }

    public Boolean getLastAttemptPassed() {
        return lastAttemptPassed;
    }

    public void setLastAttemptPassed(Boolean lastAttemptPassed) {
        this.lastAttemptPassed = lastAttemptPassed;
    }

    public Timestamp getLastAttemptStartedAt() {
        return lastAttemptStartedAt;
    }

    public void setLastAttemptStartedAt(Timestamp lastAttemptStartedAt) {
        this.lastAttemptStartedAt = lastAttemptStartedAt;
    }

    public Timestamp getLastAttemptSubmittedAt() {
        return lastAttemptSubmittedAt;
    }

    public void setLastAttemptSubmittedAt(Timestamp lastAttemptSubmittedAt) {
        this.lastAttemptSubmittedAt = lastAttemptSubmittedAt;
    }

    public int getAttemptCount() {
        return attemptCount;
    }

    public void setAttemptCount(int attemptCount) {
        this.attemptCount = attemptCount;
    }

    public Integer getChapterId() {
        return chapterId;
    }

    public void setChapterId(Integer chapterId) {
        this.chapterId = chapterId;
    }
}
