package com.lms.dao;

import com.lms.entity.LessonProgress;
import com.lms.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Data Access Object for the "lesson_progresses" table.
 * Pure JDBC, no business logic.
 */
public class LessonProgressDAO {

    /**
     * Find progress of one lesson for one trainee.
     */
    public LessonProgress findByUserAndLesson(
            int userId,
            int lessonId)
            throws SQLException {

        String sql =
                "SELECT id, user_id, lesson_id, completed, " +
                        "completed_at, created_at " +
                        "FROM lesson_progresses " +
                        "WHERE user_id = ? " +
                        "AND lesson_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ps.setInt(2, lessonId);

            try (ResultSet rs = ps.executeQuery()) {

                if (!rs.next()) {
                    return null;
                }

                return mapRow(rs);
            }
        }
    }


    /**
     * Mark a lesson as completed.
     *
     * If the progress record does not exist,
     * a new record is created.
     *
     * If it already exists,
     * the existing record is updated.
     */
    public void markCompleted(
            int userId,
            int lessonId)
            throws SQLException {

        String sql =
                "INSERT INTO lesson_progresses " +
                        "(user_id, lesson_id, completed, completed_at) " +
                        "VALUES (?, ?, TRUE, CURRENT_TIMESTAMP) " +
                        "ON CONFLICT (user_id, lesson_id) " +
                        "DO UPDATE SET " +
                        "completed = TRUE, " +
                        "completed_at = CURRENT_TIMESTAMP";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ps.setInt(2, lessonId);

            ps.executeUpdate();
        }
    }


    /**
     * Map one ResultSet row to LessonProgress entity.
     */
    private LessonProgress mapRow(ResultSet rs)
            throws SQLException {

        LessonProgress progress =
                new LessonProgress();

        progress.setId(
                rs.getInt("id")
        );

        progress.setUserId(
                rs.getInt("user_id")
        );

        progress.setLessonId(
                rs.getInt("lesson_id")
        );

        progress.setCompleted(
                rs.getBoolean("completed")
        );

        progress.setCompletedAt(
                rs.getTimestamp("completed_at")
        );

        progress.setCreatedAt(
                rs.getTimestamp("created_at")
        );

        return progress;
    }
}