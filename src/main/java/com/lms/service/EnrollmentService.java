package com.lms.service;

import com.lms.dao.CourseDAO;
import com.lms.dao.EnrollmentDAO;
import com.lms.dao.UserDAO;
import com.lms.dto.*;
import com.lms.entity.Course;
import com.lms.entity.Enrollment;
import com.lms.entity.User;

import java.sql.SQLException;
import java.util.List;

public class EnrollmentService {

    private final EnrollmentDAO dao =
            new EnrollmentDAO();

    public int createPendingEnrollment(
            int userId,
            int courseId,
            double costPrice)
            throws SQLException {

        return dao.createPendingEnrollment(
                userId,
                courseId,
                costPrice
        );
    }

    public void markAsPaid(
            int enrollmentId)
            throws SQLException {

        dao.updatePaidStatus(
                enrollmentId
        );
    }

    public List<EnrollmentHistoryDto> getEnrollmentHistory(
            int userId) {

        return dao.getEnrollmentHistory(
                userId
        );
    }

    public Page<EnrollmentHistoryDto> getEnrollmentHistoryPage(
            int userId, int page, int size) {
        if (page < 1) page = 1;
        if (size <= 0) size = 5;

        int totalElements = dao.countEnrollmentHistory(userId);
        int totalPages = totalElements == 0 ? 0 : (int) Math.ceil((double) totalElements / size);

        if (totalPages > 0 && page > totalPages) {
            page = totalPages;
        }

        int offset = (page - 1) * size;
        List<EnrollmentHistoryDto> list = dao.getEnrollmentHistoryPage(userId, size, offset);

        return new Page<>(list, page, size, totalElements);
    }

    public boolean isStudentEnrolled(
            int studentId,
            int courseId)
            throws SQLException {

        return dao.isStudentEnrolled(
                studentId,
                courseId
        );
    }

    /**
     * Get all active courses enrolled by one student.
     * <p>
     * Used by the Trainee Dashboard.
     */
    public List<TraineeCourseDTO> getActiveCoursesByStudentId(
            int studentId)
            throws SQLException {

        return dao.findActiveCoursesByStudentId(
                studentId
        );
    }

    /**
     * Build the authenticated trainee's dashboard data from active enrollments
     * and lesson completion records.
     */
    public TraineeDashboardDTO getTraineeDashboard(
            int studentId,
            String keyword,
            String learningStatus,
            String sort,
            int page,
            int size) throws SQLException {

        refreshStudentCourseProgress(studentId);

        List<TraineeCourseDTO> courses =
                dao.findActiveCoursesByStudentId(studentId);

        int inProgressCourses = 0;
        int completedCourses = 0;

        for (TraineeCourseDTO course : courses) {
            int progressPercent = course.getProgressPercent();

            if (progressPercent >= 100) {
                completedCourses++;
            } else if (progressPercent > 0) {
                inProgressCourses++;
            }
        }

        int[] lessonProgress =
                dao.countLessonProgressForActiveCourses(studentId);

        int overallProgressPercent = lessonProgress[1] == 0
                ? 0
                : (int) Math.round(
                lessonProgress[0] * 100.0 / lessonProgress[1]
        );

        TraineeDashboardDTO dashboard = new TraineeDashboardDTO();
        dashboard.setEnrolledCourses(courses.size());
        dashboard.setInProgressCourses(inProgressCourses);
        dashboard.setCompletedCourses(completedCourses);
        dashboard.setOverallProgressPercent(overallProgressPercent);

        if (page < 1) {
            page = 1;
        }

        if (size <= 0) {
            size = 6;
        }

        int totalElements = dao.countDashboardCourses(
                studentId,
                keyword,
                learningStatus
        );

        int totalPages = totalElements == 0
                ? 0
                : (int) Math.ceil((double) totalElements / size);

        if (page > totalPages) {
            page = 1;
        }

        int offset = (page - 1) * size;

        List<TraineeCourseDTO> pageCourses = dao.findDashboardCourses(
                studentId,
                keyword,
                learningStatus,
                sort,
                size,
                offset
        );

        Page<TraineeCourseDTO> coursePage = new Page<>(
                pageCourses,
                page,
                size,
                totalElements
        );

        dashboard.setCourses(pageCourses);
        dashboard.setCoursePage(coursePage);

        return dashboard;
    }

    /**
     * Recalculate and update course progress
     * for one student's enrollment.
     */
    public void updateCourseProgress(
            int studentId,
            int courseId)
            throws SQLException {

        int totalLessons =
                dao.countLessonsByCourseId(
                        courseId
                );

        if (totalLessons <= 0) {

            dao.updateProgressPercent(
                    studentId,
                    courseId,
                    0
            );

            return;
        }

        int completedLessons =
                dao.countCompletedLessons(
                        studentId,
                        courseId
                );

        int progressPercent =
                (int) Math.round(
                        completedLessons * 100.0
                                / totalLessons
                );

        // Keep the value within 0-100.
        progressPercent =
                Math.max(
                        0,
                        Math.min(
                                100,
                                progressPercent
                        )
                );

        dao.updateProgressPercent(
                studentId,
                courseId,
                progressPercent
        );
    }

    /**
     * Recalculate progress of all active courses
     * belonging to one student.
     */
    public void refreshStudentCourseProgress(
            int studentId)
            throws SQLException {

        List<TraineeCourseDTO> courses =
                dao.findActiveCoursesByStudentId(
                        studentId
                );

        for (TraineeCourseDTO course : courses) {

            updateCourseProgress(
                    studentId,
                    course.getCourseId()
            );
        }
    }

    /**
     * Get paginated admin enrollments.
     */
    public Page<AdminEnrollmentDTO> getAdminEnrollmentsPage(
            String keyword,
            String status,
            Integer courseId,
            int page,
            int size) throws SQLException {

        if (page < 1) page = 1;
        if (size <= 0) size = 10;

        int offset = (page - 1) * size;

        int totalElements = dao.countAdminEnrollments(keyword, status, courseId);
        int totalPages = totalElements == 0 ? 0 : (int) Math.ceil((double) totalElements / size);

        if (totalPages > 0 && page > totalPages) {
            page = totalPages;
            offset = (page - 1) * size;
        }

        List<AdminEnrollmentDTO> enrollments = dao.getAdminEnrollments(keyword, status, courseId, offset, size);

        return new Page<>(enrollments, page, size, totalElements);
    }

    /**
     * Get admin enrollment detail by ID.
     */
    public AdminEnrollmentDTO getAdminEnrollmentById(int enrollmentId) throws SQLException {
        return dao.getAdminEnrollmentById(enrollmentId);
    }

    /**
     * Update enrollment status by ID.
     */
    public void updateEnrollmentStatus(int enrollmentId, String status) throws SQLException {
        dao.updateEnrollmentStatus(enrollmentId, status);
    }
}
