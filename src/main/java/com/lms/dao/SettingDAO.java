package com.lms.dao;

import com.lms.entity.Setting;
import com.lms.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Data Access Object for the "settings" table (Master Lookup Data).
 * Pure JDBC operations with whitelist sorting and dynamic pagination.
 */
public class SettingDAO {

    private static final Map<String, String> ALLOWED_SORT_COLUMNS;

    static {
        Map<String, String> map = new HashMap<>();
        map.put("id", "s.id");
        map.put("name", "s.name");
        map.put("type", "s.type");
        map.put("value", "s.value");
        map.put("priority", "s.priority");
        map.put("status", "s.status");
        map.put("created_at", "s.created_at");
        ALLOWED_SORT_COLUMNS = Collections.unmodifiableMap(map);
    }

    /**
     * [DAO: Setting Search] Queries settings with dynamic filters, whitelist column sorting, and pagination.
     * [Flow] SQL SELECT WHERE 1=1 -> PreparedStatement parameters -> ResultSet -> mapRow() -> List<Setting>.
     * [Rules] PreparedStatement ILIKE binding; maps sortBy to ALLOWED_SORT_COLUMNS whitelist against SQL injection.
     * [Output] List<Setting> domain entities matching criteria.
     */
    public List<Setting> search(
            String keyword,
            String type,
            String status,
            int page,
            int pageSize,
            String sortBy,
            String sortOrder) throws SQLException {

        StringBuilder sql = new StringBuilder(
                "SELECT s.* FROM settings s WHERE 1=1 "
        );

        List<Object> params = new ArrayList<>();

        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append("AND (s.name ILIKE ? OR s.value ILIKE ? OR s.description ILIKE ?) ");
            String kw = "%" + keyword.trim() + "%";
            params.add(kw);
            params.add(kw);
            params.add(kw);
        }

        if (type != null && !type.trim().isEmpty() && !"ALL".equalsIgnoreCase(type.trim())) {
            sql.append("AND s.type = ? ");
            params.add(type.trim());
        }

        if (status != null && !status.isEmpty() && !"ALL".equalsIgnoreCase(status.trim())) {
            sql.append("AND s.status = ? ");
            params.add(status.trim());
        }

        String column = ALLOWED_SORT_COLUMNS.getOrDefault(
                sortBy == null ? "" : sortBy.toLowerCase().trim(),
                "s.priority"
        );
        String direction = "DESC".equalsIgnoreCase(sortOrder) ? "DESC" : "ASC";

        sql.append("ORDER BY ").append(column).append(" ").append(direction);
        if (!"s.id".equals(column)) {
            sql.append(", s.id ASC");
        }

        int offset = Math.max(0, (page - 1) * pageSize);
        sql.append(" LIMIT ? OFFSET ?");
        params.add(pageSize);
        params.add(offset);

        List<Setting> list = new ArrayList<>();
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
     * [DAO: Count Query] Counts total setting records matching search filters for pagination metadata.
     * [Flow] SQL SELECT COUNT(*) WHERE 1=1 -> PreparedStatement parameters -> ResultSet.getInt(1).
     * [Rules] Mirrors search() WHERE clause filtering to provide exact count for Page<Setting>.
     * [Output] Integer total count of matching rows.
     */
    public int countSearch(
            String keyword,
            String type,
            String status) throws SQLException {

        StringBuilder sql = new StringBuilder(
                "SELECT COUNT(*) FROM settings s WHERE 1=1 "
        );

        List<String> params = new ArrayList<>();

        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append("AND (s.name ILIKE ? OR s.value ILIKE ? OR s.description ILIKE ?) ");
            String kw = "%" + keyword.trim() + "%";
            params.add(kw);
            params.add(kw);
            params.add(kw);
        }

        if (type != null && !type.trim().isEmpty() && !"ALL".equalsIgnoreCase(type.trim())) {
            sql.append("AND s.type = ? ");
            params.add(type.trim());
        }

        if (status != null && !status.isEmpty() && !"ALL".equalsIgnoreCase(status.trim())) {
            sql.append("AND s.status = ? ");
            params.add(status.trim());
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
     * [DAO: Primary Key Lookup] Reads a single setting record by primary key ID.
     * [Flow] SQL SELECT * FROM settings WHERE id = ? -> PreparedStatement.setInt(1, id) -> mapRow().
     * [Rules] Exact primary key lookup; returns null if no row matches the given ID.
     * [Output] Setting entity or null if not found.
     */
    public Setting findById(int id) throws SQLException {
        String sql = "SELECT * FROM settings WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapRow(rs) : null;
            }
        }
    }

    /**
     * [DAO: Root Types Lookup] Retrieves all active root setting categories (type IS NULL and status = 'Active').
     * [Flow] SQL SELECT WHERE type IS NULL AND status = 'Active' ORDER BY priority, name -> List<Setting>.
     * [Rules] Used to populate parent type selection dropdowns in admin forms.
     * [Output] List<Setting> root categories.
     */
    public List<Setting> findActiveSettingTypes() throws SQLException {
        String sql = "SELECT * FROM settings WHERE type IS NULL AND status = 'Active' ORDER BY priority ASC, name ASC";
        List<Setting> types = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                types.add(mapRow(rs));
            }
        }

        return types;
    }

    /**
     * [DAO: Distinct Types] Queries unique setting type names for list filter dropdowns.
     * [Flow] SQL SELECT DISTINCT type FROM settings WHERE type IS NOT NULL AND status = 'Active' ORDER BY type ASC.
     * [Rules] Returns active setting category type names.
     * [Output] List<String> distinct category names.
     */
    public List<String> findAllDistinctTypes() throws SQLException {
        String sql = "SELECT DISTINCT type FROM settings WHERE type IS NOT NULL AND status = 'Active' ORDER BY type ASC";
        List<String> list = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                String t = rs.getString("type");
                if (t != null && !t.trim().isEmpty()) {
                    list.add(t.trim());
                }
            }
        }

        return list;
    }

    /**
     * [DAO: Child Settings Lookup] Reads active child settings for a specific category type name.
     * [Flow] SQL SELECT WHERE type = ? AND status = 'Active' ORDER BY priority, name -> List<Setting>.
     * [Rules] Queries child settings filtered by parent type name.
     * [Output] List<Setting> matching the specified type.
     */
    public List<Setting> findActiveByType(String type) throws SQLException {
        String sql = "SELECT * FROM settings WHERE type = ? AND status = 'Active' ORDER BY priority ASC, name ASC";
        List<Setting> list = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, type);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        }

        return list;
    }

    /**
     * [DAO: Uniqueness Check] Checks whether a (type, value) setting combination already exists.
     * [Flow] SQL SELECT COUNT(*) WHERE value = ? AND type = ? (AND id <> ?) -> ResultSet.getInt(1) > 0.
     * [Rules] Handles null type (root setting) and optional excludeId to support unique validation during update.
     * [Output] Boolean true if a duplicate record exists; false otherwise.
     */
    public boolean existsByValue(String type, String value, Integer excludeId) throws SQLException {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM settings WHERE LOWER(value) = LOWER(?) ");
        if (type == null) {
            sql.append("AND type IS NULL ");
        } else {
            sql.append("AND LOWER(type) = LOWER(?) ");
        }

        if (excludeId != null) {
            sql.append("AND id <> ? ");
        }

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            int idx = 1;
            ps.setString(idx++, value.trim());
            if (type != null) {
                ps.setString(idx++, type.trim());
            }
            if (excludeId != null) {
                ps.setInt(idx++, excludeId);
            }

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }

        return false;
    }

    /**
     * [DAO: Record Insertion] Inserts a new setting record into the settings table and returns generated ID.
     * [Flow] SQL INSERT INTO settings VALUES (...) -> PreparedStatement.getGeneratedKeys() -> generated key ID.
     * [Rules] Sets timestamps; enforces default status and positive priority; returns auto-generated primary key.
     * [Output] Generated integer primary key ID (>0) or -1 on failure.
     */
    public int insert(Setting setting) throws SQLException {
        String sql = "INSERT INTO settings (type, name, value, priority, status, description, created_at, updated_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            Timestamp now = new Timestamp(System.currentTimeMillis());

            if (setting.getType() == null || setting.getType().trim().isEmpty()) {
                ps.setNull(1, Types.VARCHAR);
            } else {
                ps.setString(1, setting.getType().trim());
            }

            ps.setString(2, setting.getName().trim());
            ps.setString(3, setting.getValue().trim());
            ps.setInt(4, Math.max(1, setting.getPriority()));
            ps.setString(5, setting.getStatus() == null ? "Active" : setting.getStatus());
            ps.setString(6, setting.getDescription());
            ps.setTimestamp(7, now);
            ps.setTimestamp(8, now);

            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }

        return -1;
    }

    /**
     * [DAO: Record Update] Updates all editable attributes of an existing setting record by primary key ID.
     * [Flow] SQL UPDATE settings SET type=?, name=?, value=?, priority=?, status=?, description=?, updated_at=? WHERE id=?.
     * [Rules] Binds all parameters and refreshes updated_at timestamp; matches target row by primary key id.
     * [Output] Boolean true if at least one row was updated; false otherwise.
     */
    public boolean update(Setting setting) throws SQLException {
        String sql = "UPDATE settings SET type = ?, name = ?, value = ?, priority = ?, status = ?, description = ?, updated_at = ? " +
                "WHERE id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            if (setting.getType() == null || setting.getType().trim().isEmpty()) {
                ps.setNull(1, Types.VARCHAR);
            } else {
                ps.setString(1, setting.getType().trim());
            }

            ps.setString(2, setting.getName().trim());
            ps.setString(3, setting.getValue().trim());
            ps.setInt(4, Math.max(1, setting.getPriority()));
            ps.setString(5, setting.getStatus());
            ps.setString(6, setting.getDescription());
            ps.setTimestamp(7, new Timestamp(System.currentTimeMillis()));
            ps.setInt(8, setting.getId());

            return ps.executeUpdate() > 0;
        }
    }

    /**
     * [DAO: Status Mutation] Lightweight update mutating only status and updated_at timestamp for a setting.
     * [Flow] SQL UPDATE settings SET status = ?, updated_at = ? WHERE id = ? -> executeUpdate() > 0.
     * [Rules] Direct status mutation without affecting other setting fields.
     * [Output] Boolean true if updated successfully.
     */
    public boolean updateStatus(int id, String status) throws SQLException {
        String sql = "UPDATE settings SET status = ?, updated_at = ? WHERE id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, status);
            ps.setTimestamp(2, new Timestamp(System.currentTimeMillis()));
            ps.setInt(3, id);

            return ps.executeUpdate() > 0;
        }
    }

    /**
     * [Helper: Row Mapper] Maps the current ResultSet row into a Setting domain entity.
     * [Flow] ResultSet column getters -> Setting entity setters.
     * [Rules] Extracts all column attributes including primary key, type, value, priority, status, and timestamps.
     * [Output] Populated Setting entity instance.
     */
    private Setting mapRow(ResultSet rs) throws SQLException {
        Setting s = new Setting();
        s.setId(rs.getInt("id"));
        s.setType(rs.getString("type"));
        s.setName(rs.getString("name"));
        s.setValue(rs.getString("value"));
        s.setPriority(rs.getInt("priority"));
        s.setStatus(rs.getString("status"));
        s.setDescription(rs.getString("description"));
        s.setCreatedAt(rs.getTimestamp("created_at"));
        s.setUpdatedAt(rs.getTimestamp("updated_at"));
        return s;
    }
}
