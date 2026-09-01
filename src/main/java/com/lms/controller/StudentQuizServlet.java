package com.lms.controller;

import com.lms.dto.CourseDTO;
import com.lms.dto.QuizDTO;
import com.lms.dto.UserDTO;
import com.lms.service.CourseService;
import com.lms.service.QuizService;
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

@WebServlet("/courses/quizzes")
public class StudentQuizServlet extends HttpServlet {

    private final QuizService quizService = new QuizService();
    private final CourseService courseService = new CourseService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) 
            throws ServletException, IOException {
        
        // 1. Verify user session
        UserDTO currentUser = (UserDTO) req.getSession().getAttribute("currentUser");
        if (currentUser == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        // 2. Read courseId parameter
        String courseIdParam = req.getParameter("courseId");
        if (courseIdParam == null || courseIdParam.trim().isEmpty()) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Thiếu courseId.");
            return;
        }

        try {
            int courseId = Integer.parseInt(courseIdParam);

            // Check if student is actively enrolled in this course
            EnrollmentService enrollmentService = new EnrollmentService();
            if (!enrollmentService.isStudentEnrolled(currentUser.getId(), courseId)) {
                req.getSession().setAttribute("errorMsg", "Bạn cần đăng ký khóa học này trước khi xem hoặc làm bài kiểm tra.");
                resp.sendRedirect(req.getContextPath() + "/courses/detail?id=" + courseId);
                return;
            }

            // 3. Load course details for Breadcrumb
            CourseDTO course = courseService.getCourseById(courseId);
            if (course == null) {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Không tìm thấy khóa học.");
                return;
            }
            req.setAttribute("course", course);

            // 4. Load quizzes list for the student
            List<QuizDTO> quizzes = quizService.getStudentQuizList(courseId, currentUser.getId());
            req.setAttribute("quizzes", quizzes);

            // 5. Forward to quiz list JSP
            req.getRequestDispatcher("/WEB-INF/views/course/quiz-list.jsp").forward(req, resp);

        } catch (NumberFormatException e) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "courseId không đúng định dạng.");
        } catch (SQLException | ServiceException e) {
            throw new ServletException(e);
        }
    }
}
