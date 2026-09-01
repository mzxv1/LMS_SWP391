package com.lms.service;

import com.lms.dao.SettingDAO;
import com.lms.dto.Page;
import com.lms.entity.Setting;
import com.lms.entity.SettingType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SettingServiceTest {

    @Mock
    private SettingDAO settingDAO;

    @InjectMocks
    private SettingService settingService;

    private Setting validRoleSetting;
    private Setting validCourseCategorySetting;
    private Setting validCourseLevelSetting;
    private Setting validSubjectSetting;
    private Setting validSemesterSetting;

    @BeforeEach
    void setUp() {
        validRoleSetting = new Setting(0, "User Role", "Manager", "MANAGER", 2, "Active", "Mô tả cho role giảng viên");
        validCourseCategorySetting = new Setting(0, "Course Category", "Cloud Computing", "CLOUD", 6, "Active", "Điện toán đám mây");
        validCourseLevelSetting = new Setting(0, "Course Level", "Expert Level", "EXPERT_LVL", 4, "Active", "Cấp độ chuyên gia");
        validSubjectSetting = new Setting(0, "Subject", "Java Core", "JAVA_CORE", 1, "Active", "Lập trình Java");
        validSemesterSetting = new Setting(0, "Semester", "Spring", "SP26", 1, "Active", "Học kỳ mùa xuân 2026");
    }

    @Test
    @DisplayName("Create Setting: Valid User Role creates successfully")
    void testCreateSetting_ValidUserRole() throws SQLException {
        when(settingDAO.existsByValue(eq("User Role"), eq("MANAGER"), isNull())).thenReturn(false);
        when(settingDAO.insert(any(Setting.class))).thenReturn(10);

        int resultId = settingService.createSetting(validRoleSetting);

        assertEquals(10, resultId);
        verify(settingDAO, times(1)).insert(validRoleSetting);
    }

    @Test
    @DisplayName("Create Setting: Valid Course Category creates successfully")
    void testCreateSetting_ValidCourseCategory() throws SQLException {
        when(settingDAO.existsByValue(eq("Course Category"), eq("CLOUD"), isNull())).thenReturn(false);
        when(settingDAO.insert(any(Setting.class))).thenReturn(15);

        int resultId = settingService.createSetting(validCourseCategorySetting);

        assertEquals(15, resultId);
        verify(settingDAO, times(1)).insert(validCourseCategorySetting);
    }

    @Test
    @DisplayName("Create Setting: Valid Course Level creates successfully")
    void testCreateSetting_ValidCourseLevel() throws SQLException {
        when(settingDAO.existsByValue(eq("Course Level"), eq("EXPERT_LVL"), isNull())).thenReturn(false);
        when(settingDAO.insert(any(Setting.class))).thenReturn(25);

        int resultId = settingService.createSetting(validCourseLevelSetting);

        assertEquals(25, resultId);
        verify(settingDAO, times(1)).insert(validCourseLevelSetting);
    }

    @Test
    @DisplayName("Create Setting: Valid Subject creates successfully")
    void testCreateSetting_ValidSubject() throws SQLException {
        when(settingDAO.existsByValue(eq("Subject"), eq("JAVA_CORE"), isNull())).thenReturn(false);
        when(settingDAO.insert(any(Setting.class))).thenReturn(35);

        int resultId = settingService.createSetting(validSubjectSetting);

        assertEquals(35, resultId);
        verify(settingDAO, times(1)).insert(validSubjectSetting);
    }

    @Test
    @DisplayName("Create Setting: Valid Semester creates successfully")
    void testCreateSetting_ValidSemester() throws SQLException {
        when(settingDAO.existsByValue(eq("Semester"), eq("SP26"), isNull())).thenReturn(false);
        when(settingDAO.insert(any(Setting.class))).thenReturn(45);

        int resultId = settingService.createSetting(validSemesterSetting);

        assertEquals(45, resultId);
        verify(settingDAO, times(1)).insert(validSemesterSetting);
    }

    @Test
    @DisplayName("SRS Validation: Name containing digits must throw IllegalArgumentException")
    void testCreateSetting_DigitInName_ThrowsException() {
        Setting invalid = new Setting(0, "User Role", "Manager123", "MANAGER", 1, "Active", "Desc");

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            settingService.createSetting(invalid);
        });

        assertTrue(ex.getMessage().contains("không được chứa chữ số"));
    }

    @Test
    @DisplayName("SRS Validation: Name exceeding 20 characters must throw IllegalArgumentException")
    void testCreateSetting_NameTooLong_ThrowsException() {
        Setting invalid = new Setting(0, "User Role", "ThisNameIsFarTooLongForSRS", "MANAGER", 1, "Active", "Desc");

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            settingService.createSetting(invalid);
        });

        assertTrue(ex.getMessage().contains("không được vượt quá 20 ký tự"));
    }

    @Test
    @DisplayName("SRS Validation: Empty or blank name must throw IllegalArgumentException")
    void testCreateSetting_BlankName_ThrowsException() {
        Setting invalid = new Setting(0, "User Role", "   ", "MANAGER", 1, "Active", "Desc");

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            settingService.createSetting(invalid);
        });

        assertTrue(ex.getMessage().contains("bắt buộc"));
    }

    @Test
    @DisplayName("SRS Validation: Empty or blank type must throw IllegalArgumentException")
    void testCreateSetting_BlankType_ThrowsException() {
        Setting invalid = new Setting(0, "   ", "Manager", "MANAGER", 1, "Active", "Desc");

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            settingService.createSetting(invalid);
        });

        assertTrue(ex.getMessage().contains("Loại cấu hình (Type) là bắt buộc"));
    }

    @Test
    @DisplayName("SRS Validation: Priority <= 0 must throw IllegalArgumentException")
    void testCreateSetting_NegativeOrZeroPriority_ThrowsException() {
        Setting zeroPriority = new Setting(0, "User Role", "Manager", "MANAGER", 0, "Active", "Desc");
        Setting negativePriority = new Setting(0, "User Role", "Manager", "MANAGER", -5, "Active", "Desc");

        assertThrows(IllegalArgumentException.class, () -> settingService.createSetting(zeroPriority));
        assertThrows(IllegalArgumentException.class, () -> settingService.createSetting(negativePriority));
    }

    @Test
    @DisplayName("SRS Validation: Duplicate (type, value) must throw IllegalArgumentException")
    void testCreateSetting_DuplicateValueInSameType_ThrowsException() throws SQLException {
        when(settingDAO.existsByValue(eq("User Role"), eq("MANAGER"), isNull())).thenReturn(true);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            settingService.createSetting(validRoleSetting);
        });

        assertTrue(ex.getMessage().contains("đã tồn tại"));
        verify(settingDAO, never()).insert(any(Setting.class));
    }

    @Test
    @DisplayName("Update Setting: Self-duplicate is allowed when excludeId matches current ID")
    void testUpdateSetting_SelfDuplicateAllowed() throws SQLException {
        Setting existing = new Setting(5, "User Role", "Manager", "MANAGER", 2, "Active", "Updated desc");
        when(settingDAO.existsByValue(eq("User Role"), eq("MANAGER"), eq(5))).thenReturn(false);
        when(settingDAO.update(any(Setting.class))).thenReturn(true);

        boolean updated = settingService.updateSetting(existing);

        assertTrue(updated);
        verify(settingDAO, times(1)).update(existing);
    }

    @Test
    @DisplayName("Toggle Status: Active inverts to Inactive")
    void testToggleStatus_ActiveToInactive() throws SQLException {
        Setting activeSetting = new Setting(1, "User Role", "Admin", "ADMIN", 1, "Active", "Desc");
        when(settingDAO.findById(1)).thenReturn(activeSetting);
        when(settingDAO.updateStatus(1, "Inactive")).thenReturn(true);

        boolean result = settingService.toggleStatus(1);

        assertTrue(result);
        verify(settingDAO, times(1)).updateStatus(1, "Inactive");
    }

    @Test
    @DisplayName("Get Available Setting Types: Returns active DB root types and LMS domain types")
    void testGetAvailableSettingTypes() throws SQLException {
        Setting rootRole = new Setting(1, null, "User Role", "USER_ROLE", 1, "Active", "");
        Setting rootCategory = new Setting(2, null, "Course Category", "COURSE_CATEGORY", 2, "Active", "");
        when(settingDAO.findActiveSettingTypes()).thenReturn(Arrays.asList(rootRole, rootCategory));

        List<String> types = settingService.getAvailableSettingTypes();

        assertTrue(types.contains("User Role"));
        assertTrue(types.contains("Course Category"));
        assertTrue(types.contains("Course Level"));
        assertTrue(types.contains("Subject"));
        assertTrue(types.contains("Semester"));
    }

    @Test
    @DisplayName("SettingType: fromString resolves display names and enum names accurately")
    void testSettingType_FromString() {
        assertEquals(SettingType.USER_ROLE, SettingType.fromString("User Role"));
        assertEquals(SettingType.USER_ROLE, SettingType.fromString("USER_ROLE"));
        assertEquals(SettingType.COURSE_CATEGORY, SettingType.fromString("Course Category"));
        assertEquals(SettingType.COURSE_LEVEL, SettingType.fromString("Course Level"));
        assertEquals(SettingType.SUBJECT, SettingType.fromString("Subject"));
        assertEquals(SettingType.SEMESTER, SettingType.fromString("Semester"));
        assertNull(SettingType.fromString(null));
    }

    @Test
    @DisplayName("Pagination: Normalizes invalid page numbers and delegates to DAO")
    void testGetSettingsPage_NormalizesBounds() throws SQLException {
        when(settingDAO.countSearch(null, null, null)).thenReturn(15);
        when(settingDAO.search(null, null, null, 1, 10, "priority", "ASC"))
                .thenReturn(Arrays.asList(validRoleSetting, validSubjectSetting));

        Page<Setting> page = settingService.getSettingsPage(null, null, null, -1, 0, "priority", "ASC");

        assertNotNull(page);
        assertEquals(1, page.getPage());
        assertEquals(10, page.getSize());
        assertEquals(15, page.getTotalElements());
        assertEquals(2, page.getTotalPages());
    }
}
