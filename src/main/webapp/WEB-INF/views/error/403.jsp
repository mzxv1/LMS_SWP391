<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" isErrorPage="true" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <title>403 - Không có quyền truy cập</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
</head>
<body class="d-flex align-items-center justify-content-center vh-100 bg-light">
    <div class="text-center">
        <h1 class="display-1 fw-bold text-danger">403</h1>
        <p class="fs-4">Bạn không có quyền truy cập trang này.</p>
        <a href="<%= request.getContextPath() %>/profile" class="btn btn-primary mt-3">Về trang hồ sơ</a>
    </div>
</body>
</html>
