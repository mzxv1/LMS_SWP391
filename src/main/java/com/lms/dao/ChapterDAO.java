package com.lms.dao;

import com.lms.entity.Chapter;
import com.lms.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/** Data Access Object for the "chapters" table. Pure JDBC, no business logic. */
public class ChapterDAO {

    public List<Chapter> findByCourseId(int courseId) throws SQLException {
        String sql = "SELECT * FROM chapters WHERE course_id = ? ORDER BY order_index ASC, id ASC";
        List<Chapter> chapters = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, courseId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Chapter c = new Chapter();
                    c.setId(rs.getInt("id"));
                    c.setCourseId(rs.getInt("course_id"));
                    c.setTitle(rs.getString("title"));
                    c.setOrderIndex(rs.getInt("order_index"));
                    c.setCreatedAt(rs.getTimestamp("created_at"));
                    chapters.add(c);
                }
            }
        }
        return chapters;
    }
}
