package com.lms.controller;

import com.lms.dto.Page;
import com.lms.entity.Setting;
import com.lms.service.SettingService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

/**
 * Handles Administrator operations for the Settings master lookup table.
 * Supports LMS Master Data categories: User Role, Course Category, Course Level, Subject, Semester.
 *
 * Routes:
 * - GET  /admin/settings         → list settings (filter by type/status, search, sort)
 * - GET  /admin/settings/detail  → view/create/edit a setting
 * - POST /admin/settings/detail  → save a setting (create or update)
 * - POST /admin/settings/status  → activate/deactivate a setting
 */
@WebServlet({"/admin/settings", "/admin/settings/detail", "/admin/settings/status"})
public class AdminSettingServlet extends HttpServlet {

    private final SettingService settingService = new SettingService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String path = req.getServletPath();
        try {
            switch (path) {
                case "/admin/settings":
                    handleList(req, resp);
                    break;
                case "/admin/settings/detail":
                    handleDetail(req, resp);
                    break;
                default:
                    resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            }
        } catch (SQLException e) {
            throw new ServletException("Database error in AdminSettingServlet", e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String path = req.getServletPath();
        try {
            switch (path) {
                case "/admin/settings/detail":
                    handleSaveDetail(req, resp);
                    break;
                case "/admin/settings/status":
                    handleToggleStatus(req, resp);
                    break;
                default:
                    resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            }
        } catch (SQLException e) {
            throw new ServletException("Database error in AdminSettingServlet", e);
        }
    }

    /**
     * [Route: GET /admin/settings] Displays paginated list of system configuration settings (SRS 3.1.1).
     * [Flow] AdminSettingServlet.handleList() -> SettingService.getSettingsPage() -> SettingDAO.search() -> DB.
     * [Rules] Supports keyword search, type/status filter, and SQL injection-safe whitelist column sorting.
     * [Response] Populates 'settingPage' request attribute and forwards to /WEB-INF/views/admin/settings-list.jsp.
     */
    private void handleList(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException, SQLException {

        String keyword = req.getParameter("keyword");
        String type = req.getParameter("type");
        String status = req.getParameter("status");
        String sortBy = req.getParameter("sortBy");
        String sortOrder = req.getParameter("sortOrder");

        int page = 1;
        try {
            String pageParam = req.getParameter("page");
            if (pageParam != null && !pageParam.trim().isEmpty()) {
                page = Math.max(1, Integer.parseInt(pageParam.trim()));
            }
        } catch (NumberFormatException ignored) {}

        int pageSize = 10;
        if (sortBy == null || sortBy.trim().isEmpty()) {
            sortBy = "priority";
            sortOrder = "ASC";
        }

        Page<Setting> settingPage = settingService.getSettingsPage(
                keyword, type, status, page, pageSize, sortBy, sortOrder
        );

        List<String> distinctTypes = settingService.getAllDistinctTypes();

        consumeFlashMessages(req);

        req.setAttribute("settingPage", settingPage);
        req.setAttribute("distinctTypes", distinctTypes);
        req.setAttribute("keyword", keyword);
        req.setAttribute("type", type);
        req.setAttribute("status", status);
        req.setAttribute("sortBy", sortBy);
        req.setAttribute("sortOrder", sortOrder);
        req.setAttribute("pageTitle", "Quản lý Cấu hình (Setting List)");

        req.getRequestDispatcher("/WEB-INF/views/admin/settings-list.jsp").forward(req, resp);
    }

    /**
     * [Route: GET /admin/settings/detail] Loads setting details for edit mode or initializes a blank create form (SRS 3.1.2).
     * [Flow] AdminSettingServlet.handleDetail() -> SettingService.getSettingById() / getAvailableSettingTypes() -> DB.
     * [Rules] Validates setting ID existence; loads available setting types for dropdown parent type selection.
     * [Response] Sets 'setting', 'mode', and 'settingTypes' attributes; forwards to admin/setting-details.jsp.
     */
    private void handleDetail(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException, SQLException {

        String idParam = req.getParameter("id");
        String mode = "create";
        Setting setting = new Setting();

        if (idParam != null && !idParam.trim().isEmpty()) {
            try {
                int id = Integer.parseInt(idParam.trim());
                Setting found = settingService.getSettingById(id);
                if (found != null) {
                    setting = found;
                    mode = "edit";
                } else {
                    req.getSession().setAttribute("error", "Không tìm thấy cấu hình với ID: " + id);
                    resp.sendRedirect(req.getContextPath() + "/admin/settings");
                    return;
                }
            } catch (NumberFormatException e) {
                req.getSession().setAttribute("error", "ID cấu hình không hợp lệ.");
                resp.sendRedirect(req.getContextPath() + "/admin/settings");
                return;
            }
        }

        List<String> settingTypes = settingService.getAvailableSettingTypes();

        consumeFlashMessages(req);

        req.setAttribute("setting", setting);
        req.setAttribute("mode", mode);
        req.setAttribute("settingTypes", settingTypes);
        req.setAttribute("pageTitle", "create".equals(mode) ? "Tạo Cấu hình mới (New Setting)" : "Chi tiết Cấu hình (Setting Details)");

        req.getRequestDispatcher("/WEB-INF/views/admin/setting-details.jsp").forward(req, resp);
    }

    /**
     * [Route: POST /admin/settings/detail] Creates or updates a system configuration setting record.
     * [Flow] AdminSettingServlet.handleSaveDetail() -> SettingService.createSetting() / updateSetting() -> SettingDAO.
     * [Rules] Enforces SRS validation (non-digit name, positive priority, unique (type, value)); catches errors.
     * [Response] On success redirects to /admin/settings (PRG); on validation error re-forwards to form preserving inputs.
     */
    private void handleSaveDetail(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException, SQLException {

        req.setCharacterEncoding("UTF-8");

        String idParam = req.getParameter("id");
        String type = req.getParameter("type");
        String name = req.getParameter("name");
        String value = req.getParameter("value");
        String priorityParam = req.getParameter("priority");
        String status = req.getParameter("status");
        String description = req.getParameter("description");

        int id = 0;
        if (idParam != null && !idParam.trim().isEmpty()) {
            try {
                id = Integer.parseInt(idParam.trim());
            } catch (NumberFormatException ignored) {}
        }

        int priority = 1;
        if (priorityParam != null && !priorityParam.trim().isEmpty()) {
            try {
                priority = Integer.parseInt(priorityParam.trim());
            } catch (NumberFormatException ignored) {}
        }

        Setting setting = new Setting();
        setting.setId(id);
        setting.setType(type);
        setting.setName(name);
        setting.setValue(value);
        setting.setPriority(priority);
        setting.setStatus(status != null ? status : "Active");
        setting.setDescription(description);

        try {
            if (id > 0) {
                settingService.updateSetting(setting);
                req.getSession().setAttribute("message", "Cập nhật cấu hình '" + setting.getName() + "' thành công!");
            } else {
                settingService.createSetting(setting);
                req.getSession().setAttribute("message", "Thêm mới cấu hình '" + setting.getName() + "' thành công!");
            }
            resp.sendRedirect(req.getContextPath() + "/admin/settings");
        } catch (IllegalArgumentException e) {
            req.setAttribute("error", e.getMessage());
            req.setAttribute("setting", setting);
            req.setAttribute("mode", id > 0 ? "edit" : "create");
            req.setAttribute("settingTypes", settingService.getAvailableSettingTypes());
            req.setAttribute("pageTitle", id > 0 ? "Chi tiết Cấu hình (Setting Details)" : "Tạo Cấu hình mới (New Setting)");
            req.getRequestDispatcher("/WEB-INF/views/admin/setting-details.jsp").forward(req, resp);
        }
    }

    /**
     * [Route: POST /admin/settings/status] Toggles setting status between Active and Inactive.
     * [Flow] AdminSettingServlet.handleToggleStatus() -> SettingService.toggleStatus() -> SettingDAO.updateStatus().
     * [Rules] Reads target setting ID; validates record existence; inverts active status string.
     * [Response] Sets session flash notification message and redirects back to returnUrl preserving list state.
     */
    private void handleToggleStatus(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException, SQLException {

        String idParam = req.getParameter("id");
        String returnUrl = req.getParameter("returnUrl");

        if (idParam != null && !idParam.trim().isEmpty()) {
            try {
                int id = Integer.parseInt(idParam.trim());
                settingService.toggleStatus(id);
                req.getSession().setAttribute("message", "Đổi trạng thái cấu hình thành công!");
            } catch (Exception e) {
                req.getSession().setAttribute("error", e.getMessage());
            }
        }

        if (returnUrl != null && !returnUrl.trim().isEmpty()) {
            resp.sendRedirect(returnUrl);
        } else {
            resp.sendRedirect(req.getContextPath() + "/admin/settings");
        }
    }

    /**
     * [Helper: Flash Scope] Transfers session flash attributes ('message', 'error') to request scope.
     * [Flow] HttpServletRequest.getSession().getAttribute() -> setAttribute() -> removeAttribute().
     * [Rules] Ensures flash notifications display only once upon page refresh and prevents stale messages.
     * [Output] Populates request scope attributes and cleans up session attributes.
     */
    private void consumeFlashMessages(HttpServletRequest req) {
        String sessionMsg = (String) req.getSession().getAttribute("message");
        if (sessionMsg != null) {
            req.setAttribute("message", sessionMsg);
            req.getSession().removeAttribute("message");
        }
        String sessionError = (String) req.getSession().getAttribute("error");
        if (sessionError != null) {
            req.setAttribute("error", sessionError);
            req.getSession().removeAttribute("error");
        }
    }
}
