package com.lms.service;

import com.lms.dao.EnrollmentDAO;
import com.lms.dao.CourseDAO;
import com.lms.dao.LessonDAO;
import com.lms.dao.LessonProgressDAO;
import com.lms.dto.LessonDTO;
import com.lms.dto.TraineeCourseDTO;
import com.lms.entity.Course;
import com.lms.entity.LessonProgress;

import java.sql.SQLException;
import java.util.List;

public class TraineeLessonService {

    private final LessonDAO lessonDAO =
            new LessonDAO();

    private final EnrollmentDAO enrollmentDAO =
            new EnrollmentDAO();

    private final CourseDAO courseDAO =
            new CourseDAO();

    private final LessonProgressDAO lessonProgressDAO =
            new LessonProgressDAO();


    /**
     * Get one lesson for an enrolled trainee.
     *
     * The trainee must have an ACTIVE enrollment
     * in the course containing the requested lesson.
     */
    public LessonDTO getLessonDetail(
            int studentId,
            int lessonId)
            throws ServiceException, SQLException {

        // --------------------------------------------------------
        // 1. Find lesson
        // --------------------------------------------------------

        LessonDTO lesson =
                lessonDAO.findById(lessonId);

        if (lesson == null) {

            throw new ServiceException(
                    "Không tìm thấy bài học."
            );
        }


        // --------------------------------------------------------
        // 2. Check trainee enrollment
        // --------------------------------------------------------

        List<TraineeCourseDTO> courses =
                enrollmentDAO.findActiveCoursesByStudentId(
                        studentId
                );

        boolean enrolled = false;

        for (TraineeCourseDTO course : courses) {

            if (course.getCourseId()
                    == lesson.getCourseId()) {

                enrolled = true;
                break;
            }
        }


        // --------------------------------------------------------
        // 3. Reject unauthorized access
        // --------------------------------------------------------

        if (!enrolled) {

            throw new ServiceException(
                    "Bạn không có quyền truy cập bài học này."
            );
        }

        Course course = courseDAO.findById(lesson.getCourseId());
        lesson.setCourseTitle(course == null ? null : course.getTitle());

        LessonProgress progress = lessonProgressDAO
                .findByUserAndLesson(studentId, lessonId);

        lesson.setCompleted(
                progress != null && progress.isCompleted()
        );


        // --------------------------------------------------------
        // 4. Return lesson
        // --------------------------------------------------------

        return lesson;
    }
}
