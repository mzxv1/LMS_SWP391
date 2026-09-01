package com.lms.dto;

/**
 * [DTO: Admin User Form] Form backing object for Admin user creation and modification (SRS 3.2.2 & 3.2.3).
 * [Fields] Captures credentials, personal info, role selection, and active status from HTML form submissions.
 * [Validation] Validated in UserService before persisting (username >=4, password >=6, email regex, unique checks).
 */
public class AdminUserFormDTO {

    private Integer id;              // null when creating a new user
    private String username;
    private String password;         // required on create, optional on edit (blank = keep current)
    private String confirmPassword;  // used on create form for validation
    private String email;
    private String fullName;
    private String phone;
    private String role;             // ADMIN / EXPERT / STUDENT
    private boolean active = true;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getConfirmPassword() {
        return confirmPassword;
    }

    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
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
}
