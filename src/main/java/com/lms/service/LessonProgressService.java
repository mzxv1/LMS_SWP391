package com.lms.service;

import com.lms.dao.LessonProgressDAO;
import com.lms.entity.LessonProgress;

import java.sql.SQLException;

/**
 * Service for managing trainee lesson progress.
 * Contains business logic related to lesson completion.
 */
public class LessonProgressService {

    private final LessonProgressDAO lessonProgressDAO =
            new LessonProgressDAO();


    /**
     * Get progress of one lesson for one trainee.
     */
    public LessonProgress getLessonProgress(
            int userId,
            int lessonId)
            throws SQLException {

        return lessonProgressDAO.findByUserAndLesson(
                userId,
                lessonId
        );
    }


    /**
     * Check whether a trainee has completed a lesson.
     */
    public boolean isLessonCompleted(
            int userId,
            int lessonId)
            throws SQLException {

        LessonProgress progress =
                lessonProgressDAO.findByUserAndLesson(
                        userId,
                        lessonId
                );

        return progress != null
                && progress.isCompleted();
    }


    /**
     * Mark one lesson as completed.
     */
    public void markLessonCompleted(
            int userId,
            int lessonId)
            throws SQLException {

        lessonProgressDAO.markCompleted(
                userId,
                lessonId
        );
    }
}