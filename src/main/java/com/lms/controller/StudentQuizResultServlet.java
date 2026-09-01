package com.lms.controller;

import com.lms.dto.CourseDTO;
import com.lms.dto.QuizDTO;
import com.lms.dto.QuizAttemptDTO;
import com.lms.dto.UserDTO;
import com.lms.entity.Question;
import com.lms.service.CourseService;
import com.lms.service.QuizService;
import com.lms.service.ServiceException;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

@WebServlet("/courses/quizzes/result")
public class StudentQuizResultServlet extends HttpServlet {

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

        // 2. Read parameters (support both attemptId and quizId)
        String attemptIdParam = req.getParameter("attemptId");
        String quizIdParam = req.getParameter("quizId");

        int attemptId = -1;
        try {
            if (attemptIdParam != null && !attemptIdParam.trim().isEmpty()) {
                attemptId = Integer.parseInt(attemptIdParam);
            } else if (quizIdParam != null && !quizIdParam.trim().isEmpty()) {
                int quizId = Integer.parseInt(quizIdParam);
                // Get all attempts for this quiz and user
                List<QuizAttemptDTO> attempts = quizService.getQuizAttempts(quizId, currentUser.getId());
                // Find the latest completed attempt (submittedAt != null)
                for (QuizAttemptDTO att : attempts) {
                    if (att.getSubmittedAt() != null) {
                        attemptId = att.getId();
                        break;
                    }
                }
                if (attemptId == -1) {
                    // No completed attempt found, redirect to quiz details page
                    resp.sendRedirect(req.getContextPath() + "/courses/quizzes/detail?id=" + quizId);
                    return;
                }
            } else {
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Thiếu mã lượt làm bài (attemptId) hoặc mã bài kiểm tra (quizId).");
                return;
            }

            // 3. Load Quiz Attempt
            QuizAttemptDTO attempt = quizService.getQuizAttempt(attemptId);
            if (attempt == null || attempt.getUserId() != currentUser.getId()) {
                resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Bạn không có quyền xem kết quả lượt làm bài này.");
                return;
            }

            // 4. If not submitted yet, redirect back to the taking page
            if (attempt.getSubmittedAt() == null) {
                resp.sendRedirect(req.getContextPath() + "/courses/quizzes/taking?attemptId=" + attemptId);
                return;
            }

            // 5. Load Quiz details
            QuizDTO quiz = quizService.getStudentQuizDetail(attempt.getQuizId(), currentUser.getId());
            req.setAttribute("quiz", quiz);

            // 6. Load Course details for breadcrumbs
            CourseDTO course = courseService.getCourseById(quiz.getCourseId());
            req.setAttribute("course", course);

            // 7. Load questions & options of the attempt
            List<Question> questions = quizService.getAttemptQuestions(attemptId);
            req.setAttribute("questions", questions);
            req.setAttribute("attempt", attempt);

            // 8. Load student's selected answers map (question_id -> option_id)
            Map<Integer, Integer> selectedAnswers = quizService.getSelectedAnswers(attemptId);
            req.setAttribute("selectedAnswers", selectedAnswers);

            // Calculate completion duration
            long durationMs = attempt.getSubmittedAt().getTime() - attempt.getStartedAt().getTime();
            long durationSec = durationMs / 1000;
            long min = durationSec / 60;
            long sec = durationSec % 60;
            String durationStr = min + " phút " + sec + " giây";
            req.setAttribute("durationStr", durationStr);

            // 9. Forward to the result page
            req.getRequestDispatcher("/WEB-INF/views/course/quiz-result.jsp").forward(req, resp);

        } catch (NumberFormatException e) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Mã lượt làm bài không đúng định dạng.");
        } catch (SQLException | ServiceException e) {
            throw new ServletException(e);
        }
    }
}
