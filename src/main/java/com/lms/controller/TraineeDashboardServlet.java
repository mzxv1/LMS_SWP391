package com.lms.controller;

import com.lms.dto.TraineeDashboardDTO;
import com.lms.dto.UserDTO;
import com.lms.service.EnrollmentService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;

/**
 * Controller for the Trainee Dashboard.
 *
 * Route:
 *
 * GET /trainee/dashboard
 *      -> Recalculate course progress
 *      -> Display courses currently enrolled
 *         by the logged-in student.
 */
@WebServlet("/trainee/dashboard")
public class TraineeDashboardServlet extends HttpServlet {

    private static final int DEFAULT_PAGE = 1;
    private static final int DEFAULT_PAGE_SIZE = 6;

    private final EnrollmentService enrollmentService =
            new EnrollmentService();


    @Override
    protected void doGet(
            HttpServletRequest req,
            HttpServletResponse resp)
            throws ServletException, IOException {

        try {

            // ----------------------------------------------------
            // Get current logged-in student
            // ----------------------------------------------------

            UserDTO currentUser =
                    (UserDTO) req.getSession()
                            .getAttribute("currentUser");

            if (currentUser == null) {

                resp.sendError(
                        HttpServletResponse.SC_UNAUTHORIZED
                );

                return;
            }


            String search = normalizeSearch(req.getParameter("search"));
            String status = normalizeStatus(req.getParameter("status"));
            String sort = normalizeSort(req.getParameter("sort"));
            int page = parsePositiveInt(
                    req.getParameter("page"),
                    DEFAULT_PAGE
            );

            TraineeDashboardDTO dashboard = enrollmentService
                    .getTraineeDashboard(
                            currentUser.getId(),
                            search,
                            status,
                            sort,
                            page,
                            DEFAULT_PAGE_SIZE
                    );


            // ----------------------------------------------------
            // Send data to JSP
            // ----------------------------------------------------

            req.setAttribute(
                    "courses",
                    dashboard.getCourses()
            );

            req.setAttribute(
                    "dashboard",
                    dashboard
            );

            req.setAttribute("coursePage", dashboard.getCoursePage());
            req.setAttribute("search", search);
            req.setAttribute("status", status);
            req.setAttribute("sort", sort);


            // ----------------------------------------------------
            // Forward to dashboard
            // ----------------------------------------------------

            req.getRequestDispatcher(
                    "/WEB-INF/views/trainee/dashboard.jsp"
            ).forward(req, resp);

        } catch (SQLException e) {

            throw new ServletException(
                    "Database error while loading trainee dashboard.",
                    e
            );
        }
    }

    private String normalizeSearch(String search) {
        if (search == null) {
            return "";
        }

        String normalized = search.trim();
        return normalized.length() > 200
                ? normalized.substring(0, 200)
                : normalized;
    }

    private String normalizeStatus(String status) {
        if ("IN_PROGRESS".equals(status) || "COMPLETED".equals(status)) {
            return status;
        }

        return "ALL";
    }

    private String normalizeSort(String sort) {
        if (sort == null) {
            return "newest";
        }

        switch (sort) {
            case "name_asc":
            case "name_desc":
            case "progress_asc":
            case "progress_desc":
            case "oldest":
            case "newest":
                return sort;
            default:
                return "newest";
        }
    }

    private int parsePositiveInt(String value, int defaultValue) {
        if (value == null || value.isBlank()) {
            return defaultValue;
        }

        try {
            int parsed = Integer.parseInt(value);
            return parsed > 0 ? parsed : defaultValue;
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
}
