package com.lms.service;

import com.lms.dao.QuestionDAO;
import com.lms.dao.ChapterDAO;
import com.lms.entity.Option;
import com.lms.entity.Question;
import com.lms.entity.Chapter;
import com.lms.entity.Quiz;
import com.lms.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * Business logic layer for Question Management (Expert/Lecturer role).
 * Ensures lecturers can only manage questions for chapters belonging to courses they own.
 */
public class QuestionService {

    private final QuestionDAO questionDAO = new QuestionDAO();
    private final ChapterDAO chapterDAO = new ChapterDAO();

    /** Verify that a chapter belongs to a course owned by the given expert. */
    private void verifyChapterOwner(int chapterId, int expertId) throws ServiceException, SQLException {
        String sql = "SELECT 1 FROM chapters ch JOIN courses c ON ch.course_id = c.id WHERE ch.id = ? AND c.expert_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, chapterId);
            ps.setInt(2, expertId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new ServiceException("Bạn không có quyền quản lý câu hỏi cho chương học này hoặc chương học không tồn tại.");
                }
            }
        }
    }

    /** Verify that a quiz belongs to a course owned by the given expert. */
    private void verifyQuizOwner(int quizId, int expertId) throws ServiceException, SQLException {
        String sql = "SELECT 1 FROM quizzes q JOIN courses c ON q.course_id = c.id WHERE q.id = ? AND c.expert_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, quizId);
            ps.setInt(2, expertId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new ServiceException("Bạn không có quyền quản lý bài kiểm tra này hoặc bài kiểm tra không tồn tại.");
                }
            }
        }
    }

    public List<Question> getQuestionsForChapter(int chapterId, int expertId) throws ServiceException, SQLException {
        verifyChapterOwner(chapterId, expertId);
        return questionDAO.findByChapterId(chapterId);
    }

    public List<Question> searchQuestions(int chapterId, int expertId, String keyword) throws ServiceException, SQLException {
        verifyChapterOwner(chapterId, expertId);
        return questionDAO.search(chapterId, keyword);
    }

    public void addQuestion(Question q, int expertId) throws ServiceException, SQLException {
        verifyChapterOwner(q.getChapterId(), expertId);
        validateQuestion(q);
        questionDAO.insert(q);
    }

    public void deleteQuestion(int questionId, int expertId) throws ServiceException, SQLException {
        Question q = questionDAO.findById(questionId);
        if (q == null) {
            throw new ServiceException("Không tìm thấy câu hỏi với ID: " + questionId);
        }
        verifyChapterOwner(q.getChapterId(), expertId);
        questionDAO.delete(questionId);
    }

    public Question getQuestionById(int questionId, int expertId) throws ServiceException, SQLException {
        Question q = questionDAO.findById(questionId);
        if (q == null) {
            throw new ServiceException("Không tìm thấy câu hỏi với ID: " + questionId);
        }
        verifyChapterOwner(q.getChapterId(), expertId);
        return q;
    }

    public void updateQuestion(Question q, int expertId) throws ServiceException, SQLException {
        Question existing = questionDAO.findById(q.getId());
        if (existing == null) {
            throw new ServiceException("Không tìm thấy câu hỏi với ID: " + q.getId());
        }
        verifyChapterOwner(existing.getChapterId(), expertId);
        
        // Force the original chapterId to prevent ID manipulation
        q.setChapterId(existing.getChapterId());
        validateQuestion(q);
        
        questionDAO.update(q);
    }

    public List<Chapter> getChaptersForCourse(int courseId, int expertId) throws ServiceException, SQLException {
        // verify that the course belongs to the expert
        String checkSql = "SELECT 1 FROM courses WHERE id = ? AND expert_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(checkSql)) {
            ps.setInt(1, courseId);
            ps.setInt(2, expertId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new ServiceException("Bạn không có quyền xem chương học của khóa học này.");
                }
            }
        }
        return chapterDAO.findByCourseId(courseId);
    }

    public List<Quiz> getQuizzesForCourse(int courseId, int expertId) throws ServiceException, SQLException {
        // verify that the course belongs to the expert
        String checkSql = "SELECT 1 FROM courses WHERE id = ? AND expert_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(checkSql)) {
            ps.setInt(1, courseId);
            ps.setInt(2, expertId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new ServiceException("Bạn không có quyền xem bài kiểm tra của khóa học này.");
                }
            }
        }

        String sql = "SELECT * FROM quizzes WHERE course_id = ? ORDER BY id ASC";
        java.util.ArrayList<Quiz> list = new java.util.ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, courseId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Quiz quiz = new Quiz();
                    quiz.setId(rs.getInt("id"));
                    quiz.setCourseId(rs.getInt("course_id"));
                    quiz.setTitle(rs.getString("title"));
                    quiz.setTotalQuestions(rs.getInt("total_questions"));
                    quiz.setPassScore(rs.getInt("pass_score"));
                    quiz.setCreatedAt(rs.getTimestamp("created_at"));
                    list.add(quiz);
                }
            }
        }
        return list;
    }

    public Quiz getQuizById(int quizId, int expertId) throws ServiceException, SQLException {
        verifyQuizOwner(quizId, expertId);
        String sql = "SELECT * FROM quizzes WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, quizId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Quiz quiz = new Quiz();
                    quiz.setId(rs.getInt("id"));
                    quiz.setCourseId(rs.getInt("course_id"));
                    quiz.setTitle(rs.getString("title"));
                    quiz.setTotalQuestions(rs.getInt("total_questions"));
                    quiz.setPassScore(rs.getInt("pass_score"));
                    quiz.setCreatedAt(rs.getTimestamp("created_at"));
                    return quiz;
                }
            }
        }
        return null;
    }

    public Chapter getChapterById(int chapterId, int expertId) throws ServiceException, SQLException {
        verifyChapterOwner(chapterId, expertId);
        String sql = "SELECT * FROM chapters WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, chapterId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Chapter c = new Chapter();
                    c.setId(rs.getInt("id"));
                    c.setCourseId(rs.getInt("course_id"));
                    c.setTitle(rs.getString("title"));
                    c.setOrderIndex(rs.getInt("order_index"));
                    c.setCreatedAt(rs.getTimestamp("created_at"));
                    return c;
                }
            }
        }
        return null;
    }

    private void validateQuestion(Question q) throws ServiceException {
        if (q.getContent() == null || q.getContent().trim().isEmpty()) {
            throw new ServiceException("Nội dung câu hỏi không được để trống.");
        }
        if (q.getOptions() == null || q.getOptions().isEmpty()) {
            throw new ServiceException("Danh sách đáp án lựa chọn không được để trống.");
        }

        int correctCount = 0;
        for (Option opt : q.getOptions()) {
            if (opt.getOptionText() == null || opt.getOptionText().trim().isEmpty()) {
                throw new ServiceException("Nội dung đáp án lựa chọn không được để trống.");
            }
            if (opt.isCorrect()) {
                correctCount++;
            }
        }

        if (correctCount != 1) {
            throw new ServiceException("Phải chọn chính xác một đáp án đúng.");
        }
    }

    public List<Question> getQuestionsPaginated(int chapterId, int expertId, String keyword, int page, int size) throws ServiceException, SQLException {
        verifyChapterOwner(chapterId, expertId);
        int limit = size;
        int offset = (page - 1) * size;
        return questionDAO.findPaginated(chapterId, keyword, limit, offset);
    }

    public int getTotalQuestionsCount(int chapterId, int expertId, String keyword) throws ServiceException, SQLException {
        verifyChapterOwner(chapterId, expertId);
        return questionDAO.countByChapterId(chapterId, keyword);
    }
}
