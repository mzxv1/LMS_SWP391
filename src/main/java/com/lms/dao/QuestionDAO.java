package com.lms.dao;

import com.lms.entity.Option;
import com.lms.entity.Question;
import com.lms.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/** Data Access Object for the "questions" and "answer_options" tables. Pure JDBC, no business logic. */
public class QuestionDAO {

    public List<Question> findByChapterId(int chapterId) throws SQLException {
        String sql = "SELECT * FROM questions WHERE chapter_id = ? ORDER BY id DESC";
        List<Question> questions = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, chapterId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Question q = mapRow(rs);
                    q.setOptions(findOptionsByQuestionId(conn, q.getId()));
                    questions.add(q);
                }
            }
        }
        return questions;
    }

    public List<Question> search(int chapterId, String keyword) throws SQLException {
        StringBuilder sql = new StringBuilder("SELECT q.* FROM questions q WHERE q.chapter_id = ?");
        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append(" AND (q.content ILIKE ? OR EXISTS (SELECT 1 FROM answer_options o WHERE o.question_id = q.id AND o.content ILIKE ?))");
        }
        sql.append(" ORDER BY q.id DESC");

        List<Question> questions = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            ps.setInt(1, chapterId);
            if (keyword != null && !keyword.trim().isEmpty()) {
                String like = "%" + keyword.trim() + "%";
                ps.setString(2, like);
                ps.setString(3, like);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Question q = mapRow(rs);
                    q.setOptions(findOptionsByQuestionId(conn, q.getId()));
                    questions.add(q);
                }
            }
        }
        return questions;
    }

    public Question findById(int id) throws SQLException {
        String sql = "SELECT * FROM questions WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Question q = mapRow(rs);
                    q.setOptions(findOptionsByQuestionId(conn, q.getId()));
                    return q;
                }
            }
        }
        return null;
    }

    public void insert(Question q) throws SQLException {
        String insertQuestionSql = "INSERT INTO questions (chapter_id, content, explanation) VALUES (?, ?, ?)";
        String insertOptionSql = "INSERT INTO answer_options (question_id, content, is_correct) VALUES (?, ?, ?)";

        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false); // Start transaction

            try (PreparedStatement psQ = conn.prepareStatement(insertQuestionSql, Statement.RETURN_GENERATED_KEYS)) {
                psQ.setInt(1, q.getChapterId());
                psQ.setString(2, q.getContent());
                psQ.setString(3, q.getExplanation());
                psQ.executeUpdate();

                try (ResultSet rs = psQ.getGeneratedKeys()) {
                    if (rs.next()) {
                        q.setId(rs.getInt(1));
                    }
                }
            }

            try (PreparedStatement psO = conn.prepareStatement(insertOptionSql)) {
                for (Option opt : q.getOptions()) {
                    psO.setInt(1, q.getId());
                    psO.setString(2, opt.getOptionText());
                    psO.setBoolean(3, opt.isCorrect());
                    psO.executeUpdate();
                }
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
                conn.close();
            }
        }
    }

    public void update(Question q) throws SQLException {
        String updateQuestionSql = "UPDATE questions SET content = ?, explanation = ? WHERE id = ?";
        String deleteOptionsSql = "DELETE FROM answer_options WHERE question_id = ?";
        String insertOptionSql = "INSERT INTO answer_options (question_id, content, is_correct) VALUES (?, ?, ?)";

        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);

            try (PreparedStatement psQ = conn.prepareStatement(updateQuestionSql)) {
                psQ.setString(1, q.getContent());
                psQ.setString(2, q.getExplanation());
                psQ.setInt(3, q.getId());
                psQ.executeUpdate();
            }

            try (PreparedStatement psD = conn.prepareStatement(deleteOptionsSql)) {
                psD.setInt(1, q.getId());
                psD.executeUpdate();
            }

            try (PreparedStatement psO = conn.prepareStatement(insertOptionSql)) {
                for (Option opt : q.getOptions()) {
                    psO.setInt(1, q.getId());
                    psO.setString(2, opt.getOptionText());
                    psO.setBoolean(3, opt.isCorrect());
                    psO.executeUpdate();
                }
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
                conn.close();
            }
        }
    }

    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM questions WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    private List<Option> findOptionsByQuestionId(Connection conn, int questionId) throws SQLException {
        String sql = "SELECT * FROM answer_options WHERE question_id = ? ORDER BY id ASC";
        List<Option> options = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, questionId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    options.add(new Option(
                        rs.getInt("id"),
                        rs.getInt("question_id"),
                        rs.getString("content"),
                        rs.getBoolean("is_correct")
                    ));
                }
            }
        }
        return options;
    }

    public int countByChapterId(int chapterId, String keyword) throws SQLException {
        StringBuilder sql = new StringBuilder("SELECT COUNT(DISTINCT q.id) FROM questions q WHERE q.chapter_id = ?");
        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append(" AND (q.content ILIKE ? OR EXISTS (SELECT 1 FROM answer_options o WHERE o.question_id = q.id AND o.content ILIKE ?))");
        }
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            ps.setInt(1, chapterId);
            if (keyword != null && !keyword.trim().isEmpty()) {
                String like = "%" + keyword.trim() + "%";
                ps.setString(2, like);
                ps.setString(3, like);
            }
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return 0;
    }

    public List<Question> findPaginated(int chapterId, String keyword, int limit, int offset) throws SQLException {
        StringBuilder sql = new StringBuilder("SELECT q.* FROM questions q WHERE q.chapter_id = ?");
        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append(" AND (q.content ILIKE ? OR EXISTS (SELECT 1 FROM answer_options o WHERE o.question_id = q.id AND o.content ILIKE ?))");
        }
        sql.append(" ORDER BY q.id DESC LIMIT ? OFFSET ?");

        List<Question> questions = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            int paramIndex = 1;
            ps.setInt(paramIndex++, chapterId);
            if (keyword != null && !keyword.trim().isEmpty()) {
                String like = "%" + keyword.trim() + "%";
                ps.setString(paramIndex++, like);
                ps.setString(paramIndex++, like);
            }
            ps.setInt(paramIndex++, limit);
            ps.setInt(paramIndex++, offset);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Question q = mapRow(rs);
                    q.setOptions(findOptionsByQuestionId(conn, q.getId()));
                    questions.add(q);
                }
            }
        }
        return questions;
    }

    private Question mapRow(ResultSet rs) throws SQLException {
        Question q = new Question();
        q.setId(rs.getInt("id"));
        q.setChapterId(rs.getInt("chapter_id"));
        q.setContent(rs.getString("content"));
        q.setExplanation(rs.getString("explanation"));
        q.setCreatedAt(rs.getTimestamp("created_at"));
        return q;
    }
}
