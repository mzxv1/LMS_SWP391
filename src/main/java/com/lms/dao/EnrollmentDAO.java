package com.lms.dao;

import com.lms.dto.AdminEnrollmentDTO;
import com.lms.dto.EnrollmentHistoryDto;
import com.lms.dto.RecentEnrollmentDTO;
import com.lms.dto.TraineeCourseDTO;
import com.lms.entity.Enrollment;
import com.lms.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class EnrollmentDAO {
    public int createPendingEnrollment(int userId, int courseId, double costPrice) throws SQLException {
        String sql = "INSERT INTO enrollments (student_id, course_id, is_paid, course_price, status) " +
                "VALUES (?, ?, false, ?, 'PENDING') " +
                "ON CONFLICT (student_id, course_id) DO UPDATE " +
                "SET course_price = EXCLUDED.course_price " +
                "WHERE enrollments.status = 'PENDING' AND enrollments.is_paid = false " +
                "RETURNING id";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, courseId);
            ps.setDouble(3, costPrice);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("id");
                }
            }
        }
        return -1;
    }

    public void updatePaidStatus(int enrollmentId) throws SQLException {
        String sql = "UPDATE enrollments SET is_paid = true, status = 'ACTIVE' WHERE id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, enrollmentId);
            ps.executeUpdate();
        }
    }

    public void updateEnrollmentStatus(int enrollmentId, String status) throws SQLException{
        String sql = "UPDATE enrollments SET status = ? WHERE id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, enrollmentId);
            ps.executeUpdate();
        }
    }

    public List<EnrollmentHistoryDto> getEnrollmentHistory(int userId){
        String sql = "Select e.id as eid, c.id as cid, p.id as pid, c.title, e.course_price, p.payment_method, p.status as pstatus, e.enrolled_at\n" +
                "From enrollments e \n" +
                "Join courses c on e.course_id = c.id\n" +
                "Left Join (\n" +
                "    SELECT id, enrollment_id, payment_method, status,\n" +
                "    ROW_NUMBER() OVER(PARTITION BY enrollment_id ORDER BY id DESC) as rn\n" +
                "    FROM payments\n" +
                ") p on p.enrollment_id = e.id AND p.rn = 1\n" +
                "Where e.student_id = ?\n" +
                "Order by e.enrolled_at DESC";
        List<EnrollmentHistoryDto> lst = new ArrayList<EnrollmentHistoryDto>();
        try {
            Connection con = DBConnection.getConnection();
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()){
                EnrollmentHistoryDto e = new EnrollmentHistoryDto();
                e.setEnrollmentId(rs.getInt("eid"));
                e.setCourseId(rs.getInt("cid"));
                e.setPaymentId(rs.getInt("pid"));
                e.setTitle(rs.getString("title"));
                e.setPrice(rs.getDouble("course_price"));
                e.setPayment_method(rs.getString("payment_method"));
                e.setStatus(rs.getString("pstatus"));
                e.setEnroll_at(rs.getDate("enrolled_at"));
                lst.add(e);
            }
        } catch (Exception e){
            e.printStackTrace();
        }
        return lst;
    }

    public int countEnrollmentHistory(int userId) {
        String sql = "SELECT count(*) FROM enrollments WHERE student_id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    public List<EnrollmentHistoryDto> getEnrollmentHistoryPage(int userId, int limit, int offset) {
        String sql = "Select e.id as eid, c.id as cid, p.id as pid, c.title, e.course_price, p.payment_method, p.status as pstatus, e.enrolled_at\n" +
                "From enrollments e \n" +
                "Join courses c on e.course_id = c.id\n" +
                "Left Join (\n" +
                "    SELECT id, enrollment_id, payment_method, status,\n" +
                "    ROW_NUMBER() OVER(PARTITION BY enrollment_id ORDER BY id DESC) as rn\n" +
                "    FROM payments\n" +
                ") p on p.enrollment_id = e.id AND p.rn = 1\n" +
                "Where e.student_id = ?\n" +
                "Order by e.enrolled_at DESC\n" +
                "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
        List<EnrollmentHistoryDto> lst = new ArrayList<>();
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, offset);
            ps.setInt(3, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()){
                    EnrollmentHistoryDto e = new EnrollmentHistoryDto();
                    e.setEnrollmentId(rs.getInt("eid"));
                    e.setCourseId(rs.getInt("cid"));
                    e.setPaymentId(rs.getInt("pid"));
                    e.setTitle(rs.getString("title"));
                    e.setPrice(rs.getDouble("course_price"));
                    e.setPayment_method(rs.getString("payment_method"));
                    e.setStatus(rs.getString("pstatus"));
                    e.setEnroll_at(rs.getDate("enrolled_at"));
                    lst.add(e);
                }
            }
        } catch (Exception e){
            e.printStackTrace();
        }
        return lst;
    }

    /**
     * Get all active courses enrolled by one student.
     *
     * Used by the Trainee Dashboard.
     */
    public List<TraineeCourseDTO> findActiveCoursesByStudentId(
            int studentId) throws SQLException {

        String sql =
                "SELECT " +
                        "c.id AS course_id, " +
                        "c.title, " +
                        "c.description, " +
                        "s.name AS category, " +
                        "c.price, " +
                        "c.duration_hours, " +
                        "e.progress_percent, " +
                        "e.status AS enrollment_status, " +
                        "e.enrolled_at " +
                        "FROM enrollments e " +
                        "JOIN courses c " +
                        "ON e.course_id = c.id " +
                        "LEFT JOIN settings s " +
                        "ON s.id = c.category_id " +
                        "WHERE e.student_id = ? " +
                        "AND e.status = 'ACTIVE' " +
                        "ORDER BY e.enrolled_at DESC";

        List<TraineeCourseDTO> courses =
                new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, studentId);

            try (ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {

                    TraineeCourseDTO course =
                            new TraineeCourseDTO();

                    course.setCourseId(
                            rs.getInt("course_id")
                    );

                    course.setTitle(
                            rs.getString("title")
                    );

                    course.setDescription(
                            rs.getString("description")
                    );

                    course.setCategory(
                            rs.getString("category")
                    );

                    course.setPrice(
                            rs.getDouble("price")
                    );

                    course.setDurationHours(
                            rs.getInt("duration_hours")
                    );

                    course.setProgressPercent(
                            rs.getInt("progress_percent")
                    );

                    course.setEnrollmentStatus(
                            rs.getString("enrollment_status")
                    );

                    course.setEnrolledAt(
                            rs.getDate("enrolled_at")
                    );

                    courses.add(course);
                }
            }
        }

        return courses;
    }

    /**
     * Load one page of an authenticated trainee's active courses. Search,
     * learning-status filtering and sorting are restricted to that trainee's
     * enrollment records.
     */
    public List<TraineeCourseDTO> findDashboardCourses(
            int studentId,
            String keyword,
            String learningStatus,
            String sort,
            int limit,
            int offset) throws SQLException {

        StringBuilder sql = new StringBuilder(
                "SELECT " +
                        "c.id AS course_id, c.title, c.description, " +
                        "s.name AS category, c.price, c.duration_hours, " +
                        "e.progress_percent, e.status AS enrollment_status, " +
                        "e.enrolled_at " +
                        "FROM enrollments e " +
                        "JOIN courses c ON e.course_id = c.id " +
                        "LEFT JOIN settings s ON s.id = c.category_id " +
                        "WHERE e.student_id = ? " +
                        "AND e.status = 'ACTIVE' "
        );

        List<Object> parameters = new ArrayList<>();
        parameters.add(studentId);

        appendDashboardFilters(sql, parameters, keyword, learningStatus);
        appendDashboardSort(sql, sort);

        sql.append(" LIMIT ? OFFSET ?");
        parameters.add(limit);
        parameters.add(offset);

        List<TraineeCourseDTO> courses = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            setParameters(ps, parameters);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    courses.add(mapTraineeCourse(rs));
                }
            }
        }

        return courses;
    }

    /**
     * Count dashboard courses after applying the list-only search and filter.
     */
    public int countDashboardCourses(
            int studentId,
            String keyword,
            String learningStatus) throws SQLException {

        StringBuilder sql = new StringBuilder(
                "SELECT COUNT(*) " +
                        "FROM enrollments e " +
                        "JOIN courses c ON e.course_id = c.id " +
                        "WHERE e.student_id = ? " +
                        "AND e.status = 'ACTIVE' "
        );

        List<Object> parameters = new ArrayList<>();
        parameters.add(studentId);

        appendDashboardFilters(sql, parameters, keyword, learningStatus);

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            setParameters(ps, parameters);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    private void appendDashboardFilters(
            StringBuilder sql,
            List<Object> parameters,
            String keyword,
            String learningStatus) {

        if (keyword != null && !keyword.isBlank()) {
            sql.append("AND LOWER(c.title) LIKE LOWER(?) ESCAPE '\\' ");
            parameters.add("%" + escapeLikePattern(keyword.trim()) + "%");
        }

        if ("IN_PROGRESS".equals(learningStatus)) {
            sql.append("AND e.progress_percent BETWEEN 1 AND 99 ");
        } else if ("COMPLETED".equals(learningStatus)) {
            sql.append("AND e.progress_percent = 100 ");
        }
    }

    private void appendDashboardSort(StringBuilder sql, String sort) {
        sql.append("ORDER BY ");

        switch (sort) {
            case "name_asc":
                sql.append("LOWER(c.title) ASC, e.enrolled_at DESC");
                break;
            case "name_desc":
                sql.append("LOWER(c.title) DESC, e.enrolled_at DESC");
                break;
            case "progress_asc":
                sql.append("e.progress_percent ASC, e.enrolled_at DESC");
                break;
            case "progress_desc":
                sql.append("e.progress_percent DESC, e.enrolled_at DESC");
                break;
            case "oldest":
                sql.append("e.enrolled_at ASC");
                break;
            case "newest":
            default:
                sql.append("e.enrolled_at DESC");
                break;
        }
    }

    private void setParameters(
            PreparedStatement ps,
            List<Object> parameters) throws SQLException {

        for (int i = 0; i < parameters.size(); i++) {
            Object parameter = parameters.get(i);

            if (parameter instanceof Integer) {
                ps.setInt(i + 1, (Integer) parameter);
            } else {
                ps.setString(i + 1, parameter.toString());
            }
        }
    }

    private TraineeCourseDTO mapTraineeCourse(ResultSet rs)
            throws SQLException {

        TraineeCourseDTO course = new TraineeCourseDTO();
        course.setCourseId(rs.getInt("course_id"));
        course.setTitle(rs.getString("title"));
        course.setDescription(rs.getString("description"));
        course.setCategory(rs.getString("category"));
        course.setPrice(rs.getDouble("price"));
        course.setDurationHours(rs.getInt("duration_hours"));
        course.setProgressPercent(rs.getInt("progress_percent"));
        course.setEnrollmentStatus(rs.getString("enrollment_status"));
        course.setEnrolledAt(rs.getDate("enrolled_at"));

        return course;
    }

    private String escapeLikePattern(String keyword) {
        return keyword
                .replace("\\", "\\\\")
                .replace("%", "\\%")
                .replace("_", "\\_");
    }

    /**
     * Total number of enrollment records system-wide (any status).
     * Used by the Admin Dashboard summary cards.
     */
    public int countAllEnrollments() throws SQLException {
        String sql = "SELECT COUNT(*) FROM enrollments";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    /**
     * Total number of currently ACTIVE enrollments system-wide.
     * Used by the Admin Dashboard summary cards.
     */
    public int countActiveEnrollments() throws SQLException {
        String sql = "SELECT COUNT(*) FROM enrollments WHERE status = 'ACTIVE'";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    /**
     * Total number of non-cancelled enrollments across every course owned
     * by one expert. Used by the Expert Dashboard summary cards.
     */
    public int countEnrollmentsForExpert(int expertId) throws SQLException {
        String sql =
                "SELECT COUNT(*) " +
                        "FROM enrollments e " +
                        "JOIN courses c ON c.id = e.course_id " +
                        "WHERE c.expert_id = ? AND e.status <> 'CANCELLED'";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, expertId);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    /**
     * Number of distinct students enrolled in at least one of one expert's
     * courses (non-cancelled). Used by the Expert Dashboard summary cards.
     */
    public int countDistinctStudentsForExpert(int expertId) throws SQLException {
        String sql =
                "SELECT COUNT(DISTINCT e.student_id) " +
                        "FROM enrollments e " +
                        "JOIN courses c ON c.id = e.course_id " +
                        "WHERE c.expert_id = ? AND e.status <> 'CANCELLED'";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, expertId);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    /**
     * Most recent enrollments system-wide, newest first.
     * Used by the Admin Dashboard "recent activity" panel.
     */
    public List<RecentEnrollmentDTO> findRecentEnrollmentsGlobal(int limit) throws SQLException {
        String sql =
                "SELECT c.id AS course_id, c.title AS course_title, " +
                        "u.full_name AS student_name, e.status, e.enrolled_at " +
                        "FROM enrollments e " +
                        "JOIN courses c ON c.id = e.course_id " +
                        "JOIN users u ON u.id = e.student_id " +
                        "ORDER BY e.enrolled_at DESC " +
                        "LIMIT ?";

        List<RecentEnrollmentDTO> result = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, limit);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(mapRecentEnrollment(rs));
                }
            }
        }

        return result;
    }

    /**
     * Most recent enrollments across one expert's own courses, newest first.
     * Used by the Expert Dashboard "recent activity" panel.
     */
    public List<RecentEnrollmentDTO> findRecentEnrollmentsForExpert(int expertId, int limit) throws SQLException {
        String sql =
                "SELECT c.id AS course_id, c.title AS course_title, " +
                        "u.full_name AS student_name, e.status, e.enrolled_at " +
                        "FROM enrollments e " +
                        "JOIN courses c ON c.id = e.course_id " +
                        "JOIN users u ON u.id = e.student_id " +
                        "WHERE c.expert_id = ? " +
                        "ORDER BY e.enrolled_at DESC " +
                        "LIMIT ?";

        List<RecentEnrollmentDTO> result = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, expertId);
            ps.setInt(2, limit);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(mapRecentEnrollment(rs));
                }
            }
        }

        return result;
    }

    private RecentEnrollmentDTO mapRecentEnrollment(ResultSet rs) throws SQLException {
        RecentEnrollmentDTO dto = new RecentEnrollmentDTO();
        dto.setCourseId(rs.getInt("course_id"));
        dto.setCourseTitle(rs.getString("course_title"));
        dto.setStudentName(rs.getString("student_name"));
        dto.setStatus(rs.getString("status"));
        dto.setEnrolledAt(rs.getTimestamp("enrolled_at"));
        return dto;
    }

    /**
     * Check whether a student has an active enrollment
     * for a specific course.
     */
    public boolean isStudentEnrolled(
            int studentId,
            int courseId)
            throws SQLException {

        String sql =
                "SELECT 1 " +
                        "FROM enrollments " +
                        "WHERE student_id = ? " +
                        "AND course_id = ? " +
                        "AND status = 'ACTIVE' " +
                        "AND is_paid = TRUE " +
                        "LIMIT 1";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, studentId);
            ps.setInt(2, courseId);

            try (ResultSet rs = ps.executeQuery()) {

                return rs.next();
            }
        }
    }

    /**
     * Count total lessons of a course.
     */
    public int countLessonsByCourseId(
            int courseId)
            throws SQLException {

        String sql =
                "SELECT COUNT(*) " +
                        "FROM lessons " +
                        "WHERE course_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, courseId);

            try (ResultSet rs = ps.executeQuery()) {

                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }

        return 0;
    }


    /**
     * Count completed lessons of a student
     * in a specific course.
     */
    public int countCompletedLessons(
            int studentId,
            int courseId)
            throws SQLException {

        String sql =
                "SELECT COUNT(*) " +
                        "FROM lesson_progresses lp " +
                        "JOIN lessons l " +
                        "ON lp.lesson_id = l.id " +
                        "WHERE lp.user_id = ? " +
                        "AND l.course_id = ? " +
                        "AND lp.completed = TRUE";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, studentId);
            ps.setInt(2, courseId);

            try (ResultSet rs = ps.executeQuery()) {

                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }

        return 0;
    }

    /**
     * Count completed and total lessons across a student's active enrollments.
     * The result is used to calculate dashboard-wide learning progress from
     * the same lesson-completion data as an individual course's progress.
     */
    public int[] countLessonProgressForActiveCourses(
            int studentId) throws SQLException {

        String sql =
                "SELECT " +
                        "COUNT(l.id) AS total_lessons, " +
                        "COUNT(lp.lesson_id) FILTER (WHERE lp.completed = TRUE) " +
                        "AS completed_lessons " +
                        "FROM enrollments e " +
                        "LEFT JOIN lessons l ON l.course_id = e.course_id " +
                        "LEFT JOIN lesson_progresses lp " +
                        "ON lp.lesson_id = l.id " +
                        "AND lp.user_id = e.student_id " +
                        "WHERE e.student_id = ? " +
                        "AND e.status = 'ACTIVE'";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, studentId);

            try (ResultSet rs = ps.executeQuery()) {

                if (rs.next()) {
                    return new int[]{
                            rs.getInt("completed_lessons"),
                            rs.getInt("total_lessons")
                    };
                }
            }
        }

        return new int[]{0, 0};
    }

    /**
     * Update progress percentage of one enrollment.
     */
    public void updateProgressPercent(
            int studentId,
            int courseId,
            int progressPercent)
            throws SQLException {

        String sql =
                "UPDATE enrollments " +
                        "SET progress_percent = ? " +
                        "WHERE student_id = ? " +
                        "AND course_id = ? " +
                        "AND status = 'ACTIVE'";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, progressPercent);
            ps.setInt(2, studentId);
            ps.setInt(3, courseId);

            ps.executeUpdate();
        }
    }
    public List<AdminEnrollmentDTO> getAdminEnrollments(String keyword, String status, Integer courseId, int offset, int limit) throws SQLException {
        StringBuilder sql = new StringBuilder(
                "SELECT e.id AS enrollment_id, u.full_name AS student_name, u.email AS student_email, " +
                "c.title AS course_title, e.enrolled_at, e.status, " +
                "e.is_paid, p.amount, p.payment_method " +
                "FROM enrollments e " +
                "JOIN users u ON e.student_id = u.id " +
                "JOIN courses c ON e.course_id = c.id " +
                "LEFT JOIN (SELECT enrollment_id, amount, payment_method, ROW_NUMBER() OVER(PARTITION BY enrollment_id ORDER BY id DESC) as rn FROM payments) p " +
                "ON p.enrollment_id = e.id AND p.rn = 1 " +
                "WHERE 1=1 ");

        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append("AND u.full_name ILIKE ? ");
        }
        if (status != null && !status.trim().isEmpty()) {
            sql.append("AND e.status = ? ");
        }
        if (courseId != null) {
            sql.append("AND e.course_id = ? ");
        }
        sql.append("ORDER BY e.enrolled_at DESC ");
        sql.append("LIMIT ? OFFSET ?");

        List<com.lms.dto.AdminEnrollmentDTO> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            int paramIndex = 1;
            if (keyword != null && !keyword.trim().isEmpty()) {
                ps.setString(paramIndex++, "%" + keyword.trim() + "%");
            }
            if (status != null && !status.trim().isEmpty()) {
                ps.setString(paramIndex++, status);
            }
            if (courseId != null) {
                ps.setInt(paramIndex++, courseId);
            }
            ps.setInt(paramIndex++, limit);
            ps.setInt(paramIndex++, offset);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    com.lms.dto.AdminEnrollmentDTO dto = new com.lms.dto.AdminEnrollmentDTO();
                    dto.setEnrollmentId(rs.getInt("enrollment_id"));
                    dto.setStudentName(rs.getString("student_name"));
                    dto.setStudentEmail(rs.getString("student_email"));
                    dto.setCourseTitle(rs.getString("course_title"));
                    dto.setEnrolledAt(rs.getTimestamp("enrolled_at"));
                    dto.setStatus(rs.getString("status"));
                    dto.setPaid(rs.getBoolean("is_paid"));
                    dto.setAmountPaid(rs.getDouble("amount"));
                    dto.setPaymentMethod(rs.getString("payment_method"));
                    list.add(dto);
                }
            }
        }
        return list;
    }

    public int countAdminEnrollments(String keyword, String status, Integer courseId) throws SQLException {
        StringBuilder sql = new StringBuilder(
                "SELECT COUNT(*) FROM enrollments e " +
                "JOIN users u ON e.student_id = u.id " +
                "WHERE 1=1 ");

        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append("AND u.full_name ILIKE ? ");
        }
        if (status != null && !status.trim().isEmpty()) {
            sql.append("AND e.status = ? ");
        }
        if (courseId != null) {
            sql.append("AND e.course_id = ? ");
        }

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            int paramIndex = 1;
            if (keyword != null && !keyword.trim().isEmpty()) {
                ps.setString(paramIndex++, "%" + keyword.trim() + "%");
            }
            if (status != null && !status.trim().isEmpty()) {
                ps.setString(paramIndex++, status);
            }
            if (courseId != null) {
                ps.setInt(paramIndex++, courseId);
            }

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return 0;
    }

    public AdminEnrollmentDTO getAdminEnrollmentById(int enrollmentId) throws SQLException {
        String sql = "SELECT e.id AS enrollment_id, u.full_name AS student_name, u.email AS student_email, u.phone AS student_phone, " +
                "c.title AS course_title, c.duration_hours, expert.full_name AS expert_name, cat.name AS category_name, " +
                "e.enrolled_at, e.status, " +
                "e.is_paid, p.amount, p.payment_method, p.status AS payment_status " +
                "FROM enrollments e " +
                "JOIN users u ON e.student_id = u.id " +
                "JOIN courses c ON e.course_id = c.id " +
                "LEFT JOIN users expert ON c.expert_id = expert.id " +
                "LEFT JOIN settings cat ON c.category_id = cat.id " +
                "LEFT JOIN (SELECT enrollment_id, amount, payment_method, status, ROW_NUMBER() OVER(PARTITION BY enrollment_id ORDER BY id DESC) as rn FROM payments) p " +
                "ON p.enrollment_id = e.id AND p.rn = 1 " +
                "WHERE e.id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, enrollmentId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    com.lms.dto.AdminEnrollmentDTO dto = new com.lms.dto.AdminEnrollmentDTO();
                    dto.setEnrollmentId(rs.getInt("enrollment_id"));
                    dto.setStudentName(rs.getString("student_name"));
                    dto.setStudentEmail(rs.getString("student_email"));
                    dto.setStudentPhone(rs.getString("student_phone"));
                    dto.setCourseTitle(rs.getString("course_title"));
                    dto.setExpertName(rs.getString("expert_name"));
                    dto.setCategoryName(rs.getString("category_name"));
                    dto.setDurationHours(rs.getInt("duration_hours"));
                    dto.setEnrolledAt(rs.getTimestamp("enrolled_at"));
                    dto.setStatus(rs.getString("status"));
                    dto.setPaid(rs.getBoolean("is_paid"));
                    dto.setAmountPaid(rs.getDouble("amount"));
                    dto.setPaymentMethod(rs.getString("payment_method"));
                    dto.setPaymentStatus(rs.getString("payment_status"));
                    return dto;
                }
            }
        }
        return null;
    }
}
