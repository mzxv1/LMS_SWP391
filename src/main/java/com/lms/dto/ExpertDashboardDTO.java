package com.lms.dto;

import java.util.List;

/**
 * Presentation model for the Expert Dashboard overview
 * (own course summary + student reach + revenue + recent activity).
 */
public class ExpertDashboardDTO {

    // Courses (owned by this expert only)
    private int totalCourses;
    private int publishedCourses;
    private int draftCourses;
    private int archivedCourses;

    // Students & revenue
    private int totalStudents;       // distinct students across all of this expert's courses
    private int totalEnrollments;    // non-cancelled enrollments across all of this expert's courses
    private double totalRevenue;     // sum of SUCCESS payments for this expert's courses

    // Lists
    private List<CourseDTO> topCourses;
    private List<RecentEnrollmentDTO> recentEnrollments;

    public int getTotalCourses() {
        return totalCourses;
    }

    public void setTotalCourses(int totalCourses) {
        this.totalCourses = totalCourses;
    }

    public int getPublishedCourses() {
        return publishedCourses;
    }

    public void setPublishedCourses(int publishedCourses) {
        this.publishedCourses = publishedCourses;
    }

    public int getDraftCourses() {
        return draftCourses;
    }

    public void setDraftCourses(int draftCourses) {
        this.draftCourses = draftCourses;
    }

    public int getArchivedCourses() {
        return archivedCourses;
    }

    public void setArchivedCourses(int archivedCourses) {
        this.archivedCourses = archivedCourses;
    }

    public int getTotalStudents() {
        return totalStudents;
    }

    public void setTotalStudents(int totalStudents) {
        this.totalStudents = totalStudents;
    }

    public int getTotalEnrollments() {
        return totalEnrollments;
    }

    public void setTotalEnrollments(int totalEnrollments) {
        this.totalEnrollments = totalEnrollments;
    }

    public double getTotalRevenue() {
        return totalRevenue;
    }

    public void setTotalRevenue(double totalRevenue) {
        this.totalRevenue = totalRevenue;
    }

    public List<CourseDTO> getTopCourses() {
        return topCourses;
    }

    public void setTopCourses(List<CourseDTO> topCourses) {
        this.topCourses = topCourses;
    }

    public List<RecentEnrollmentDTO> getRecentEnrollments() {
        return recentEnrollments;
    }

    public void setRecentEnrollments(List<RecentEnrollmentDTO> recentEnrollments) {
        this.recentEnrollments = recentEnrollments;
    }
}
