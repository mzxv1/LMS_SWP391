package com.lms.controller;

import com.lms.dto.AdminUserFormDTO;
import com.lms.dto.Page;
import com.lms.dto.UserDTO;
import com.lms.service.ServiceException;
import com.lms.service.UserService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;

/**
 * Handles Administrator operations for user account management.
 * Not to be confused with UserAuthenticationServlet (login/register/Google auth)
 * or ProfileServlet (self-service profile editing) — this controller is
 * Admin-only account administration.
 *
 * Routes:
 * - GET  /admin/users          → list users (search/filter by role+status, sort, paginate)
 * - GET  /admin/users/new      → new user form
 * - POST /admin/users/new      → create user
 * - GET  /admin/users/detail   → view/edit user detail
 * - POST /admin/users/detail   → save user detail (info, role, status, password reset)
 * - POST /admin/users/status   → toggle Activate/Deactivate directly from the list
 *   (self-protection: the currently logged-in admin cannot deactivate their own account)
 */
@WebServlet({"/admin/users", "/admin/users/new", "/admin/users/detail", "/admin/users/status"})
public class AdminUserServlet extends HttpServlet {

    private final UserService userService = new UserService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String path = req.getServletPath();
        try {
            switch (path) {
                case "/admin/users":
                    list(req, resp);
                    break;
                case "/admin/users/new":
                    consumeFlashMessages(req);
                    req.setAttribute("pageTitle", "Thêm người dùng mới (New User)");
                    req.getRequestDispatcher("/WEB-INF/views/admin/user-form.jsp").forward(req, resp);
                    break;
                case "/admin/users/detail":
                    detail(req, resp);
                    break;
                default:
                    resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            }
        } catch (SQLException e) {
            throw new ServletException("Database error in AdminUserServlet", e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String path = req.getServletPath();
        try {
            switch (path) {
                case "/admin/users/new":
                    create(req, resp);
                    break;
                case "/admin/users/detail":
                    update(req, resp);
                    break;
                case "/admin/users/status":
                    handleToggleStatus(req, resp);
                    break;
                default:
                    resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            }
        } catch (SQLException e) {
            throw new ServletException("Database error in AdminUserServlet", e);
        }
    }

    /**
     * [Route: GET /admin/users] Displays paginated list of user accounts with search/filter options (SRS 3.2.1).
     * [Flow] AdminUserServlet.list() -> UserService.getUsersPage() -> UserDAO.search() -> PostgreSQL.
     * [Rules] Keyword ILIKE filter, role/status exact filter, and SQL injection-safe whitelist column sorting.
     * [Response] Populates 'userPage' request attribute and forwards to /WEB-INF/views/admin/user-list.jsp.
     */
    private void list(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException, SQLException {

        String keyword = req.getParameter("keyword");
        String role = req.getParameter("role");
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
            sortBy = "id";
            sortOrder = "ASC";
        }

        Page<UserDTO> userPage = userService.getUsersPage(
                keyword, role, status, page, pageSize, sortBy, sortOrder
        );

        consumeFlashMessages(req);

        req.setAttribute("userPage", userPage);
        req.setAttribute("keyword", keyword);
        req.setAttribute("role", role);
        req.setAttribute("status", status);
        req.setAttribute("sortBy", sortBy);
        req.setAttribute("sortOrder", sortOrder);
        req.setAttribute("pageTitle", "Quản lý người dùng (User List)");

        req.getRequestDispatcher("/WEB-INF/views/admin/user-list.jsp").forward(req, resp);
    }

    /**
     * [Route: POST /admin/users/status] Directly toggles user active status (Active <-> Inactive) from User List.
     * [Flow] AdminUserServlet.handleToggleStatus() -> UserService.toggleUserStatus() -> UserDAO.updateStatus().
     * [Rules] Enforces Admin Self-Protection (targetUserId != currentAdminId); prevents locking own active account.
     * [Response] Sets session flash notification and redirects back to list preserving query/filter/page state.
     */
    private void handleToggleStatus(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException, SQLException {

        int id = parseId(req.getParameter("id"));
        UserDTO currentUser = (UserDTO) req.getSession().getAttribute("currentUser");
        Integer currentAdminId = currentUser != null ? currentUser.getId() : null;

        if (id <= 0) {
            req.getSession().setAttribute("error", "ID người dùng không hợp lệ.");
            redirectToListWithState(req, resp);
            return;
        }

        try {
            boolean newStatus = userService.toggleUserStatus(id, currentAdminId);
            if (newStatus) {
                req.getSession().setAttribute("message", "Kích hoạt tài khoản người dùng thành công.");
            } else {
                req.getSession().setAttribute("message", "Đã vô hiệu hóa tài khoản người dùng.");
            }
        } catch (ServiceException e) {
            req.getSession().setAttribute("error", e.getMessage());
        }

        redirectToListWithState(req, resp);
    }

    /**
     * [Helper: Navigation] Redirects back to User List preserving search keyword, filters, sort, and page.
     * [Flow] Builds query string parameters (URLEncoder UTF-8) -> HttpServletResponse.sendRedirect().
     * [Rules] Prioritizes explicit 'returnUrl' parameter when valid; otherwise rebuilds parameterized list URL.
     * [Response] Issues HTTP 302 redirect back to /admin/users with preserved query parameters.
     */
    private void redirectToListWithState(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String returnUrl = req.getParameter("returnUrl");
        if (returnUrl != null && !returnUrl.trim().isEmpty() && returnUrl.startsWith(req.getContextPath())) {
            resp.sendRedirect(returnUrl);
            return;
        }

        StringBuilder sb = new StringBuilder(req.getContextPath()).append("/admin/users?");
        appendQueryParam(sb, "keyword", req.getParameter("keyword"));
        appendQueryParam(sb, "role", req.getParameter("role"));
        appendQueryParam(sb, "status", req.getParameter("status"));
        appendQueryParam(sb, "sortBy", req.getParameter("sortBy"));
        appendQueryParam(sb, "sortOrder", req.getParameter("sortOrder"));
        appendQueryParam(sb, "page", req.getParameter("page"));

        String url = sb.toString();
        if (url.endsWith("?") || url.endsWith("&")) {
            url = url.substring(0, url.length() - 1);
        }
        resp.sendRedirect(url);
    }

    /**
     * [Helper: URL Builder] Appends a URL-encoded query parameter key-value pair to a StringBuilder.
     * [Flow] URLEncoder.encode(value, StandardCharsets.UTF_8) appended to StringBuilder.
     * [Rules] Only appends non-null and non-blank parameter values followed by '&'.
     * [Output] Mutates StringBuilder URL state in-place.
     */
    private void appendQueryParam(StringBuilder sb, String key, String value) {
        if (value != null && !value.trim().isEmpty()) {
            sb.append(key).append("=").append(URLEncoder.encode(value.trim(), StandardCharsets.UTF_8)).append("&");
        }
    }

    /**
     * [Route: GET /admin/users/detail] Displays user account details and profile edit form (SRS 3.2.2).
     * [Flow] AdminUserServlet.detail() -> UserService.getUserById() -> UserDAO.findById() -> PostgreSQL.
     * [Rules] Validates user ID; consumes flash messages; handles user not found exception gracefully.
     * [Response] Sets 'user' request attribute and forwards to /WEB-INF/views/admin/user-detail.jsp.
     */
    private void detail(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException, SQLException {

        int id = parseId(req.getParameter("id"));
        if (id <= 0) {
            req.getSession().setAttribute("error", "ID người dùng không hợp lệ.");
            resp.sendRedirect(req.getContextPath() + "/admin/users");
            return;
        }

        consumeFlashMessages(req);

        try {
            UserDTO user = userService.getUserById(id);
            req.setAttribute("user", user);
            req.setAttribute("pageTitle", "Chi tiết người dùng (User Detail)");
            req.getRequestDispatcher("/WEB-INF/views/admin/user-detail.jsp").forward(req, resp);
        } catch (ServiceException e) {
            req.getSession().setAttribute("error", e.getMessage());
            resp.sendRedirect(req.getContextPath() + "/admin/users");
        }
    }

    /**
     * [Route: POST /admin/users/new] Creates a new user account through Admin management interface (SRS 3.2.3).
     * [Flow] AdminUserServlet.create() -> UserService.createUserByAdmin() -> UserDAO.insert() -> DB.
     * [Rules] Validates mandatory fields, password confirmation, email format, unique username/email, and BCrypt hash.
     * [Response] On success redirects to /admin/users (PRG); on error preserves form data and forwards to user-form.jsp.
     */
    private void create(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException, SQLException {

        req.setCharacterEncoding("UTF-8");
        AdminUserFormDTO dto = buildFormDTO(req, false);

        try {
            UserDTO created = userService.createUserByAdmin(dto);
            req.getSession().setAttribute("message",
                    "Tạo người dùng '" + created.getUsername() + "' thành công! Mật khẩu tạm thời: "
                            + created.getGeneratedPassword()
                            + " (vui lòng gửi cho người dùng và yêu cầu đổi mật khẩu sau khi đăng nhập).");
            resp.sendRedirect(req.getContextPath() + "/admin/users");
        } catch (ServiceException e) {
            req.setAttribute("error", e.getMessage());
            req.setAttribute("formData", dto);
            req.setAttribute("pageTitle", "Thêm người dùng mới (New User)");
            req.getRequestDispatcher("/WEB-INF/views/admin/user-form.jsp").forward(req, resp);
        }
    }

    /**
     * [Route: POST /admin/users/detail] Updates user profile, role, status, and optional password by Administrator.
     * [Flow] AdminUserServlet.update() -> UserService.updateUserByAdmin() -> UserDAO.update() / updatePassword().
     * [Rules] Enforces Self-Protection against self-deactivation/demotion, email uniqueness, validate-before-mutate.
     * [Response] On success redirects to /admin/users/detail?id=...; on error re-forwards to user-detail.jsp.
     */
    private void update(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException, SQLException {

        req.setCharacterEncoding("UTF-8");
        AdminUserFormDTO dto = buildFormDTO(req, true);
        UserDTO currentUser = (UserDTO) req.getSession().getAttribute("currentUser");
        Integer currentAdminId = currentUser != null ? currentUser.getId() : null;

        try {
            UserDTO updated = userService.updateUserByAdmin(dto, currentAdminId);
            req.getSession().setAttribute("message", "Cập nhật thông tin người dùng '" + updated.getUsername() + "' thành công!");
            resp.sendRedirect(req.getContextPath() + "/admin/users/detail?id=" + dto.getId());
        } catch (ServiceException e) {
            req.setAttribute("error", e.getMessage());
            try {
                req.setAttribute("user", userService.getUserById(dto.getId()));
            } catch (ServiceException ignored) {}
            req.setAttribute("pageTitle", "Chi tiết người dùng (User Detail)");
            req.getRequestDispatcher("/WEB-INF/views/admin/user-detail.jsp").forward(req, resp);
        }
    }

    /**
     * [Helper: Data Binding] Parses and binds HTTP request form parameters into an AdminUserFormDTO.
     * [Flow] HttpServletRequest.getParameter() -> AdminUserFormDTO setters.
     * [Rules] Extracts text fields, handles optional ID parameter, and parses active radio/checkbox values.
     * [Output] Populated AdminUserFormDTO instance.
     */
    private AdminUserFormDTO buildFormDTO(HttpServletRequest req, boolean withId) {
        AdminUserFormDTO dto = new AdminUserFormDTO();
        if (withId) {
            dto.setId(parseId(req.getParameter("id")));
        }
        dto.setUsername(req.getParameter("username"));
        dto.setPassword(req.getParameter("password"));
        dto.setConfirmPassword(req.getParameter("confirmPassword"));
        dto.setEmail(req.getParameter("email"));
        dto.setFullName(req.getParameter("fullName"));
        dto.setPhone(req.getParameter("phone"));
        dto.setRole(req.getParameter("role"));

        // Status: Radio Active/Inactive or checkbox
        String status = req.getParameter("status");
        if (status != null && !status.trim().isEmpty()) {
            dto.setActive("Active".equalsIgnoreCase(status.trim()) || "true".equalsIgnoreCase(status.trim()));
        } else {
            dto.setActive("on".equals(req.getParameter("active")) || "true".equals(req.getParameter("active")));
        }

        return dto;
    }

    /**
     * [Helper: Flash Scope] Transfers session flash messages ('message', 'error') into request attributes.
     * [Flow] HttpServletRequest.getSession().getAttribute() -> setAttribute() -> removeAttribute().
     * [Rules] Ensures flash notifications display once and are purged immediately to prevent stale messages.
     * [Output] Populates request scope attributes and removes session keys.
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

    /**
     * [Helper: Utility] Safely parses a raw numeric request parameter into a primitive integer ID.
     * [Flow] Integer.parseInt(raw) with Exception catch block.
     * [Rules] Returns -1 when raw string is null, empty, non-numeric, or invalid to avoid NumberFormatException.
     * [Output] Primitive int ID value (>0 if valid, -1 if invalid).
     */
    private int parseId(String raw) {
        try {
            return Integer.parseInt(raw);
        } catch (Exception e) {
            return -1;
        }
    }
}
