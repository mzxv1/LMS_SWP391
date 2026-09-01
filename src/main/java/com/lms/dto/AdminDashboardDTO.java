package com.lms.dto;

import java.util.List;

/**
 * Presentation model for the Admin Dashboard overview
 * (system-wide user/course/enrollment/revenue summary + recent activity).
 */
public class AdminDashboardDTO {

    // Users
    private int totalUsers;
    private int totalAdmins;
    private int totalExperts;
    private int totalStudents;
    private int activeUsers;
    private int inactiveUsers;

    // Courses
    private int totalCourses;
    private int publishedCourses;
    private int draftCourses;
    private int archivedCourses;

    // Enrollments & revenue
    private int totalEnrollments;
    private int activeEnrollments;
    private double totalRevenue;

    // Lists
    private List<UserDTO> recentUsers;
    private List<CourseDTO> topCourses;
    private List<RecentEnrollmentDTO> recentEnrollments;

    public int getTotalUsers() {
        return totalUsers;
    }

    public void setTotalUsers(int totalUsers) {
        this.totalUsers = totalUsers;
    }

    public int getTotalAdmins() {
        return totalAdmins;
    }

    public void setTotalAdmins(int totalAdmins) {
        this.totalAdmins = totalAdmins;
    }

    public int getTotalExperts() {
        return totalExperts;
    }

    public void setTotalExperts(int totalExperts) {
        this.totalExperts = totalExperts;
    }

    public int getTotalStudents() {
        return totalStudents;
    }

    public void setTotalStudents(int totalStudents) {
        this.totalStudents = totalStudents;
    }

    public int getActiveUsers() {
        return activeUsers;
    }

    public void setActiveUsers(int activeUsers) {
        this.activeUsers = activeUsers;
    }

    public int getInactiveUsers() {
        return inactiveUsers;
    }

    public void setInactiveUsers(int inactiveUsers) {
        this.inactiveUsers = inactiveUsers;
    }

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

    public int getTotalEnrollments() {
        return totalEnrollments;
    }

    public void setTotalEnrollments(int totalEnrollments) {
        this.totalEnrollments = totalEnrollments;
    }

    public int getActiveEnrollments() {
        return activeEnrollments;
    }

    public void setActiveEnrollments(int activeEnrollments) {
        this.activeEnrollments = activeEnrollments;
    }

    public double getTotalRevenue() {
        return totalRevenue;
    }

    public void setTotalRevenue(double totalRevenue) {
        this.totalRevenue = totalRevenue;
    }

    public List<UserDTO> getRecentUsers() {
        return recentUsers;
    }

    public void setRecentUsers(List<UserDTO> recentUsers) {
        this.recentUsers = recentUsers;
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
