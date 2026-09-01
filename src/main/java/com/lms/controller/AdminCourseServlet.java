package com.lms.controller;

import com.lms.dto.CourseDTO;
import com.lms.dto.Page;
import com.lms.service.CourseService;
import com.lms.service.ServiceException;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

/**
 * Handles Administrator operations for course oversight.
 * Admin can view all courses system-wide and approve/change publication status,
 * but cannot create, edit content, or delete a course (ownership stays with Expert).
 *
 * Routes:
 * - GET  /admin/courses            → list all courses (search/filter/sort)
 * - GET  /admin/courses/detail     → view course detail
 * - POST /admin/courses/updateStatus → change course status (DRAFT/PUBLISHED/ARCHIVED)
 */
@WebServlet({"/admin/courses", "/admin/courses/detail", "/admin/courses/updateStatus"})
public class AdminCourseServlet extends HttpServlet {

    private final CourseService courseService = new CourseService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String path = req.getServletPath();
        try {
            switch (path) {
                case "/admin/courses":
                    list(req, resp);
                    break;
                case "/admin/courses/detail":
                    detail(req, resp);
                    break;
                default:
                    resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            }
        } catch (SQLException e) {
            throw new ServletException(e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String path = req.getServletPath();
        try {
            switch (path) {
                case "/admin/courses/updateStatus":
                    updateStatus(req, resp);
                    break;
                default:
                    resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            }
        } catch (SQLException e) {
            throw new ServletException(e);
        }
    }

    /**
     * [Route: GET /admin/courses] Displays system-wide course catalog with pagination and search/filter options.
     * [Flow] AdminCourseServlet.list() -> CourseService.getCoursesPage() -> CourseDAO.searchAll() -> PostgreSQL JOIN.
     * [Rules] Supports keyword ILIKE filter, publication status filter, whitelist sorting, and LIMIT/OFFSET pagination.
     * [Response] Sets 'coursePage', 'courses', and filter attributes; forwards to /WEB-INF/views/admin/course-list.jsp.
     */
    private void list(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException, SQLException {
        String keyword = req.getParameter("keyword");
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
        try {
            String pageSizeParam = req.getParameter("pageSize");
            if (pageSizeParam != null && !pageSizeParam.trim().isEmpty()) {
                pageSize = Math.max(10, Integer.parseInt(pageSizeParam.trim()));
            }
        } catch (NumberFormatException ignored) {}

        if (sortBy == null || sortBy.trim().isEmpty()) {
            sortBy = "id";
            sortOrder = "DESC";
        }

        Page<CourseDTO> coursePage = courseService.getCoursesPage(keyword, status, page, pageSize, sortBy, sortOrder);

        req.setAttribute("coursePage", coursePage);
        req.setAttribute("courses", coursePage.getContent());
        req.setAttribute("keyword", keyword);
        req.setAttribute("status", status);
        req.setAttribute("sortBy", sortBy);
        req.setAttribute("sortOrder", sortOrder);

        req.getRequestDispatcher("/WEB-INF/views/admin/course-list.jsp").forward(req, resp);
    }

    /**
     * [Route: GET /admin/courses/detail] Loads read-only course details and metadata for Administrator review.
     * [Flow] AdminCourseServlet.detail() -> CourseService.getCourseById(id) -> CourseDAO.findById(id) -> PostgreSQL.
     * [Rules] Consumes session flash messages ('message', 'error') and validates course existence.
     * [Response] Sets 'course' request attribute and forwards to /WEB-INF/views/admin/course-detail.jsp.
     */
    private void detail(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException, SQLException {
        int id = parseId(req.getParameter("id"));

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

        try {
            CourseDTO course = courseService.getCourseById(id);
            req.setAttribute("course", course);
        } catch (ServiceException e) {
            req.setAttribute("error", e.getMessage());
        }

        req.getRequestDispatcher("/WEB-INF/views/admin/course-detail.jsp").forward(req, resp);
    }

    /**
     * [Route: POST /admin/courses/updateStatus] Updates publication status (DRAFT, PUBLISHED, ARCHIVED) of a course.
     * [Flow] AdminCourseServlet.updateStatus() -> CourseService.updateCourseStatusByAdmin() -> CourseDAO.updateStatus().
     * [Rules] Admin oversight permission without expert ownership constraint; validates status whitelist.
     * [Response] Sets session flash notification and redirects to /admin/courses/detail?id=... (PRG pattern).
     */
    private void updateStatus(HttpServletRequest req, HttpServletResponse resp)
            throws IOException, SQLException {
        int id = parseId(req.getParameter("id"));
        String status = req.getParameter("status");

        try {
            courseService.updateCourseStatusByAdmin(id, status);
            req.getSession().setAttribute("message", "Cập nhật trạng thái khóa học thành công!");
        } catch (ServiceException e) {
            req.getSession().setAttribute("error", e.getMessage());
        }

        resp.sendRedirect(req.getContextPath() + "/admin/courses/detail?id=" + id);
    }

    /**
     * [Helper: Utility] Safely parses a raw numeric request parameter into a primitive integer ID.
     * [Flow] Integer.parseInt(raw) with Exception catch block.
     * [Rules] Returns -1 when raw string is null, empty, non-numeric, or invalid to prevent NumberFormatException.
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
