package com.lms.controller;

import com.lms.dao.ChapterDAO;
import com.lms.dto.CourseDTO;
import com.lms.dto.LessonDTO;
import com.lms.dto.UserDTO;
import com.lms.entity.Chapter;
import com.lms.service.CourseService;
import com.lms.service.LessonService;
import com.lms.service.ServiceException;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

@WebServlet("/expert/lessons")
public class ExpertLessonServlet extends HttpServlet {

    private final LessonService lessonService = new LessonService();
    private final CourseService courseService = new CourseService();
    private final ChapterDAO chapterDAO = new ChapterDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("currentUser") == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        String action = req.getParameter("action");
        if (action == null) {
            action = "list";
        }

        try {
            switch (action) {
                case "list":
                    showList(req, resp);
                    break;
                case "add":
                    showAddForm(req, resp);
                    break;
                case "edit":
                    showEditForm(req, resp);
                    break;
                default:
                    showList(req, resp);
                    break;
            }
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("currentUser") == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        String action = req.getParameter("action");
        try {
            switch (action) {
                case "add":
                    addLesson(req, resp);
                    break;
                case "edit":
                    updateLesson(req, resp);
                    break;
                case "delete":
                    deleteLesson(req, resp);
                    break;
                default:
                    resp.sendRedirect(req.getContextPath() + "/expert/lessons");
                    break;
            }
        } catch (Exception e) {
            req.setAttribute("error", e.getMessage());
            doGet(req, resp);
        }
    }

    private void showList(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        String courseIdStr = req.getParameter("courseId");
        if (courseIdStr != null && !courseIdStr.isEmpty()) {
            int courseId = Integer.parseInt(courseIdStr);
            CourseDTO course = courseService.getCourseById(courseId);
            List<LessonDTO> lessons = lessonService.getLessonsByCourseId(courseId);
            List<Chapter> chapters = chapterDAO.findByCourseId(courseId);
            
            req.setAttribute("course", course);
            req.setAttribute("lessons", lessons);
            req.setAttribute("chapters", chapters);
        }
        req.getRequestDispatcher("/WEB-INF/views/expert/lesson-list.jsp").forward(req, resp);
    }

    private void showAddForm(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        int courseId = Integer.parseInt(req.getParameter("courseId"));
        CourseDTO course = courseService.getCourseById(courseId);
        List<Chapter> chapters = chapterDAO.findByCourseId(courseId);

        req.setAttribute("course", course);
        req.setAttribute("chapters", chapters);
        req.getRequestDispatcher("/WEB-INF/views/expert/lesson-form.jsp").forward(req, resp);
    }

    private void showEditForm(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        int id = Integer.parseInt(req.getParameter("id"));
        LessonDTO lesson = lessonService.getLessonById(id);
        CourseDTO course = courseService.getCourseById(lesson.getCourseId());
        List<Chapter> chapters = chapterDAO.findByCourseId(lesson.getCourseId());

        req.setAttribute("lesson", lesson);
        req.setAttribute("course", course);
        req.setAttribute("chapters", chapters);
        req.getRequestDispatcher("/WEB-INF/views/expert/lesson-form.jsp").forward(req, resp);
    }

    private void addLesson(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        int courseId = Integer.parseInt(req.getParameter("courseId"));
        LessonDTO lesson = extractLessonFromRequest(req);
        lesson.setCourseId(courseId);
        try {
            lessonService.addLesson(lesson);
            resp.sendRedirect(req.getContextPath() + "/expert/lessons?action=list&courseId=" + courseId);
        } catch (Exception e) {
            req.setAttribute("lesson", lesson);
            throw e;
        }
    }

    private void updateLesson(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        int id = Integer.parseInt(req.getParameter("id"));
        LessonDTO lesson = extractLessonFromRequest(req);
        lesson.setId(id);
        
        // Ensure courseId is preserved
        LessonDTO existing = lessonService.getLessonById(id);
        lesson.setCourseId(existing.getCourseId());

        try {
            lessonService.updateLesson(lesson);
            resp.sendRedirect(req.getContextPath() + "/expert/lessons?action=list&courseId=" + lesson.getCourseId());
        } catch (Exception e) {
            req.setAttribute("lesson", lesson);
            throw e;
        }
    }

    private void deleteLesson(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        int id = Integer.parseInt(req.getParameter("id"));
        LessonDTO existing = lessonService.getLessonById(id);
        if (existing != null) {
            lessonService.deleteLesson(id);
            resp.sendRedirect(req.getContextPath() + "/expert/lessons?action=list&courseId=" + existing.getCourseId());
        } else {
            resp.sendRedirect(req.getContextPath() + "/expert/lessons");
        }
    }

    private LessonDTO extractLessonFromRequest(HttpServletRequest req) {
        LessonDTO lesson = new LessonDTO();
        lesson.setTitle(req.getParameter("title"));
        String chapterIdStr = req.getParameter("chapterId");
        if (chapterIdStr != null && !chapterIdStr.isEmpty()) {
            lesson.setChapterId(Integer.parseInt(chapterIdStr));
        }
        lesson.setLessonType(req.getParameter("lessonType"));
        lesson.setContentUrl(req.getParameter("contentUrl"));
        
        String durationStr = req.getParameter("durationMinutes");
        if (durationStr != null && !durationStr.isEmpty()) {
            lesson.setDurationMinutes(Integer.parseInt(durationStr));
        }
        
        String orderStr = req.getParameter("orderIndex");
        if (orderStr != null && !orderStr.isEmpty()) {
            lesson.setOrderIndex(Integer.parseInt(orderStr));
        }
        
        return lesson;
    }
}
