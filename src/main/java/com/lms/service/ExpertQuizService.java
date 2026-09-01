package com.lms.service;

import com.lms.dao.ExpertQuizDAO;
import com.lms.dto.CourseDTO;
import com.lms.dto.QuizDTO;
import com.lms.dto.QuizChapterDTO;

import java.sql.SQLException;
import java.util.List;

public class ExpertQuizService {

    private final ExpertQuizDAO expertQuizDAO = new ExpertQuizDAO();
    private final CourseService courseService = new CourseService();

    private void validateOwnership(int courseId, int expertId) throws ServiceException, SQLException {
        CourseDTO course = courseService.getCourseById(courseId);
        if (course == null || course.getExpertId() != expertId) {
            throw new ServiceException("Bạn không có quyền truy cập khóa học này.");
        }
    }

    public List<QuizDTO> getQuizzesByCourse(int courseId, int expertId) throws ServiceException, SQLException {
        validateOwnership(courseId, expertId);
        return expertQuizDAO.findQuizzesByCourseId(courseId);
    }

    public QuizDTO getQuizById(int quizId, int expertId) throws ServiceException, SQLException {
        QuizDTO quiz = expertQuizDAO.findQuizById(quizId);
        if (quiz == null) {
            throw new ServiceException("Bài kiểm tra không tồn tại.");
        }
        validateOwnership(quiz.getCourseId(), expertId);
        return quiz;
    }

    public List<QuizChapterDTO> getQuizChapters(int quizId, int courseId, int expertId) throws ServiceException, SQLException {
        validateOwnership(courseId, expertId);
        return expertQuizDAO.findQuizChapters(quizId, courseId);
    }

    public void createQuiz(QuizDTO quiz, List<QuizChapterDTO> distributions, int expertId) throws ServiceException, SQLException {
        validateOwnership(quiz.getCourseId(), expertId);
        
        if (quiz.getTitle() == null || quiz.getTitle().trim().isEmpty()) {
            throw new ServiceException("Tên đề thi không được để trống.");
        }
        if (quiz.getTimeLimitMin() <= 0) {
            throw new ServiceException("Thời gian làm bài phải lớn hơn 0 phút.");
        }
        if (quiz.getPassScore() < 0 || quiz.getPassScore() > 100) {
            throw new ServiceException("Điểm số đạt yêu cầu phải nằm trong khoảng từ 0% đến 100%.");
        }

        // Sum and validate question counts
        int totalQuestions = 0;
        for (QuizChapterDTO dist : distributions) {
            if (dist.getQuestionCount() < 0) {
                throw new ServiceException("Số lượng câu hỏi không được nhỏ hơn 0.");
            }
            if (dist.getQuestionCount() > dist.getMaxQuestionsAvailable()) {
                throw new ServiceException("Số câu hỏi cấu hình cho chương '" + dist.getChapterName() + 
                                           "' vượt quá số câu hỏi hiện có trong ngân hàng (" + dist.getMaxQuestionsAvailable() + ").");
            }
            totalQuestions += dist.getQuestionCount();
        }

        if (totalQuestions <= 0) {
            throw new ServiceException("Tổng số câu hỏi trong đề thi phải lớn hơn 0. Vui lòng phân bổ câu hỏi vào các chương.");
        }

        quiz.setTotalQuestions(totalQuestions);
        expertQuizDAO.createQuiz(quiz, distributions);
    }

    public void updateQuiz(QuizDTO quiz, List<QuizChapterDTO> distributions, int expertId) throws ServiceException, SQLException {
        // Retrieve original quiz to check ownership
        QuizDTO originalQuiz = getQuizById(quiz.getId(), expertId);
        quiz.setCourseId(originalQuiz.getCourseId()); // Ensure course ID remains unchanged

        if (quiz.getTitle() == null || quiz.getTitle().trim().isEmpty()) {
            throw new ServiceException("Tên đề thi không được để trống.");
        }
        if (quiz.getTimeLimitMin() <= 0) {
            throw new ServiceException("Thời gian làm bài phải lớn hơn 0 phút.");
        }
        if (quiz.getPassScore() < 0 || quiz.getPassScore() > 100) {
            throw new ServiceException("Điểm số đạt yêu cầu phải nằm trong khoảng từ 0% đến 100%.");
        }

        // Sum and validate question counts
        int totalQuestions = 0;
        for (QuizChapterDTO dist : distributions) {
            if (dist.getQuestionCount() < 0) {
                throw new ServiceException("Số lượng câu hỏi không được nhỏ hơn 0.");
            }
            if (dist.getQuestionCount() > dist.getMaxQuestionsAvailable()) {
                throw new ServiceException("Số câu hỏi cấu hình cho chương '" + dist.getChapterName() + 
                                           "' vượt quá số câu hỏi hiện có trong ngân hàng (" + dist.getMaxQuestionsAvailable() + ").");
            }
            totalQuestions += dist.getQuestionCount();
        }

        if (totalQuestions <= 0) {
            throw new ServiceException("Tổng số câu hỏi trong đề thi phải lớn hơn 0. Vui lòng phân bổ câu hỏi vào các chương.");
        }

        quiz.setTotalQuestions(totalQuestions);
        expertQuizDAO.updateQuiz(quiz, distributions);
    }

    public void deleteQuiz(int quizId, int expertId) throws ServiceException, SQLException {
        // Check ownership
        getQuizById(quizId, expertId);
        expertQuizDAO.deleteQuiz(quizId);
    }
}
