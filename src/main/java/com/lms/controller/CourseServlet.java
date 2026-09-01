package com.lms.controller;

import com.lms.dto.CourseDTO;
import com.lms.dto.CourseFormDTO;
import com.lms.dto.UserDTO;
import com.lms.service.CourseService;
import com.lms.service.ServiceException;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

/**
 * Protected by AuthFilter + RoleFilter (EXPERT only, see web.xml).
 * Routes (dispatched on servlet path):
 *   GET  /expert/courses         -> list + search (scoped to the logged-in expert)
 *   GET  /expert/courses/new     -> blank create form
 *   POST /expert/courses/new     -> create
 *   GET  /expert/courses/detail  -> view/edit form for one course (?id=)
 *   POST /expert/courses/detail  -> update
 *   POST /expert/courses/delete  -> delete (?id=)
 */
@WebServlet({"/expert/courses", "/expert/courses/new", "/expert/courses/detail", "/expert/courses/delete"})
public class CourseServlet extends HttpServlet {

    private final CourseService courseService = new CourseService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String path = req.getServletPath();
        try {
            switch (path) {
                case "/expert/courses":
                    list(req, resp);
                    break;
                case "/expert/courses/new":
                    req.getRequestDispatcher("/WEB-INF/views/expert/course-form.jsp").forward(req, resp);
                    break;
                case "/expert/courses/detail":
                    detail(req, resp);
                    break;
                default:
                    resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            }
        } catch (SQLException e) {
            throw new ServletException(e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String path = req.getServletPath();
        try {
            switch (path) {
                case "/expert/courses/new":
                    create(req, resp);
                    break;
                case "/expert/courses/detail":
                    update(req, resp);
                    break;
                case "/expert/courses/delete":
                    delete(req, resp);
                    break;
                default:
                    resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            }
        } catch (SQLException e) {
            throw new ServletException(e);
        }
    }

    private int currentExpertId(HttpServletRequest req) {
        UserDTO currentUser = (UserDTO) req.getSession().getAttribute("currentUser");
        return currentUser.getId();
    }

    private void list(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException, SQLException {
        String keyword = req.getParameter("keyword");
        String status = req.getParameter("status");
        List<CourseDTO> courses = courseService.searchCourses(currentExpertId(req), keyword, status);
        req.setAttribute("courses", courses);
        req.setAttribute("keyword", keyword);
        req.setAttribute("status", status);
        req.getRequestDispatcher("/WEB-INF/views/expert/course-list.jsp").forward(req, resp);
    }

    private void detail(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException, SQLException {
        int id = parseId(req.getParameter("id"));
        try {
            CourseDTO course = courseService.getCourseForExpert(id, currentExpertId(req));
            req.setAttribute("course", course);
        } catch (ServiceException e) {
            req.setAttribute("error", e.getMessage());
        }
        req.getRequestDispatcher("/WEB-INF/views/expert/course-detail.jsp").forward(req, resp);
    }

    private void create(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException, SQLException {
        CourseFormDTO dto = buildFormDTO(req, false);
        try {
            courseService.createCourse(currentExpertId(req), dto);
            resp.sendRedirect(req.getContextPath() + "/expert/courses");
        } catch (ServiceException e) {
            req.setAttribute("error", e.getMessage());
            req.setAttribute("formData", dto);
            req.getRequestDispatcher("/WEB-INF/views/expert/course-form.jsp").forward(req, resp);
        }
    }

    private void update(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException, SQLException {
        CourseFormDTO dto = buildFormDTO(req, true);
        try {
            courseService.updateCourse(currentExpertId(req), dto);
            resp.sendRedirect(req.getContextPath() + "/expert/courses/detail?id=" + dto.getId());
        } catch (ServiceException e) {
            req.setAttribute("error", e.getMessage());
            try {
                req.setAttribute("course", courseService.getCourseForExpert(dto.getId(), currentExpertId(req)));
            } catch (ServiceException ignored) {
                // fall through with error already set
            }
            req.getRequestDispatcher("/WEB-INF/views/expert/course-detail.jsp").forward(req, resp);
        }
    }

    private void delete(HttpServletRequest req, HttpServletResponse resp)
            throws IOException, SQLException {
        int id = parseId(req.getParameter("id"));
        try {
            courseService.deleteCourse(id, currentExpertId(req));
        } catch (ServiceException ignored) {
            // silently ignore - list page will simply not show it
        }
        resp.sendRedirect(req.getContextPath() + "/expert/courses");
    }

    private CourseFormDTO buildFormDTO(HttpServletRequest req, boolean withId) {
        CourseFormDTO dto = new CourseFormDTO();
        if (withId) {
            dto.setId(parseId(req.getParameter("id")));
        }
        dto.setTitle(req.getParameter("title"));
        dto.setDescription(req.getParameter("description"));
        dto.setCategory(req.getParameter("category"));
        dto.setStatus(req.getParameter("status"));
        try {
            dto.setDurationHours(Integer.parseInt(req.getParameter("durationHours")));
        } catch (Exception e) {
            dto.setDurationHours(0);
        }
        try {
            dto.setPrice(new java.math.BigDecimal(req.getParameter("price")));
        } catch (Exception e) {
            dto.setPrice(java.math.BigDecimal.ZERO);
        }
        return dto;
    }

    private int parseId(String raw) {
        try {
            return Integer.parseInt(raw);
        } catch (Exception e) {
            return -1;
        }
    }
}
