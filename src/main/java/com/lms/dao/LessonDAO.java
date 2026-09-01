package com.lms.dao;

import com.lms.dto.LessonDTO;
import com.lms.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/** Data Access Object for the "lessons" table. Pure JDBC, no business logic. */
public class LessonDAO {

    /**
     * Find one lesson by its ID.
     */
    public LessonDTO findById(int id) throws SQLException {

        String sql =
                "SELECT id, course_id, chapter_id, title, " +
                        "content_url, lesson_type, duration_minutes, " +
                        "order_index " +
                        "FROM lessons " +
                        "WHERE id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {

                if (!rs.next()) {
                    return null;
                }

                return mapRow(rs);
            }
        }
    }

    /**
     * Find all lessons belonging to one course.
     */
    public List<LessonDTO> findByCourseId(int courseId)
            throws SQLException {

        String sql =
                "SELECT id, course_id, chapter_id, title, " +
                        "content_url, lesson_type, duration_minutes, " +
                        "order_index " +
                        "FROM lessons " +
                        "WHERE course_id = ? " +
                        "ORDER BY order_index ASC, id ASC";

        List<LessonDTO> lessons = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, courseId);

            try (ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {
                    lessons.add(mapRow(rs));
                }
            }
        }

        return lessons;
    }

    /**
     * Find all lessons belonging to one chapter.
     */
    public List<LessonDTO> findByChapterId(int chapterId)
            throws SQLException {

        String sql =
                "SELECT id, course_id, chapter_id, title, " +
                        "content_url, lesson_type, duration_minutes, " +
                        "order_index " +
                        "FROM lessons " +
                        "WHERE chapter_id = ? " +
                        "ORDER BY order_index ASC, id ASC";

        List<LessonDTO> lessons = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, chapterId);

            try (ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {
                    lessons.add(mapRow(rs));
                }
            }
        }

        return lessons;
    }

    /**
     * Map one ResultSet row to LessonDTO.
     */
    private LessonDTO mapRow(ResultSet rs)
            throws SQLException {

        LessonDTO lesson = new LessonDTO();

        lesson.setId(
                rs.getInt("id")
        );

        lesson.setCourseId(
                rs.getInt("course_id")
        );

        lesson.setChapterId(
                rs.getInt("chapter_id")
        );

        lesson.setTitle(
                rs.getString("title")
        );

        lesson.setContentUrl(
                rs.getString("content_url")
        );

        lesson.setLessonType(
                rs.getString("lesson_type")
        );

        lesson.setDurationMinutes(
                rs.getInt("duration_minutes")
        );

        lesson.setOrderIndex(
                rs.getInt("order_index")
        );

        return lesson;
    }

    public void insert(LessonDTO lesson) throws SQLException {
        String sql = "INSERT INTO lessons (course_id, chapter_id, title, content_url, lesson_type, duration_minutes, order_index) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, lesson.getCourseId());
            if (lesson.getChapterId() > 0) {
                ps.setInt(2, lesson.getChapterId());
            } else {
                ps.setNull(2, Types.INTEGER);
            }
            ps.setString(3, lesson.getTitle());
            ps.setString(4, lesson.getContentUrl());
            ps.setString(5, lesson.getLessonType());
            ps.setInt(6, lesson.getDurationMinutes());
            ps.setInt(7, lesson.getOrderIndex());
            ps.executeUpdate();
        }
    }

    public void update(LessonDTO lesson) throws SQLException {
        String sql = "UPDATE lessons SET course_id=?, chapter_id=?, title=?, content_url=?, lesson_type=?, duration_minutes=?, order_index=? WHERE id=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, lesson.getCourseId());
            if (lesson.getChapterId() > 0) {
                ps.setInt(2, lesson.getChapterId());
            } else {
                ps.setNull(2, Types.INTEGER);
            }
            ps.setString(3, lesson.getTitle());
            ps.setString(4, lesson.getContentUrl());
            ps.setString(5, lesson.getLessonType());
            ps.setInt(6, lesson.getDurationMinutes());
            ps.setInt(7, lesson.getOrderIndex());
            ps.setInt(8, lesson.getId());
            ps.executeUpdate();
        }
    }

    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM lessons WHERE id=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }
}