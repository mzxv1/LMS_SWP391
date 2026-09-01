package com.lms.controller;

import com.lms.dto.CourseDTO;
import com.lms.service.CourseService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

/**
 * Public landing page - NOT behind AuthFilter (see web.xml), reachable by
 * anonymous visitors as well as logged-in users of any role.
 * Shows the full catalog of currently active (status = PUBLISHED) courses.
 */
@WebServlet("/home")
public class HomeServlet extends HttpServlet {

    // Only the most popular courses are shown on the landing page.
    private static final int TOP_COURSES_LIMIT = 9;

    private final CourseService courseService = new CourseService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        System.out.println("HomeServlet doGet");
        try {
            List<CourseDTO> courses = courseService.listTopPublishedCoursesByEnrollment(TOP_COURSES_LIMIT);
            System.out.println("Loaded " + courses.size() + " published courses");
            req.setAttribute("courses", courses);
            System.out.println("Set courses attribute in request");
        } catch (SQLException e) {
            log("Failed to load published courses", e);
            req.setAttribute("error", "Không thể tải danh sách khóa học lúc này, vui lòng thử lại sau.");
        }
        req.getRequestDispatcher("/WEB-INF/views/home.jsp").forward(req, resp);
    }
}
