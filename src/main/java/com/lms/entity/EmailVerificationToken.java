package com.lms.entity;

import java.sql.Timestamp;

/**
 * Persistence entity mapped to the "email_verification_tokens" table.
 * Holds a *pending* registration: the account does not exist in "users"
 * yet - it is only inserted once the confirmation link is clicked (see
 * UserService.confirmRegistration()). Only the SHA-256 hash of the raw
 * token is stored, never the raw token itself - see util.TokenUtil.
 */
public class EmailVerificationToken {

    private int id;
    private String tokenHash;
    private String username;
    private String passwordHash;
    private String email;
    private String fullName;
    private Timestamp expiresAt;
    private Timestamp createdAt;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTokenHash() {
        return tokenHash;
    }

    public void setTokenHash(String tokenHash) {
        this.tokenHash = tokenHash;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public Timestamp getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(Timestamp expiresAt) {
        this.expiresAt = expiresAt;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }
}
