<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<c:set var="pageTitle" value="Quản lý khóa học" scope="request" />
<jsp:include page="/WEB-INF/views/common/header.jsp" />

<div class="d-flex flex-grow-1">
    <jsp:include page="/WEB-INF/views/common/sidebar.jsp" />

    <main class="flex-grow-1 p-4 bg-light">
        <div class="container-fluid">

            <!-- Title & Alerts -->
            <div class="d-flex justify-content-between align-items-center mb-4">
                <h2 class="h3 fw-bold text-dark mb-0">Quản lý khóa học (Hệ thống)</h2>
            </div>

            <c:if test="${not empty message}">
                <div class="alert alert-success alert-dismissible fade show" role="alert">
                    <i class="bi bi-check-circle-fill me-2"></i>${message}
                    <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
                </div>
            </c:if>

            <c:if test="${not empty error}">
                <div class="alert alert-danger alert-dismissible fade show" role="alert">
                    <i class="bi bi-exclamation-triangle-fill me-2"></i>${error}
                    <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
                </div>
            </c:if>

            <!-- Search & Filter Toolbar -->
            <div class="card border-0 shadow-sm rounded-3 mb-4">
                <div class="card-body p-3">
                    <form method="get" action="${ctx}/admin/courses" class="row g-2 align-items-center">
                        <input type="hidden" name="sortBy" value="${sortBy}">
                        <input type="hidden" name="sortOrder" value="${sortOrder}">

                        <div class="col-md-6">
                            <div class="input-group">
                                <span class="input-group-text bg-light"><i class="bi bi-search"></i></span>
                                <input type="text" name="keyword" class="form-control"
                                       placeholder="Tìm theo tên khóa học, danh mục, chuyên gia..." value="${keyword}">
                            </div>
                        </div>
                        <div class="col-md-3">
                            <select name="status" class="form-select">
                                <option value="">-- Tất cả trạng thái --</option>
                                <option value="DRAFT" ${status == 'DRAFT' ? 'selected' : ''}>DRAFT</option>
                                <option value="PUBLISHED" ${status == 'PUBLISHED' ? 'selected' : ''}>PUBLISHED</option>
                                <option value="ARCHIVED" ${status == 'ARCHIVED' ? 'selected' : ''}>ARCHIVED</option>
                            </select>
                        </div>
                        <div class="col-md-3 d-flex gap-2">
                            <button type="submit" class="btn btn-primary flex-grow-1">
                                <i class="bi bi-funnel me-1"></i> Lọc dữ liệu
                            </button>
                            <a href="${ctx}/admin/courses" class="btn btn-outline-secondary" title="Đặt lại bộ lọc">
                                <i class="bi bi-arrow-counterclockwise"></i>
                            </a>
                        </div>
                    </form>
                </div>
            </div>

            <!-- Table of Courses with Sorting Headers -->
            <div class="card border-0 shadow-sm rounded-3 overflow-hidden">
                <div class="table-responsive">
                    <table class="table table-hover mb-0 align-middle">
                        <thead class="table-secondary text-secondary">
                            <tr>
                                <th scope="col" style="width: 80px;">
                                    <a href="${ctx}/admin/courses?keyword=${keyword}&status=${status}&sortBy=id&sortOrder=${sortBy == 'id' && sortOrder == 'ASC' ? 'DESC' : 'ASC'}"
                                       class="text-decoration-none text-dark fw-bold d-inline-flex align-items-center">
                                        #
                                        <i class="bi bi-arrow-down-up ms-1 text-muted small"></i>
                                    </a>
                                </th>
                                <th scope="col">
                                    <a href="${ctx}/admin/courses?keyword=${keyword}&status=${status}&sortBy=title&sortOrder=${sortBy == 'title' && sortOrder == 'ASC' ? 'DESC' : 'ASC'}"
                                       class="text-decoration-none text-dark fw-bold d-inline-flex align-items-center">
                                        Tên khóa học
                                        <i class="bi bi-arrow-down-up ms-1 text-muted small"></i>
                                    </a>
                                </th>
                                <th scope="col">
                                    <a href="${ctx}/admin/courses?keyword=${keyword}&status=${status}&sortBy=expert&sortOrder=${sortBy == 'expert' && sortOrder == 'ASC' ? 'DESC' : 'ASC'}"
                                       class="text-decoration-none text-dark fw-bold d-inline-flex align-items-center">
                                        Chuyên gia phụ trách
                                        <i class="bi bi-arrow-down-up ms-1 text-muted small"></i>
                                    </a>
                                </th>
                                <th scope="col">
                                    <a href="${ctx}/admin/courses?keyword=${keyword}&status=${status}&sortBy=category&sortOrder=${sortBy == 'category' && sortOrder == 'ASC' ? 'DESC' : 'ASC'}"
                                       class="text-decoration-none text-dark fw-bold d-inline-flex align-items-center">
                                        Danh mục
                                        <i class="bi bi-arrow-down-up ms-1 text-muted small"></i>
                                    </a>
                                </th>
                                <th scope="col" style="width: 120px;">
                                    <a href="${ctx}/admin/courses?keyword=${keyword}&status=${status}&sortBy=duration&sortOrder=${sortBy == 'duration' && sortOrder == 'ASC' ? 'DESC' : 'ASC'}"
                                       class="text-decoration-none text-dark fw-bold d-inline-flex align-items-center">
                                        Thời lượng
                                        <i class="bi bi-arrow-down-up ms-1 text-muted small"></i>
                                    </a>
                                </th>
                                <th scope="col" style="width: 130px;">
                                    <a href="${ctx}/admin/courses?keyword=${keyword}&status=${status}&sortBy=status&sortOrder=${sortBy == 'status' && sortOrder == 'ASC' ? 'DESC' : 'ASC'}"
                                       class="text-decoration-none text-dark fw-bold d-inline-flex align-items-center">
                                        Trạng thái
                                        <i class="bi bi-arrow-down-up ms-1 text-muted small"></i>
                                    </a>
                                </th>
                                <th scope="col" style="width: 150px;">
                                    <a href="${ctx}/admin/courses?keyword=${keyword}&status=${status}&sortBy=updated_at&sortOrder=${sortBy == 'updated_at' && sortOrder == 'ASC' ? 'DESC' : 'ASC'}"
                                       class="text-decoration-none text-dark fw-bold d-inline-flex align-items-center">
                                        Cập nhật
                                        <i class="bi bi-arrow-down-up ms-1 text-muted small"></i>
                                    </a>
                                </th>
                                <th scope="col" class="text-end" style="width: 120px;">Thao tác</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:choose>
                                <c:when test="${not empty coursePage.content}">
                                    <c:forEach var="c" items="${coursePage.content}">
                                        <tr>
                                            <td class="text-muted fw-semibold">${c.id}</td>
                                            <td class="fw-semibold text-dark">
                                                <a href="${ctx}/admin/courses/detail?id=${c.id}" class="text-decoration-none text-primary">
                                                    ${c.title}
                                                </a>
                                            </td>
                                            <td><i class="bi bi-person me-1 text-secondary"></i>${c.expertName}</td>
                                            <td><span class="badge bg-light text-dark border">${empty c.category ? 'Chưa phân loại' : c.category}</span></td>
                                            <td>${c.durationHours} giờ</td>
                                            <td>
                                                <c:choose>
                                                    <c:when test="${c.status == 'PUBLISHED'}">
                                                        <span class="badge bg-success-subtle text-success border border-success-subtle">PUBLISHED</span>
                                                    </c:when>
                                                    <c:when test="${c.status == 'DRAFT'}">
                                                        <span class="badge bg-warning-subtle text-warning-emphasis border border-warning-subtle">DRAFT</span>
                                                    </c:when>
                                                    <c:otherwise>
                                                        <span class="badge bg-secondary-subtle text-secondary border border-secondary-subtle">ARCHIVED</span>
                                                    </c:otherwise>
                                                </c:choose>
                                            </td>
                                            <td>
                                                <small class="text-muted">
                                                    <fmt:formatDate value="${c.updatedAt}" pattern="dd/MM/yyyy HH:mm" />
                                                </small>
                                            </td>
                                            <td class="text-end">
                                                <a href="${ctx}/admin/courses/detail?id=${c.id}"
                                                   class="btn btn-sm btn-outline-primary" title="Xem chi tiết & phê duyệt">
                                                    <i class="bi bi-eye"></i> Chi tiết
                                                </a>
                                            </td>
                                        </tr>
                                    </c:forEach>
                                </c:when>
                                <c:otherwise>
                                    <tr>
                                        <td colspan="8" class="text-center text-muted py-4">
                                            <i class="bi bi-journal-x fs-2 d-block mb-2"></i>
                                            Không tìm thấy khóa học nào phù hợp với bộ lọc.
                                        </td>
                                    </tr>
                                </c:otherwise>
                            </c:choose>
                        </tbody>
                    </table>
                </div>

                <!-- Bootstrap 5 Pagination Footer -->
                <c:if test="${coursePage.totalPages > 1}">
                    <div class="card-footer bg-white border-0 py-3 d-flex justify-content-between align-items-center">
                        <div class="text-muted small">
                            Hiển thị trang <strong>${coursePage.page}</strong> / <strong>${coursePage.totalPages}</strong>
                            (Tổng số <strong>${coursePage.totalElements}</strong> khóa học)
                        </div>
                        <nav aria-label="Course pagination">
                            <ul class="pagination pagination-sm mb-0">
                                <!-- Previous Button -->
                                <li class="page-item ${coursePage.hasPrevious() ? '' : 'disabled'}">
                                    <a class="page-link"
                                       href="${ctx}/admin/courses?page=${coursePage.page - 1}&keyword=${keyword}&status=${status}&sortBy=${sortBy}&sortOrder=${sortOrder}">
                                        &laquo;
                                    </a>
                                </li>

                                <!-- Numeric Page Buttons -->
                                <c:forEach begin="1" end="${coursePage.totalPages}" var="p">
                                    <li class="page-item ${p == coursePage.page ? 'active' : ''}">
                                        <a class="page-link"
                                           href="${ctx}/admin/courses?page=${p}&keyword=${keyword}&status=${status}&sortBy=${sortBy}&sortOrder=${sortOrder}">
                                            ${p}
                                        </a>
                                    </li>
                                </c:forEach>

                                <!-- Next Button -->
                                <li class="page-item ${coursePage.hasNext() ? '' : 'disabled'}">
                                    <a class="page-link"
                                       href="${ctx}/admin/courses?page=${coursePage.page + 1}&keyword=${keyword}&status=${status}&sortBy=${sortBy}&sortOrder=${sortOrder}">
                                        &raquo;
                                    </a>
                                </li>
                            </ul>
                        </nav>
                    </div>
                </c:if>
            </div>
        </div>
    </main>
</div>

<jsp:include page="/WEB-INF/views/common/footer.jsp" />
