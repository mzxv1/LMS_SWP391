package com.lms.service;

import com.lms.dao.CourseDAO;
import com.lms.dao.EnrollmentDAO;
import com.lms.dao.PaymentDAO;
import com.lms.dao.UserDAO;
import com.lms.dto.AdminDashboardDTO;
import com.lms.dto.ExpertDashboardDTO;
import com.lms.dto.UserDTO;
import com.lms.entity.User;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Business logic layer for the Admin and Expert dashboard overview pages.
 * Pure read-only aggregation over existing DAOs - no mutation here.
 */
public class DashboardService {

    private static final int RECENT_USERS_LIMIT = 5;
    private static final int RECENT_ENROLLMENTS_LIMIT = 5;
    private static final int TOP_COURSES_LIMIT = 5;

    private final UserDAO userDAO = new UserDAO();
    private final CourseDAO courseDAO = new CourseDAO();
    private final EnrollmentDAO enrollmentDAO = new EnrollmentDAO();
    private final PaymentDAO paymentDAO = new PaymentDAO();


    // ============================================================
    // ADMIN DASHBOARD
    // ============================================================

    /**
     * System-wide overview for the Admin Dashboard:
     * user/course/enrollment counts, total revenue, and recent activity.
     */
    public AdminDashboardDTO getAdminDashboard() throws SQLException {

        AdminDashboardDTO dashboard = new AdminDashboardDTO();

        // --- Users ---
        Map<String, Integer> usersByRole = userDAO.countUsersByRole();
        int totalAdmins = usersByRole.getOrDefault("ADMIN", 0);
        int totalExperts = usersByRole.getOrDefault("EXPERT", 0);
        int totalStudents = usersByRole.getOrDefault("STUDENT", 0);

        dashboard.setTotalAdmins(totalAdmins);
        dashboard.setTotalExperts(totalExperts);
        dashboard.setTotalStudents(totalStudents);
        dashboard.setTotalUsers(totalAdmins + totalExperts + totalStudents);
        dashboard.setActiveUsers(userDAO.countByActiveStatus(true));
        dashboard.setInactiveUsers(userDAO.countByActiveStatus(false));

        // --- Courses ---
        Map<String, Integer> coursesByStatus = courseDAO.countCoursesByStatus();
        int published = coursesByStatus.getOrDefault("PUBLISHED", 0);
        int draft = coursesByStatus.getOrDefault("DRAFT", 0);
        int archived = coursesByStatus.getOrDefault("ARCHIVED", 0);

        dashboard.setPublishedCourses(published);
        dashboard.setDraftCourses(draft);
        dashboard.setArchivedCourses(archived);
        dashboard.setTotalCourses(published + draft + archived);

        // --- Enrollments & revenue ---
        dashboard.setTotalEnrollments(enrollmentDAO.countAllEnrollments());
        dashboard.setActiveEnrollments(enrollmentDAO.countActiveEnrollments());
        dashboard.setTotalRevenue(paymentDAO.sumRevenue());

        // --- Lists ---
        List<UserDTO> recentUsers = userDAO.findRecent(RECENT_USERS_LIMIT)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());

        dashboard.setRecentUsers(recentUsers);
        dashboard.setTopCourses(courseDAO.findTopPublishedByEnrollment(TOP_COURSES_LIMIT));
        dashboard.setRecentEnrollments(enrollmentDAO.findRecentEnrollmentsGlobal(RECENT_ENROLLMENTS_LIMIT));

        return dashboard;
    }


    // ============================================================
    // EXPERT DASHBOARD
    // ============================================================

    /**
     * Per-expert overview for the Expert Dashboard: own course counts,
     * student reach, revenue earned, and recent enrollment activity.
     */
    public ExpertDashboardDTO getExpertDashboard(int expertId) throws SQLException {

        ExpertDashboardDTO dashboard = new ExpertDashboardDTO();

        // --- Courses (owned by this expert) ---
        Map<String, Integer> coursesByStatus = courseDAO.countCoursesByStatusForExpert(expertId);
        int published = coursesByStatus.getOrDefault("PUBLISHED", 0);
        int draft = coursesByStatus.getOrDefault("DRAFT", 0);
        int archived = coursesByStatus.getOrDefault("ARCHIVED", 0);

        dashboard.setPublishedCourses(published);
        dashboard.setDraftCourses(draft);
        dashboard.setArchivedCourses(archived);
        dashboard.setTotalCourses(published + draft + archived);

        // --- Students & revenue ---
        dashboard.setTotalStudents(enrollmentDAO.countDistinctStudentsForExpert(expertId));
        dashboard.setTotalEnrollments(enrollmentDAO.countEnrollmentsForExpert(expertId));
        dashboard.setTotalRevenue(paymentDAO.sumRevenueForExpert(expertId));

        // --- Lists ---
        dashboard.setTopCourses(courseDAO.findTopByExpertEnrollment(expertId, TOP_COURSES_LIMIT));
        dashboard.setRecentEnrollments(
                enrollmentDAO.findRecentEnrollmentsForExpert(expertId, RECENT_ENROLLMENTS_LIMIT)
        );

        return dashboard;
    }


    /**
     * [Helper: Model Transformation] Maps a User database domain entity into a UserDTO representation model.
     * Mirrors UserService.toDTO(): strips the password hash, copies public profile attributes.
     */
    private UserDTO toDTO(User u) {
        UserDTO dto = new UserDTO();
        dto.setId(u.getId());
        dto.setUsername(u.getUsername());
        dto.setEmail(u.getEmail());
        dto.setFullName(u.getFullName());
        dto.setPhone(u.getPhone());
        dto.setRole(u.getRole().name());
        dto.setActive(u.isActive());
        dto.setCreatedAt(u.getCreatedAt());
        return dto;
    }
}
