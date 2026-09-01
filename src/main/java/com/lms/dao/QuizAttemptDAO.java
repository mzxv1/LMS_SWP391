package com.lms.dao;

import com.lms.dto.QuizAttemptDTO;
import com.lms.entity.Question;
import com.lms.entity.Option;
import com.lms.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class QuizAttemptDAO {

    public List<QuizAttemptDTO> findAttemptsByQuizAndUser(int quizId, int userId) throws SQLException {
        String sql = "SELECT id, user_id, quiz_id, score, is_passed, started_at, submitted_at " +
                     "FROM quiz_attempts " +
                     "WHERE quiz_id = ? AND user_id = ? " +
                     "ORDER BY started_at DESC";
        
        List<QuizAttemptDTO> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, quizId);
            ps.setInt(2, userId);
            
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    QuizAttemptDTO dto = new QuizAttemptDTO();
                    dto.setId(rs.getInt("id"));
                    dto.setUserId(rs.getInt("user_id"));
                    dto.setQuizId(rs.getInt("quiz_id"));
                    
                    int score = rs.getInt("score");
                    if (rs.wasNull()) {
                        dto.setScore(null);
                    } else {
                        dto.setScore(score);
                    }
                    
                    boolean isPassed = rs.getBoolean("is_passed");
                    if (rs.wasNull()) {
                        dto.setIsPassed(null);
                    } else {
                        dto.setIsPassed(isPassed);
                    }
                    
                    dto.setStartedAt(rs.getTimestamp("started_at"));
                    dto.setSubmittedAt(rs.getTimestamp("submitted_at"));
                    list.add(dto);
                }
            }
        }
        return list;
    }

    public QuizAttemptDTO findInProgressAttempt(int quizId, int userId) throws SQLException {
        String sql = "SELECT id, user_id, quiz_id, score, is_passed, started_at, submitted_at " +
                     "FROM quiz_attempts " +
                     "WHERE quiz_id = ? AND user_id = ? AND submitted_at IS NULL " +
                     "LIMIT 1";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, quizId);
            ps.setInt(2, userId);
            
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    QuizAttemptDTO dto = new QuizAttemptDTO();
                    dto.setId(rs.getInt("id"));
                    dto.setUserId(rs.getInt("user_id"));
                    dto.setQuizId(rs.getInt("quiz_id"));
                    dto.setScore(null);
                    dto.setIsPassed(null);
                    dto.setStartedAt(rs.getTimestamp("started_at"));
                    dto.setSubmittedAt(null);
                    return dto;
                }
            }
        }
        return null;
    }

    public int startAttempt(int quizId, int userId) throws SQLException {
        String insertAttemptSql = "INSERT INTO quiz_attempts (user_id, quiz_id, started_at) VALUES (?, ?, CURRENT_TIMESTAMP) RETURNING id";
        String selectConfigSql = "SELECT chapter_id, question_count FROM quiz_chapters WHERE quiz_id = ?";
        String selectQuestionsSql = "SELECT id FROM questions WHERE chapter_id = ?";
        String insertAttemptQuestionSql = "INSERT INTO quiz_attempt_questions (attempt_id, question_id) VALUES (?, ?)";

        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);

            // 1. Insert attempt
            int attemptId = -1;
            try (PreparedStatement ps = conn.prepareStatement(insertAttemptSql)) {
                ps.setInt(1, userId);
                ps.setInt(2, quizId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        attemptId = rs.getInt(1);
                    }
                }
            }

            if (attemptId == -1) {
                throw new SQLException("Khởi tạo lượt thi thất bại.");
            }

            // 2. Load configurations
            List<int[]> configs = new ArrayList<>();
            try (PreparedStatement ps = conn.prepareStatement(selectConfigSql)) {
                ps.setInt(1, quizId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        configs.add(new int[]{rs.getInt("chapter_id"), rs.getInt("question_count")});
                    }
                }
            }

            // 3. For each chapter, pick random questions
            List<Integer> chosenQuestionIds = new ArrayList<>();
            try (PreparedStatement ps = conn.prepareStatement(selectQuestionsSql)) {
                for (int[] config : configs) {
                    int chapterId = config[0];
                    int questionCount = config[1];

                    ps.setInt(1, chapterId);
                    List<Integer> questionIds = new ArrayList<>();
                    try (ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                            questionIds.add(rs.getInt("id"));
                        }
                    }

                    // Shuffle questions to make it random
                    java.util.Collections.shuffle(questionIds);

                    // Pick top questionCount
                    int limit = Math.min(questionCount, questionIds.size());
                    for (int i = 0; i < limit; i++) {
                        chosenQuestionIds.add(questionIds.get(i));
                    }
                }
            }

            // Shuffle the final list so they don't appear strictly grouped by chapter
            java.util.Collections.shuffle(chosenQuestionIds);

            // 4. Save chosen questions to database
            try (PreparedStatement ps = conn.prepareStatement(insertAttemptQuestionSql)) {
                for (int questionId : chosenQuestionIds) {
                    ps.setInt(1, attemptId);
                    ps.setInt(2, questionId);
                    ps.addBatch();
                }
                ps.executeBatch();
            }

            conn.commit();
            return attemptId;
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    // Ignore
                }
            }
            throw e;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    // Ignore
                }
            }
        }
    }

    public List<Question> findQuestionsByAttemptId(int attemptId) throws SQLException {
        String sql = "SELECT q.id as question_id, q.content as question_content, q.explanation as question_explanation, " +
                     "       ao.id as option_id, ao.content as option_content, ao.is_correct " +
                     "FROM quiz_attempt_questions qaq " +
                     "JOIN questions q ON qaq.question_id = q.id " +
                     "LEFT JOIN answer_options ao ON q.id = ao.question_id " +
                     "WHERE qaq.attempt_id = ? " +
                     "ORDER BY qaq.id ASC, ao.id ASC";

        List<Question> questions = new ArrayList<>();
        Question currentQuestion = null;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, attemptId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int questionId = rs.getInt("question_id");
                    
                    // If we encounter a new question, create a new Question object
                    if (currentQuestion == null || currentQuestion.getId() != questionId) {
                        currentQuestion = new Question();
                        currentQuestion.setId(questionId);
                        currentQuestion.setContent(rs.getString("question_content"));
                        currentQuestion.setExplanation(rs.getString("question_explanation"));
                        questions.add(currentQuestion);
                    }
                    
                    // Add option if it exists
                    int optionId = rs.getInt("option_id");
                    if (!rs.wasNull()) {
                        Option opt = new Option();
                        opt.setId(optionId);
                        opt.setQuestionId(questionId);
                        opt.setOptionText(rs.getString("option_content"));
                        opt.setCorrect(rs.getBoolean("is_correct"));
                        currentQuestion.getOptions().add(opt);
                    }
                }
            }
        }
        return questions;
    }

    public QuizAttemptDTO findAttemptById(int attemptId) throws SQLException {
        String sql = "SELECT id, user_id, quiz_id, score, is_passed, started_at, submitted_at " +
                     "FROM quiz_attempts " +
                     "WHERE id = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, attemptId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    QuizAttemptDTO dto = new QuizAttemptDTO();
                    dto.setId(rs.getInt("id"));
                    dto.setUserId(rs.getInt("user_id"));
                    dto.setQuizId(rs.getInt("quiz_id"));
                    
                    int score = rs.getInt("score");
                    if (rs.wasNull()) {
                        dto.setScore(null);
                    } else {
                        dto.setScore(score);
                    }
                    
                    boolean isPassed = rs.getBoolean("is_passed");
                    if (rs.wasNull()) {
                        dto.setIsPassed(null);
                    } else {
                        dto.setIsPassed(isPassed);
                    }
                    
                    dto.setStartedAt(rs.getTimestamp("started_at"));
                    dto.setSubmittedAt(rs.getTimestamp("submitted_at"));
                    return dto;
                }
            }
        }
        return null;
    }

    public void submitAttempt(int attemptId, java.util.Map<Integer, Integer> answers, int quizId) throws SQLException {
        String insertAnswerSql = "INSERT INTO quiz_answers (attempt_id, question_id, option_id) VALUES (?, ?, ?)";
        String selectCorrectAnswersSql = "SELECT qaq.question_id, ao.id as correct_option_id " +
                                         "FROM quiz_attempt_questions qaq " +
                                         "JOIN answer_options ao ON qaq.question_id = ao.question_id AND ao.is_correct = true " +
                                         "WHERE qaq.attempt_id = ?";
        String selectQuizDetailsSql = "SELECT total_questions, pass_score FROM quizzes WHERE id = ?";
        String updateAttemptSql = "UPDATE quiz_attempts SET score = ?, is_passed = ?, submitted_at = CURRENT_TIMESTAMP WHERE id = ?";

        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);

            // 1. Save answers
            try (PreparedStatement ps = conn.prepareStatement(insertAnswerSql)) {
                for (java.util.Map.Entry<Integer, Integer> entry : answers.entrySet()) {
                    ps.setInt(1, attemptId);
                    ps.setInt(2, entry.getKey());
                    ps.setInt(3, entry.getValue());
                    ps.addBatch();
                }
                ps.executeBatch();
            }

            // 2. Load correct answers of this attempt
            java.util.Map<Integer, Integer> correctAnswers = new java.util.HashMap<>();
            try (PreparedStatement ps = conn.prepareStatement(selectCorrectAnswersSql)) {
                ps.setInt(1, attemptId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        correctAnswers.put(rs.getInt("question_id"), rs.getInt("correct_option_id"));
                    }
                }
            }

            // 3. Count correct answers
            int correctCount = 0;
            for (java.util.Map.Entry<Integer, Integer> entry : answers.entrySet()) {
                Integer selectedOptionId = entry.getValue();
                Integer correctOptionId = correctAnswers.get(entry.getKey());
                if (selectedOptionId != null && selectedOptionId.equals(correctOptionId)) {
                    correctCount++;
                }
            }

            // 4. Load quiz configurations
            int totalQuestions = correctAnswers.size(); // Fallback to how many questions are in the attempt
            int passScore = 70; // Default fallback
            try (PreparedStatement ps = conn.prepareStatement(selectQuizDetailsSql)) {
                ps.setInt(1, quizId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        totalQuestions = rs.getInt("total_questions");
                        passScore = rs.getInt("pass_score");
                    }
                }
            }

            if (totalQuestions <= 0) {
                totalQuestions = 1;
            }

            // Calculate percentage score (0-100)
            int score = (int) Math.round((double) correctCount * 100 / totalQuestions);
            boolean isPassed = score >= passScore;

            // 5. Update attempt
            try (PreparedStatement ps = conn.prepareStatement(updateAttemptSql)) {
                ps.setInt(1, score);
                ps.setBoolean(2, isPassed);
                ps.setInt(3, attemptId);
                ps.executeUpdate();
            }

            conn.commit();
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    // Ignore
                }
            }
            throw e;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    // Ignore
                }
            }
        }
    }

    public java.util.Map<Integer, Integer> findSelectedAnswers(int attemptId) throws SQLException {
        String sql = "SELECT question_id, option_id FROM quiz_answers WHERE attempt_id = ?";
        java.util.Map<Integer, Integer> map = new java.util.HashMap<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, attemptId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    map.put(rs.getInt("question_id"), rs.getInt("option_id"));
                }
            }
        }
        return map;
    }
}

