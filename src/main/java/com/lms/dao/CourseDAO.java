package com.lms.dao;

import com.lms.dto.CourseDTO;
import com.lms.dto.CourseDetailDTO;
import com.lms.dto.LessonDTO;
import com.lms.entity.Course;
import com.lms.util.DBConnection;

import java.sql.*;
import java.util.*;

/** Data Access Object for the "courses" table. Pure JDBC, no business logic. */
public class CourseDAO {

    private static final Map<String, String> ALLOWED_COURSE_SORT_COLUMNS;

    static {
        Map<String, String> map = new HashMap<>();
        map.put("id", "c.id");
        map.put("title", "c.title");
        map.put("category", "s.name");
        map.put("price", "c.price");
        map.put("duration", "c.duration_hours");
        map.put("expert", "u.full_name");
        map.put("status", "c.status");
        map.put("updated_at", "c.updated_at");
        map.put("created_at", "c.created_at");
        ALLOWED_COURSE_SORT_COLUMNS = Collections.unmodifiableMap(map);
    }

    // ============================================================
    // SHARED COURSE DATA ACCESS
    // ============================================================

    /**
     * Shared course entity lookup by primary key ID.
     * Used by Admin (detail view), Expert (ownership validation), and Student/Checkout contexts.
     *
     * Database behavior:
     * - Queries 'courses' table joined with 'settings' to resolve category name (s.name AS category).
     * - Returns mapped Course entity, or null if not found.
     */
    public Course findById(int id) throws SQLException {
        String sql = "SELECT c.*, s.name AS category FROM courses c LEFT JOIN settings s ON s.id = c.category_id WHERE c.id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapRow(rs) : null;
            }
        }
    }

    public List<Course> findAll() throws SQLException {
        String sql = "SELECT c.*, s.name AS category FROM courses c LEFT JOIN settings s ON s.id = c.category_id ORDER BY c.id DESC";

        List<Course> courses = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                courses.add(mapRow(rs));
            }
        }

        return courses;
    }

    // ============================================================
    // EXPERT COURSE MANAGEMENT
    // ============================================================

    /**
     * Courses owned by one expert - used so an EXPERT only manages their own courses.
     */
    public List<Course> findByExpertId(int expertId) throws SQLException {
        String sql = "SELECT c.*, s.name AS category FROM courses c LEFT JOIN settings s ON s.id = c.category_id WHERE c.expert_id = ? ORDER BY c.id DESC";

        List<Course> courses = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, expertId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    courses.add(mapRow(rs));
                }
            }
        }

        return courses;
    }

    /**
     * Search within one expert's own courses (title/category), used by Course List page.
     */
    public List<Course> search(
            int expertId,
            String keyword,
            String status) throws SQLException {

        StringBuilder sql = new StringBuilder(
                "SELECT c.*, s.name AS category FROM courses c " +
                        "LEFT JOIN settings s ON s.id = c.category_id " +
                        "WHERE c.expert_id = ? " +
                        "AND (c.title ILIKE ? OR s.name ILIKE ?)"
        );

        if (status != null && !status.isEmpty()) {
            sql.append(" AND c.status = ?");
        }

        sql.append(" ORDER BY c.id DESC");

        List<Course> courses = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            String like =
                    "%" + (keyword == null ? "" : keyword.trim()) + "%";

            ps.setInt(1, expertId);
            ps.setString(2, like);
            ps.setString(3, like);

            if (status != null && !status.isEmpty()) {
                ps.setString(4, status);
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    courses.add(mapRow(rs));
                }
            }
        }

        return courses;
    }

    // ============================================================
    // [DungBD] ADMIN COURSE MANAGEMENT LOGIC
    // ============================================================

    /**
     * [DAO: Course Count Query] Counts total course records matching search/filter parameters for pagination.
     * [Flow] SQL SELECT COUNT(1) FROM courses c JOIN users u LEFT JOIN settings s WHERE 1=1 -> ResultSet.getInt(1).
     * [Rules] Mirrors searchAll() WHERE clause filtering to provide exact total count for Page<CourseDTO>.
     * [Output] Integer total count of matching course rows.
     */
    public int countSearchAll(
            String keyword,
            String status) throws SQLException {

        StringBuilder sql = new StringBuilder(
                "SELECT COUNT(1) " +
                "FROM courses c " +
                "JOIN users u ON c.expert_id = u.id " +
                "LEFT JOIN settings s ON s.id = c.category_id " +
                "WHERE 1=1 "
        );

        List<String> params = new ArrayList<>();

        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append("AND (c.title ILIKE ? OR s.name ILIKE ? OR u.full_name ILIKE ?) ");
            String kw = "%" + keyword.trim() + "%";
            params.add(kw);
            params.add(kw);
            params.add(kw);
        }

        if (status != null && !status.trim().isEmpty() && !"ALL".equalsIgnoreCase(status.trim())) {
            sql.append("AND c.status = ? ");
            params.add(status.trim().toUpperCase());
        }

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) {
                ps.setString(i + 1, params.get(i));
            }

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return 0;
    }

    /**
     * [DAO: Course Search & Pagination] Queries system courses with multi-table JOINs, whitelist sorting, and LIMIT/OFFSET.
     * [Flow] SQL SELECT -> PreparedStatement (ILIKE keyword, status, LIMIT, OFFSET) -> ResultSet -> List<CourseDTO>.
     * [Rules] Administrator global access; maps sortBy to ALLOWED_COURSE_SORT_COLUMNS whitelist against SQL injection.
     * [Output] List<CourseDTO> presentation objects.
     */
    public List<CourseDTO> searchAll(
            String keyword,
            String status,
            int page,
            int pageSize,
            String sortBy,
            String sortOrder) throws SQLException {

        StringBuilder sql = new StringBuilder(
                "SELECT c.*, u.full_name AS expert_name, s.name AS category " +
                "FROM courses c " +
                "JOIN users u ON c.expert_id = u.id " +
                "LEFT JOIN settings s ON s.id = c.category_id " +
                "WHERE 1=1 "
        );

        List<Object> params = new ArrayList<>();

        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append("AND (c.title ILIKE ? OR s.name ILIKE ? OR u.full_name ILIKE ?) ");
            String kw = "%" + keyword.trim() + "%";
            params.add(kw);
            params.add(kw);
            params.add(kw);
        }

        if (status != null && !status.trim().isEmpty() && !"ALL".equalsIgnoreCase(status.trim())) {
            sql.append("AND c.status = ? ");
            params.add(status.trim().toUpperCase());
        }

        String column = ALLOWED_COURSE_SORT_COLUMNS.getOrDefault(
                sortBy == null ? "" : sortBy.toLowerCase().trim(),
                "c.id"
        );
        String direction = "ASC".equalsIgnoreCase(sortOrder) ? "ASC" : "DESC";

        sql.append("ORDER BY ").append(column).append(" ").append(direction);
        if (!"c.id".equals(column)) {
            sql.append(", c.id DESC");
        }

        int offset = Math.max(0, (page - 1) * pageSize);
        sql.append(" LIMIT ? OFFSET ?");
        params.add(pageSize);
        params.add(offset);

        List<CourseDTO> courses = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) {
                Object p = params.get(i);
                if (p instanceof Integer) {
                    ps.setInt(i + 1, (Integer) p);
                } else {
                    ps.setString(i + 1, p.toString());
                }
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    CourseDTO dto = new CourseDTO();

                    dto.setId(rs.getInt("id"));
                    dto.setTitle(rs.getString("title"));
                    dto.setDescription(rs.getString("description"));
                    dto.setCategory(rs.getString("category"));
                    dto.setPrice(rs.getBigDecimal("price"));
                    dto.setDurationHours(rs.getInt("duration_hours"));
                    dto.setExpertId(rs.getInt("expert_id"));
                    dto.setExpertName(rs.getString("expert_name"));
                    dto.setStatus(rs.getString("status"));
                    dto.setCreatedAt(rs.getTimestamp("created_at"));
                    dto.setUpdatedAt(rs.getTimestamp("updated_at"));

                    courses.add(dto);
                }
            }
        }

        return courses;
    }

    /**
     * [DAO: Course Search - Overload] Non-paginated overload for backward compatibility.
     */
    public List<CourseDTO> searchAll(String keyword, String status) throws SQLException {
        return searchAll(keyword, status, 1, Integer.MAX_VALUE, "id", "DESC");
    }

    public int insert(Course course) throws SQLException {
        String sql = "INSERT INTO courses (title, description, category_id, price, duration_hours, expert_id, status, created_at, updated_at) " +
                "VALUES (?, ?, (SELECT id FROM settings WHERE name = ? AND type = 'Course Category' LIMIT 1), ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     sql,
                     Statement.RETURN_GENERATED_KEYS)) {

            Timestamp now =
                    new Timestamp(System.currentTimeMillis());

            ps.setString(1, course.getTitle());
            ps.setString(2, course.getDescription());
            ps.setString(3, course.getCategory());
            ps.setBigDecimal(4, course.getPrice());
            ps.setInt(5, course.getDurationHours());
            ps.setInt(6, course.getExpertId());
            ps.setString(7, course.getStatus());
            ps.setTimestamp(8, now);
            ps.setTimestamp(9, now);

            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
        }

        return -1;
    }

    public boolean update(Course course) throws SQLException {
        String sql =
                "UPDATE courses SET " +
                        "title = ?, " +
                        "description = ?, " +
                        "category_id = (SELECT id FROM settings WHERE name = ? AND type = 'Course Category' LIMIT 1), " +
                        "price = ?, " +
                        "duration_hours = ?, " +
                        "status = ?, " +
                        "updated_at = ? " +
                        "WHERE id = ? AND expert_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, course.getTitle());
            ps.setString(2, course.getDescription());
            ps.setString(3, course.getCategory());
            ps.setBigDecimal(4, course.getPrice());
            ps.setInt(5, course.getDurationHours());
            ps.setString(6, course.getStatus());
            ps.setTimestamp(
                    7,
                    new Timestamp(System.currentTimeMillis())
            );
            ps.setInt(8, course.getId());
            ps.setInt(9, course.getExpertId());

            return ps.executeUpdate() > 0;
        }
    }

    /**
     * [DAO: Status Mutation] Updates the status and updated_at timestamp for a specific course ID.
     * [Flow] SQL UPDATE courses SET status = ?, updated_at = ? WHERE id = ? -> PreparedStatement.executeUpdate().
     * [Rules] Administrator oversight operation; mutates publication status without expert_id ownership constraint.
     * [Output] Boolean true if at least one row was updated, false otherwise.
     */
    public boolean updateStatus(
            int id,
            String status) throws SQLException {

        String sql =
                "UPDATE courses " +
                        "SET status = ?, updated_at = ? " +
                        "WHERE id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, status);
            ps.setTimestamp(
                    2,
                    new Timestamp(System.currentTimeMillis())
            );
            ps.setInt(3, id);

            return ps.executeUpdate() > 0;
        }
    }

    public boolean deleteById(
            int id,
            int expertId) throws SQLException {

        String sql =
                "DELETE FROM courses " +
                        "WHERE id = ? AND expert_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            ps.setInt(2, expertId);

            return ps.executeUpdate() > 0;
        }
    }

    private Course mapRow(ResultSet rs) throws SQLException {
        Course c = new Course();

        c.setId(rs.getInt("id"));
        c.setTitle(rs.getString("title"));
        c.setDescription(rs.getString("description"));
        c.setPrice(rs.getBigDecimal("price"));
        c.setCategory(rs.getString("category"));
        c.setPrice(rs.getBigDecimal("price"));
        c.setDurationHours(rs.getInt("duration_hours"));
        c.setExpertId(rs.getInt("expert_id"));
        c.setStatus(rs.getString("status"));
        c.setCreatedAt(rs.getTimestamp("created_at"));
        c.setUpdatedAt(rs.getTimestamp("updated_at"));

        return c;
    }

    // ============================================================
    // PUBLIC COURSE (Student/Guest-facing)
    // ============================================================

    /**
     * All currently "active" courses for the public Home page:
     * status = PUBLISHED and the owning expert's account is active.
     */
    public List<Course> findAllPublished() throws SQLException {
        String sql = "SELECT c.*, s.name AS category FROM courses c " +
                "JOIN users u ON u.id = c.expert_id " +
                "LEFT JOIN settings s ON s.id = c.category_id " +
                "WHERE c.status = 'PUBLISHED' AND u.active = TRUE " +
                "ORDER BY c.updated_at DESC";

        List<Course> courses = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                courses.add(mapRow(rs));
            }
        }

        return courses;
    }

    /**
     * Top N currently "active" courses for the public Home page, ranked by
     * popularity (number of non-cancelled enrollments, highest first).
     * Only PUBLISHED courses owned by an ACTIVE expert are eligible.
     */
    public List<CourseDTO> findTopPublishedByEnrollment(int limit) throws SQLException {
        String sql =
                "SELECT c.*, u.full_name AS expert_name, s.name AS category, " +
                        "COUNT(e.id) FILTER (WHERE e.status <> 'CANCELLED') AS enrollment_count " +
                        "FROM courses c " +
                        "JOIN users u ON u.id = c.expert_id " +
                        "LEFT JOIN settings s ON s.id = c.category_id " +
                        "LEFT JOIN enrollments e ON e.course_id = c.id " +
                        "WHERE c.status = 'PUBLISHED' AND u.active = TRUE " +
                        "GROUP BY c.id, u.full_name, s.name " +
                        "ORDER BY enrollment_count DESC, c.created_at DESC " +
                        "LIMIT ?";

        List<CourseDTO> courses = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, limit);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    courses.add(mapDtoRowWithEnrollmentCount(rs));
                }
            }
        }

        return courses;
    }

    /**
     * Top N courses owned by one expert, ranked by popularity (number of
     * non-cancelled enrollments, highest first). Used by the Expert Dashboard.
     */
    public List<CourseDTO> findTopByExpertEnrollment(int expertId, int limit) throws SQLException {
        String sql =
                "SELECT c.*, u.full_name AS expert_name, s.name AS category, " +
                        "COUNT(e.id) FILTER (WHERE e.status <> 'CANCELLED') AS enrollment_count " +
                        "FROM courses c " +
                        "JOIN users u ON u.id = c.expert_id " +
                        "LEFT JOIN settings s ON s.id = c.category_id " +
                        "LEFT JOIN enrollments e ON e.course_id = c.id " +
                        "WHERE c.expert_id = ? " +
                        "GROUP BY c.id, u.full_name, s.name " +
                        "ORDER BY enrollment_count DESC, c.created_at DESC " +
                        "LIMIT ?";

        List<CourseDTO> courses = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, expertId);
            ps.setInt(2, limit);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    courses.add(mapDtoRowWithEnrollmentCount(rs));
                }
            }
        }

        return courses;
    }

    /**
     * System-wide course counts grouped by status (DRAFT/PUBLISHED/ARCHIVED).
     * Used by the Admin Dashboard summary cards.
     */
    public Map<String, Integer> countCoursesByStatus() throws SQLException {
        String sql = "SELECT status, COUNT(*) AS cnt FROM courses GROUP BY status";

        Map<String, Integer> counts = new HashMap<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                counts.put(rs.getString("status"), rs.getInt("cnt"));
            }
        }

        return counts;
    }

    /**
     * Course counts grouped by status for one expert's own courses.
     * Used by the Expert Dashboard summary cards.
     */
    public Map<String, Integer> countCoursesByStatusForExpert(int expertId) throws SQLException {
        String sql = "SELECT status, COUNT(*) AS cnt FROM courses WHERE expert_id = ? GROUP BY status";

        Map<String, Integer> counts = new HashMap<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, expertId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    counts.put(rs.getString("status"), rs.getInt("cnt"));
                }
            }
        }

        return counts;
    }

    /**
     * Maps a result row that includes the joined expert_name/category plus an
     * aggregated enrollment_count column (see findTopPublishedByEnrollment /
     * findTopByExpertEnrollment) into a presentation CourseDTO.
     */
    private CourseDTO mapDtoRowWithEnrollmentCount(ResultSet rs) throws SQLException {
        CourseDTO dto = new CourseDTO();

        dto.setId(rs.getInt("id"));
        dto.setTitle(rs.getString("title"));
        dto.setDescription(rs.getString("description"));
        dto.setCategory(rs.getString("category"));
        dto.setPrice(rs.getBigDecimal("price"));
        dto.setDurationHours(rs.getInt("duration_hours"));
        dto.setExpertId(rs.getInt("expert_id"));
        dto.setExpertName(rs.getString("expert_name"));
        dto.setStatus(rs.getString("status"));
        dto.setCreatedAt(rs.getTimestamp("created_at"));
        dto.setUpdatedAt(rs.getTimestamp("updated_at"));
        dto.setEnrollmentCount(rs.getInt("enrollment_count"));

        return dto;
    }


    // ============================================================
    // PUBLIC COURSE LIST
    // ============================================================

    /**
     * Public course list with:
     * - keyword search
     * - category filter
     * - sorting
     * - pagination
     * <p>
     * Only PUBLISHED courses owned by ACTIVE experts are displayed.
     */
    public List<CourseDTO> findPublicCourses(
            String keyword,
            String category,
            String sort,
            int limit,
            int offset) throws SQLException {

        StringBuilder sql = new StringBuilder(
                "SELECT c.*, u.full_name AS expert_name, s.name AS category " +
                        "FROM courses c " +
                        "JOIN users u ON u.id = c.expert_id " +
                        "LEFT JOIN settings s ON s.id = c.category_id " +
                        "WHERE c.status = 'PUBLISHED' " +
                        "AND u.active = TRUE "
        );

        List<Object> parameters = new ArrayList<>();

        if (keyword != null && !keyword.trim().isEmpty()) {

            sql.append(
                    "AND (" +
                            "LOWER(c.title) LIKE LOWER(?) ESCAPE '\\' " +
                            "OR LOWER(c.description) LIKE LOWER(?) ESCAPE '\\' " +
                            "OR LOWER(s.name) LIKE LOWER(?) ESCAPE '\\'" +
                            ") "
            );

            String searchValue =
                    "%" + escapeLikePattern(keyword.trim()) + "%";

            parameters.add(searchValue);
            parameters.add(searchValue);
            parameters.add(searchValue);
        }

        if (category != null
                && !category.trim().isEmpty()
                && !"ALL".equalsIgnoreCase(category.trim())) {

            sql.append("AND s.name = ? ");

            parameters.add(category.trim());
        }

        sql.append("ORDER BY ");

        switch (sort == null ? "newest" : sort.toLowerCase()) {

            case "oldest":
                sql.append("c.created_at ASC");
                break;

            case "price_asc":
                sql.append("c.price ASC, c.created_at DESC");
                break;

            case "price_desc":
                sql.append("c.price DESC, c.created_at DESC");
                break;

            case "newest":
            default:
                sql.append("c.created_at DESC");
                break;
        }

        sql.append(" LIMIT ? OFFSET ?");

        parameters.add(limit);
        parameters.add(offset);

        List<CourseDTO> courses = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps =
                     conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < parameters.size(); i++) {

                Object parameter = parameters.get(i);

                if (parameter instanceof Integer) {
                    ps.setInt(i + 1, (Integer) parameter);
                } else {
                    ps.setString(i + 1, parameter.toString());
                }
            }

            try (ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {

                    CourseDTO dto = new CourseDTO();

                    dto.setId(rs.getInt("id"));
                    dto.setTitle(rs.getString("title"));
                    dto.setDescription(
                            rs.getString("description")
                    );
                    dto.setCategory(
                            rs.getString("category")
                    );
                    dto.setPrice(
                            rs.getBigDecimal("price")
                    );
                    dto.setDurationHours(
                            rs.getInt("duration_hours")
                    );
                    dto.setExpertId(
                            rs.getInt("expert_id")
                    );
                    dto.setExpertName(
                            rs.getString("expert_name")
                    );
                    dto.setStatus(
                            rs.getString("status")
                    );
                    dto.setCreatedAt(
                            rs.getTimestamp("created_at")
                    );
                    dto.setUpdatedAt(
                            rs.getTimestamp("updated_at")
                    );

                    courses.add(dto);
                }
            }
        }

        return courses;
    }


    /**
     * Count public courses matching the current search/filter.
     * Used to calculate total pages.
     */
    public int countPublicCourses(
            String keyword,
            String category) throws SQLException {

        StringBuilder sql = new StringBuilder(
                "SELECT COUNT(*) " +
                        "FROM courses c " +
                        "JOIN users u ON u.id = c.expert_id " +
                        "LEFT JOIN settings s ON s.id = c.category_id " +
                        "WHERE c.status = 'PUBLISHED' " +
                        "AND u.active = TRUE "
        );

        List<String> parameters = new ArrayList<>();

        if (keyword != null && !keyword.trim().isEmpty()) {

            sql.append(
                    "AND (" +
                            "LOWER(c.title) LIKE LOWER(?) ESCAPE '\\' " +
                            "OR LOWER(c.description) LIKE LOWER(?) ESCAPE '\\' " +
                            "OR LOWER(s.name) LIKE LOWER(?) ESCAPE '\\'" +
                            ") "
            );

            String searchValue =
                    "%" + escapeLikePattern(keyword.trim()) + "%";

            parameters.add(searchValue);
            parameters.add(searchValue);
            parameters.add(searchValue);
        }

        if (category != null
                && !category.trim().isEmpty()
                && !"ALL".equalsIgnoreCase(category.trim())) {

            sql.append("AND s.name = ? ");

            parameters.add(category.trim());
        }

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps =
                     conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < parameters.size(); i++) {
                ps.setString(i + 1, parameters.get(i));
            }

            try (ResultSet rs = ps.executeQuery()) {

                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }

        return 0;
    }

    /**
     * Escapes SQL LIKE metacharacters so public catalog searches treat user
     * input literally. For example, '%' must not match every published course.
     */
    private String escapeLikePattern(String keyword) {
        return keyword
                .replace("\\", "\\\\")
                .replace("%", "\\%")
                .replace("_", "\\_");
    }


    /**
     * Get all available course categories
     * for the public filter dropdown.
     */
    public List<String> findAllCategories()
            throws SQLException {

        String sql =
                "SELECT name " +
                        "FROM settings " +
                        "WHERE type = 'Course Category' AND status = 'Active' " +
                        "ORDER BY priority ASC, name ASC";

        List<String> categories =
                new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps =
                     conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                categories.add(
                        rs.getString("name")
                );
            }
        }

        return categories;
    }


    // ============================================================
    // PUBLIC COURSE DETAIL
    // ============================================================

    /**
     * Find one published course for public detail page.
     * <p>
     * Public users can only see:
     * - PUBLISHED courses
     * - courses owned by an active expert
     * <p>
     * Also loads:
     * - expert name
     * - average rating
     * - review count
     * - lesson count
     */
    public CourseDetailDTO findPublishedCourseDetail(
            int courseId) throws SQLException {

        String sql =
                "SELECT c.*, " +
                        "s.name AS category, " +
                        "u.full_name AS expert_name, " +
                        "COALESCE(AVG(r.rating), 0) AS average_rating, " +
                        "COUNT(DISTINCT r.id) AS review_count, " +
                        "COUNT(DISTINCT l.id) AS lesson_count " +
                        "FROM courses c " +
                        "JOIN users u ON u.id = c.expert_id " +
                        "LEFT JOIN settings s ON s.id = c.category_id " +
                        "LEFT JOIN reviews r ON r.course_id = c.id " +
                        "LEFT JOIN lessons l ON l.course_id = c.id " +
                        "WHERE c.id = ? " +
                        "AND c.status = 'PUBLISHED' " +
                        "AND u.active = TRUE " +
                        "GROUP BY c.id, s.name, u.full_name";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps =
                     conn.prepareStatement(sql)) {

            ps.setInt(1, courseId);

            try (ResultSet rs = ps.executeQuery()) {

                if (!rs.next()) {
                    return null;
                }

                CourseDetailDTO dto =
                        new CourseDetailDTO();

                dto.setId(
                        rs.getInt("id")
                );

                dto.setTitle(
                        rs.getString("title")
                );

                dto.setDescription(
                        rs.getString("description")
                );

                dto.setCategory(
                        rs.getString("category")
                );

                dto.setPrice(
                        rs.getBigDecimal("price")
                );

                dto.setDurationHours(
                        rs.getInt("duration_hours")
                );

                dto.setExpertId(
                        rs.getInt("expert_id")
                );

                dto.setExpertName(
                        rs.getString("expert_name")
                );

                dto.setStatus(
                        rs.getString("status")
                );

                dto.setCreatedAt(
                        rs.getTimestamp("created_at")
                );

                dto.setUpdatedAt(
                        rs.getTimestamp("updated_at")
                );

                dto.setAverageRating(
                        rs.getDouble("average_rating")
                );

                dto.setReviewCount(
                        rs.getInt("review_count")
                );

                dto.setLessonCount(
                        rs.getInt("lesson_count")
                );

                return dto;
            }
        }
    }


    /**
     * Get the first 3 lesson titles for the public course detail page.
     * <p>
     * Public users who have not enrolled in the course
     * can only preview the first 3 lessons.
     * <p>
     * Video URL is intentionally not exposed.
     */
    public List<LessonDTO> findPreviewLessonsByCourseId(
            int courseId) throws SQLException {

        String sql =
                "SELECT id, course_id, title, " +
                        "duration_minutes, order_index " +
                        "FROM lessons " +
                        "WHERE course_id = ? " +
                        "ORDER BY order_index ASC, id ASC " +
                        "LIMIT 3";

        List<LessonDTO> lessons =
                new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps =
                     conn.prepareStatement(sql)) {

            ps.setInt(1, courseId);

            try (ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {

                    LessonDTO lesson =
                            new LessonDTO();

                    lesson.setId(
                            rs.getInt("id")
                    );

                    lesson.setCourseId(
                            rs.getInt("course_id")
                    );

                    lesson.setTitle(
                            rs.getString("title")
                    );

                    lesson.setDurationMinutes(
                            rs.getInt("duration_minutes")
                    );

                    lesson.setOrderIndex(
                            rs.getInt("order_index")
                    );

                    lessons.add(lesson);
                }
            }
        }

        return lessons;
    }


    /**
     * Get all lessons of a course.
     * <p>
     * This method is reserved for users who are allowed
     * to access the full course content, such as enrolled students.
     * <p>
     * Video URL is intentionally not exposed here.
     */
    public List<LessonDTO> findLessonsByCourseId(
            int courseId) throws SQLException {

        String sql =
                "SELECT id, course_id, title, " +
                        "duration_minutes, order_index " +
                        "FROM lessons " +
                        "WHERE course_id = ? " +
                        "ORDER BY order_index ASC, id ASC";

        List<LessonDTO> lessons =
                new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps =
                     conn.prepareStatement(sql)) {

            ps.setInt(1, courseId);

            try (ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {

                    LessonDTO lesson =
                            new LessonDTO();

                    lesson.setId(
                            rs.getInt("id")
                    );

                    lesson.setCourseId(
                            rs.getInt("course_id")
                    );

                    lesson.setTitle(
                            rs.getString("title")
                    );

                    lesson.setDurationMinutes(
                            rs.getInt("duration_minutes")
                    );

                    lesson.setOrderIndex(
                            rs.getInt("order_index")
                    );

                    lessons.add(lesson);
                }
            }
        }

        return lessons;
    }

    /**
     * Get lessons grouped by chapter information
     * for the Trainee My Course Detail page.
     *
     * This method is dedicated to the trainee course-detail flow
     * and does not modify the existing lesson query.
     */
    public List<LessonDTO> findLessonsForTraineeCourseDetail(
            int courseId) throws SQLException {

        String sql =
                "SELECT id, course_id, chapter_id, title, " +
                        "duration_minutes, order_index " +
                        "FROM lessons " +
                        "WHERE course_id = ? " +
                        "ORDER BY chapter_id ASC, " +
                        "order_index ASC, id ASC";

        List<LessonDTO> lessons =
                new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps =
                     conn.prepareStatement(sql)) {

            ps.setInt(1, courseId);

            try (ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {

                    LessonDTO lesson =
                            new LessonDTO();

                    lesson.setId(
                            rs.getInt("id")
                    );

                    lesson.setCourseId(
                            rs.getInt("course_id")
                    );

                    lesson.setChapterId(
                            rs.getInt("chapter_id")
                    );

                    lesson.setTitle(
                            rs.getString("title")
                    );

                    lesson.setDurationMinutes(
                            rs.getInt("duration_minutes")
                    );

                    lesson.setOrderIndex(
                            rs.getInt("order_index")
                    );

                    lessons.add(lesson);
                }
            }
        }

        return lessons;
    }
}
