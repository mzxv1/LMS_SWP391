package com.lms.dao;

import com.lms.entity.Payment;
import com.lms.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class PaymentDAO {
    public int createPayment(int userId, int enrollmentId, String method, double amount) throws SQLException{
        String sql = "Insert into payments (user_id, enrollment_id, payment_method, amount, status) " +
                "Values (?, ?, ?, ?, 'PENDING') Returning id";
        try {
            Connection con = DBConnection.getConnection();
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setInt(1, userId);
            ps.setInt(2, enrollmentId);
            ps.setString(3, method);
            ps.setDouble(4, amount);
            ResultSet rs = ps.executeQuery();
            if(rs.next())
                return rs.getInt("id");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }

    public Payment findById(int id) throws SQLException{
        String sql = "Select * From payments where id = ?";
        try {
            Connection con = DBConnection.getConnection();
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()){
                Payment p = new Payment();
                p.setId(rs.getInt("id"));
                p.setUser_id(rs.getInt("user_id"));
                p.setEnrollment_id(rs.getInt("enrollment_id"));
                p.setPayment_method(rs.getString("payment_method"));
                p.setAmount(rs.getDouble("amount"));
                p.setStatus(rs.getString("status"));
                return p;
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    public void updateStatus(int paymentId, String status) throws SQLException {
        String sql = "UPDATE payments SET status = ? WHERE id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, paymentId);
            ps.executeUpdate();
        }
    }

    /**
     * Total revenue collected system-wide from SUCCESS payments.
     * Used by the Admin Dashboard summary cards.
     */
    public double sumRevenue() throws SQLException {
        String sql = "SELECT COALESCE(SUM(amount), 0) FROM payments WHERE status = 'SUCCESS'";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getDouble(1) : 0;
        }
    }

    /**
     * Total revenue earned from SUCCESS payments across one expert's own
     * courses. Used by the Expert Dashboard summary cards.
     */
    public double sumRevenueForExpert(int expertId) throws SQLException {
        String sql =
                "SELECT COALESCE(SUM(p.amount), 0) " +
                        "FROM payments p " +
                        "JOIN enrollments e ON e.id = p.enrollment_id " +
                        "JOIN courses c ON c.id = e.course_id " +
                        "WHERE c.expert_id = ? AND p.status = 'SUCCESS'";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, expertId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getDouble(1) : 0;
            }
        }
    }
}
