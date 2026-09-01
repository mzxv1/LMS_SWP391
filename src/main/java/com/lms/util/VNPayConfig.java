package com.lms.util;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class VNPayConfig {
    public static String vnp_TmnCode = "PCIBXK3Y";
    public static String vnp_HashSecret = "QJYYUIHWKGKWHOUYSOVPNKROJIGUNRUN";
    public static String vnp_PayUrl = "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html\n";
    public static String vnp_ReturnUrl = "http://localhost:8080/vnpay-return";
    public static String vnp_apiUrl = "https://sandbox.vnpayment.vn/merchant_webapi/api/transaction";

    public static String hashAllFields(Map<String, String> fields){
        List<String> fieldNames = new ArrayList<>(fields.keySet());
        Collections.sort(fieldNames);
        StringBuilder sb = new StringBuilder();
        Iterator<String> itr = fieldNames.iterator();
        while (itr.hasNext()){
            String fieldName = itr.next();
            String fieldValue = fields.get(fieldName);
            if((fieldValue != null) && (fieldValue.length() > 0)){
                sb.append(fieldName).append("=").append(fieldValue);
            }
            if(itr.hasNext()){
                sb.append("&");
            }
        }
        return hmacSHA512(vnp_HashSecret, sb.toString());
    }

    public static String hmacSHA512(String key, String data){
        try {
            Mac hmac512 = Mac.getInstance("HmacSHA512");
            SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(), "HmacSHA512");
            hmac512.init(secretKey);
            byte[] result = hmac512.doFinal(data.getBytes(StandardCharsets.UTF_8));
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
}
