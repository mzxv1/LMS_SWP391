package com.lms.entity;

import java.sql.Timestamp;

/**
 * Persistence entity mapped to the "password_reset_tokens" table.
 * Only ever stores the SHA-256 hash of the raw token, never the raw
 * token itself - see util.TokenUtil.
 */
public class PasswordResetToken {

    private int id;
    private int userId;
    private String tokenHash;
    private Timestamp expiresAt;
    private Timestamp usedAt;
    private Timestamp createdAt;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getTokenHash() {
        return tokenHash;
    }

    public void setTokenHash(String tokenHash) {
        this.tokenHash = tokenHash;
    }

    public Timestamp getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(Timestamp expiresAt) {
        this.expiresAt = expiresAt;
    }

    public Timestamp getUsedAt() {
        return usedAt;
    }

    public void setUsedAt(Timestamp usedAt) {
        this.usedAt = usedAt;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }
}
