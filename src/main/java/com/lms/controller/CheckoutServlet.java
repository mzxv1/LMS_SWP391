package com.lms.controller;

import com.lms.dto.CourseDetailDTO;
import com.lms.dto.UserDTO;
import com.lms.service.CourseService;
import com.lms.service.EnrollmentService;
import com.lms.service.PaymentService;
import com.lms.service.VNPayService;
import com.lms.service.UserService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;

@WebServlet({"/checkout", "/checkout/process"})
public class CheckoutServlet extends HttpServlet {
    private final CourseService courseService = new CourseService();
    private final EnrollmentService enrollmentService = new EnrollmentService();
    private final PaymentService paymentService = new PaymentService();
    private final VNPayService vnPayService = new VNPayService();
    private final UserService userService = new UserService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws
            ServletException, IOException{
        HttpSession session = req.getSession();
        UserDTO u = (UserDTO)session.getAttribute("currentUser");
        if(u == null || !u.getRole().equals("STUDENT")){
            resp.sendRedirect(req.getContextPath()+"/login");
            return;
        }
        try {
            int courseId = Integer.parseInt(req.getParameter("id"));
            CourseDetailDTO c = courseService.getPublicCourseDetail(courseId);
            req.setAttribute("course", c);

            req.getRequestDispatcher("/WEB-INF/views/course/checkout.jsp").forward(req, resp);
        } catch (NumberFormatException | com.lms.service.ServiceException e){
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Không tìm thấy khóa học");
        } catch (Exception e){
            e.printStackTrace();
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Lỗi hệ thống");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws
            ServletException, IOException{
        HttpSession session = req.getSession();
        UserDTO u = (UserDTO) session.getAttribute("currentUser");
        if(u == null){
            resp.sendRedirect(req.getContextPath()+"/login");
            return;
        }
        try {
            int courseId = Integer.parseInt(req.getParameter("courseId"));
            String paymentMethod = req.getParameter("paymentMethod");
            String email = req.getParameter("email");

            UserDTO targetUser = userService.getUserByEmail(email);
            if (targetUser == null) {
                resp.sendRedirect(req.getContextPath()+"/checkout?id="+courseId+"&error=" + java.net.URLEncoder.encode("Người dùng với email được đăng ký không tồn tại.", "UTF-8"));
                return;
            }

            CourseDetailDTO c = courseService.getPublicCourseDetail(courseId);

            int enrollmentId = enrollmentService.createPendingEnrollment(targetUser.getId(), courseId, c.getPrice().doubleValue());
            if (enrollmentId == -1) {
                throw new java.sql.SQLException("Người dùng này đã đăng ký khóa học.");
            }
            
            // Thanh toán được lưu cho người đang đăng nhập (người trả tiền)
            int paymentId = paymentService.createPendingPayment(u.getId(), enrollmentId, paymentMethod, c.getPrice().doubleValue());

            if(paymentMethod.equals("VNPAY")){
                String paymentUrl = vnPayService.createPaymentUrl(paymentId, c.getPrice().doubleValue(), req);
                resp.sendRedirect(paymentUrl);
            } else if (paymentMethod.equals("SEPAY")) {
                // Đã chuyển logic sang SePayServlet theo guide
                resp.sendRedirect(req.getContextPath()+"/sepay?paymentId=" + paymentId + "&amount=" + c.getPrice() + "&courseId=" + courseId);
            } else {
                resp.sendRedirect(req.getContextPath()+"/checkout?id="+courseId+"&error=InvalidMethod");
            }
        } catch (java.sql.SQLException sqle) {
            sqle.printStackTrace();
            if (sqle.getMessage() != null && (sqle.getMessage().contains("already enrolled") || sqle.getMessage().contains("Người dùng này đã đăng ký khóa học."))) {
                resp.sendRedirect(req.getContextPath()+"/checkout?id="+req.getParameter("courseId")+"&error=" + java.net.URLEncoder.encode(sqle.getMessage(), "UTF-8"));
            } else {
                resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Lỗi cơ sở dữ liệu");
            }
        } catch (Exception e) {
            e.printStackTrace();
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Có lỗi xảy ra khi xử lý thanh toán");
        }
    }
}
