package com.lms.service;

import com.lms.dao.QuizDAO;
import com.lms.dao.QuizAttemptDAO;
import com.lms.dto.QuizDTO;
import com.lms.dto.QuizAttemptDTO;
import com.lms.entity.Question;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public class QuizService {

    private final QuizDAO quizDAO = new QuizDAO();
    private final QuizAttemptDAO quizAttemptDAO = new QuizAttemptDAO();

    public List<QuizDTO> getStudentQuizList(int courseId, int userId) throws SQLException {
        return quizDAO.findQuizzesWithLastAttempt(courseId, userId);
    }

    public QuizDTO getStudentQuizDetail(int quizId, int userId) throws SQLException {
        return quizDAO.findQuizWithLastAttempt(quizId, userId);
    }

    public List<QuizAttemptDTO> getQuizAttempts(int quizId, int userId) throws SQLException {
        return quizAttemptDAO.findAttemptsByQuizAndUser(quizId, userId);
    }

    public int startStudentQuiz(int quizId, int userId) throws SQLException {
        return quizAttemptDAO.startAttempt(quizId, userId);
    }

    public List<Question> getAttemptQuestions(int attemptId) throws SQLException {
        return quizAttemptDAO.findQuestionsByAttemptId(attemptId);
    }

    public QuizAttemptDTO getQuizAttempt(int attemptId) throws SQLException {
        return quizAttemptDAO.findAttemptById(attemptId);
    }

    public void submitStudentQuiz(int attemptId, Map<Integer, Integer> answers, int quizId) throws SQLException {
        quizAttemptDAO.submitAttempt(attemptId, answers, quizId);
    }

    public Map<Integer, Integer> getSelectedAnswers(int attemptId) throws SQLException {
        return quizAttemptDAO.findSelectedAnswers(attemptId);
    }
}
