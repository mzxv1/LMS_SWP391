package com.lms.service;

import jakarta.servlet.http.HttpServletRequest;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

public class VNPayService {
    private static final String vnp_TmnCode = "PCIBXK3Y";
    private static final String vnp_HashSecret = "QJYYUIHWKGKWHOUYSOVPNKROJIGUNRUN";
    private static final String vnp_PayUrl = "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html";
    private static final String vnp_ReturnUrl = "http://localhost:8080/lms_webapp_war/vnpay/return";

    public String createPaymentUrl(int paymentId, double amount, HttpServletRequest req){
        String vnp_Version = "2.1.0";
        String vnp_Command = "pay";
        String vnp_OrderInfo = "Thanh toan khoa hoc " + paymentId;
        String orderType = "other";
        String vnp_TxnRef = String.valueOf(paymentId) + System.currentTimeMillis();
        String vnp_IpAddr  = "127.0.0.1";

        long vnp_Amount = (long) (amount * 100);

        Map<String, String> vnp_Params = new HashMap<>();
        vnp_Params.put("vnp_Version", vnp_Version);
        vnp_Params.put("vnp_Command", vnp_Command);
        vnp_Params.put("vnp_TmnCode", vnp_TmnCode);
        vnp_Params.put("vnp_Amount", String.valueOf(vnp_Amount));
        vnp_Params.put("vnp_CurrCode", "VND");
        vnp_Params.put("vnp_TxnRef", vnp_TxnRef);
        vnp_Params.put("vnp_OrderInfo", vnp_OrderInfo);
        vnp_Params.put("vnp_OrderType", orderType);
        vnp_Params.put("vnp_Locale", "vn");
        vnp_Params.put("vnp_ReturnUrl", vnp_ReturnUrl);
        vnp_Params.put("vnp_IpAddr", vnp_IpAddr);

        Calendar cld = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        String vnp_CreateDate = formatter.format(cld.getTime());
        vnp_Params.put("vnp_CreateDate", vnp_CreateDate);

        cld.add(Calendar.MINUTE, 15);
        String vnp_ExpireDate = formatter.format(cld.getTime());
        vnp_Params.put("vnp_ExpireDate", vnp_ExpireDate);

        List<String> fieldNames = new ArrayList<>(vnp_Params.keySet());
        Collections.sort(fieldNames);
        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();

        try {
            for (String fieldName : fieldNames){
                String fieldValue = vnp_Params.get(fieldName);
                if(fieldValue != null && fieldValue.length() > 0){
                    hashData.append(fieldName).append('=').append(URLEncoder.encode(fieldValue,
                            StandardCharsets.US_ASCII.toString()));
                    query.append(URLEncoder.encode(fieldName, StandardCharsets.US_ASCII.toString())).append('=')
                            .append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));
                    if(!fieldName.equals(fieldNames.get(fieldNames.size() - 1))){
                        query.append('&');
                        hashData.append('&');
                    }
                }
            }
            String queryUrl = query.toString();
            String vnp_SecureHash = hmacSHA512(vnp_HashSecret, hashData.toString());
            queryUrl += "&vnp_SecureHash=" + vnp_SecureHash;
            return vnp_PayUrl + "?" + queryUrl;
        }
        catch (Exception e){
            throw new RuntimeException("Error generating VNPay URL", e);
        }
    }

    public static String hmacSHA512(String key, String data){
        try {
            Mac hmac512 = Mac.getInstance("HmacSHA512");
            SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(), "HmacSHA512");
            hmac512.init(secretKey);
            byte[] result = hmac512.doFinal(data.getBytes());
            StringBuilder sb = new StringBuilder(2 * result.length);
            for (byte b : result){
                sb.append(String.format("%02x", b & 0xff));
            }
            return sb.toString();
        }
        catch (Exception e){
            return "";
        }
    }

    public String hashAllFields(Map<String, String> fields) {
        List<String> fieldNames = new ArrayList<>(fields.keySet());
        Collections.sort(fieldNames);
        StringBuilder sb = new StringBuilder();
        for (String fieldName : fieldNames) {
            String fieldValue = fields.get(fieldName);
            if ((fieldValue != null) && (fieldValue.length() > 0)) {
                sb.append(fieldName).append("=").append(fieldValue);
                if (!fieldName.equals(fieldNames.get(fieldNames.size() - 1))) {
                    sb.append("&");
                }
            }
        }
        return hmacSHA512(vnp_HashSecret, sb.toString());
    }

    public enum ReturnStatus {
        SUCCESS,
        FAILED,
        INVALID_SIGNATURE
    }

    public ReturnStatus processReturnUrl(Map<String, String> fields, String secureHash, String responseCode, String txnRefStr, PaymentService paymentService, EnrollmentService enrollmentService) {
        String signValue = hashAllFields(fields);
        
        if (signValue.equals(secureHash)) {
            if ("00".equals(responseCode)) {
                try {
                    int paymentId = Integer.parseInt(txnRefStr.substring(0, txnRefStr.length() - 13));
                    com.lms.entity.Payment p = paymentService.getPaymentById(paymentId);
                    if (p != null && "PENDING".equals(p.getStatus())) {
                        paymentService.updatePaymentStatus(paymentId, "SUCCESS");
                        enrollmentService.markAsPaid(p.getEnrollment_id());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    return ReturnStatus.FAILED;
                }
                return ReturnStatus.SUCCESS;
            } else {
                try {
                    int paymentId = Integer.parseInt(txnRefStr.substring(0, txnRefStr.length() - 13));
                    com.lms.entity.Payment p = paymentService.getPaymentById(paymentId);
                    if (p != null && "PENDING".equals(p.getStatus())) {
                        paymentService.updatePaymentStatus(paymentId, "FAILED");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return ReturnStatus.FAILED;
            }
        } else {
            return ReturnStatus.INVALID_SIGNATURE;
        }
    }
}
