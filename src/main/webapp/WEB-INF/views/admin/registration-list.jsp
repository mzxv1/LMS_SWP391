<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <title>Quản lý đăng ký - LMS</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.1/font/bootstrap-icons.css">
    <link rel="stylesheet" href="${ctx}/css/style.css">
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
            <div class="d-flex justify-content-between align-items-center mb-4">
                <h2 class="fw-bold text-primary">Danh sách đăng ký</h2>
            </div>

            <!-- Filters -->
            <div class="card shadow-sm border-0 mb-4 bg-white rounded-3">
                <div class="card-body p-4">
                    <form action="${ctx}/admin/registrations" method="GET" class="row g-3 align-items-end">
                        <div class="col-md-4">
                            <label class="form-label text-muted small fw-bold">Tìm kiếm</label>
                            <div class="input-group">
                                <span class="input-group-text bg-white"><i class="bi bi-search"></i></span>
                                <input type="text" name="keyword" class="form-control border-start-0 ps-0" placeholder="Tìm theo tên học viên..." value="${keyword}">
                            </div>
                        </div>
                        <div class="col-md-3">
                            <label class="form-label text-muted small fw-bold">Khóa học</label>
                            <select name="courseId" class="form-select shadow-none">
                                <option value="">Tất cả khóa học</option>
                                <c:forEach var="c" items="${courses}">
                                    <option value="${c.id}" ${c.id == courseId ? 'selected' : ''}>${c.title}</option>
                                </c:forEach>
                            </select>
                        </div>
                        <div class="col-md-3">
                            <label class="form-label text-muted small fw-bold">Trạng thái</label>
                            <select name="status" class="form-select shadow-none">
                                <option value="">Tất cả trạng thái</option>
                                <option value="ACTIVE" ${status == 'ACTIVE' ? 'selected' : ''}>Active</option>
                                <option value="COMPLETED" ${status == 'COMPLETED' ? 'selected' : ''}>Completed</option>
                                <option value="CANCELLED" ${status == 'CANCELLED' ? 'selected' : ''}>Cancelled</option>
                                <option value="PENDING" ${status == 'PENDING' ? 'selected' : ''}>Pending</option>
                            </select>
                        </div>
                        <div class="col-md-2">
                            <button type="submit" class="btn btn-primary w-100 fw-bold shadow-sm"><i class="bi bi-funnel"></i> Lọc dữ liệu</button>
                        </div>
                    </form>
                </div>
            </div>

            <!-- Table -->
            <div class="card shadow-sm border-0 rounded-3 overflow-hidden">
                <div class="card-body p-0">
                    <div class="table-responsive">
                        <table class="table table-hover align-middle mb-0 custom-table">
                            <thead class="table-light">
                                <tr>
                                    <th class="ps-4">ID</th>
                                    <th>Học viên</th>
                                    <th>Khóa học</th>
                                    <th>Ngày đăng ký</th>
                                    <th>Đã thanh toán</th>
                                    <th>Trạng thái</th>
                                    <th class="text-center pe-4">Hành động</th>
                                </tr>
                            </thead>
                            <tbody class="border-top-0">
                                <c:forEach var="e" items="${enrollments}">
                                    <tr>
                                        <td class="ps-4 text-muted">#${e.enrollmentId}</td>
                                        <td>
                                            <div class="d-flex align-items-center">
                                                <div class="avatar bg-primary text-white rounded-circle d-flex align-items-center justify-content-center me-3" style="width: 40px; height: 40px; font-weight: bold;">
                                                    ${e.studentName.substring(0,1).toUpperCase()}
                                                </div>
                                                <div>
                                                    <div class="fw-bold text-dark">${e.studentName}</div>
                                                    <div class="text-muted small">${e.studentEmail}</div>
                                                </div>
                                            </div>
                                        </td>
                                        <td><span class="fw-medium text-dark">${e.courseTitle}</span></td>
                                        <td class="text-muted"><fmt:formatDate value="${e.enrolledAt}" pattern="dd/MM/yyyy HH:mm"/></td>
                                        <td class="fw-bold text-success"><fmt:formatNumber value="${e.amountPaid}" type="currency" currencySymbol="VND" maxFractionDigits="0"/></td>
                                        <td>
                                            <form action="${ctx}/admin/registrations" method="POST" class="d-flex align-items-center mb-0 m-0">
                                                <input type="hidden" name="action" value="updateStatus">
                                                <input type="hidden" name="id" value="${e.enrollmentId}">
                                                <input type="hidden" name="queryString" value="${pageContext.request.queryString}">
                                                <select name="status" class="form-select form-select-sm rounded-pill px-3 py-1 fw-medium shadow-none w-auto
                                                    ${e.status == 'ACTIVE' ? 'bg-success-subtle text-success border-success-subtle' :
                                                    (e.status == 'COMPLETED' ? 'bg-primary-subtle text-primary border-primary-subtle' :
                                                    (e.status == 'CANCELLED' ? 'bg-danger-subtle text-danger border-danger-subtle' :
                                                    'bg-warning-subtle text-warning border-warning-subtle'))}"
                                                    onchange="this.form.submit()">
                                                    <option value="PENDING" class="bg-white text-dark" ${e.status == 'PENDING' ? 'selected' : ''}>Pending</option>
                                                    <option value="ACTIVE" class="bg-white text-dark" ${e.status == 'ACTIVE' ? 'selected' : ''}>Active</option>
                                                    <option value="COMPLETED" class="bg-white text-dark" ${e.status == 'COMPLETED' ? 'selected' : ''}>Completed</option>
                                                    <option value="CANCELLED" class="bg-white text-dark" ${e.status == 'CANCELLED' ? 'selected' : ''}>Cancelled</option>
                                                </select>
                                                <c:if test="${e.paid}"><i class="bi bi-check-circle-fill text-success ms-2 fs-6" title="Đã thanh toán"></i></c:if>
                                            </form>
                                        </td>
                                        <td class="text-center pe-4">
                                            <a href="${ctx}/admin/registrations?action=detail&id=${e.enrollmentId}" class="btn btn-sm btn-outline-primary rounded-pill px-3">
                                                <i class="bi bi-eye"></i> Chi tiết
                                            </a>
                                        </td>
                                    </tr>
                                </c:forEach>
                                <c:if test="${empty enrollments}">
                                    <tr>
                                        <td colspan="7" class="text-center py-5 text-muted">
                                            <i class="bi bi-inbox fs-1 d-block mb-3 text-secondary"></i>
                                            Không tìm thấy dữ liệu đăng ký nào phù hợp.
                                        </td>
                                    </tr>
                                </c:if>
                            </tbody>
                        </table>
                    </div>
                </div>
                
                <!-- Pagination -->
                <c:if test="${totalPages > 1}">
                    <div class="card-footer bg-white py-3 border-top">
                        <nav aria-label="Page navigation">
                            <ul class="pagination justify-content-end mb-0">
                                <li class="page-item ${currentPage == 1 ? 'disabled' : ''}">
                                    <a class="page-link border-0 text-dark" href="${ctx}/admin/registrations?keyword=${keyword}&courseId=${courseId}&status=${status}&page=${currentPage - 1}"><i class="bi bi-chevron-left"></i> Trước</a>
                                </li>
                                <c:forEach begin="1" end="${totalPages}" var="i">
                                    <li class="page-item ${currentPage == i ? 'active' : ''}">
                                        <a class="page-link ${currentPage == i ? 'bg-primary border-primary rounded-3' : 'border-0 text-dark'}" href="${ctx}/admin/registrations?keyword=${keyword}&courseId=${courseId}&status=${status}&page=${i}">${i}</a>
                                    </li>
                                </c:forEach>
                                <li class="page-item ${currentPage == totalPages ? 'disabled' : ''}">
                                    <a class="page-link border-0 text-dark" href="${ctx}/admin/registrations?keyword=${keyword}&courseId=${courseId}&status=${status}&page=${currentPage + 1}">Sau <i class="bi bi-chevron-right"></i></a>
                                </li>
                            </ul>
                        </nav>
                    </div>
                </c:if>
            </div>
        </div>
    </div>
</div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
