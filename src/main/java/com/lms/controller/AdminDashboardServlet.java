package com.lms.controller;

import com.lms.dto.AdminDashboardDTO;
import com.lms.service.DashboardService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;

/**
 * Controller for the Admin Dashboard.
 *
 * Route:
 *
 * GET /admin/dashboard
 *      -> System-wide overview: user/course/enrollment counts,
 *         total revenue, and recent activity.
 *
 * Protected by AuthFilter + RoleFilter (see web.xml) - only a
 * logged-in ADMIN can reach this.
 */
@WebServlet("/admin/dashboard")
public class AdminDashboardServlet extends HttpServlet {

    private final DashboardService dashboardService = new DashboardService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        try {
            AdminDashboardDTO dashboard = dashboardService.getAdminDashboard();
            req.setAttribute("dashboard", dashboard);

            req.getRequestDispatcher("/WEB-INF/views/admin/dashboard.jsp")
                    .forward(req, resp);

        } catch (SQLException e) {
            throw new ServletException("Database error while loading admin dashboard.", e);
        }
    }
}
