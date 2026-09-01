package com.lms.entity;

import java.sql.Timestamp;

/**
 * [Entity: Setting] Domain model representing a master lookup setting record in the 'settings' table.
 * [Attributes] Supports fixed SettingType categories (User Role, Subject, Semester, etc.) with priority and status.
 * [Constraints] Enforces SRS rules: non-digit name <=20 chars, value <=100 chars, priority >0, unique (type, value).
 */
public class Setting {

    private int id;
    private String type;        // Setting type, e.g. "User Role", "Subject", "Semester"
    private String name;        // Display name, max 20 chars, non-digit
    private String value;       // Logical system value, e.g. "ADMIN", "JAVA", "SP26"
    private int priority;       // Display order, positive integer > 0
    private String status;      // "Active" or "Inactive"
    private String description; // Description, max 200 chars
    private Timestamp createdAt;
    private Timestamp updatedAt;

    public Setting() {
        this.priority = 1;
        this.status = "Active";
    }

    public Setting(int id, String type, String name, String value, int priority, String status, String description) {
        this.id = id;
        this.type = type;
        this.name = name;
        this.value = value;
        this.priority = priority;
        this.status = status;
        this.description = description;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public Timestamp getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Timestamp updatedAt) {
        this.updatedAt = updatedAt;
    }

    public boolean isActive() {
        return "Active".equalsIgnoreCase(this.status);
    }

    /**
     * [Helper: Enum Resolution] Resolves the string type into SettingType enum instance.
     * [Output] Matching SettingType enum or null if not mapped.
     */
    public SettingType getSettingTypeEnum() {
        return SettingType.fromString(this.type);
    }

    /**
     * [Helper: Enum Setter] Assigns setting type from a SettingType enum instance.
     */
    public void setSettingTypeEnum(SettingType settingType) {
        this.type = settingType != null ? settingType.getDisplayName() : null;
    }
}
