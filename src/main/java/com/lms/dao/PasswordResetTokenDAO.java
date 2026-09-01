package com.lms.dao;

import com.lms.entity.PasswordResetToken;
import com.lms.util.DBConnection;

import java.sql.*;

/**
 * Data Access Object for the "password_reset_tokens" table.
 * Pure JDBC - no business logic here, that belongs in UserService.
 */
public class PasswordResetTokenDAO {

    public int insert(PasswordResetToken token) throws SQLException {
        String sql = "INSERT INTO password_reset_tokens (user_id, token_hash, expires_at, created_at) " +
                "VALUES (?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, token.getUserId());
            ps.setString(2, token.getTokenHash());
            ps.setTimestamp(3, token.getExpiresAt());
            ps.setTimestamp(4, new Timestamp(System.currentTimeMillis()));
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
        }
        return -1;
    }

    public PasswordResetToken findByTokenHash(String tokenHash) throws SQLException {
        String sql = "SELECT * FROM password_reset_tokens WHERE token_hash = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, tokenHash);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapRow(rs) : null;
            }
        }
    }

    public boolean markUsed(int id) throws SQLException {
        String sql = "UPDATE password_reset_tokens SET used_at = ? WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setTimestamp(1, new Timestamp(System.currentTimeMillis()));
            ps.setInt(2, id);
            return ps.executeUpdate() > 0;
        }
    }

    /** Invalidates (marks used) every still-usable token for a user, e.g. on reissue. */
    public void invalidateAllForUser(int userId) throws SQLException {
        String sql = "UPDATE password_reset_tokens SET used_at = ? " +
                "WHERE user_id = ? AND used_at IS NULL";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setTimestamp(1, new Timestamp(System.currentTimeMillis()));
            ps.setInt(2, userId);
            ps.executeUpdate();
        }
    }

    /** Housekeeping: permanently removes tokens whose TTL has long since passed. */
    public int deleteExpired() throws SQLException {
        String sql = "DELETE FROM password_reset_tokens WHERE expires_at < ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setTimestamp(1, new Timestamp(System.currentTimeMillis()));
            return ps.executeUpdate();
        }
    }

    private PasswordResetToken mapRow(ResultSet rs) throws SQLException {
        PasswordResetToken t = new PasswordResetToken();
        t.setId(rs.getInt("id"));
        t.setUserId(rs.getInt("user_id"));
        t.setTokenHash(rs.getString("token_hash"));
        t.setExpiresAt(rs.getTimestamp("expires_at"));
        t.setUsedAt(rs.getTimestamp("used_at"));
        t.setCreatedAt(rs.getTimestamp("created_at"));
        return t;
    }
}
