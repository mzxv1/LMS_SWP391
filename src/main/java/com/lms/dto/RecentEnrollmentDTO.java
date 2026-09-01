package com.lms.dto;

import java.sql.Timestamp;

/**
 * [DTO: Recent Enrollment] Lightweight presentation row for dashboard
 * "recent activity" panels (Admin: system-wide, Expert: own courses only).
 */
public class RecentEnrollmentDTO {

    private int courseId;
    private String courseTitle;
    private String studentName;
    private String status;
    private Timestamp enrolledAt;

    public int getCourseId() {
        return courseId;
    }

    public void setCourseId(int courseId) {
        this.courseId = courseId;
    }

    public String getCourseTitle() {
        return courseTitle;
    }

    public void setCourseTitle(String courseTitle) {
        this.courseTitle = courseTitle;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Timestamp getEnrolledAt() {
        return enrolledAt;
    }

    public void setEnrolledAt(Timestamp enrolledAt) {
        this.enrolledAt = enrolledAt;
    }
}
