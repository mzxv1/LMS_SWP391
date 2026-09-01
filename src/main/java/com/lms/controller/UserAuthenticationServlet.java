package com.lms.controller;

import com.lms.dto.EmailVerificationRequestDTO;
import com.lms.dto.GoogleUserDTO;
import com.lms.dto.PasswordResetDTO;
import com.lms.dto.PasswordResetRequestDTO;
import com.lms.dto.UserDTO;
import com.lms.dto.UserLoginDTO;
import com.lms.dto.UserRegisterDTO;
import com.lms.service.ServiceException;
import com.lms.service.UserService;
import com.lms.util.AppConfig;
import com.lms.util.GoogleOAuthClient;
import com.lms.util.MailSender;
import com.lms.util.TokenUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.SQLException;

/**
 * All authentication entry points - NOT behind AuthFilter (see web.xml), these
 * must stay reachable by anonymous visitors.
 * Routes (dispatched on servlet path):
 *   GET  /login                  -> login form (redirects to /profile if already logged in)
 *   POST /login                  -> authenticate, then honour ?next= or fall back to /profile
 *   GET  /register               -> blank registration form
 *   POST /register               -> validate + email a confirmation link (account not created yet)
 *   GET  /register/confirm       -> validate ?token=, create the account, log the user in, -> /home
 *   GET  /logout                 -> invalidate session, back to /login
 *   GET  /login/google           -> generate state, stash next, redirect to Google
 *                                    (also handles first-time sign-up: an unknown but
 *                                    verified Google email auto-provisions a STUDENT account)
 *   GET  /login/google/callback  -> verify state, exchange code, log in (or provision), redirect
 *   GET  /forgot-password        -> email form
 *   POST /forgot-password        -> create token + send mail, always neutral message
 *   GET  /reset-password         -> validate ?token=, show new-password form
 *   POST /reset-password         -> apply new password, redirect to /login + flashSuccess
 */
@WebServlet({"/login", "/register", "/register/confirm", "/logout",
        "/login/google", "/login/google/callback",
        "/forgot-password", "/reset-password"})
public class UserAuthenticationServlet extends HttpServlet {

    private static final String LOGIN_VIEW = "/WEB-INF/views/auth/login.jsp";
    private static final String REGISTER_VIEW = "/WEB-INF/views/auth/register.jsp";
    private static final String FORGOT_PASSWORD_VIEW = "/WEB-INF/views/auth/forgot-password.jsp";
    private static final String RESET_PASSWORD_VIEW = "/WEB-INF/views/auth/reset-password.jsp";

    private static final String OAUTH_STATE_SESSION_KEY = "googleOAuthState";
    private static final String OAUTH_NEXT_SESSION_KEY = "googleOAuthNext";

    private static final String NEUTRAL_RESET_MESSAGE =
            "Nếu email tồn tại trong hệ thống, chúng tôi đã gửi liên kết đặt lại mật khẩu.";

    private final UserService userService = new UserService();
    private final GoogleOAuthClient googleOAuthClient = new GoogleOAuthClient();
    private final MailSender mailSender = new MailSender();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        switch (req.getServletPath()) {
            case "/login":
                showLoginForm(req, resp);
                break;
            case "/register":
                showRegisterForm(req, resp);
                break;
            case "/register/confirm":
                confirmRegistration(req, resp);
                break;
            case "/logout":
                logout(req, resp);
                break;
            case "/login/google":
                startGoogleLogin(req, resp);
                break;
            case "/login/google/callback":
                handleGoogleCallback(req, resp);
                break;
            case "/forgot-password":
                req.getRequestDispatcher(FORGOT_PASSWORD_VIEW).forward(req, resp);
                break;
            case "/reset-password":
                showResetPasswordForm(req, resp);
                break;
            default:
                resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        switch (req.getServletPath()) {
            case "/login":
                login(req, resp);
                break;
            case "/register":
                register(req, resp);
                break;
            case "/forgot-password":
                forgotPassword(req, resp);
                break;
            case "/reset-password":
                resetPassword(req, resp);
                break;
            default:
                resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    private void showLoginForm(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        // already logged in -> go straight to landing page
        HttpSession session = req.getSession(false);
        if (session != null && session.getAttribute("currentUser") != null) {
            resp.sendRedirect(req.getContextPath() + "/profile");
            return;
        }
        req.setAttribute("googleEnabled", googleOAuthClient.isConfigured());
        req.getRequestDispatcher(LOGIN_VIEW).forward(req, resp);
    }

    private void showRegisterForm(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.setAttribute("googleEnabled", googleOAuthClient.isConfigured());
        req.getRequestDispatcher(REGISTER_VIEW).forward(req, resp);
    }

    private void login(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        UserLoginDTO dto = new UserLoginDTO();
        dto.setUsername(req.getParameter("username"));
        dto.setPassword(req.getParameter("password"));
        String next = req.getParameter("next");

        try {
            UserDTO user = userService.login(dto);
            HttpSession session = req.getSession(true);
            session.setAttribute("currentUser", user);

            if (next != null && !next.isBlank() && next.startsWith("/")) {
                resp.sendRedirect(req.getContextPath() + next);
            } else {
                if(user.getRole() == "ADMIN")
                    resp.sendRedirect(req.getContextPath() + "/admin/dashboard");
                else if(user.getRole() == "EXPERT")
                    resp.sendRedirect(req.getContextPath() + "/expert/dashboard");
                else
                    resp.sendRedirect(req.getContextPath() + "/trainee/dashboard");
            }
        } catch (ServiceException e) {
            req.setAttribute("error", e.getMessage());
            req.setAttribute("username", dto.getUsername());
            req.setAttribute("googleEnabled", googleOAuthClient.isConfigured());
            req.getRequestDispatcher(LOGIN_VIEW).forward(req, resp);
        } catch (SQLException e) {
            log("Login failed for username=" + dto.getUsername(), e);
            req.setAttribute("error", "Lỗi hệ thống, vui lòng thử lại sau.");
            req.setAttribute("googleEnabled", googleOAuthClient.isConfigured());
            req.getRequestDispatcher(LOGIN_VIEW).forward(req, resp);
        }
    }

    private void register(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        UserRegisterDTO dto = new UserRegisterDTO();
        dto.setUsername(req.getParameter("username"));
        dto.setPassword(req.getParameter("password"));
        dto.setConfirmPassword(req.getParameter("confirmPassword"));
        dto.setEmail(req.getParameter("email"));
        dto.setFullName(req.getParameter("fullName"));

        try {
            // Nothing is written to "users" here - only a pending, single-use
            // confirmation token. The account is created in confirmRegistration()
            // once the emailed link is clicked.
            EmailVerificationRequestDTO pending = userService.registerPending(dto);

            String baseUrl = AppConfig.get("app.base.url",
                    req.getScheme() + "://" + req.getServerName() + ":" + req.getServerPort()
                            + req.getContextPath());
            String confirmLink = baseUrl + "/register/confirm?token=" + pending.getRawToken();
            mailSender.sendRegistrationConfirmation(
                    pending.getRecipientEmail(), pending.getRecipientName(), confirmLink);

            req.setAttribute("success",
                    "Chúng tôi đã gửi một liên kết xác nhận tới " + pending.getRecipientEmail()
                            + ". Vui lòng kiểm tra email và nhấn vào liên kết để hoàn tất đăng ký.");
            req.setAttribute("googleEnabled", googleOAuthClient.isConfigured());
            req.getRequestDispatcher(REGISTER_VIEW).forward(req, resp);
        } catch (ServiceException e) {
            req.setAttribute("error", e.getMessage());
            req.setAttribute("formData", dto);
            req.setAttribute("googleEnabled", googleOAuthClient.isConfigured());
            req.getRequestDispatcher(REGISTER_VIEW).forward(req, resp);
        } catch (SQLException e) {
            log("Registration failed for username=" + dto.getUsername(), e);
            req.setAttribute("error", "Lỗi hệ thống, vui lòng thử lại sau.");
            req.setAttribute("googleEnabled", googleOAuthClient.isConfigured());
            req.getRequestDispatcher(REGISTER_VIEW).forward(req, resp);
        }
    }

    /**
     * Confirmation-link landing point: creates the account for a still-valid
     * pending registration, logs the new user straight in, and sends them to
     * the home page - i.e. the request arrives with a brand-new session.
     */
    private void confirmRegistration(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String token = req.getParameter("token");
        try {
            UserDTO created = userService.confirmRegistration(token);

            HttpSession session = req.getSession(true);
            session.setAttribute("currentUser", created);
            session.setAttribute("flashSuccess",
                    "Xác nhận email thành công! Chào mừng " + created.getFullName() + " đến với LMS.");
            resp.sendRedirect(req.getContextPath() + "/home");
        } catch (ServiceException e) {
            req.setAttribute("error", e.getMessage());
            req.setAttribute("googleEnabled", googleOAuthClient.isConfigured());
            req.getRequestDispatcher(REGISTER_VIEW).forward(req, resp);
        } catch (SQLException e) {
            log("Registration confirmation failed for token=" + token, e);
            req.setAttribute("error", "Lỗi hệ thống, vui lòng thử lại sau.");
            req.setAttribute("googleEnabled", googleOAuthClient.isConfigured());
            req.getRequestDispatcher(REGISTER_VIEW).forward(req, resp);
        }
    }

    private void logout(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        HttpSession session = req.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        resp.sendRedirect(req.getContextPath() + "/login");
    }

    // ---------- Google sign-in ----------

    private void startGoogleLogin(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        if (!googleOAuthClient.isConfigured()) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        String state = TokenUtil.generateToken();
        HttpSession session = req.getSession(true);
        session.setAttribute(OAUTH_STATE_SESSION_KEY, state);

        String next = req.getParameter("next");
        if (next != null && next.startsWith("/")) {
            session.setAttribute(OAUTH_NEXT_SESSION_KEY, next);
        } else {
            session.removeAttribute(OAUTH_NEXT_SESSION_KEY);
        }

        resp.sendRedirect(googleOAuthClient.buildAuthorizeUrl(state));
    }

    private void handleGoogleCallback(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        String expectedState = session != null ? (String) session.getAttribute(OAUTH_STATE_SESSION_KEY) : null;
        String actualState = req.getParameter("state");

        if (session == null || expectedState == null || !expectedState.equals(actualState)) {
            req.setAttribute("error", "Phiên đăng nhập Google không hợp lệ, vui lòng thử lại.");
            req.setAttribute("googleEnabled", googleOAuthClient.isConfigured());
            req.getRequestDispatcher(LOGIN_VIEW).forward(req, resp);
            return;
        }
        session.removeAttribute(OAUTH_STATE_SESSION_KEY);

        String code = req.getParameter("code");
        if (code == null || code.isBlank()) {
            req.setAttribute("error", "Đăng nhập Google đã bị hủy hoặc thất bại.");
            req.setAttribute("googleEnabled", googleOAuthClient.isConfigured());
            req.getRequestDispatcher(LOGIN_VIEW).forward(req, resp);
            return;
        }

        try {
            GoogleUserDTO profile = googleOAuthClient.exchangeCodeForProfile(code);
            UserDTO user = userService.loginWithGoogle(profile);
            session.setAttribute("currentUser", user);

            String next = (String) session.getAttribute(OAUTH_NEXT_SESSION_KEY);
            session.removeAttribute(OAUTH_NEXT_SESSION_KEY);
            if (next != null && !next.isBlank() && next.startsWith("/")) {
                resp.sendRedirect(req.getContextPath() + next);
            } else {
                resp.sendRedirect(req.getContextPath() + "/profile");
            }
        } catch (IOException e) {
            log("Google OAuth exchange failed", e);
            req.setAttribute("error", "Không thể kết nối với Google, vui lòng thử lại sau.");
            req.setAttribute("googleEnabled", googleOAuthClient.isConfigured());
            req.getRequestDispatcher(LOGIN_VIEW).forward(req, resp);
        } catch (ServiceException e) {
            req.setAttribute("error", e.getMessage());
            req.setAttribute("googleEnabled", googleOAuthClient.isConfigured());
            req.getRequestDispatcher(LOGIN_VIEW).forward(req, resp);
        } catch (SQLException e) {
            log("Google login failed", e);
            req.setAttribute("error", "Lỗi hệ thống, vui lòng thử lại sau.");
            req.setAttribute("googleEnabled", googleOAuthClient.isConfigured());
            req.getRequestDispatcher(LOGIN_VIEW).forward(req, resp);
        }
    }

    // ---------- Forgot / reset password ----------

    private void forgotPassword(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String email = req.getParameter("email");

        try {
            PasswordResetRequestDTO resetRequest = userService.createPasswordResetToken(email);
            if (resetRequest != null) {
                // app.base.url already includes the context path (see app.properties).
                String baseUrl = AppConfig.get("app.base.url",
                        req.getScheme() + "://" + req.getServerName() + ":" + req.getServerPort()
                                + req.getContextPath());
                String resetLink = baseUrl + "/reset-password?token=" + resetRequest.getRawToken();
                mailSender.sendPasswordReset(
                        resetRequest.getRecipientEmail(), resetRequest.getRecipientName(), resetLink);
            }
            // Same message whether or not the address exists - no user enumeration.
            req.setAttribute("success", NEUTRAL_RESET_MESSAGE);
            req.getRequestDispatcher(FORGOT_PASSWORD_VIEW).forward(req, resp);
        } catch (SQLException e) {
            log("Forgot-password request failed for email=" + email, e);
            req.setAttribute("error", "Lỗi hệ thống, vui lòng thử lại sau.");
            req.getRequestDispatcher(FORGOT_PASSWORD_VIEW).forward(req, resp);
        }
    }

    private void showResetPasswordForm(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String token = req.getParameter("token");
        try {
            userService.validateResetToken(token);
            req.setAttribute("token", token);
            req.getRequestDispatcher(RESET_PASSWORD_VIEW).forward(req, resp);
        } catch (ServiceException e) {
            req.setAttribute("error", e.getMessage());
            req.getRequestDispatcher(RESET_PASSWORD_VIEW).forward(req, resp);
        } catch (SQLException e) {
            log("Reset-password token validation failed", e);
            req.setAttribute("error", "Lỗi hệ thống, vui lòng thử lại sau.");
            req.getRequestDispatcher(RESET_PASSWORD_VIEW).forward(req, resp);
        }
    }

    private void resetPassword(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        PasswordResetDTO dto = new PasswordResetDTO();
        dto.setToken(req.getParameter("token"));
        dto.setNewPassword(req.getParameter("newPassword"));
        dto.setConfirmPassword(req.getParameter("confirmPassword"));

        try {
            userService.resetPassword(dto);
            req.getSession().setAttribute("flashSuccess",
                    "Đặt lại mật khẩu thành công! Vui lòng đăng nhập bằng mật khẩu mới.");
            resp.sendRedirect(req.getContextPath() + "/login");
        } catch (ServiceException e) {
            req.setAttribute("error", e.getMessage());
            req.setAttribute("token", dto.getToken());
            req.getRequestDispatcher(RESET_PASSWORD_VIEW).forward(req, resp);
        } catch (SQLException e) {
            log("Reset-password failed", e);
            req.setAttribute("error", "Lỗi hệ thống, vui lòng thử lại sau.");
            req.setAttribute("token", dto.getToken());
            req.getRequestDispatcher(RESET_PASSWORD_VIEW).forward(req, resp);
        }
    }
}
