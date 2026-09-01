package com.lms.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet("/payment-status")
public class PaymentStatusServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // Có thể lấy các tham số nếu cần thiết, ví dụ paymentId để query DB hiển thị chi tiết
        req.getRequestDispatcher("/WEB-INF/views/course/payment-status.jsp").forward(req, resp);
    }
}
