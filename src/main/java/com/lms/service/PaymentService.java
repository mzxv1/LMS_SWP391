package com.lms.service;

import com.lms.dao.PaymentDAO;
import com.lms.entity.Payment;

import java.sql.SQLException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class PaymentService {
    private final PaymentDAO dao = new PaymentDAO();
    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(10);

    public int createPendingPayment(int userId, int enrollmentId, String method, double amount) throws SQLException {
        int paymentId = dao.createPayment(userId, enrollmentId, method, amount);
        
        if ("SEPAY".equalsIgnoreCase(method)) {
            scheduler.schedule(() -> {
                try {
                    Payment p = dao.findById(paymentId);
                    if (p != null && "PENDING".equalsIgnoreCase(p.getStatus())) {
                        dao.updateStatus(paymentId, "FAILED");
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }, 30, TimeUnit.SECONDS);
        } else if ("VNPAY".equalsIgnoreCase(method)) {
            scheduler.schedule(() -> {
                try {
                    Payment p = dao.findById(paymentId);
                    if (p != null && "PENDING".equalsIgnoreCase(p.getStatus())) {
                        dao.updateStatus(paymentId, "FAILED");
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }, 1, TimeUnit.MINUTES);
        }
        
        return paymentId;
    }
    
    public Payment getPaymentById(int id) throws SQLException {
        return dao.findById(id);
    }
    
    public void updatePaymentStatus(int paymentId, String status) throws SQLException {
        dao.updateStatus(paymentId, status);
    }
}
