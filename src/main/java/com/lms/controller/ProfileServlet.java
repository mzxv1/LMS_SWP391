package com.lms.controller;

import com.lms.dto.UserDTO;
import com.lms.dto.UserProfileUpdateDTO;
import com.lms.service.ServiceException;
import com.lms.service.UserService;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.SQLException;

/** Protected by AuthFilter (see web.xml) - only a logged-in user can reach this. */
@WebServlet("/profile")
public class ProfileServlet extends HttpServlet {

    private final UserService userService = new UserService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        UserDTO sessionUser = (UserDTO) req.getSession().getAttribute("currentUser");
        try {
            UserDTO freshUser = userService.getProfile(sessionUser.getId());
            req.setAttribute("user", freshUser);
        } catch (ServiceException | SQLException e) {
            req.setAttribute("user", sessionUser);
        }
        forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        UserDTO sessionUser = (UserDTO) req.getSession().getAttribute("currentUser");

        UserProfileUpdateDTO dto = new UserProfileUpdateDTO();
        dto.setFullName(req.getParameter("fullName"));
        dto.setNewPassword(req.getParameter("newPassword"));
        dto.setConfirmNewPassword(req.getParameter("confirmNewPassword"));

        try {
            UserDTO updated = userService.updateProfile(sessionUser.getId(), dto);
            req.getSession().setAttribute("currentUser", updated); // keep session in sync
            req.setAttribute("success", "Cập nhật hồ sơ thành công.");
            req.setAttribute("user", updated);
        } catch (ServiceException e) {
            req.setAttribute("error", e.getMessage());
            req.setAttribute("user", sessionUser);
        } catch (SQLException e) {
            log("Profile update failed for userId=" + sessionUser.getId(), e);
            req.setAttribute("error", "Lỗi hệ thống, vui lòng thử lại sau.");
            req.setAttribute("user", sessionUser);
        }
        forward(req, resp);
    }

    private void forward(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        RequestDispatcher rd = req.getRequestDispatcher("/WEB-INF/views/profile/profile.jsp");
        rd.forward(req, resp);
    }
}
