package com.lms.dto;


import java.sql.Date;

public class EnrollmentHistoryDto {
    private int enrollmentId;
    private int courseId;
    private int paymentId;
    private String title;
    private double price;
    private String payment_method;
    private String status;
    private Date enroll_at;

    public int getEnrollmentId() {
        return enrollmentId;
    }

    public void setEnrollmentId(int enrollmentId) {
        this.enrollmentId = enrollmentId;
    }

    public int getCourseId() {
        return courseId;
    }

    public void setCourseId(int courseId) {
        this.courseId = courseId;
    }

    public int getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(int paymentId) {
        this.paymentId = paymentId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getPayment_method() {
        return payment_method;
    }

    public void setPayment_method(String payment_method) {
        this.payment_method = payment_method;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Date getEnroll_at() {
        return enroll_at;
    }

    public void setEnroll_at(Date enroll_at) {
        this.enroll_at = enroll_at;
    }
}
