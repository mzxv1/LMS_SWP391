package com.lms.controller;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.lms.entity.Payment;
import com.lms.service.EnrollmentService;
import com.lms.service.PaymentService;
import com.lms.util.SePayConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

@WebServlet("/sepay")
public class SePayServlet extends HttpServlet {
    private final PaymentService paymentService = new PaymentService();
    private final EnrollmentService enrollmentService = new EnrollmentService();
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws
            ServletException, IOException{
        String action = req.getParameter("action");
        String paymentIdStr = req.getParameter("paymentId");

        // 1. Logic tự động kiểm tra trạng thái
        if ("check".equals(action)) {
            handleAjaxCheck(req, resp, paymentIdStr);
            return;
        }

        String amountStr = req.getParameter("amount");
        if (paymentIdStr == null || amountStr == null) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Thiếu thông tin thanh toán");
            return;
        }
        String description = "PAY" + paymentIdStr;

        String bankName = SePayConfig.getBankName();
        String accountNumber = SePayConfig.getBankAccount();
        String courseId = req.getParameter("courseId");

        String qrUrl = "https://qr.sepay.vn/img?acc=" + accountNumber
                + "&bank=" + bankName
                + "&amount=" + amountStr
                + "&des=" + description;
        req.setAttribute("paymentId", paymentIdStr);
        req.setAttribute("amount", amountStr);
        req.setAttribute("description", description);
        req.setAttribute("qrUrl", qrUrl);
        req.setAttribute("bankName", bankName);
        req.setAttribute("accountNumber", accountNumber);
        req.setAttribute("courseId", courseId);
        req.getRequestDispatcher("/WEB-INF/views/course/bank-transfer.jsp").forward(req, resp);
    }

    private void handleAjaxCheck(HttpServletRequest req, HttpServletResponse resp, String paymentIdStr) throws IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        try {
            int paymentId = Integer.parseInt(paymentIdStr);
            Payment p = paymentService.getPaymentById(paymentId);

            if (p == null) {
                resp.getWriter().write("{\"status\": \"ERROR\"}");
                return;
            }

            // Nếu Database đã ghi nhận là thành công
            if ("SUCCESS".equals(p.getStatus())) {
                resp.getWriter().write("{\"status\": \"SUCCESS\"}");
                return;
            }

            // Nếu Database đã ghi nhận là thất bại
            if ("FAILED".equals(p.getStatus())) {
                resp.getWriter().write("{\"status\": \"FAILED\"}");
                return;
            }

            // GỌI API LÊN SEPAY
            String expectedContent = "PAY" + paymentId;
            boolean isPaid = checkTransactionFromSePay(expectedContent, p.getAmount());

            if (isPaid) {
                paymentService.updatePaymentStatus(paymentId, "SUCCESS");
                try {
                    enrollmentService.markAsPaid(p.getEnrollment_id());
                } catch (Exception e) {
                    e.printStackTrace();
                }
                resp.getWriter().write("{\"status\": \"SUCCESS\"}");
            } else {
                resp.getWriter().write("{\"status\": \"PENDING\"}");
            }

        } catch (Exception e) {
            e.printStackTrace();
            resp.getWriter().write("{\"status\": \"ERROR\"}");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String paymentIdStr = req.getParameter("paymentId");

        try {
            int paymentId = Integer.parseInt(paymentIdStr);
            Payment p = paymentService.getPaymentById(paymentId);

            if (p == null) {
                resp.sendError(404, "Payment không tồn tại");
                return;
            }
            // Nếu Database đã ghi nhận là thành công (c
            if ("SUCCESS".equals(p.getStatus())) {
                resp.sendRedirect(req.getContextPath() + "/payment-status?status=SUCCESS");
                return;
            }
            if ("FAILED".equals(p.getStatus())) {
                resp.sendRedirect(req.getContextPath() + "/payment-status?status=FAILED");
                return;
            }
            // Gọi API kiểm tra giao dịch SePay
            String expectedContent = "PAY" + paymentId;
            boolean isPaid = checkTransactionFromSePay(expectedContent, p.getAmount());
            if (isPaid) {
                // Xác nhận thanh toán thành công
                paymentService.updatePaymentStatus(paymentId, "SUCCESS");
                enrollmentService.markAsPaid(p.getEnrollment_id());
                resp.sendRedirect(req.getContextPath() + "/payment-status?status=SUCCESS");
            } else {
                // Chưa thấy giao dịch, quay lại Servlet theo dạng GET kèm tham số báo lỗi
                resp.sendRedirect(req.getContextPath() + "/sepay?paymentId=" + paymentId
                        + "&amount=" + p.getAmount() + "&error=NotFound");
            }
        } catch (Exception e) {
            e.printStackTrace();
            resp.sendError(500, "Lỗi Server");
        }
    }

    private boolean checkTransactionFromSePay(String expectedContent, double amount) {
        try {
            URL url = new URL("https://my.sepay.vn/userapi/transactions/list?limit=20");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            String apiToken = SePayConfig.getApiToken();
            conn.setRequestProperty("Authorization", "Bearer " + apiToken);
            conn.setRequestProperty("Content-Type", "application/json");
            int responseCode = conn.getResponseCode();
            System.out.println("[SePay API] Response Code: " + responseCode);
            if (responseCode == 200) {
                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    sb.append(line);
                }
                
                String jsonStr = sb.toString();
                System.out.println("[SePay API] JSON Response: " + jsonStr);
                
                JsonObject jsonResponse = JsonParser.parseString(jsonStr).getAsJsonObject();
                JsonArray transactions = jsonResponse.getAsJsonArray("transactions");
                if (transactions != null) {
                    System.out.println("[SePay API] Found " + transactions.size() + " transactions.");
                    for (JsonElement element : transactions) {
                        JsonObject txn = element.getAsJsonObject();
                        String content = txn.get("transaction_content") != null && !txn.get("transaction_content").isJsonNull() 
                                         ? txn.get("transaction_content").getAsString() : "";
                        double txnAmount = txn.get("amount_in") != null && !txn.get("amount_in").isJsonNull() 
                                           ? txn.get("amount_in").getAsDouble() : 0.0;
                        
                        System.out.println("[SePay API] Checking txn - Content: '" + content + "', Amount: " + txnAmount + " | Expected: '" + expectedContent + "', Amount: " + amount);
                        
                        if (content.toUpperCase().contains(expectedContent.toUpperCase()) && txnAmount >= amount) {
                            System.out.println("[SePay API] MATCH FOUND!");
                            return true;
                        }
                    }
                } else {
                    System.out.println("[SePay API] 'transactions' array is null or missing.");
                }
            } else {
                System.out.println("[SePay API] Lỗi gọi API. Vui lòng kiểm tra lại API Token trong sepay.properties!");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
