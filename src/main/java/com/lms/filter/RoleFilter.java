package com.lms.filter;

import com.lms.dto.UserDTO;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

/**
 * Runs AFTER AuthFilter (see ordering in web.xml), so currentUser is guaranteed
 * to be present. Checks that the logged-in user's role matches the required
 * role for the URL prefix being accessed (/admin/** -> ADMIN, /expert/** -> EXPERT).
 * Mismatch -> HTTP 403.
 */
public class RoleFilter implements Filter {

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;

        HttpSession session = request.getSession(false);
        UserDTO currentUser = (session != null) ? (UserDTO) session.getAttribute("currentUser") : null;

        String uri = request.getRequestURI().substring(request.getContextPath().length());
        String requiredRole = uri.startsWith("/admin") ? "ADMIN"
                : uri.startsWith("/expert") ? "EXPERT"
                : uri.startsWith("/trainee") ? "STUDENT"
                : null;

        if (requiredRole != null && (currentUser == null || !requiredRole.equals(currentUser.getRole()))) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Bạn không có quyền truy cập trang này.");
            return;
        }

        chain.doFilter(req, res);
    }
}
