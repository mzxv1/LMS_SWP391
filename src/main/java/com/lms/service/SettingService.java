package com.lms.service;

import com.lms.dao.SettingDAO;
import com.lms.dto.Page;
import com.lms.entity.Setting;
import com.lms.entity.SettingType;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Service handling business rules, validations, and pagination for Setting master data.
 */
public class SettingService {

    private final SettingDAO settingDAO;

    public SettingService() {
        this.settingDAO = new SettingDAO();
    }

    public SettingService(SettingDAO settingDAO) {
        this.settingDAO = settingDAO;
    }

    /**
     * [Admin: Search & Pagination] Retrieves a paginated Page container of settings with multi-field filters.
     * [Flow] SettingService.getSettingsPage() -> SettingDAO.countSearch() & SettingDAO.search() -> Page<Setting>.
     * [Rules] Normalizes page (>=1) and size (>=10); applies whitelist column sorting preventing SQL injection.
     * [Output] Page<Setting> containing filtered records, total element count, and pagination calculations.
     */
    public Page<Setting> getSettingsPage(
            String keyword,
            String type,
            String status,
            int page,
            int pageSize,
            String sortBy,
            String sortOrder) throws SQLException {

        if (page < 1) page = 1;
        if (pageSize <= 0) pageSize = 10;

        int totalElements = settingDAO.countSearch(keyword, type, status);
        List<Setting> content = settingDAO.search(keyword, type, status, page, pageSize, sortBy, sortOrder);

        return new Page<>(content, page, pageSize, totalElements);
    }

    /**
     * [Admin: Lookup] Retrieves a single configuration setting record by its primary key ID.
     * [Flow] SettingService.getSettingById(id) -> SettingDAO.findById(id) -> Setting entity.
     * [Rules] Queries settings table directly; returns null if no matching ID is found.
     * [Output] Setting entity or null if not found.
     */
    public Setting getSettingById(int id) throws SQLException {
        return settingDAO.findById(id);
    }

    /**
     * [Admin: Form Options] Retrieves all available configuration types combining database root types and LMS domain types.
     * [Flow] SettingDAO.findActiveSettingTypes() + SettingType.getAllLmsTypes() -> List<String>.
     * [Rules] Supports User Role, Course Category, Course Level, Subject, Semester, and any custom DB types.
     * [Output] List<String> available setting type names for dropdowns.
     */
    public List<String> getAvailableSettingTypes() throws SQLException {
        Set<String> typeNames = new LinkedHashSet<>();
        // 1. Load active root types from database
        List<Setting> rootSettings = settingDAO.findActiveSettingTypes();
        for (Setting s : rootSettings) {
            if (s.getName() != null && !s.getName().trim().isEmpty()) {
                typeNames.add(s.getName().trim());
            }
        }
        // 2. Merge with standard LMS domain types
        for (SettingType st : SettingType.getAllLmsTypes()) {
            typeNames.add(st.getDisplayName());
        }
        return new ArrayList<>(typeNames);
    }

    /**
     * [Admin: Filter Options] Retrieves distinct active type names for list filter dropdowns.
     * [Flow] SettingDAO.findAllDistinctTypes() + getAvailableSettingTypes() -> List<String>.
     * [Rules] Guarantees all active types in database and domain types appear in filter.
     * [Output] List<String> distinct type category names.
     */
    public List<String> getAllDistinctTypes() throws SQLException {
        Set<String> set = new LinkedHashSet<>(getAvailableSettingTypes());
        set.addAll(settingDAO.findAllDistinctTypes());
        return new ArrayList<>(set);
    }

    /**
     * [Admin: Create] Validates business constraints and persists a new setting record.
     * [Flow] SettingService.createSetting(setting) -> validateSetting(setting, null) -> SettingDAO.insert().
     * [Rules] Enforces SRS constraints (non-digit name <=20 chars, value <=100, priority >0, unique (type, value)).
     * [Output] Generated integer primary key ID (>0).
     */
    public int createSetting(Setting setting) throws SQLException, IllegalArgumentException {
        validateSetting(setting, null);
        return settingDAO.insert(setting);
    }

    /**
     * [Admin: Update] Validates business constraints and updates an existing setting record.
     * [Flow] SettingService.updateSetting(setting) -> validateSetting(setting, id) -> SettingDAO.update().
     * [Rules] Verifies ID > 0; enforces SRS constraints and unique (type, value) check excluding current ID.
     * [Output] Boolean true if updated successfully; throws IllegalArgumentException on validation failure.
     */
    public boolean updateSetting(Setting setting) throws SQLException, IllegalArgumentException {
        if (setting == null || setting.getId() <= 0) {
            throw new IllegalArgumentException("ID cấu hình không hợp lệ.");
        }
        validateSetting(setting, setting.getId());
        return settingDAO.update(setting);
    }

    /**
     * [Admin: Toggle] Switches the status of a setting between 'Active' and 'Inactive'.
     * [Flow] SettingService.toggleStatus(id) -> SettingDAO.findById(id) -> SettingDAO.updateStatus(id, newStatus).
     * [Rules] Inverts 'Active' to 'Inactive' and vice versa; throws IllegalArgumentException if ID not found.
     * [Output] Boolean true if update succeeded.
     */
    public boolean toggleStatus(int id) throws SQLException, IllegalArgumentException {
        Setting existing = settingDAO.findById(id);
        if (existing == null) {
            throw new IllegalArgumentException("Không tìm thấy cấu hình với ID: " + id);
        }

        String newStatus = "Active".equalsIgnoreCase(existing.getStatus()) ? "Inactive" : "Active";
        return settingDAO.updateStatus(id, newStatus);
    }

    /**
     * [Admin: Status Update] Explicitly updates the status of a setting record to 'Active' or 'Inactive'.
     * [Flow] SettingService.updateStatus(id, status) -> SettingDAO.updateStatus(id, status).
     * [Rules] Validates that status string strictly equals 'Active' or 'Inactive' (case-insensitive).
     * [Output] Boolean true if updated successfully; throws IllegalArgumentException on invalid status.
     */
    public boolean updateStatus(int id, String status) throws SQLException, IllegalArgumentException {
        if (!"Active".equalsIgnoreCase(status) && !"Inactive".equalsIgnoreCase(status)) {
            throw new IllegalArgumentException("Trạng thái phải là 'Active' hoặc 'Inactive'.");
        }
        return settingDAO.updateStatus(id, status);
    }

    /**
     * [Internal: Validation] Enforces SRS specification rules and Master Data constraints for setting attributes.
     * [Flow] SettingService.validateSetting() -> SettingDAO.existsByValue(type, value, excludeId).
     * [Rules] Non-digit name <=20 chars, type non-empty, value <=100, priority >0, description <=200, unique (type, value).
     * [Output] Sanitizes input fields in-place or throws IllegalArgumentException on validation failure.
     */
    public void validateSetting(Setting setting, Integer excludeId) throws SQLException, IllegalArgumentException {
        if (setting == null) {
            throw new IllegalArgumentException("Dữ liệu cấu hình không được để trống.");
        }

        // 1. Name validation (SRS: Non-digit string, max 20 chars)
        String name = setting.getName();
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Tên cấu hình (Name) là bắt buộc.");
        }
        name = name.trim();
        if (name.length() > 20) {
            throw new IllegalArgumentException("Tên cấu hình không được vượt quá 20 ký tự (hiện tại: " + name.length() + ").");
        }
        if (name.matches(".*\\d.*")) {
            throw new IllegalArgumentException("Tên cấu hình không được chứa chữ số (theo đặc tả SRS: non-digit string).");
        }
        setting.setName(name);

        // 2. Type validation (Dynamic master data category)
        String type = setting.getType();
        if (type == null || type.trim().isEmpty()) {
            throw new IllegalArgumentException("Loại cấu hình (Type) là bắt buộc.");
        }
        type = type.trim();
        SettingType resolvedType = SettingType.fromString(type);
        if (resolvedType != null) {
            setting.setType(resolvedType.getDisplayName());
        } else {
            setting.setType(type);
        }

        // 3. Value validation (String, max 100 chars)
        String value = setting.getValue();
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("Giá trị cấu hình (Value) là bắt buộc.");
        }
        value = value.trim();
        if (value.length() > 100) {
            throw new IllegalArgumentException("Giá trị cấu hình không được vượt quá 100 ký tự.");
        }
        setting.setValue(value);

        // 4. Priority validation (Positive integer > 0)
        if (setting.getPriority() <= 0) {
            throw new IllegalArgumentException("Thứ tự ưu tiên (Priority) phải là số nguyên dương lớn hơn 0.");
        }

        // 5. Status validation
        String status = setting.getStatus();
        if (status == null || (!"Active".equalsIgnoreCase(status) && !"Inactive".equalsIgnoreCase(status))) {
            setting.setStatus("Active");
        } else {
            setting.setStatus("Active".equalsIgnoreCase(status) ? "Active" : "Inactive");
        }

        // 6. Description validation (max 200 chars)
        String desc = setting.getDescription();
        if (desc != null) {
            desc = desc.trim();
            if (desc.length() > 200) {
                throw new IllegalArgumentException("Mô tả không được vượt quá 200 ký tự.");
            }
            setting.setDescription(desc);
        }

        // 7. Uniqueness constraint for (type, value)
        if (settingDAO.existsByValue(setting.getType(), setting.getValue(), excludeId)) {
            throw new IllegalArgumentException("Giá trị '" + setting.getValue() + "' đã tồn tại trong nhóm '" +
                    setting.getType() + "'. Vui lòng chọn giá trị khác.");
        }
    }
}
