package com.lms.controller;

import com.lms.service.EnrollmentService;
import com.lms.service.PaymentService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

import com.lms.service.VNPayService;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

@WebServlet({"/vnpay/ipn", "/vnpay/return"})
public class VNPayServlet extends HttpServlet {
    private final EnrollmentService enrollmentService = new EnrollmentService();
    private final PaymentService paymentService = new PaymentService();
    private final VNPayService vnPayService = new VNPayService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws
            ServletException, IOException{
        String path = req.getServletPath();

        if ("/vnpay/return".equals(path)) {
            handleReturn(req, resp);
        } else {
            handleIpn(req, resp);
        }
    }

    private void handleReturn(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Map<String, String> fields = new HashMap<>();
        for (Enumeration<String> params = req.getParameterNames(); params.hasMoreElements(); ) {
            String fieldName = URLEncoder.encode(params.nextElement(), StandardCharsets.US_ASCII.toString());
            String fieldValue = URLEncoder.encode(req.getParameter(fieldName), StandardCharsets.US_ASCII.toString());
            if ((fieldValue != null) && (fieldValue.length() > 0)) {
                fields.put(fieldName, fieldValue);
            }
        }

        String vnp_SecureHash = req.getParameter("vnp_SecureHash");
        if (fields.containsKey("vnp_SecureHashType")) fields.remove("vnp_SecureHashType");
        if (fields.containsKey("vnp_SecureHash")) fields.remove("vnp_SecureHash");

        String responseCode = req.getParameter("vnp_ResponseCode");
        String txnRefStr = req.getParameter("vnp_TxnRef");

        VNPayService.ReturnStatus status = vnPayService.processReturnUrl(fields, vnp_SecureHash, responseCode, txnRefStr, paymentService, enrollmentService);

        if (status == VNPayService.ReturnStatus.SUCCESS) {
            resp.sendRedirect(req.getContextPath() + "/payment-status?status=SUCCESS");
        } else if (status == VNPayService.ReturnStatus.FAILED) {
            resp.sendRedirect(req.getContextPath() + "/payment-status?status=FAILED");
        } else {
            resp.sendRedirect(req.getContextPath() + "/payment-status?status=FAILED&message=Invalid_Signature");
        }
    }

    private void handleIpn(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String responseCode = req.getParameter("vnp_ResponseCode");
        String txnRefStr = req.getParameter("vnp_TxnRef");

        try {
            int paymentId = Integer.parseInt(txnRefStr.substring(0, txnRefStr.length() - 13));

            if("00".equals(responseCode)){
                System.out.println("VNPay IPN: Payment Success for " + txnRefStr);

                paymentService.updatePaymentStatus(paymentId, "SUCCESS");
                com.lms.entity.Payment p = paymentService.getPaymentById(paymentId);
                if (p != null) {
                    enrollmentService.markAsPaid(p.getEnrollment_id());
                }

                resp.getWriter().write("{\"RspCode\":\"00\",\"Message\":\"Confirm Success\"}");
            } else {
                paymentService.updatePaymentStatus(paymentId, "FAILED");
                resp.getWriter().write("{\"RspCode\":\"00\",\"Message\":\"Confirm Success\"}");
            }
        } catch (Exception e) {
            e.printStackTrace();
            resp.getWriter().write("{\"RspCode\":\"99\",\"Message\":\"Unknown error\"}");
        }
    }
}
