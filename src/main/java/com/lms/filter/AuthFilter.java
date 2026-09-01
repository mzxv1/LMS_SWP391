package com.lms.filter;

import com.lms.dto.UserDTO;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

/**
 * Blocks access to any URL it is mapped to (see web.xml) unless the
 * current HttpSession has a "currentUser" attribute (i.e. the user is logged in).
 * Not logged in -> redirect to /login with a "next" param to return afterwards.
 */
public class AuthFilter implements Filter {

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;

        HttpSession session = request.getSession(false);
        UserDTO currentUser = (session != null) ? (UserDTO) session.getAttribute("currentUser") : null;

        if (currentUser == null) {
            String contextPath = request.getContextPath();
            String next = request.getRequestURI().substring(contextPath.length());
            response.sendRedirect(contextPath + "/login?next=" + next);
            return;
        }

        chain.doFilter(req, res);
    }
}
