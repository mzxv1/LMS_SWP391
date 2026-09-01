package com.lms.dto;

import java.util.List;

/**
 * Presentation model for the trainee dashboard overview.
 */
public class TraineeDashboardDTO {

    private List<TraineeCourseDTO> courses;
    private Page<TraineeCourseDTO> coursePage;
    private int enrolledCourses;
    private int inProgressCourses;
    private int completedCourses;
    private int overallProgressPercent;

    public List<TraineeCourseDTO> getCourses() {
        return courses;
    }

    public void setCourses(List<TraineeCourseDTO> courses) {
        this.courses = courses;
    }

    public Page<TraineeCourseDTO> getCoursePage() {
        return coursePage;
    }

    public void setCoursePage(Page<TraineeCourseDTO> coursePage) {
        this.coursePage = coursePage;
    }

    public int getEnrolledCourses() {
        return enrolledCourses;
    }

    public void setEnrolledCourses(int enrolledCourses) {
        this.enrolledCourses = enrolledCourses;
    }

    public int getInProgressCourses() {
        return inProgressCourses;
    }

    public void setInProgressCourses(int inProgressCourses) {
        this.inProgressCourses = inProgressCourses;
    }

    public int getCompletedCourses() {
        return completedCourses;
    }

    public void setCompletedCourses(int completedCourses) {
        this.completedCourses = completedCourses;
    }

    public int getOverallProgressPercent() {
        return overallProgressPercent;
    }

    public void setOverallProgressPercent(int overallProgressPercent) {
        this.overallProgressPercent = overallProgressPercent;
    }
}
