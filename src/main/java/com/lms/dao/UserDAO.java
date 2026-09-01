package com.lms.dao;

import com.lms.entity.Role;
import com.lms.entity.User;
import com.lms.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Data Access Object for the "users" table.
 * Pure JDBC - no business logic here, that belongs in UserService.
 */
public class UserDAO {

    private static final Map<String, String> ALLOWED_SORT_COLUMNS;

    static {
        Map<String, String> map = new HashMap<>();
        map.put("id", "u.id");
        map.put("username", "u.username");
        map.put("full_name", "u.full_name");
        map.put("fullname", "u.full_name");
        map.put("email", "u.email");
        map.put("phone", "u.phone");
        map.put("role", "u.role");
        map.put("active", "u.active");
        map.put("status", "u.active");
        map.put("created_at", "u.created_at");
        ALLOWED_SORT_COLUMNS = Collections.unmodifiableMap(map);
    }

    // ============================================================
    // SHARED USER DATA ACCESS
    // ============================================================

    /**
     * Shared user lookup by primary key ID.
     * Used by Admin (User Detail & Status Toggle), Self-service Profile, and Auth contexts.
     *
     * Database behavior:
     * - Queries 'users' table where id = ?.
     * - Returns mapped User entity, or null if not found.
     */
    public User findById(int id) throws SQLException {
        String sql = "SELECT * FROM users WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapRow(rs) : null;
            }
        }
    }

    // ============================================================
    // AUTHENTICATION
    // ============================================================

    public User findByUsername(String username) throws SQLException {
        String sql = "SELECT * FROM users WHERE username = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapRow(rs) : null;
            }
        }
    }

    public User findByEmail(String email) throws SQLException {
        String sql = "SELECT * FROM users WHERE email = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapRow(rs) : null;
            }
        }
    }

    public List<User> findAll() throws SQLException {
        String sql = "SELECT * FROM users ORDER BY id DESC";
        List<User> users = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                users.add(mapRow(rs));
            }
        }
        return users;
    }

    // ============================================================
    // [DungBD] ADMIN USER MANAGEMENT LOGIC
    // ============================================================

    /**
     * [DAO: User Search & Pagination] Queries users with dynamic multi-field filters and safe column sorting.
     * [Flow] SQL SELECT WHERE 1=1 -> PreparedStatement parameters -> ResultSet -> mapRow() -> List<User>.
     * [Rules] Binds ILIKE keyword across 4 fields; maps sortBy to ALLOWED_SORT_COLUMNS whitelist against SQL injection.
     * [Output] List<User> entities matching search criteria.
     */
    public List<User> search(
            String keyword,
            String role,
            String status,
            int page,
            int pageSize,
            String sortBy,
            String sortOrder) throws SQLException {

        StringBuilder sql = new StringBuilder("SELECT u.* FROM users u WHERE 1=1 ");
        List<Object> params = new ArrayList<>();

        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append("AND (u.username ILIKE ? OR u.full_name ILIKE ? OR u.email ILIKE ? OR u.phone ILIKE ?) ");
            String kw = "%" + keyword.trim() + "%";
            params.add(kw);
            params.add(kw);
            params.add(kw);
            params.add(kw);
        }

        if (role != null && !role.trim().isEmpty() && !"ALL".equalsIgnoreCase(role.trim())) {
            sql.append("AND u.role = ? ");
            params.add(role.trim().toUpperCase());
        }

        if (status != null && !status.trim().isEmpty() && !"ALL".equalsIgnoreCase(status.trim())) {
            if ("Active".equalsIgnoreCase(status.trim()) || "true".equalsIgnoreCase(status.trim())) {
                sql.append("AND u.active = TRUE ");
            } else if ("Inactive".equalsIgnoreCase(status.trim()) || "false".equalsIgnoreCase(status.trim())) {
                sql.append("AND u.active = FALSE ");
            }
        }

        String column = ALLOWED_SORT_COLUMNS.getOrDefault(
                sortBy == null ? "" : sortBy.toLowerCase().trim(),
                "u.id"
        );
        String direction = "DESC".equalsIgnoreCase(sortOrder) ? "DESC" : "ASC";
        sql.append("ORDER BY ").append(column).append(" ").append(direction);
        if (!"u.id".equals(column)) {
            sql.append(", u.id ASC");
        }

        int offset = Math.max(0, (page - 1) * pageSize);
        sql.append(" LIMIT ? OFFSET ?");
        params.add(pageSize);
        params.add(offset);

        List<User> list = new ArrayList<>();
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
                    list.add(mapRow(rs));
                }
            }
        }
        return list;
    }

    /**
     * [DAO: User Count Query] Counts total user records matching search filters for pagination metadata.
     * [Flow] SQL SELECT COUNT(*) FROM users WHERE 1=1 -> PreparedStatement -> ResultSet.getInt(1).
     * [Rules] Mirrors search() WHERE clause filtering to provide exact count for Page<UserDTO>.
     * [Output] Integer total count of matching rows.
     */
    public int countSearch(
            String keyword,
            String role,
            String status) throws SQLException {

        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM users u WHERE 1=1 ");
        List<String> params = new ArrayList<>();

        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append("AND (u.username ILIKE ? OR u.full_name ILIKE ? OR u.email ILIKE ? OR u.phone ILIKE ?) ");
            String kw = "%" + keyword.trim() + "%";
            params.add(kw);
            params.add(kw);
            params.add(kw);
            params.add(kw);
        }

        if (role != null && !role.trim().isEmpty() && !"ALL".equalsIgnoreCase(role.trim())) {
            sql.append("AND u.role = ? ");
            params.add(role.trim().toUpperCase());
        }

        if (status != null && !status.trim().isEmpty() && !"ALL".equalsIgnoreCase(status.trim())) {
            if ("Active".equalsIgnoreCase(status.trim()) || "true".equalsIgnoreCase(status.trim())) {
                sql.append("AND u.active = TRUE ");
            } else if ("Inactive".equalsIgnoreCase(status.trim()) || "false".equalsIgnoreCase(status.trim())) {
                sql.append("AND u.active = FALSE ");
            }
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

    /** Legacy search method for backwards compatibility */
    public List<User> search(String keyword, String role) throws SQLException {
        return search(keyword, role, null, 1, 100, "id", "DESC");
    }

    public List<User> findByRole(Role role) throws SQLException {
        String sql = "SELECT * FROM users WHERE role = ? ORDER BY full_name";
        List<User> users = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, role.name());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    users.add(mapRow(rs));
                }
            }
        }
        return users;
    }

    /**
     * User counts grouped by role (ADMIN/EXPERT/STUDENT).
     * Used by the Admin Dashboard summary cards.
     */
    public Map<String, Integer> countUsersByRole() throws SQLException {
        String sql = "SELECT role, COUNT(*) AS cnt FROM users GROUP BY role";

        Map<String, Integer> counts = new HashMap<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                counts.put(rs.getString("role"), rs.getInt("cnt"));
            }
        }

        return counts;
    }

    /**
     * Count active (or inactive) user accounts system-wide.
     * Used by the Admin Dashboard summary cards.
     */
    public int countByActiveStatus(boolean active) throws SQLException {
        String sql = "SELECT COUNT(*) FROM users WHERE active = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setBoolean(1, active);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    /**
     * Most recently created user accounts, newest first.
     * Used by the Admin Dashboard "recent activity" panel.
     */
    public List<User> findRecent(int limit) throws SQLException {
        String sql = "SELECT * FROM users ORDER BY created_at DESC LIMIT ?";

        List<User> users = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, limit);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    users.add(mapRow(rs));
                }
            }
        }

        return users;
    }

    /**
     * [DAO: User Insertion] Inserts a new user record into the users table and returns generated primary key.
     * [Flow] SQL INSERT INTO users VALUES (...) -> PreparedStatement.getGeneratedKeys() -> primary key ID.
     * [Rules] Persists BCrypt password hash, role, status, and created_at timestamp.
     * [Output] Generated integer primary key ID (>0) or -1 on failure.
     */
    public int insert(User user) throws SQLException {
        String sql = "INSERT INTO users (username, password_hash, email, full_name, phone, role, active, created_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, user.getUsername());
            ps.setString(2, user.getPasswordHash());
            ps.setString(3, user.getEmail());
            ps.setString(4, user.getFullName());
            ps.setString(5, user.getPhone());
            ps.setString(6, user.getRole().name());
            ps.setBoolean(7, user.isActive());
            ps.setTimestamp(8, new Timestamp(System.currentTimeMillis()));
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
        }
        return -1;
    }

    /**
     * [DAO: User Update] Updates profile attributes, contact info, role, and active status for a user ID.
     * [Flow] SQL UPDATE users SET email=?, full_name=?, phone=?, role=?, active=? WHERE id=?.
     * [Rules] Modifies all user fields except password_hash and username by primary key id.
     * [Output] Boolean true if at least one row was updated; false otherwise.
     */
    public boolean update(User user) throws SQLException {
        String sql = "UPDATE users SET email = ?, full_name = ?, phone = ?, role = ?, active = ? WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, user.getEmail());
            ps.setString(2, user.getFullName());
            ps.setString(3, user.getPhone());
            ps.setString(4, user.getRole().name());
            ps.setBoolean(5, user.isActive());
            ps.setInt(6, user.getId());
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * [DAO: Status Mutation] Lightweight update mutating only the active boolean column for a user ID.
     * [Flow] SQL UPDATE users SET active = ? WHERE id = ? -> PreparedStatement.executeUpdate() > 0.
     * [Rules] Mutates only active flag; used by Admin User List direct toggle.
     * [Output] Boolean true if update succeeded.
     */
    public boolean updateStatus(int id, boolean active) throws SQLException {
        String sql = "UPDATE users SET active = ? WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setBoolean(1, active);
            ps.setInt(2, id);
            return ps.executeUpdate() > 0;
        }
    }

    // ============================================================
    // USER SELF-SERVICE (Profile)
    // ============================================================

    /** Self-service profile update (no role/active change allowed here). */
    public boolean updateProfile(int id, String fullName, String email, String phone) throws SQLException {
        String sql = "UPDATE users SET full_name = ?, email = ?, phone = ? WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, fullName);
            ps.setString(2, email);
            ps.setString(3, phone);
            ps.setInt(4, id);
            return ps.executeUpdate() > 0;
        }
    }

    // ============================================================
    // PASSWORD RECOVERY
    // ============================================================

    /**
     * [DAO: Password Update] Updates the password_hash column for a specific user ID.
     * [Flow] SQL UPDATE users SET password_hash = ? WHERE id = ? -> PreparedStatement.executeUpdate() > 0.
     * [Rules] Sets BCrypt hashed password for Admin reset or user self-service password recovery.
     * [Output] Boolean true if updated successfully.
     */
    public boolean updatePassword(int id, String newPasswordHash) throws SQLException {
        String sql = "UPDATE users SET password_hash = ? WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, newPasswordHash);
            ps.setInt(2, id);
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * [DAO: User Deletion] Permanently deletes a user record by primary key ID.
     * [Flow] SQL DELETE FROM users WHERE id = ? -> PreparedStatement.executeUpdate() > 0.
     * [Rules] Hard deletes user row; may throw SQLException if foreign key constraints exist.
     * [Output] Boolean true if a row was deleted.
     */
    public boolean deleteById(int id) throws SQLException {
        String sql = "DELETE FROM users WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * [Helper: Row Mapper] Maps the current ResultSet row into a User domain entity.
     * [Flow] ResultSet column getters -> User entity setters.
     * [Rules] Extracts user attributes including id, username, password_hash, email, name, role, and active.
     * [Output] Populated User domain entity instance.
     */
    private User mapRow(ResultSet rs) throws SQLException {
        User u = new User();
        u.setId(rs.getInt("id"));
        u.setUsername(rs.getString("username"));
        u.setPasswordHash(rs.getString("password_hash"));
        u.setEmail(rs.getString("email"));
        u.setFullName(rs.getString("full_name"));
        u.setPhone(rs.getString("phone"));
        u.setRole(Role.valueOf(rs.getString("role")));
        u.setActive(rs.getBoolean("active"));
        u.setCreatedAt(rs.getTimestamp("created_at"));
        return u;
    }
}
