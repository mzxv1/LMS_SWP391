package com.lms.controller;

import com.lms.dto.CourseDTO;
import com.lms.dto.QuizDTO;
import com.lms.dto.QuizAttemptDTO;
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

@WebServlet("/courses/quizzes/detail")
public class StudentQuizDetailServlet extends HttpServlet {

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

        // 2. Read quiz ID parameter
        String quizIdParam = req.getParameter("id");
        if (quizIdParam == null || quizIdParam.trim().isEmpty()) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Thiếu mã bài kiểm tra (id).");
            return;
        }

        try {
            int quizId = Integer.parseInt(quizIdParam);

            // 3. Load quiz detail
            QuizDTO quiz = quizService.getStudentQuizDetail(quizId, currentUser.getId());
            if (quiz == null) {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Không tìm thấy bài kiểm tra.");
                return;
            }

            // Verify if student is enrolled in the course that this quiz belongs to
            EnrollmentService enrollmentService = new EnrollmentService();
            if (!enrollmentService.isStudentEnrolled(currentUser.getId(), quiz.getCourseId())) {
                req.getSession().setAttribute("errorMsg", "Bạn cần đăng ký khóa học này trước khi xem hoặc làm bài kiểm tra.");
                resp.sendRedirect(req.getContextPath() + "/courses/detail?id=" + quiz.getCourseId());
                return;
            }

            req.setAttribute("quiz", quiz);

            // 4. Load course details for Breadcrumb
            CourseDTO course = courseService.getCourseById(quiz.getCourseId());
            req.setAttribute("course", course);

            // 5. Load quiz attempts history for the user
            List<QuizAttemptDTO> attempts = quizService.getQuizAttempts(quizId, currentUser.getId());
            req.setAttribute("attempts", attempts);

            // Check if there is an in-progress attempt (the first one if submittedAt is null)
            QuizAttemptDTO inProgressAttempt = null;
            if (attempts != null && !attempts.isEmpty()) {
                QuizAttemptDTO latest = attempts.get(0);
                if (latest.getSubmittedAt() == null) {
                    inProgressAttempt = latest;
                }
            }
            req.setAttribute("inProgressAttempt", inProgressAttempt);

            // 6. Forward to quiz detail JSP
            req.getRequestDispatcher("/WEB-INF/views/course/quiz-detail.jsp").forward(req, resp);

        } catch (NumberFormatException e) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Mã bài kiểm tra không đúng định dạng.");
        } catch (SQLException | ServiceException e) {
            throw new ServletException(e);
        }
    }
}
