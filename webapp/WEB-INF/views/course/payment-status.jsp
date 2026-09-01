<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>Trạng thái thanh toán</title>
    <!-- Include Bootstrap or your CSS here if needed -->
    <style>
        .status-container {
            text-align: center;
            margin-top: 80px;
            font-family: Arial, sans-serif;
        }
        .icon-success {
            font-size: 80px;
            color: #28a745;
            margin-bottom: 20px;
        }
        .icon-error {
            font-size: 80px;
            color: #dc3545;
            margin-bottom: 20px;
        }
        .btn-history {
            display: inline-block;
            margin-top: 30px;
            padding: 12px 24px;
            background-color: #007bff;
            color: white;
            text-decoration: none;
            border-radius: 5px;
            font-size: 16px;
        }
        .btn-history:hover {
            background-color: #0056b3;
        }
    </style>
</head>
<body>
    <jsp:include page="../common/header.jsp" />

    <div class="status-container">
        <% 
            String status = request.getParameter("status");
            if ("SUCCESS".equals(status)) { 
        %>
            <div class="icon-success">✔</div>
            <h1 style="color: #28a745;">Thanh toán thành công!</h1>
            <p style="font-size: 18px; color: #555;">Cảm ơn bạn. Đơn hàng của bạn đã được ghi nhận và khóa học đã được kích hoạt.</p>
            
            <!-- Nút điều hướng về trang lịch sử đăng ký -->
            <a href="${pageContext.request.contextPath}/enrollment-history" class="btn-history">Xem lịch sử đăng ký khóa học</a>
        
        <% } else { %>
            <div class="icon-error">✖</div>
            <h1 style="color: #dc3545;">Thanh toán chưa hoàn tất</h1>
            <p style="font-size: 18px; color: #555;">Hệ thống chưa ghi nhận được thanh toán của bạn hoặc đã có lỗi xảy ra.</p>
            
            <a href="${pageContext.request.contextPath}/" class="btn-history" style="background-color: #6c757d;">Quay về trang chủ</a>
        <% } %>
    </div>
</body>
</html>
