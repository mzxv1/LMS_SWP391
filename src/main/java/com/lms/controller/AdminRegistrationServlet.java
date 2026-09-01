package com.lms.controller;

import com.lms.service.CourseService;
import com.lms.service.EnrollmentService;
import com.lms.dto.AdminEnrollmentDTO;
import com.lms.dto.CourseDTO;
import com.lms.dto.Page;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

@WebServlet("/admin/registrations")
public class AdminRegistrationServlet extends HttpServlet {

    private EnrollmentService enrollmentService;
    private CourseService courseService;

    @Override
    public void init() throws ServletException {
        enrollmentService = new EnrollmentService();
        courseService = new CourseService();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String action = request.getParameter("action");

        if ("detail".equals(action)) {
            viewDetail(request, response);
        } else {
            listRegistrations(request, response);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String action = request.getParameter("action");

        if ("updateStatus".equals(action)) {
            updateStatus(request, response);
        } else {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid action");
        }
    }

    private void updateStatus(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            String idStr = request.getParameter("id");
            String status = request.getParameter("status");

            if (idStr == null || idStr.trim().isEmpty() || status == null || status.trim().isEmpty()) {
                response.sendRedirect(request.getContextPath() + "/admin/registrations");
                return;
            }

            int enrollmentId = Integer.parseInt(idStr);
            enrollmentService.updateEnrollmentStatus(enrollmentId, status);

            // Redirect back to the same page with query parameters if they exist
            String queryString = request.getParameter("queryString");
            if (queryString != null && !queryString.trim().isEmpty()) {
                response.sendRedirect(request.getContextPath() + "/admin/registrations?" + queryString);
            } else {
                response.sendRedirect(request.getContextPath() + "/admin/registrations");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database error");
        } catch (NumberFormatException e) {
            response.sendRedirect(request.getContextPath() + "/admin/registrations");
        }
    }

    private void listRegistrations(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            String keyword = request.getParameter("keyword");
            String status = request.getParameter("status");
            String courseIdStr = request.getParameter("courseId");
            String pageStr = request.getParameter("page");

            Integer courseId = null;
            if (courseIdStr != null && !courseIdStr.trim().isEmpty()) {
                courseId = Integer.parseInt(courseIdStr);
            }

            int page = 1;
            int limit = 10;
            if (pageStr != null && !pageStr.trim().isEmpty()) {
                page = Integer.parseInt(pageStr);
            }

            Page<AdminEnrollmentDTO> enrollmentPage = enrollmentService.getAdminEnrollmentsPage(keyword, status, courseId, page, limit);
            List<CourseDTO> courses = courseService.listAllCourses();

            request.setAttribute("enrollments", enrollmentPage.getContent());
            request.setAttribute("courses", courses);
            request.setAttribute("currentPage", enrollmentPage.getPage());
            request.setAttribute("totalPages", enrollmentPage.getTotalPages());
            request.setAttribute("keyword", keyword);
            request.setAttribute("status", status);
            request.setAttribute("courseId", courseId);

            request.getRequestDispatcher("/WEB-INF/views/admin/registration-list.jsp").forward(request, response);
        } catch (SQLException e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database error");
        }
    }

    private void viewDetail(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            String idStr = request.getParameter("id");
            if (idStr == null || idStr.trim().isEmpty()) {
                response.sendRedirect(request.getContextPath() + "/admin/registrations");
                return;
            }

            int enrollmentId = Integer.parseInt(idStr);
            AdminEnrollmentDTO enrollment = enrollmentService.getAdminEnrollmentById(enrollmentId);

            if (enrollment == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "Registration not found");
                return;
            }

            request.setAttribute("enrollment", enrollment);
            request.getRequestDispatcher("/WEB-INF/views/admin/registration-detail.jsp").forward(request, response);
        } catch (SQLException e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database error");
        } catch (NumberFormatException e) {
            response.sendRedirect(request.getContextPath() + "/admin/registrations");
        }
    }
}
