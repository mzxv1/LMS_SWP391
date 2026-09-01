<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <title>Lịch sử đăng ký khóa học</title>
    <!-- Thêm Bootstrap 5 & FontAwesome -->
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700&display=swap" rel="stylesheet">
    <style>
        body {
            font-family: 'Inter', sans-serif;
            background-color: #f8fafc;
            color: #334155;
        }
        .history-container {
            max-width: 1200px;
            margin: 40px auto;
            padding: 0 15px;
        }
        .page-title {
            font-weight: 700;
            color: #0f172a;
            margin-bottom: 30px;
            font-size: 2rem;
            position: relative;
            padding-bottom: 10px;
        }
        .page-title::after {
            content: '';
            width: 80px;
            height: 4px;
            background: linear-gradient(90deg, #3b82f6, #6366f1);
            position: absolute;
            bottom: 0;
            left: 0;
            border-radius: 2px;
        }

        /* Table Styles */
        .table-card {
            background: #ffffff;
            border-radius: 16px;
            box-shadow: 0 4px 20px rgba(0, 0, 0, 0.05);
            padding: 20px;
            overflow: hidden;
        }
        .table {
            margin-bottom: 0;
            border-collapse: separate;
            border-spacing: 0 12px;
        }
        .table thead th {
            border-bottom: none;
            color: #64748b;
            font-weight: 600;
            text-transform: uppercase;
            font-size: 0.85rem;
            letter-spacing: 0.5px;
            padding: 12px 20px;
        }
        .table tbody tr {
            box-shadow: 0 2px 10px rgba(0, 0, 0, 0.02);
            transition: transform 0.2s, box-shadow 0.2s;
        }
        .table tbody tr:hover {
            transform: translateY(-2px);
            box-shadow: 0 5px 15px rgba(0, 0, 0, 0.08);
        }
        .table tbody td {
            background-color: #ffffff;
            border-top: 1px solid #f1f5f9;
            border-bottom: 1px solid #f1f5f9;
            padding: 20px;
            vertical-align: middle;
            color: #334155;
        }
        .table tbody td:first-child {
            border-left: 1px solid #f1f5f9;
            border-top-left-radius: 12px;
            border-bottom-left-radius: 12px;
        }
        .table tbody td:last-child {
            border-right: 1px solid #f1f5f9;
            border-top-right-radius: 12px;
            border-bottom-right-radius: 12px;
        }
        
        .course-name {
            font-weight: 600;
            font-size: 1.1rem;
            color: #0f172a;
        }
        .price-highlight {
            color: #059669;
            font-weight: 600;
        }

        /* Status Badges */
        .status-badge {
            padding: 6px 14px;
            border-radius: 20px;
            font-weight: 600;
            font-size: 0.85rem;
            text-transform: uppercase;
            display: inline-flex;
            align-items: center;
            gap: 6px;
        }
        .status-success { background-color: #dcfce7; color: #166534; }
        .status-pending { background-color: #fef9c3; color: #854d0e; }
        .status-failed  { background-color: #fee2e2; color: #991b1b; }

        /* Action Buttons */
        .btn-action {
            padding: 8px 16px;
            border-radius: 8px;
            font-weight: 500;
            font-size: 0.9rem;
            transition: all 0.2s;
            text-decoration: none;
            display: inline-block;
        }
        .btn-learn {
            background-color: #eff6ff;
            color: #2563eb;
            border: 1px solid #bfdbfe;
        }
        .btn-learn:hover {
            background-color: #3b82f6;
            color: #ffffff;
        }
        .btn-pay {
            background-color: #fffbeb;
            color: #d97706;
            border: 1px solid #fde68a;
        }
        .btn-pay:hover {
            background-color: #f59e0b;
            color: #ffffff;
        }

        /* Empty State */
        .empty-state {
            text-align: center;
            padding: 60px 20px;
        }
        .empty-state i {
            font-size: 4rem;
            color: #cbd5e1;
            margin-bottom: 20px;
        }
    </style>
</head>
<body>
    <jsp:include page="../common/header.jsp" />

    <div class="history-container">
        <h2 class="page-title">Lịch sử đăng ký khóa học</h2>
        
        <div class="table-card">
            <c:if test="${empty historyList}">
                <div class="empty-state">
                    <i class="fa-solid fa-folder-open"></i>
                    <h4 class="mt-3">Bạn chưa đăng ký khóa học nào</h4>
                    <p class="text-muted">Hãy bắt đầu hành trình học tập bằng cách khám phá các khóa học của chúng tôi.</p>
                    <a href="${pageContext.request.contextPath}/courses" class="btn btn-primary mt-2">Khám phá ngay</a>
                </div>
            </c:if>

            <c:if test="${not empty historyList}">
                <div class="table-responsive">
                    <table class="table">
                        <thead>
                            <tr>
                                <th>Tên Khóa Học</th>
                                <th>Ngày Đăng Ký</th>
                                <th>Hình Thức</th>
                                <th>Số Tiền</th>
                                <th>Trạng Thái</th>
                                <th class="text-end">Hành Động</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:forEach var="item" items="${historyList}">
                                <tr>
                                    <td>
                                        <div class="course-name">${item.title}</div>
                                    </td>
                                    <td>
                                        <div class="text-muted"><i class="fa-regular fa-calendar-alt me-1"></i> <fmt:formatDate value="${item.enroll_at}" pattern="dd/MM/yyyy HH:mm"/></div>
                                    </td>
                                    <td>
                                        <span class="badge bg-light text-dark border"><i class="fa-solid fa-money-bill-wave me-1"></i> ${item.payment_method}</span>
                                    </td>
                                    <td class="price-highlight">
                                        <fmt:formatNumber value="${item.price}" type="number" groupingUsed="true"/> đ
                                    </td>
                                    <td>
                                        <c:choose>
                                            <c:when test="${item.status == 'SUCCESS'}">
                                                <span class="status-badge status-success"><i class="fa-solid fa-check"></i> Thành công</span>
                                            </c:when>
                                            <c:otherwise>
                                                <span class="status-badge status-failed"><i class="fa-solid fa-xmark"></i> Thất bại</span>
                                            </c:otherwise>
                                        </c:choose>
                                    </td>
                                    <td class="text-end">
                                        <c:choose>
                                            <c:when test="${item.status == 'SUCCESS'}">
                                                <a href="${pageContext.request.contextPath}/trainee/courses/detail?id=${item.courseId}" class="btn-action btn-learn">
                                                    Vào học
                                                </a>
                                            </c:when>
                                            <c:otherwise>
                                                <a href="${pageContext.request.contextPath}/checkout?id=${item.courseId}" class="btn-action btn-pay">
                                                    Thanh toán lại
                                                </a>
                                            </c:otherwise>
                                        </c:choose>
                                    </td>
                                </tr>
                            </c:forEach>
                        </tbody>
                    </table>
                </div>
                
                <!-- Pagination -->
                <c:if test="${totalPages > 1}">
                    <nav aria-label="Page navigation" class="mt-4">
                        <ul class="pagination justify-content-center">
                            <li class="page-item ${currentPage == 1 ? 'disabled' : ''}">
                                <a class="page-link" href="${pageContext.request.contextPath}/enrollment-history?page=${currentPage - 1}" tabindex="-1">Trước</a>
                            </li>
                            <c:forEach begin="1" end="${totalPages}" var="i">
                                <li class="page-item ${currentPage == i ? 'active' : ''}">
                                    <a class="page-link" href="${pageContext.request.contextPath}/enrollment-history?page=${i}">${i}</a>
                                </li>
                            </c:forEach>
                            <li class="page-item ${currentPage == totalPages ? 'disabled' : ''}">
                                <a class="page-link" href="${pageContext.request.contextPath}/enrollment-history?page=${currentPage + 1}">Sau</a>
                            </li>
                        </ul>
                    </nav>
                </c:if>
            </c:if>
        </div>
    </div>

    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
