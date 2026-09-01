package com.lms.entity;

import java.util.Arrays;
import java.util.List;

/**
 * [Enum: SettingType] Enumeration defining Master Lookup Data types aligned with LMS system architecture.
 * [Domain Types] User Role, Course Category, Course Level, Subject, Semester.
 * [Rules] Used in validation, dropdown rendering, and dynamic filtering across Admin Setting modules.
 */
public enum SettingType {

    USER_ROLE("User Role"),
    COURSE_CATEGORY("Course Category"),
    COURSE_LEVEL("Course Level"),
    SUBJECT("Subject"),
    SEMESTER("Semester");

    private final String displayName;

    SettingType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    /**
     * [Helper: All Types] Returns all known standard LMS configuration types.
     * [Output] List containing User Role, Course Category, Course Level, Subject, Semester.
     */
    public static List<SettingType> getAllLmsTypes() {
        return Arrays.asList(USER_ROLE, COURSE_CATEGORY, COURSE_LEVEL, SUBJECT, SEMESTER);
    }

    /**
     * [Helper: Type Resolution] Flexibly parses a string into matching SettingType enum.
     * [Rules] Matches against enum constant name, display name, or underscore-separated name (case-insensitive).
     * [Output] Matching SettingType or null if not found.
     */
    public static SettingType fromString(String text) {
        if (text == null || text.trim().isEmpty()) {
            return null;
        }
        String clean = text.trim();
        for (SettingType type : SettingType.values()) {
            if (type.name().equalsIgnoreCase(clean)
                    || type.displayName.equalsIgnoreCase(clean)
                    || type.displayName.replace(" ", "_").equalsIgnoreCase(clean)
                    || type.displayName.replace(" ", "").equalsIgnoreCase(clean)) {
                return type;
            }
        }
        return null;
    }

    /**
     * [Helper: Validation] Checks whether a string represents a valid known SettingType.
     * [Output] True if recognized; false otherwise.
     */
    public static boolean isValid(String text) {
        return fromString(text) != null;
    }
}
