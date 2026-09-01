package com.lms.controller;

import com.lms.dto.TraineeCourseDetailDTO;
import com.lms.dto.UserDTO;
import com.lms.service.EnrollmentService;
import com.lms.service.ServiceException;
import com.lms.service.TraineeCourseService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;

/**
 * Controller for trainee course features.
 *
 * Routes:
 *
 * GET /trainee/courses/detail?id={courseId}
 *      -> My Course Detail
 *
 * GET /trainee/courses/lessons?id={courseId}
 *      -> Lesson List
 */
@WebServlet({
        "/trainee/courses/detail",
        "/trainee/courses/lessons"
})
public class TraineeCourseServlet extends HttpServlet {

    private final TraineeCourseService traineeCourseService =
            new TraineeCourseService();

    private final EnrollmentService enrollmentService =
            new EnrollmentService();


    @Override
    protected void doGet(
            HttpServletRequest req,
            HttpServletResponse resp)
            throws ServletException, IOException {

        String path = req.getServletPath();

        try {

            switch (path) {

                case "/trainee/courses/detail":
                    courseDetail(req, resp);
                    break;

                case "/trainee/courses/lessons":
                    lessonList(req, resp);
                    break;

                default:
                    resp.sendError(
                            HttpServletResponse.SC_NOT_FOUND
                    );
            }

        } catch (SQLException e) {

            throw new ServletException(
                    "Database error while processing trainee course request.",
                    e
            );
        }
    }


    // ============================================================
    // LESSON LIST
    // ============================================================

    private void lessonList(
            HttpServletRequest req,
            HttpServletResponse resp)
            throws ServletException, IOException, SQLException {

        UserDTO currentUser = (UserDTO) req.getSession()
                .getAttribute("currentUser");

        if (currentUser == null) {
            resp.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        int courseId = parsePositiveInt(req.getParameter("id"), -1);

        if (courseId <= 0) {
            resp.sendError(
                    HttpServletResponse.SC_NOT_FOUND,
                    "Không tìm thấy khóa học."
            );
            return;
        }

        try {
            enrollmentService.updateCourseProgress(currentUser.getId(), courseId);

            TraineeCourseDetailDTO course = traineeCourseService
                    .getCourseDetail(currentUser.getId(), courseId);

            req.setAttribute("course", course);
            req.getRequestDispatcher(
                    "/WEB-INF/views/trainee/lesson-list.jsp"
            ).forward(req, resp);

        } catch (ServiceException e) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, e.getMessage());
        }
    }


    // ============================================================
    // MY COURSE DETAIL
    // ============================================================

    private void courseDetail(
            HttpServletRequest req,
            HttpServletResponse resp)
            throws ServletException, IOException, SQLException {

        // --------------------------------------------------------
        // Get current student
        // --------------------------------------------------------

        UserDTO currentUser =
                (UserDTO) req.getSession()
                        .getAttribute("currentUser");

        if (currentUser == null) {

            resp.sendError(
                    HttpServletResponse.SC_UNAUTHORIZED
            );

            return;
        }


        // --------------------------------------------------------
        // Read course ID
        // --------------------------------------------------------

        int courseId =
                parsePositiveInt(
                        req.getParameter("id"),
                        -1
                );

        if (courseId <= 0) {

            resp.sendError(
                    HttpServletResponse.SC_NOT_FOUND,
                    "Không tìm thấy khóa học."
            );

            return;
        }


        // --------------------------------------------------------
        // Refresh course progress
        // --------------------------------------------------------

        enrollmentService.updateCourseProgress(
                currentUser.getId(),
                courseId
        );


        // --------------------------------------------------------
        // Load trainee course detail
        // --------------------------------------------------------

        try {

            TraineeCourseDetailDTO course =
                    traineeCourseService.getCourseDetail(
                            currentUser.getId(),
                            courseId
                    );


            // ----------------------------------------------------
            // Send data to JSP
            // ----------------------------------------------------

            req.setAttribute(
                    "course",
                    course
            );


            // ----------------------------------------------------
            // Forward to My Course Detail
            // ----------------------------------------------------

            req.getRequestDispatcher(
                    "/WEB-INF/views/trainee/course-detail.jsp"
            ).forward(req, resp);

        } catch (ServiceException e) {

            /*
             * Student is not enrolled in this course,
             * course does not exist, or access is not allowed.
             */
            resp.sendError(
                    HttpServletResponse.SC_FORBIDDEN,
                    e.getMessage()
            );
        }
    }


    // ============================================================
    // UTILITY
    // ============================================================

    /**
     * Parse a positive integer query parameter.
     */
    private int parsePositiveInt(
            String value,
            int defaultValue) {

        if (value == null || value.isBlank()) {
            return defaultValue;
        }

        try {

            int parsed =
                    Integer.parseInt(value);

            return parsed > 0
                    ? parsed
                    : defaultValue;

        } catch (NumberFormatException e) {

            return defaultValue;
        }
    }
}
