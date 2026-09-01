package com.lms.controller;

import com.lms.dto.LessonDTO;
import com.lms.dto.UserDTO;
import com.lms.service.EnrollmentService;
import com.lms.service.LessonProgressService;
import com.lms.service.ServiceException;
import com.lms.service.TraineeLessonService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;

/**
 * Controller for trainee lesson features.
 *
 * Routes:
 *
 * GET /trainee/lessons/detail?id={lessonId}
 *      -> Lesson Detail
 *
 * GET /trainee/lessons/viewer?id={lessonId}
 *      -> Lesson Viewer
 *
 * POST /trainee/lessons/complete?id={lessonId}
 *      -> Mark lesson as completed
 *      -> Recalculate course progress
 */
@WebServlet({
        "/trainee/lessons/detail",
        "/trainee/lessons/viewer",
        "/trainee/lessons/complete"
})
public class TraineeLessonServlet extends HttpServlet {

    private final TraineeLessonService traineeLessonService =
            new TraineeLessonService();

    private final LessonProgressService lessonProgressService =
            new LessonProgressService();

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

                case "/trainee/lessons/detail":
                    lessonDetail(req, resp);
                    break;

                case "/trainee/lessons/viewer":
                    lessonViewer(req, resp);
                    break;

                default:
                    resp.sendError(
                            HttpServletResponse.SC_NOT_FOUND
                    );
            }

        } catch (SQLException e) {

            throw new ServletException(
                    "Database error while processing trainee lesson request.",
                    e
            );
        }
    }


    @Override
    protected void doPost(
            HttpServletRequest req,
            HttpServletResponse resp)
            throws ServletException, IOException {

        String path = req.getServletPath();

        try {

            switch (path) {

                case "/trainee/lessons/complete":
                    completeLesson(req, resp);
                    break;

                default:
                    resp.sendError(
                            HttpServletResponse.SC_NOT_FOUND
                    );
            }

        } catch (SQLException e) {

            throw new ServletException(
                    "Database error while completing lesson.",
                    e
            );
        }
    }


    // ============================================================
    // LESSON DETAIL
    // ============================================================

    private void lessonDetail(
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
        // Read lesson ID
        // --------------------------------------------------------

        int lessonId =
                parsePositiveInt(
                        req.getParameter("id"),
                        -1
                );

        if (lessonId <= 0) {

            resp.sendError(
                    HttpServletResponse.SC_NOT_FOUND,
                    "Không tìm thấy bài học."
            );

            return;
        }


        // --------------------------------------------------------
        // Load lesson detail
        // --------------------------------------------------------

        try {

            LessonDTO lesson =
                    traineeLessonService.getLessonDetail(
                            currentUser.getId(),
                            lessonId
                    );

            if (!validateExpectedCourse(
                    req.getParameter("courseId"),
                    lesson,
                    resp
            )) {
                return;
            }


            // ----------------------------------------------------
            // Send data to JSP
            // ----------------------------------------------------

            req.setAttribute(
                    "lesson",
                    lesson
            );


            // ----------------------------------------------------
            // Forward to Lesson Detail
            // ----------------------------------------------------

            req.getRequestDispatcher(
                    "/WEB-INF/views/trainee/lesson-detail.jsp"
            ).forward(req, resp);

        } catch (ServiceException e) {

            /*
             * Lesson does not exist or the student
             * is not enrolled in its course.
             */
            resp.sendError(
                    HttpServletResponse.SC_FORBIDDEN,
                    e.getMessage()
            );
        }
    }


    // ============================================================
    // LESSON VIEWER
    // ============================================================

    private void lessonViewer(
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
        // Read lesson ID
        // --------------------------------------------------------

        int lessonId =
                parsePositiveInt(
                        req.getParameter("id"),
                        -1
                );

        if (lessonId <= 0) {

            resp.sendError(
                    HttpServletResponse.SC_NOT_FOUND,
                    "Không tìm thấy bài học."
            );

            return;
        }


        // --------------------------------------------------------
        // Load lesson
        // --------------------------------------------------------

        try {

            LessonDTO lesson =
                    traineeLessonService.getLessonDetail(
                            currentUser.getId(),
                            lessonId
                    );

            if (!validateExpectedCourse(
                    req.getParameter("courseId"),
                    lesson,
                    resp
            )) {
                return;
            }


            // ----------------------------------------------------
            // Load lesson progress
            // ----------------------------------------------------

            boolean completed =
                    lessonProgressService.isLessonCompleted(
                            currentUser.getId(),
                            lessonId
                    );


            // ----------------------------------------------------
            // Set completion status
            // ----------------------------------------------------

            lesson.setCompleted(
                    completed
            );


            // ----------------------------------------------------
            // Send data to JSP
            // ----------------------------------------------------

            req.setAttribute(
                    "lesson",
                    lesson
            );


            // ----------------------------------------------------
            // Forward to Lesson Viewer
            // ----------------------------------------------------

            req.getRequestDispatcher(
                    "/WEB-INF/views/trainee/lesson-viewer.jsp"
            ).forward(req, resp);

        } catch (ServiceException e) {

            /*
             * Lesson does not exist or the student
             * is not enrolled in its course.
             */
            resp.sendError(
                    HttpServletResponse.SC_FORBIDDEN,
                    e.getMessage()
            );
        }
    }


    // ============================================================
    // COMPLETE LESSON
    // ============================================================

    private void completeLesson(
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

            resp.sendRedirect(
                    req.getContextPath() + "/login"
            );

            return;
        }


        // --------------------------------------------------------
        // Read lesson ID
        // --------------------------------------------------------

        int lessonId =
                parsePositiveInt(
                        req.getParameter("id"),
                        -1
                );

        if (lessonId <= 0) {

            resp.sendError(
                    HttpServletResponse.SC_NOT_FOUND,
                    "Không tìm thấy bài học."
            );

            return;
        }


        try {

            // ----------------------------------------------------
            // Check lesson and enrollment
            // ----------------------------------------------------

            LessonDTO lesson =
                    traineeLessonService.getLessonDetail(
                            currentUser.getId(),
                            lessonId
                    );

            if (!validateExpectedCourse(
                    req.getParameter("courseId"),
                    lesson,
                    resp
            )) {
                return;
            }


            // ----------------------------------------------------
            // Mark lesson as completed
            // ----------------------------------------------------

            lessonProgressService.markLessonCompleted(
                    currentUser.getId(),
                    lessonId
            );


            // ----------------------------------------------------
            // Recalculate course progress
            // ----------------------------------------------------

            enrollmentService.updateCourseProgress(
                    currentUser.getId(),
                    lesson.getCourseId()
            );


            // ----------------------------------------------------
            // Redirect back to Lesson Viewer
            // ----------------------------------------------------

            resp.sendRedirect(
                    req.getContextPath()
                            + "/trainee/lessons/viewer?id="
                            + lessonId
                            + "&courseId="
                            + lesson.getCourseId()
            );

        } catch (ServiceException e) {

            /*
             * Lesson does not exist or the student
             * is not enrolled in its course.
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
     *
     * Examples:
     *
     * id=1      -> 1
     * id=-1     -> default value
     * id=abc    -> default value
     * id=null   -> default value
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

    private boolean validateExpectedCourse(
            String courseIdParameter,
            LessonDTO lesson,
            HttpServletResponse resp) throws IOException {

        int expectedCourseId = parsePositiveInt(courseIdParameter, -1);

        if (expectedCourseId > 0
                && expectedCourseId != lesson.getCourseId()) {
            resp.sendError(
                    HttpServletResponse.SC_NOT_FOUND,
                    "Bài học không thuộc khóa học đã chọn."
            );
            return false;
        }

        return true;
    }
}
