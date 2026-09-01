package com.lms.controller;

import com.lms.dto.CourseDTO;
import com.lms.dto.QuizDTO;
import com.lms.dto.QuizAttemptDTO;
import com.lms.dto.UserDTO;
import com.lms.entity.Question;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet({
    "/courses/quizzes/start",
    "/courses/quizzes/taking",
    "/courses/quizzes/submit"
})
public class StudentQuizTakingServlet extends HttpServlet {

    private final QuizService quizService = new QuizService();
    private final CourseService courseService = new CourseService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) 
            throws ServletException, IOException {
        String path = req.getServletPath();
        if ("/courses/quizzes/taking".equals(path)) {
            handleTaking(req, resp);
        } else {
            resp.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) 
            throws ServletException, IOException {
        String path = req.getServletPath();
        if ("/courses/quizzes/start".equals(path)) {
            handleStart(req, resp);
        } else if ("/courses/quizzes/submit".equals(path)) {
            handleSubmit(req, resp);
        } else {
            resp.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
        }
    }

    private void handleStart(HttpServletRequest req, HttpServletResponse resp) 
            throws ServletException, IOException {
        UserDTO currentUser = (UserDTO) req.getSession().getAttribute("currentUser");
        if (currentUser == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        String quizIdParam = req.getParameter("quizId");
        if (quizIdParam == null || quizIdParam.trim().isEmpty()) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Thiếu mã bài kiểm tra (quizId).");
            return;
        }

        try {
            int quizId = Integer.parseInt(quizIdParam);
            QuizDTO quiz = quizService.getStudentQuizDetail(quizId, currentUser.getId());
            if (quiz == null) {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Không tìm thấy bài kiểm tra.");
                return;
            }

            EnrollmentService enrollmentService = new EnrollmentService();
            if (!enrollmentService.isStudentEnrolled(currentUser.getId(), quiz.getCourseId())) {
                req.getSession().setAttribute("errorMsg", "Bạn cần đăng ký khóa học này trước khi làm bài kiểm tra.");
                resp.sendRedirect(req.getContextPath() + "/courses/detail?id=" + quiz.getCourseId());
                return;
            }

            int attemptId = quizService.startStudentQuiz(quizId, currentUser.getId());
            resp.sendRedirect(req.getContextPath() + "/courses/quizzes/taking?attemptId=" + attemptId);
        } catch (NumberFormatException e) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Mã bài kiểm tra không đúng định dạng.");
        } catch (SQLException e) {
            throw new ServletException(e);
        }
    }

    private void handleTaking(HttpServletRequest req, HttpServletResponse resp) 
            throws ServletException, IOException {
        UserDTO currentUser = (UserDTO) req.getSession().getAttribute("currentUser");
        if (currentUser == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        String attemptIdParam = req.getParameter("attemptId");
        if (attemptIdParam == null || attemptIdParam.trim().isEmpty()) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Thiếu mã lượt làm bài (attemptId).");
            return;
        }

        try {
            int attemptId = Integer.parseInt(attemptIdParam);
            QuizAttemptDTO attempt = quizService.getQuizAttempt(attemptId);
            if (attempt == null || attempt.getUserId() != currentUser.getId()) {
                resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Bạn không có quyền truy cập lượt làm bài này.");
                return;
            }

            if (attempt.getSubmittedAt() != null) {
                resp.sendRedirect(req.getContextPath() + "/courses/quizzes/result?attemptId=" + attemptId);
                return;
            }

            QuizDTO quiz = quizService.getStudentQuizDetail(attempt.getQuizId(), currentUser.getId());
            if (quiz == null) {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Không tìm thấy bài kiểm tra.");
                return;
            }

            EnrollmentService enrollmentService = new EnrollmentService();
            if (!enrollmentService.isStudentEnrolled(currentUser.getId(), quiz.getCourseId())) {
                req.getSession().setAttribute("errorMsg", "Bạn cần đăng ký khóa học này trước khi làm bài kiểm tra.");
                resp.sendRedirect(req.getContextPath() + "/courses/detail?id=" + quiz.getCourseId());
                return;
            }

            req.setAttribute("quiz", quiz);

            CourseDTO course = courseService.getCourseById(quiz.getCourseId());
            req.setAttribute("course", course);

            List<Question> questions = quizService.getAttemptQuestions(attemptId);
            req.setAttribute("questions", questions);
            req.setAttribute("totalQuestions", questions.size());
            req.setAttribute("attempt", attempt);

            long now = System.currentTimeMillis();
            long startedAt = attempt.getStartedAt().getTime();
            long limitSeconds = quiz.getTimeLimitMin() * 60L;
            long elapsedSeconds = (now - startedAt) / 1000;
            long remainingSeconds = limitSeconds - elapsedSeconds;
            if (remainingSeconds < 0) {
                remainingSeconds = 0;
            }
            req.setAttribute("remainingSeconds", remainingSeconds);

            req.getRequestDispatcher("/WEB-INF/views/course/quiz-taking.jsp").forward(req, resp);

        } catch (NumberFormatException e) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Mã lượt làm bài không đúng định dạng.");
        } catch (SQLException | ServiceException e) {
            throw new ServletException(e);
        }
    }

    private void handleSubmit(HttpServletRequest req, HttpServletResponse resp) 
            throws ServletException, IOException {
        UserDTO currentUser = (UserDTO) req.getSession().getAttribute("currentUser");
        if (currentUser == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        String attemptIdParam = req.getParameter("attemptId");
        if (attemptIdParam == null || attemptIdParam.trim().isEmpty()) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Thiếu mã lượt làm bài (attemptId).");
            return;
        }

        try {
            int attemptId = Integer.parseInt(attemptIdParam);
            QuizAttemptDTO attempt = quizService.getQuizAttempt(attemptId);
            if (attempt == null || attempt.getUserId() != currentUser.getId()) {
                resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Bạn không có quyền nộp bài cho lượt làm bài này.");
                return;
            }

            QuizDTO quiz = quizService.getStudentQuizDetail(attempt.getQuizId(), currentUser.getId());
            EnrollmentService enrollmentService = new EnrollmentService();
            if (quiz == null || !enrollmentService.isStudentEnrolled(currentUser.getId(), quiz.getCourseId())) {
                req.getSession().setAttribute("errorMsg", "Bạn cần đăng ký khóa học này trước khi làm bài kiểm tra.");
                resp.sendRedirect(req.getContextPath() + "/courses/detail?id=" + (quiz != null ? quiz.getCourseId() : ""));
                return;
            }

            if (attempt.getSubmittedAt() != null) {
                resp.sendRedirect(req.getContextPath() + "/courses/quizzes/result?attemptId=" + attemptId);
                return;
            }

            List<Question> questions = quizService.getAttemptQuestions(attemptId);
            Map<Integer, Integer> answers = new HashMap<>();

            for (Question q : questions) {
                String optionIdParam = req.getParameter("question_" + q.getId());
                if (optionIdParam != null && !optionIdParam.trim().isEmpty()) {
                    try {
                        int optionId = Integer.parseInt(optionIdParam);
                        answers.put(q.getId(), optionId);
                    } catch (NumberFormatException e) {
                        // Ignore
                    }
                }
            }

            quizService.submitStudentQuiz(attemptId, answers, attempt.getQuizId());
            resp.sendRedirect(req.getContextPath() + "/courses/quizzes/result?attemptId=" + attemptId);

        } catch (NumberFormatException e) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Mã lượt làm bài không đúng định dạng.");
        } catch (SQLException e) {
            throw new ServletException(e);
        }
    }
}
