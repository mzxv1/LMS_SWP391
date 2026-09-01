package com.lms.service;

import com.lms.dao.ChapterDAO;
import com.lms.dao.CourseDAO;
import com.lms.dao.EnrollmentDAO;
import com.lms.dao.LessonProgressDAO;
import com.lms.dto.ChapterDTO;
import com.lms.dto.LessonDTO;
import com.lms.dto.TraineeCourseDetailDTO;
import com.lms.dto.TraineeCourseDTO;
import com.lms.entity.Chapter;
import com.lms.entity.Course;
import com.lms.entity.LessonProgress;
import com.lms.dao.QuizDAO;
import com.lms.dto.QuizDTO;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class TraineeCourseService {

    private final CourseDAO courseDAO =
            new CourseDAO();

    private final EnrollmentDAO enrollmentDAO =
            new EnrollmentDAO();

    private final ChapterDAO chapterDAO =
            new ChapterDAO();

    private final LessonProgressDAO lessonProgressDAO =
            new LessonProgressDAO();

    private final QuizDAO quizDAO =
            new QuizDAO();


    /**
     * Get course detail for a trainee.
     *
     * The trainee must have an ACTIVE enrollment
     * for the requested course.
     */
    public TraineeCourseDetailDTO getCourseDetail(
            int studentId,
            int courseId)
            throws ServiceException, SQLException {

        // --------------------------------------------------------
        // 1. Check whether the student is enrolled
        // --------------------------------------------------------

        TraineeCourseDTO enrolledCourse =
                findEnrolledCourse(
                        studentId,
                        courseId
                );

        if (enrolledCourse == null) {

            throw new ServiceException(
                    "Bạn chưa đăng ký khóa học này hoặc khóa học không còn hoạt động."
            );
        }


        // --------------------------------------------------------
        // 2. Load course
        // --------------------------------------------------------

        Course course =
                courseDAO.findById(courseId);

        if (course == null) {

            throw new ServiceException(
                    "Không tìm thấy khóa học."
            );
        }


        // --------------------------------------------------------
        // 3. Build trainee course detail
        // --------------------------------------------------------

        TraineeCourseDetailDTO detail =
                new TraineeCourseDetailDTO();

        detail.setCourseId(
                course.getId()
        );

        detail.setTitle(
                course.getTitle()
        );

        detail.setDescription(
                course.getDescription()
        );

        detail.setCategory(
                course.getCategory()
        );

        detail.setPrice(
                course.getPrice()
        );

        detail.setDurationHours(
                course.getDurationHours()
        );

        detail.setExpertId(
                course.getExpertId()
        );


        // --------------------------------------------------------
        // 4. Enrollment information
        // --------------------------------------------------------

        detail.setProgressPercent(
                enrolledCourse.getProgressPercent()
        );

        detail.setEnrollmentStatus(
                enrolledCourse.getEnrollmentStatus()
        );

        detail.setEnrolledAt(
                enrolledCourse.getEnrolledAt()
        );


        // --------------------------------------------------------
        // 5. Load chapters
        // --------------------------------------------------------

        List<Chapter> chapters =
                chapterDAO.findByCourseId(
                        courseId
                );


        // --------------------------------------------------------
        // 6. Load lessons
        // --------------------------------------------------------

        List<LessonDTO> lessons =
                courseDAO.findLessonsForTraineeCourseDetail(
                        courseId
                );

        int completedLessons = 0;
        List<LessonDTO> lessonPreview = new ArrayList<>();

        for (LessonDTO lesson : lessons) {
            LessonProgress progress = lessonProgressDAO
                    .findByUserAndLesson(studentId, lesson.getId());

            boolean completed = progress != null && progress.isCompleted();

            lesson.setCompleted(completed);

            if (completed) {
                completedLessons++;
            }

            if (lessonPreview.size() < 3) {
                lessonPreview.add(lesson);
            }
        }

        detail.setTotalLessons(lessons.size());
        detail.setCompletedLessons(completedLessons);
        detail.setLessonPreview(lessonPreview);

        // Load course quizzes with attempts for this trainee
        List<QuizDTO> quizzes = quizDAO.findQuizzesWithLastAttempt(courseId, studentId);

        // --------------------------------------------------------
        // 7. Build chapter -> lesson structure
        // --------------------------------------------------------

        List<ChapterDTO> chapterDTOs =
                new ArrayList<>();


        for (Chapter chapter : chapters) {

            ChapterDTO chapterDTO =
                    new ChapterDTO();

            chapterDTO.setId(
                    chapter.getId()
            );

            chapterDTO.setCourseId(
                    chapter.getCourseId()
            );

            chapterDTO.setTitle(
                    chapter.getTitle()
            );

            chapterDTO.setOrderIndex(
                    chapter.getOrderIndex()
            );


            List<LessonDTO> chapterLessons =
                    new ArrayList<>();


            for (LessonDTO lesson : lessons) {

                if (lesson.getChapterId()
                        == chapter.getId()) {

                    chapterLessons.add(
                            lesson
                    );
                }
            }


            chapterDTO.setLessons(
                    chapterLessons
            );

            // Filter quizzes belonging to this chapter
            List<QuizDTO> chapterQuizzes = new ArrayList<>();
            for (QuizDTO quiz : quizzes) {
                if (quiz.getChapterId() != null && quiz.getChapterId() == chapter.getId()) {
                    chapterQuizzes.add(quiz);
                }
            }
            chapterDTO.setQuizzes(chapterQuizzes);

            chapterDTOs.add(
                    chapterDTO
            );
        }


        // --------------------------------------------------------
        // 8. Attach chapters to course detail
        // --------------------------------------------------------

        detail.setChapters(
                chapterDTOs
        );


        return detail;
    }


    /**
     * Find one active enrolled course for a student.
     *
     * The existing EnrollmentDAO returns the student's
     * active courses as TraineeCourseDTO.
     */
    private TraineeCourseDTO findEnrolledCourse(
            int studentId,
            int courseId)
            throws SQLException {

        List<TraineeCourseDTO> courses =
                enrollmentDAO.findActiveCoursesByStudentId(
                        studentId
                );

        for (TraineeCourseDTO course : courses) {

            if (course.getCourseId() == courseId) {
                return course;
            }
        }

        return null;
    }
}
