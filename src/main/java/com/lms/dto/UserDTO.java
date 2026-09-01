package com.lms.dto;

import java.sql.Timestamp;

/**
 * [DTO: User Presentation] Safe representation model of a User for JSPs, session attributes, and responses.
 * [Security] Deliberately excludes sensitive credentials (password_hash) to prevent accidental data leaks.
 * [Usage] Transported across Controller, Service, and View layers for user display and authentication contexts.
 */
public class UserDTO {

    private int id;
    private String username;
    private String email;
    private String fullName;
    private String phone;
    private String role;
    private boolean active;
    private Timestamp createdAt;

    /**
     * Only populated right after an Admin creates an account with an
     * auto-generated password (see UserService#createUserByAdmin). Never
     * persisted or read back from the database - purely a one-time
     * display value so the admin can copy it to give to the new user.
     */
    private String generatedPassword;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
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

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public String getGeneratedPassword() {
        return generatedPassword;
    }

    public void setGeneratedPassword(String generatedPassword) {
        this.generatedPassword = generatedPassword;
    }
}
