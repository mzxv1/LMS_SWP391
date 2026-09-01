package com.lms.service;

import com.lms.dao.LessonDAO;
import com.lms.dto.LessonDTO;

import java.sql.SQLException;
import java.util.List;

/**
 * Business logic layer for Lesson Management.
 *
 * Responsibilities:
 * - Load lesson detail
 * - Load lessons by course
 * - Load lessons by chapter
 */
public class LessonService {

    private final LessonDAO lessonDAO =
            new LessonDAO();


    // ============================================================
    // LESSON DETAIL
    // ============================================================

    /**
     * Get one lesson by ID.
     */
    public LessonDTO getLessonById(
            int lessonId)
            throws ServiceException, SQLException {

        if (lessonId <= 0) {
            throw new ServiceException(
                    "ID bài học không hợp lệ."
            );
        }

        LessonDTO lesson =
                lessonDAO.findById(
                        lessonId
                );

        if (lesson == null) {
            throw new ServiceException(
                    "Không tìm thấy bài học."
            );
        }

        return lesson;
    }


    // ============================================================
    // LESSON LIST BY COURSE
    // ============================================================

    /**
     * Get all lessons belonging to one course.
     */
    public List<LessonDTO> getLessonsByCourseId(
            int courseId)
            throws ServiceException, SQLException {

        if (courseId <= 0) {
            throw new ServiceException(
                    "ID khóa học không hợp lệ."
            );
        }

        return lessonDAO.findByCourseId(
                courseId
        );
    }


    // ============================================================
    // LESSON LIST BY CHAPTER
    // ============================================================

    /**
     * Get all lessons belonging to one chapter.
     */
    public List<LessonDTO> getLessonsByChapterId(
            int chapterId)
            throws ServiceException, SQLException {

        if (chapterId <= 0) {
            throw new ServiceException(
                    "ID chapter không hợp lệ."
            );
        }

        return lessonDAO.findByChapterId(
                chapterId
        );
    }

    public void addLesson(LessonDTO lesson) throws ServiceException, SQLException {
        if (lesson.getTitle() == null || lesson.getTitle().trim().isEmpty()) {
            throw new ServiceException("Tiêu đề bài học không được để trống.");
        }
        if (lesson.getCourseId() <= 0) {
            throw new ServiceException("Khóa học không hợp lệ.");
        }
        lessonDAO.insert(lesson);
    }

    public void updateLesson(LessonDTO lesson) throws ServiceException, SQLException {
        if (lesson.getId() <= 0) {
            throw new ServiceException("ID bài học không hợp lệ.");
        }
        if (lesson.getTitle() == null || lesson.getTitle().trim().isEmpty()) {
            throw new ServiceException("Tiêu đề bài học không được để trống.");
        }
        lessonDAO.update(lesson);
    }

    public void deleteLesson(int lessonId) throws ServiceException, SQLException {
        if (lessonId <= 0) {
            throw new ServiceException("ID bài học không hợp lệ.");
        }
        LessonDTO existing = lessonDAO.findById(lessonId);
        if (existing == null) {
            throw new ServiceException("Bài học không tồn tại.");
        }
        lessonDAO.delete(lessonId);
    }
}