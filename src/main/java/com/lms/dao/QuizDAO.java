package com.lms.dao;

import com.lms.dto.QuizDTO;
import com.lms.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class QuizDAO {

    public List<QuizDTO> findQuizzesWithLastAttempt(int courseId, int userId) throws SQLException {
        String sql = "SELECT q.id, q.course_id, q.title, q.total_questions, q.pass_score, q.time_limit_min, q.created_at, " +
                     "       qa.score, qa.is_passed, qa.started_at, qa.submitted_at, " +
                     "       (SELECT COUNT(*) FROM quiz_attempts WHERE quiz_id = q.id AND user_id = ?) as attempt_count, " +
                     "       qc.chapter_id " +
                     "FROM quizzes q " +
                     "LEFT JOIN quiz_chapters qc ON q.id = qc.quiz_id " +
                     "LEFT JOIN ( " +
                     "    SELECT *, " +
                     "           ROW_NUMBER() OVER (PARTITION BY quiz_id ORDER BY score DESC NULLS LAST, started_at DESC) as rn " +
                     "    FROM quiz_attempts " +
                     "    WHERE user_id = ? " +
                     ") qa ON q.id = qa.quiz_id AND qa.rn = 1 " +
                     "WHERE q.course_id = ? " +
                     "ORDER BY q.id ASC";

        List<QuizDTO> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, userId);
            ps.setInt(2, userId);
            ps.setInt(3, courseId);
            
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

                    // Handle possible NULL values from quiz_attempts
                    int score = rs.getInt("score");
                    if (rs.wasNull()) {
                        dto.setLastAttemptScore(null);
                    } else {
                        dto.setLastAttemptScore(score);
                    }

                    boolean isPassed = rs.getBoolean("is_passed");
                    if (rs.wasNull()) {
                        dto.setLastAttemptPassed(null);
                    } else {
                        dto.setLastAttemptPassed(isPassed);
                    }

                    dto.setLastAttemptStartedAt(rs.getTimestamp("started_at"));
                    dto.setLastAttemptSubmittedAt(rs.getTimestamp("submitted_at"));
                    dto.setAttemptCount(rs.getInt("attempt_count"));
                    
                    int chapterId = rs.getInt("chapter_id");
                    if (rs.wasNull()) {
                        dto.setChapterId(null);
                    } else {
                        dto.setChapterId(chapterId);
                    }

                    list.add(dto);
                }
            }
        }
        return list;
    }

    public QuizDTO findQuizWithLastAttempt(int quizId, int userId) throws SQLException {
        String sql = "SELECT q.id, q.course_id, q.title, q.total_questions, q.pass_score, q.time_limit_min, q.created_at, " +
                     "       qa.score, qa.is_passed, qa.started_at, qa.submitted_at, " +
                     "       (SELECT COUNT(*) FROM quiz_attempts WHERE quiz_id = q.id AND user_id = ?) as attempt_count, " +
                     "       qc.chapter_id " +
                     "FROM quizzes q " +
                     "LEFT JOIN quiz_chapters qc ON q.id = qc.quiz_id " +
                     "LEFT JOIN ( " +
                     "    SELECT *, " +
                     "           ROW_NUMBER() OVER (PARTITION BY quiz_id ORDER BY score DESC NULLS LAST, started_at DESC) as rn " +
                     "    FROM quiz_attempts " +
                     "    WHERE user_id = ? " +
                     ") qa ON q.id = qa.quiz_id AND qa.rn = 1 " +
                     "WHERE q.id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, userId);
            ps.setInt(2, userId);
            ps.setInt(3, quizId);
            
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

                    int score = rs.getInt("score");
                    if (rs.wasNull()) {
                        dto.setLastAttemptScore(null);
                    } else {
                        dto.setLastAttemptScore(score);
                    }

                    boolean isPassed = rs.getBoolean("is_passed");
                    if (rs.wasNull()) {
                        dto.setLastAttemptPassed(null);
                    } else {
                        dto.setLastAttemptPassed(isPassed);
                    }

                    dto.setLastAttemptStartedAt(rs.getTimestamp("started_at"));
                    dto.setLastAttemptSubmittedAt(rs.getTimestamp("submitted_at"));
                    dto.setAttemptCount(rs.getInt("attempt_count"));

                    int chapterId = rs.getInt("chapter_id");
                    if (rs.wasNull()) {
                        dto.setChapterId(null);
                    } else {
                        dto.setChapterId(chapterId);
                    }
                    return dto;
                }
            }
        }
        return null;
    }
}

