package com.lms.controller;

import com.lms.dto.EnrollmentHistoryDto;
import com.lms.dto.Page;
import com.lms.dto.UserDTO;
import com.lms.service.EnrollmentService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.List;

@WebServlet("/enrollment-history")
public class EnrollmentServlet extends HttpServlet {
    private final EnrollmentService enrollmentService = new EnrollmentService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession();
        UserDTO user = (UserDTO) session.getAttribute("currentUser");

        if (user == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }
        String pageStr = req.getParameter("page");
        int page = 1;
        if (pageStr != null && !pageStr.isEmpty()) {
            try {
                page = Integer.parseInt(pageStr);
            } catch (NumberFormatException ignored) {}
        }
        int size = 10;

        com.lms.dto.Page<EnrollmentHistoryDto> historyPage = enrollmentService.getEnrollmentHistoryPage(user.getId(), page, size);
        
        req.setAttribute("historyList", historyPage.getContent());
        req.setAttribute("currentPage", historyPage.getPage());
        req.setAttribute("totalPages", historyPage.getTotalPages());

        // Render ra file JSP
        req.getRequestDispatcher("/WEB-INF/views/course/enrollment-history.jsp").forward(req, resp);
    }
}
