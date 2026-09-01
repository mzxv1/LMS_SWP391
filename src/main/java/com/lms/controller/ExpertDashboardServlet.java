package com.lms.controller;

import com.lms.dto.ExpertDashboardDTO;
import com.lms.dto.UserDTO;
import com.lms.service.DashboardService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;

/**
 * Controller for the Expert Dashboard.
 *
 * Route:
 *
 * GET /expert/dashboard
 *      -> Overview of the logged-in expert's own courses:
 *         status breakdown, student reach, revenue earned,
 *         and recent enrollment activity.
 *
 * Protected by AuthFilter + RoleFilter (see web.xml) - only a
 * logged-in EXPERT can reach this.
 */
@WebServlet("/expert/dashboard")
public class ExpertDashboardServlet extends HttpServlet {

    private final DashboardService dashboardService = new DashboardService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        UserDTO currentUser = (UserDTO) req.getSession().getAttribute("currentUser");

        if (currentUser == null) {
            resp.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        try {
            ExpertDashboardDTO dashboard = dashboardService.getExpertDashboard(currentUser.getId());
            req.setAttribute("dashboard", dashboard);

            req.getRequestDispatcher("/WEB-INF/views/expert/dashboard.jsp")
                    .forward(req, resp);

        } catch (SQLException e) {
            throw new ServletException("Database error while loading expert dashboard.", e);
        }
    }
}
