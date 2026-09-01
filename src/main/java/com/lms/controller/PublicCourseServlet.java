package com.lms.controller;

import com.lms.dto.CourseDTO;
import com.lms.dto.CourseDetailDTO;
import com.lms.dto.Page;
import com.lms.dto.UserDTO;
import com.lms.service.CourseService;
import com.lms.service.EnrollmentService;
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
 * Public Course Controller.
 *
 * Accessible to all users:
 * - Anonymous
 * - STUDENT
 * - EXPERT
 * - ADMIN
 *
 * Routes:
 *
 * GET /courses
 *      -> Public course list
 *
 * GET /courses/detail?id={courseId}
 *      -> Public course detail
 *
 * Course list supports:
 *   keyword  -> course search keyword
 *   category -> category filter
 *   sort     -> newest / oldest / price_asc / price_desc
 *   page     -> page number
 *   size     -> number of courses per page
 */
@WebServlet({
        "/courses",
        "/courses/detail"
})
public class PublicCourseServlet extends HttpServlet {

    private static final int DEFAULT_PAGE = 1;
    private static final int DEFAULT_SIZE = 6;

    private final CourseService courseService =
            new CourseService();

    @Override
    protected void doGet(
            HttpServletRequest req,
            HttpServletResponse resp)
            throws ServletException, IOException {

        String path = req.getServletPath();

        try {

            switch (path) {

                case "/courses":
                    listCourses(req, resp);
                    break;

                case "/courses/detail":
                    courseDetail(req, resp);
                    break;

                default:
                    resp.sendError(
                            HttpServletResponse.SC_NOT_FOUND
                    );
            }

        } catch (SQLException e) {

            throw new ServletException(
                    "Database error while processing public course request.",
                    e
            );
        }
    }


    // ============================================================
    // PUBLIC COURSE LIST
    // ============================================================

    private void listCourses(
            HttpServletRequest req,
            HttpServletResponse resp)
            throws ServletException, IOException, SQLException {

        // --------------------------------------------------------
        // Read query parameters
        // --------------------------------------------------------

        String keyword =
                req.getParameter("keyword");

        String category =
                req.getParameter("category");

        String sort =
                req.getParameter("sort");

        int page =
                parsePositiveInt(
                        req.getParameter("page"),
                        DEFAULT_PAGE
                );

        int size =
                parsePositiveInt(
                        req.getParameter("size"),
                        DEFAULT_SIZE
                );


        // --------------------------------------------------------
        // Load public courses
        // --------------------------------------------------------

        Page<CourseDTO> coursePage =
                courseService.getPublicCourses(
                        keyword,
                        category,
                        sort,
                        page,
                        size
                );


        // --------------------------------------------------------
        // Load categories for filter
        // --------------------------------------------------------

        List<String> categories =
                courseService.getPublicCategories();


        // --------------------------------------------------------
        // Send data to JSP
        // --------------------------------------------------------

        req.setAttribute(
                "coursePage",
                coursePage
        );

        req.setAttribute(
                "categories",
                categories
        );

        req.setAttribute(
                "keyword",
                keyword
        );

        req.setAttribute(
                "selectedCategory",
                category
        );

        req.setAttribute(
                "selectedSort",
                sort == null || sort.isBlank()
                        ? "newest"
                        : sort
        );


        // --------------------------------------------------------
        // Forward to public course list
        // --------------------------------------------------------

        req.getRequestDispatcher(
                "/WEB-INF/views/course/public-course-list.jsp"
        ).forward(req, resp);
    }


    // ============================================================
    // PUBLIC COURSE DETAIL
    // ============================================================

    private void courseDetail(
            HttpServletRequest req,
            HttpServletResponse resp)
            throws ServletException, IOException, SQLException {

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
        // Load public course detail
        // --------------------------------------------------------

        try {

            CourseDetailDTO course =
                    courseService.getPublicCourseDetail(
                            courseId
                    );

            // ----------------------------------------------------
            // Send data to JSP
            // ----------------------------------------------------

            req.setAttribute(
                    "course",
                    course
            );

            // Verify if student is logged in and actively enrolled in this course
            UserDTO currentUser = (UserDTO) req.getSession().getAttribute("currentUser");
            boolean isEnrolled = false;
            if (currentUser != null && "STUDENT".equals(currentUser.getRole())) {
                EnrollmentService enrollmentService = new EnrollmentService();
                isEnrolled = enrollmentService.isStudentEnrolled(currentUser.getId(), courseId);
            }
            req.setAttribute("isEnrolled", isEnrolled);


            // ----------------------------------------------------
            // Forward to detail page
            // ----------------------------------------------------

            req.getRequestDispatcher(
                    "/WEB-INF/views/course/public-course-detail.jsp"
            ).forward(req, resp);

        } catch (ServiceException e) {

            // Course does not exist OR:
            // - course is not PUBLISHED
            // - expert account is inactive

            resp.sendError(
                    HttpServletResponse.SC_NOT_FOUND,
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
     * Example:
     *
     * page=2      -> 2
     * page=-1     -> default value
     * page=abc    -> default value
     * page=null   -> default value
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