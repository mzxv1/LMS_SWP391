package com.lms.dao;

import com.lms.dto.QuizDTO;
import com.lms.dto.QuizChapterDTO;
import com.lms.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ExpertQuizDAO {

    public List<QuizDTO> findQuizzesByCourseId(int courseId) throws SQLException {
        String sql = "SELECT id, course_id, title, total_questions, pass_score, time_limit_min, created_at " +
                     "FROM quizzes " +
                     "WHERE course_id = ? " +
                     "ORDER BY id ASC";

        List<QuizDTO> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, courseId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    QuizDTO dto = new QuizDTO();
                    dto.setId(rs.getInt("id"));
                    dto.setCourseId(rs.getInt("course_id"));
                    dto.setTitle(rs.getString("title"));
                    dto.setTotalQuestions(rs.getInt("total_questions"));
                    dto.setPassScore(rs.getInt("pass_score"));
                    dto.setTimeLimitMin(rs.getInt("time_limit_min"));
                    dto.setCreatedAt(rs.getTimestamp("created_at"));
                    list.add(dto);
                }
            }
        }
        return list;
    }

    public QuizDTO findQuizById(int quizId) throws SQLException {
        String sql = "SELECT id, course_id, title, total_questions, pass_score, time_limit_min, created_at " +
                     "FROM quizzes " +
                     "WHERE id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, quizId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    QuizDTO dto = new QuizDTO();
                    dto.setId(rs.getInt("id"));
                    dto.setCourseId(rs.getInt("course_id"));
                    dto.setTitle(rs.getString("title"));
                    dto.setTotalQuestions(rs.getInt("total_questions"));
                    dto.setPassScore(rs.getInt("pass_score"));
                    dto.setTimeLimitMin(rs.getInt("time_limit_min"));
                    dto.setCreatedAt(rs.getTimestamp("created_at"));
                    return dto;
                }
            }
        }
        return null;
    }

    public List<QuizChapterDTO> findQuizChapters(int quizId, int courseId) throws SQLException {
        String sql = "SELECT c.id AS chapter_id, c.title AS chapter_name, " +
                     "       COALESCE(qc.question_count, 0) AS question_count, " +
                     "       (SELECT COUNT(*) FROM questions WHERE chapter_id = c.id) AS max_questions " +
                     "FROM chapters c " +
                     "LEFT JOIN quiz_chapters qc ON c.id = qc.chapter_id AND qc.quiz_id = ? " +
                     "WHERE c.course_id = ? " +
                     "ORDER BY c.id ASC";

        List<QuizChapterDTO> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, quizId);
            ps.setInt(2, courseId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    QuizChapterDTO dto = new QuizChapterDTO();
                    dto.setChapterId(rs.getInt("chapter_id"));
                    dto.setChapterName(rs.getString("chapter_name"));
                    dto.setQuestionCount(rs.getInt("question_count"));
                    dto.setMaxQuestionsAvailable(rs.getInt("max_questions"));
                    list.add(dto);
                }
            }
        }
        return list;
    }

    public void createQuiz(QuizDTO quiz, List<QuizChapterDTO> distributions) throws SQLException {
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);

            // Insert Quiz
            String quizSql = "INSERT INTO quizzes (course_id, title, total_questions, pass_score, time_limit_min) " +
                             "VALUES (?, ?, ?, ?, ?) RETURNING id";
            int quizId = -1;
            try (PreparedStatement ps = conn.prepareStatement(quizSql)) {
                ps.setInt(1, quiz.getCourseId());
                ps.setString(2, quiz.getTitle());
                ps.setInt(3, quiz.getTotalQuestions());
                ps.setInt(4, quiz.getPassScore());
                ps.setInt(5, quiz.getTimeLimitMin());
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        quizId = rs.getInt(1);
                    }
                }
            }

            if (quizId == -1) {
                throw new SQLException("Failed to retrieve generated quiz ID");
            }

            // Insert distributions
            String distSql = "INSERT INTO quiz_chapters (quiz_id, chapter_id, question_count) VALUES (?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(distSql)) {
                for (QuizChapterDTO dist : distributions) {
                    if (dist.getQuestionCount() > 0) {
                        ps.setInt(1, quizId);
                        ps.setInt(2, dist.getChapterId());
                        ps.setInt(3, dist.getQuestionCount());
                        ps.addBatch();
                    }
                }
                ps.executeBatch();
            }

            conn.commit();
        } catch (SQLException e) {
            if (conn != null) {
                conn.rollback();
            }
            throw e;
        } finally {
            if (conn != null) {
                conn.setAutoCommit(true);
                conn.close();
            }
        }
    }

    public void updateQuiz(QuizDTO quiz, List<QuizChapterDTO> distributions) throws SQLException {
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);

            // Update Quiz
            String quizSql = "UPDATE quizzes SET title = ?, total_questions = ?, pass_score = ?, time_limit_min = ? WHERE id = ?";
            try (PreparedStatement ps = conn.prepareStatement(quizSql)) {
                ps.setString(1, quiz.getTitle());
                ps.setInt(2, quiz.getTotalQuestions());
                ps.setInt(3, quiz.getPassScore());
                ps.setInt(4, quiz.getTimeLimitMin());
                ps.setInt(5, quiz.getId());
                ps.executeUpdate();
            }

            // Delete old distributions
            String deleteSql = "DELETE FROM quiz_chapters WHERE quiz_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(deleteSql)) {
                ps.setInt(1, quiz.getId());
                ps.executeUpdate();
            }

            // Insert new distributions
            String distSql = "INSERT INTO quiz_chapters (quiz_id, chapter_id, question_count) VALUES (?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(distSql)) {
                for (QuizChapterDTO dist : distributions) {
                    if (dist.getQuestionCount() > 0) {
                        ps.setInt(1, quiz.getId());
                        ps.setInt(2, dist.getChapterId());
                        ps.setInt(3, dist.getQuestionCount());
                        ps.addBatch();
                    }
                }
                ps.executeBatch();
            }

            conn.commit();
        } catch (SQLException e) {
            if (conn != null) {
                conn.rollback();
            }
            throw e;
        } finally {
            if (conn != null) {
                conn.setAutoCommit(true);
                conn.close();
            }
        }
    }

    public void deleteQuiz(int quizId) throws SQLException {
        String sql = "DELETE FROM quizzes WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, quizId);
            ps.executeUpdate();
        }
    }
}
