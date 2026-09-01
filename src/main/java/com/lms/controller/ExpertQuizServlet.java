package com.lms.controller;

import com.lms.dto.CourseDTO;
import com.lms.dto.QuizDTO;
import com.lms.dto.QuizChapterDTO;
import com.lms.dto.UserDTO;
import com.lms.service.CourseService;
import com.lms.service.ExpertQuizService;
import com.lms.service.ServiceException;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@WebServlet({
    "/expert/quizzes/new",
    "/expert/quizzes/edit",
    "/expert/quizzes/delete"
})
public class ExpertQuizServlet extends HttpServlet {

    private final ExpertQuizService expertQuizService = new ExpertQuizService();
    private final CourseService courseService = new CourseService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) 
            throws ServletException, IOException {
        
        UserDTO currentUser = (UserDTO) req.getSession().getAttribute("currentUser");
        if (currentUser == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        String path = req.getServletPath();
        try {
            if ("/expert/quizzes/new".equals(path)) {
                showNewForm(req, resp, currentUser.getId());
            } else if ("/expert/quizzes/edit".equals(path)) {
                showEditForm(req, resp, currentUser.getId());
            } else {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            }
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) 
            throws ServletException, IOException {
        
        UserDTO currentUser = (UserDTO) req.getSession().getAttribute("currentUser");
        if (currentUser == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        String path = req.getServletPath();
        try {
            if ("/expert/quizzes/new".equals(path)) {
                createQuiz(req, resp, currentUser.getId());
            } else if ("/expert/quizzes/edit".equals(path)) {
                updateQuiz(req, resp, currentUser.getId());
            } else if ("/expert/quizzes/delete".equals(path)) {
                deleteQuiz(req, resp, currentUser.getId());
            } else {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            }
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }

    private void showNewForm(HttpServletRequest req, HttpServletResponse resp, int expertId) 
            throws ServletException, IOException, SQLException, ServiceException {
        
        String courseIdParam = req.getParameter("courseId");
        if (courseIdParam == null || courseIdParam.trim().isEmpty()) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Thiếu mã khóa học.");
            return;
        }

        int courseId = Integer.parseInt(courseIdParam);
        CourseDTO course = courseService.getCourseById(courseId);
        if (course == null || course.getExpertId() != expertId) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Bạn không có quyền quản lý khóa học này.");
            return;
        }

        List<QuizChapterDTO> distributions = expertQuizService.getQuizChapters(-1, courseId, expertId);

        req.setAttribute("course", course);
        req.setAttribute("distributions", distributions);
        req.setAttribute("isEdit", false);
        req.getRequestDispatcher("/WEB-INF/views/expert/quiz-form.jsp").forward(req, resp);
    }

    private void showEditForm(HttpServletRequest req, HttpServletResponse resp, int expertId) 
            throws ServletException, IOException, SQLException, ServiceException {
        
        String idParam = req.getParameter("id");
        if (idParam == null || idParam.trim().isEmpty()) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Thiếu mã đề thi.");
            return;
        }

        int quizId = Integer.parseInt(idParam);
        QuizDTO quiz = expertQuizService.getQuizById(quizId, expertId);
        CourseDTO course = courseService.getCourseById(quiz.getCourseId());
        List<QuizChapterDTO> distributions = expertQuizService.getQuizChapters(quizId, quiz.getCourseId(), expertId);

        req.setAttribute("quiz", quiz);
        req.setAttribute("course", course);
        req.setAttribute("distributions", distributions);
        req.setAttribute("isEdit", true);
        req.getRequestDispatcher("/WEB-INF/views/expert/quiz-form.jsp").forward(req, resp);
    }

    private void createQuiz(HttpServletRequest req, HttpServletResponse resp, int expertId) 
            throws ServletException, IOException, SQLException {
        
        int courseId = Integer.parseInt(req.getParameter("courseId"));
        String title = req.getParameter("title");
        int timeLimitMin = Integer.parseInt(req.getParameter("timeLimitMin"));
        int passScore = Integer.parseInt(req.getParameter("passScore"));

        QuizDTO quiz = new QuizDTO();
        quiz.setCourseId(courseId);
        quiz.setTitle(title);
        quiz.setTimeLimitMin(timeLimitMin);
        quiz.setPassScore(passScore);

        List<QuizChapterDTO> distributions = new ArrayList<>();
        try {
            List<QuizChapterDTO> tempChapters = expertQuizService.getQuizChapters(-1, courseId, expertId);
            for (QuizChapterDTO tc : tempChapters) {
                String countParam = req.getParameter("chapter_count_" + tc.getChapterId());
                int count = (countParam != null && !countParam.trim().isEmpty()) ? Integer.parseInt(countParam) : 0;
                
                QuizChapterDTO qc = new QuizChapterDTO();
                qc.setChapterId(tc.getChapterId());
                qc.setChapterName(tc.getChapterName());
                qc.setMaxQuestionsAvailable(tc.getMaxQuestionsAvailable());
                qc.setQuestionCount(count);
                distributions.add(qc);
            }

            expertQuizService.createQuiz(quiz, distributions, expertId);
            resp.sendRedirect(req.getContextPath() + "/expert/questions?courseId=" + courseId + "&tab=quizzes");
        } catch (ServiceException e) {
            req.setAttribute("error", e.getMessage());
            req.setAttribute("quiz", quiz);
            req.setAttribute("distributions", distributions);
            req.setAttribute("isEdit", false);
            try {
                req.setAttribute("course", courseService.getCourseById(courseId));
            } catch (Exception ignored) {}
            req.getRequestDispatcher("/WEB-INF/views/expert/quiz-form.jsp").forward(req, resp);
        }
    }

    private void updateQuiz(HttpServletRequest req, HttpServletResponse resp, int expertId) 
            throws ServletException, IOException, SQLException {
        
        int quizId = Integer.parseInt(req.getParameter("id"));
        int courseId = Integer.parseInt(req.getParameter("courseId"));
        String title = req.getParameter("title");
        int timeLimitMin = Integer.parseInt(req.getParameter("timeLimitMin"));
        int passScore = Integer.parseInt(req.getParameter("passScore"));

        QuizDTO quiz = new QuizDTO();
        quiz.setId(quizId);
        quiz.setCourseId(courseId);
        quiz.setTitle(title);
        quiz.setTimeLimitMin(timeLimitMin);
        quiz.setPassScore(passScore);

        List<QuizChapterDTO> distributions = new ArrayList<>();
        try {
            List<QuizChapterDTO> tempChapters = expertQuizService.getQuizChapters(quizId, courseId, expertId);
            for (QuizChapterDTO tc : tempChapters) {
                String countParam = req.getParameter("chapter_count_" + tc.getChapterId());
                int count = (countParam != null && !countParam.trim().isEmpty()) ? Integer.parseInt(countParam) : 0;
                
                QuizChapterDTO qc = new QuizChapterDTO();
                qc.setChapterId(tc.getChapterId());
                qc.setChapterName(tc.getChapterName());
                qc.setMaxQuestionsAvailable(tc.getMaxQuestionsAvailable());
                qc.setQuestionCount(count);
                distributions.add(qc);
            }

            expertQuizService.updateQuiz(quiz, distributions, expertId);
            resp.sendRedirect(req.getContextPath() + "/expert/questions?courseId=" + courseId + "&tab=quizzes");
        } catch (ServiceException e) {
            req.setAttribute("error", e.getMessage());
            req.setAttribute("quiz", quiz);
            req.setAttribute("distributions", distributions);
            req.setAttribute("isEdit", true);
            try {
                req.setAttribute("course", courseService.getCourseById(courseId));
            } catch (Exception ignored) {}
            req.getRequestDispatcher("/WEB-INF/views/expert/quiz-form.jsp").forward(req, resp);
        }
    }

    private void deleteQuiz(HttpServletRequest req, HttpServletResponse resp, int expertId) 
            throws ServletException, IOException, SQLException, ServiceException {
        
        int quizId = Integer.parseInt(req.getParameter("id"));
        int courseId = Integer.parseInt(req.getParameter("courseId"));
        
        expertQuizService.deleteQuiz(quizId, expertId);
        resp.sendRedirect(req.getContextPath() + "/expert/questions?courseId=" + courseId + "&tab=quizzes");
    }
}
