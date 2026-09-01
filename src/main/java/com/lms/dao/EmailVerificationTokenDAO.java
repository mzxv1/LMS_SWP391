package com.lms.dao;

import com.lms.entity.EmailVerificationToken;
import com.lms.util.DBConnection;

import java.sql.*;

/**
 * Data Access Object for the "email_verification_tokens" table.
 * Pure JDBC - no business logic here, that belongs in UserService.
 */
public class EmailVerificationTokenDAO {

    public int insert(EmailVerificationToken token) throws SQLException {
        String sql = "INSERT INTO email_verification_tokens " +
                "(token_hash, username, password_hash, email, full_name, expires_at, created_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, token.getTokenHash());
            ps.setString(2, token.getUsername());
            ps.setString(3, token.getPasswordHash());
            ps.setString(4, token.getEmail());
            ps.setString(5, token.getFullName());
            ps.setTimestamp(6, token.getExpiresAt());
            ps.setTimestamp(7, new Timestamp(System.currentTimeMillis()));
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
        }
        return -1;
    }

    public EmailVerificationToken findByTokenHash(String tokenHash) throws SQLException {
        String sql = "SELECT * FROM email_verification_tokens WHERE token_hash = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, tokenHash);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapRow(rs) : null;
            }
        }
    }

    public boolean deleteById(int id) throws SQLException {
        String sql = "DELETE FROM email_verification_tokens WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    /** Discards any earlier pending registration(s) for this email/username, e.g. on re-submission. */
    public void deleteByEmailOrUsername(String email, String username) throws SQLException {
        String sql = "DELETE FROM email_verification_tokens WHERE email = ? OR username = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            ps.setString(2, username);
            ps.executeUpdate();
        }
    }

    /** Housekeeping: permanently removes pending registrations whose TTL has long since passed. */
    public int deleteExpired() throws SQLException {
        String sql = "DELETE FROM email_verification_tokens WHERE expires_at < ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setTimestamp(1, new Timestamp(System.currentTimeMillis()));
            return ps.executeUpdate();
        }
    }

    private EmailVerificationToken mapRow(ResultSet rs) throws SQLException {
        EmailVerificationToken t = new EmailVerificationToken();
        t.setId(rs.getInt("id"));
        t.setTokenHash(rs.getString("token_hash"));
        t.setUsername(rs.getString("username"));
        t.setPasswordHash(rs.getString("password_hash"));
        t.setEmail(rs.getString("email"));
        t.setFullName(rs.getString("full_name"));
        t.setExpiresAt(rs.getTimestamp("expires_at"));
        t.setCreatedAt(rs.getTimestamp("created_at"));
        return t;
    }
}
