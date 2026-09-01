<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <title>Chi tiết đăng ký - LMS</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.1/font/bootstrap-icons.css">
    <link rel="stylesheet" href="${ctx}/css/style.css">
    <style>
        .detail-card {
            border-left: 4px solid var(--bs-primary);
            transition: transform 0.2s ease;
        }
        .detail-card:hover {
            transform: translateY(-2px);
        }
        .detail-card-payment {
            border-left-color: var(--bs-success);
        }
        .detail-card-trainee {
            border-left-color: var(--bs-info);
        }
        .info-label {
            font-size: 0.85rem;
            text-transform: uppercase;
            letter-spacing: 0.5px;
            color: #6c757d;
            font-weight: 600;
            margin-bottom: 0.25rem;
        }
        .info-value {
            font-size: 1.1rem;
            color: #212529;
            font-weight: 500;
        }
    </style>
</head>
<body class="bg-light">

<div class="container-fluid">
    <div class="row min-vh-100">
        <!-- Sidebar -->
        <div class="col-md-2 p-0 bg-dark">
            <jsp:include page="/WEB-INF/views/common/sidebar.jsp" />
        </div>

        <!-- Main Content -->
        <div class="col-md-10 p-4">
            <div class="mb-4">
                <a href="${ctx}/admin/registrations" class="text-decoration-none text-muted mb-3 d-inline-block">
                    <i class="bi bi-arrow-left"></i> Quay lại danh sách
                </a>
                <h2 class="fw-bold text-dark">
                    Chi tiết đăng ký
                </h2>
                <p class="text-muted">
                    Ngày đăng ký: <fmt:formatDate value="${enrollment.enrolledAt}" pattern="dd/MM/yyyy HH:mm"/>
                </p>
            </div>

            <div class="row g-4">
                <!-- Thông tin khóa học -->
                <div class="col-md-6">
                    <div class="card shadow-sm border-0 detail-card h-100 rounded-3">
                        <div class="card-header bg-white border-0 pt-4 pb-0">
                            <h5 class="fw-bold text-primary mb-0"><i class="bi bi-book"></i> Thông tin khóa học</h5>
                        </div>
                        <div class="card-body">
                            <div class="row g-3">
                                <div class="col-12">
                                    <div class="info-label">Tên khóa học</div>
                                    <div class="info-value text-primary">${enrollment.courseTitle}</div>
                                </div>
                                <div class="col-md-6">
                                    <div class="info-label">Chuyên gia</div>
                                    <div class="info-value">${empty enrollment.expertName ? 'N/A' : enrollment.expertName}</div>
                                </div>
                                <div class="col-md-6">
                                    <div class="info-label">Danh mục</div>
                                    <div class="info-value">
                                        <span class="badge bg-secondary-subtle text-secondary border border-secondary-subtle rounded-pill px-3 py-2">
                                            ${empty enrollment.categoryName ? 'N/A' : enrollment.categoryName}
                                        </span>
                                    </div>
                                </div>
                                <div class="col-md-6">
                                    <div class="info-label">Thời lượng</div>
                                    <div class="info-value"><i class="bi bi-clock text-muted me-1"></i> ${enrollment.durationHours} giờ</div>
                                </div>
                                <div class="col-md-6">
                                    <div class="info-label">Trạng thái khóa học</div>
                                    <div class="info-value">
                                        <c:choose>
                                            <c:when test="${enrollment.status == 'ACTIVE'}"><span class="text-success fw-bold">Đang học</span></c:when>
                                            <c:when test="${enrollment.status == 'COMPLETED'}"><span class="text-primary fw-bold">Hoàn thành</span></c:when>
                                            <c:when test="${enrollment.status == 'CANCELLED'}"><span class="text-danger fw-bold">Đã hủy</span></c:when>
                                            <c:otherwise><span class="text-warning fw-bold">${enrollment.status}</span></c:otherwise>
                                        </c:choose>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>

                <!-- Thông tin học viên -->
                <div class="col-md-6">
                    <div class="card shadow-sm border-0 detail-card detail-card-trainee h-100 rounded-3">
                        <div class="card-header bg-white border-0 pt-4 pb-0">
                            <h5 class="fw-bold text-info mb-0"><i class="bi bi-person"></i> Thông tin học viên</h5>
                        </div>
                        <div class="card-body">
                            <div class="d-flex align-items-center mb-4">
                                <div class="avatar bg-info text-white rounded-circle d-flex align-items-center justify-content-center me-3" style="width: 50px; height: 50px; font-size: 1.5rem; font-weight: bold;">
                                    ${enrollment.studentName.substring(0,1).toUpperCase()}
                                </div>
                                <div>
                                    <div class="fs-5 fw-bold text-dark">${enrollment.studentName}</div>
                                </div>
                            </div>
                            
                            <div class="row g-3">
                                <div class="col-md-6">
                                    <div class="info-label">Số điện thoại</div>
                                    <div class="info-value">
                                        <i class="bi bi-telephone text-muted me-1"></i> 
                                        ${empty enrollment.studentPhone ? '<span class="text-muted fst-italic">Chưa cập nhật</span>' : enrollment.studentPhone}
                                    </div>
                                </div>
                                <div class="col-md-6">
                                    <div class="info-label">Email</div>
                                    <div class="info-value">
                                        <i class="bi bi-envelope text-muted me-1"></i> ${enrollment.studentEmail}
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>

                <!-- Thông tin thanh toán -->
                <div class="col-md-12">
                    <div class="card shadow-sm border-0 detail-card detail-card-payment rounded-3">
                        <div class="card-header bg-white border-0 pt-4 pb-0">
                            <h5 class="fw-bold text-success mb-0"><i class="bi bi-credit-card"></i> Thông tin thanh toán</h5>
                        </div>
                        <div class="card-body">
                            <div class="row g-4 align-items-center">
                                <div class="col-md-4 border-end">
                                    <div class="info-label">Tổng tiền</div>
                                    <div class="display-6 fw-bold text-success">
                                        <fmt:formatNumber value="${enrollment.amountPaid}" type="currency" currencySymbol="VND" maxFractionDigits="0"/>
                                    </div>
                                </div>
                                <div class="col-md-4 border-end ps-md-4">
                                    <div class="info-label">Phương thức thanh toán</div>
                                    <div class="info-value d-flex align-items-center mt-2">
                                        <c:choose>
                                            <c:when test="${enrollment.paymentMethod == 'VNPAY'}">
                                                <span class="badge bg-primary fs-6 px-3 py-2 rounded-pill">VNPAY</span>
                                            </c:when>
                                            <c:when test="${enrollment.paymentMethod == 'SEPAY'}">
                                                <span class="badge bg-info fs-6 px-3 py-2 rounded-pill text-dark">Chuyển khoản (SePay)</span>
                                            </c:when>
                                            <c:otherwise>
                                                <span class="badge bg-secondary fs-6 px-3 py-2 rounded-pill">${empty enrollment.paymentMethod ? 'N/A' : enrollment.paymentMethod}</span>
                                            </c:otherwise>
                                        </c:choose>
                                    </div>
                                </div>
                                <div class="col-md-4 ps-md-4">
                                    <div class="info-label">Trạng thái</div>
                                    <div class="info-value mt-2">
                                        <c:choose>
                                            <c:when test="${enrollment.paymentStatus == 'SUCCESS'}">
                                                <span class="badge bg-success-subtle text-success fs-6 px-4 py-2 rounded-pill border border-success-subtle">
                                                    <i class="bi bi-check-circle-fill me-1"></i> Success
                                                </span>
                                            </c:when>
                                            <c:when test="${enrollment.paymentStatus == 'FAILED'}">
                                                <span class="badge bg-danger-subtle text-danger fs-6 px-4 py-2 rounded-pill border border-danger-subtle">
                                                    <i class="bi bi-x-circle-fill me-1"></i> Fail
                                                </span>
                                            </c:when>
                                            <c:otherwise>
                                                <span class="badge bg-secondary-subtle text-secondary fs-6 px-4 py-2 rounded-pill border border-secondary-subtle">
                                                    ${empty enrollment.paymentStatus ? 'Chưa thanh toán' : enrollment.paymentStatus}
                                                </span>
                                            </c:otherwise>
                                        </c:choose>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>

        </div>
    </div>
</div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
