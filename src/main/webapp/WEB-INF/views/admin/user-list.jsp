<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<c:set var="pageTitle" value="User List" scope="request" />
<jsp:include page="/WEB-INF/views/common/header.jsp" />

<div class="d-flex flex-grow-1">
    <jsp:include page="/WEB-INF/views/common/sidebar.jsp" />

    <main class="flex-grow-1 p-4 bg-light">
        <div class="container-fluid">

            <!-- Title & Top Toolbar -->
            <div class="d-flex justify-content-between align-items-center mb-3">
                <div>
                    <h2 class="h3 fw-bold text-dark mb-0">Quản lý người dùng</h2>
                    <p class="text-muted small mb-0">Danh sách tài khoản và phân quyền hệ thống</p>
                </div>
                <a href="${ctx}/admin/users/new" class="btn btn-primary shadow-sm">
                    <i class="bi bi-person-plus me-1"></i> Thêm người dùng
                </a>
            </div>

            <!-- Flash Alerts -->
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

            <!-- Filter & Search Toolbar (SRS 3.2.1) -->
            <div class="card border-0 shadow-sm rounded-3 mb-4">
                <div class="card-body p-3">
                    <form method="get" action="${ctx}/admin/users" class="row g-2 align-items-center">
                        <input type="hidden" name="sortBy" value="${sortBy}">
                        <input type="hidden" name="sortOrder" value="${sortOrder}">

                        <!-- Role Filter -->
                        <div class="col-md-2">
                            <select name="role" class="form-select" title="Lọc theo Vai trò">
                                <option value="">Tất cả vai trò</option>
                                <option value="ADMIN" ${role == 'ADMIN' ? 'selected' : ''}>ADMIN</option>
                                <option value="EXPERT" ${role == 'EXPERT' ? 'selected' : ''}>EXPERT</option>
                                <option value="STUDENT" ${role == 'STUDENT' ? 'selected' : ''}>STUDENT</option>
                            </select>
                        </div>

                        <!-- Status Filter -->
                        <div class="col-md-2">
                            <select name="status" class="form-select" title="Lọc theo Trạng thái">
                                <option value="">Tất cả trạng thái</option>
                                <option value="Active" ${status == 'Active' ? 'selected' : ''}>Active (Hoạt động)</option>
                                <option value="Inactive" ${status == 'Inactive' ? 'selected' : ''}>Inactive (Đã khóa)</option>
                            </select>
                        </div>

                        <!-- Search Input -->
                        <div class="col-md-6">
                            <div class="input-group">
                                <input type="text" name="keyword" class="form-control"
                                       placeholder="Tìm theo tên đăng nhập, họ tên, email..." value="${keyword}">
                                <button type="submit" class="btn btn-secondary">
                                    <i class="bi bi-search me-1"></i> Tìm kiếm
                                </button>
                            </div>
                        </div>

                        <!-- Reset -->
                        <div class="col-md-2 text-end">
                            <a href="${ctx}/admin/users" class="btn btn-outline-secondary w-100">
                                <i class="bi bi-arrow-counterclockwise"></i> Đặt lại
                            </a>
                        </div>
                    </form>
                </div>
            </div>

            <!-- Table List (No Delete button) -->
            <div class="card border-0 shadow-sm rounded-3 overflow-hidden">
                <div class="table-responsive">
                    <table class="table table-hover align-middle mb-0">
                        <thead class="table-secondary text-secondary">
                            <tr>
                                <th scope="col" style="width: 70px;">
                                    <a href="${ctx}/admin/users?keyword=${keyword}&role=${role}&status=${status}&sortBy=id&sortOrder=${sortBy == 'id' && sortOrder == 'ASC' ? 'DESC' : 'ASC'}"
                                       class="text-decoration-none text-dark fw-bold d-inline-flex align-items-center">
                                        ID
                                        <i class="bi bi-arrow-down-up ms-1 text-muted small"></i>
                                    </a>
                                </th>
                                <th scope="col">
                                    <a href="${ctx}/admin/users?keyword=${keyword}&role=${role}&status=${status}&sortBy=username&sortOrder=${sortBy == 'username' && sortOrder == 'ASC' ? 'DESC' : 'ASC'}"
                                       class="text-decoration-none text-dark fw-bold d-inline-flex align-items-center">
                                        Tên đăng nhập
                                        <i class="bi bi-arrow-down-up ms-1 text-muted small"></i>
                                    </a>
                                </th>
                                <th scope="col">
                                    <a href="${ctx}/admin/users?keyword=${keyword}&role=${role}&status=${status}&sortBy=full_name&sortOrder=${sortBy == 'full_name' && sortOrder == 'ASC' ? 'DESC' : 'ASC'}"
                                       class="text-decoration-none text-dark fw-bold d-inline-flex align-items-center">
                                        Họ và tên
                                        <i class="bi bi-arrow-down-up ms-1 text-muted small"></i>
                                    </a>
                                </th>
                                <th scope="col">
                                    <a href="${ctx}/admin/users?keyword=${keyword}&role=${role}&status=${status}&sortBy=email&sortOrder=${sortBy == 'email' && sortOrder == 'ASC' ? 'DESC' : 'ASC'}"
                                       class="text-decoration-none text-dark fw-bold d-inline-flex align-items-center">
                                        Email
                                        <i class="bi bi-arrow-down-up ms-1 text-muted small"></i>
                                    </a>
                                </th>
                                <th scope="col" style="width: 110px;">
                                    <a href="${ctx}/admin/users?keyword=${keyword}&role=${role}&status=${status}&sortBy=role&sortOrder=${sortBy == 'role' && sortOrder == 'ASC' ? 'DESC' : 'ASC'}"
                                       class="text-decoration-none text-dark fw-bold d-inline-flex align-items-center">
                                        Vai trò
                                        <i class="bi bi-arrow-down-up ms-1 text-muted small"></i>
                                    </a>
                                </th>
                                <th scope="col" style="width: 120px;">
                                    <a href="${ctx}/admin/users?keyword=${keyword}&role=${role}&status=${status}&sortBy=active&sortOrder=${sortBy == 'active' && sortOrder == 'ASC' ? 'DESC' : 'ASC'}"
                                       class="text-decoration-none text-dark fw-bold d-inline-flex align-items-center">
                                        Trạng thái
                                        <i class="bi bi-arrow-down-up ms-1 text-muted small"></i>
                                    </a>
                                </th>
                                <th scope="col" style="width: 140px;">
                                    <a href="${ctx}/admin/users?keyword=${keyword}&role=${role}&status=${status}&sortBy=created_at&sortOrder=${sortBy == 'created_at' && sortOrder == 'ASC' ? 'DESC' : 'ASC'}"
                                       class="text-decoration-none text-dark fw-bold d-inline-flex align-items-center">
                                        Ngày tạo
                                        <i class="bi bi-arrow-down-up ms-1 text-muted small"></i>
                                    </a>
                                </th>
                                <th scope="col" class="text-center" style="width: 190px;">Thao tác</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:choose>
                                <c:when test="${not empty userPage.content}">
                                    <c:forEach var="u" items="${userPage.content}">
                                        <tr>
                                            <td class="text-muted fw-semibold">${u.id}</td>
                                            <td class="fw-semibold">
                                                <a href="${ctx}/admin/users/detail?id=${u.id}" class="text-decoration-none text-primary">
                                                    ${u.username}
                                                </a>
                                            </td>
                                            <td class="text-dark">${u.fullName}</td>
                                            <td><span class="text-muted">${u.email}</span></td>
                                            <td>
                                                <c:choose>
                                                    <c:when test="${u.role == 'ADMIN'}">
                                                        <span class="badge bg-danger-subtle text-danger border border-danger-subtle">ADMIN</span>
                                                    </c:when>
                                                    <c:when test="${u.role == 'EXPERT'}">
                                                        <span class="badge bg-info-subtle text-info-emphasis border border-info-subtle">EXPERT</span>
                                                    </c:when>
                                                    <c:otherwise>
                                                        <span class="badge bg-secondary-subtle text-secondary border border-secondary-subtle">STUDENT</span>
                                                    </c:otherwise>
                                                </c:choose>
                                            </td>
                                            <td>
                                                <c:choose>
                                                    <c:when test="${u.active}">
                                                        <span class="badge bg-success-subtle text-success border border-success-subtle">Active</span>
                                                    </c:when>
                                                    <c:otherwise>
                                                        <span class="badge bg-danger-subtle text-danger border border-danger-subtle">Inactive</span>
                                                    </c:otherwise>
                                                </c:choose>
                                            </td>
                                            <td>
                                                <small class="text-muted">
                                                    <fmt:formatDate value="${u.createdAt}" pattern="dd/MM/yyyy" />
                                                </small>
                                            </td>
                                            <td class="text-center">
                                                <div class="d-inline-flex gap-1">
                                                    <a href="${ctx}/admin/users/detail?id=${u.id}"
                                                       class="btn btn-sm btn-outline-primary" title="Chi tiết tài khoản">
                                                        <i class="bi bi-pencil-square"></i> Chi tiết
                                                    </a>
                                                    <form method="post" action="${ctx}/admin/users/status" class="d-inline mb-0">
                                                        <input type="hidden" name="id" value="${u.id}">
                                                        <input type="hidden" name="keyword" value="${keyword}">
                                                        <input type="hidden" name="role" value="${role}">
                                                        <input type="hidden" name="status" value="${status}">
                                                        <input type="hidden" name="sortBy" value="${sortBy}">
                                                        <input type="hidden" name="sortOrder" value="${sortOrder}">
                                                        <input type="hidden" name="page" value="${userPage.page}">
                                                        <c:choose>
                                                            <c:when test="${sessionScope.currentUser != null && sessionScope.currentUser.id == u.id}">
                                                                <button type="button" class="btn btn-sm btn-outline-secondary" disabled
                                                                        title="Không thể tự vô hiệu hóa tài khoản quản trị của chính mình">
                                                                    <i class="bi bi-slash-circle"></i> Khóa
                                                                </button>
                                                            </c:when>
                                                            <c:when test="${u.active}">
                                                                <button type="submit" class="btn btn-sm btn-outline-danger"
                                                                        title="Vô hiệu hóa tài khoản"
                                                                        onclick="return confirm('Bạn có chắc muốn vô hiệu hóa tài khoản \'${u.username}\'?');">
                                                                    <i class="bi bi-person-x"></i> Khóa
                                                                </button>
                                                            </c:when>
                                                            <c:otherwise>
                                                                <button type="submit" class="btn btn-sm btn-outline-success"
                                                                        title="Kích hoạt tài khoản"
                                                                        onclick="return confirm('Bạn có chắc muốn kích hoạt tài khoản \'${u.username}\'?');">
                                                                    <i class="bi bi-person-check"></i> Mở khóa
                                                                </button>
                                                            </c:otherwise>
                                                        </c:choose>
                                                    </form>
                                                </div>
                                            </td>
                                        </tr>
                                    </c:forEach>
                                </c:when>
                                <c:otherwise>
                                    <tr>
                                        <td colspan="8" class="text-center py-4 text-muted">
                                            <i class="bi bi-people fs-2 d-block mb-2"></i>
                                            Không tìm thấy người dùng nào phù hợp với bộ lọc.
                                        </td>
                                    </tr>
                                </c:otherwise>
                            </c:choose>
                        </tbody>
                    </table>
                </div>

                <!-- Pagination Footer -->
                <c:if test="${userPage.totalPages > 1}">
                    <div class="card-footer bg-white border-0 py-3 d-flex justify-content-between align-items-center">
                        <div class="text-muted small">
                            Hiển thị trang <strong>${userPage.page}</strong> / <strong>${userPage.totalPages}</strong>
                            (Tổng số <strong>${userPage.totalElements}</strong> người dùng)
                        </div>
                        <nav aria-label="User pagination">
                            <ul class="pagination pagination-sm mb-0">
                                <!-- Previous -->
                                <li class="page-item ${userPage.hasPrevious() ? '' : 'disabled'}">
                                    <a class="page-link"
                                       href="${ctx}/admin/users?page=${userPage.page - 1}&keyword=${keyword}&role=${role}&status=${status}&sortBy=${sortBy}&sortOrder=${sortOrder}">
                                        &laquo;
                                    </a>
                                </li>

                                <!-- Page Numbers -->
                                <c:forEach begin="1" end="${userPage.totalPages}" var="p">
                                    <li class="page-item ${p == userPage.page ? 'active' : ''}">
                                        <a class="page-link"
                                           href="${ctx}/admin/users?page=${p}&keyword=${keyword}&role=${role}&status=${status}&sortBy=${sortBy}&sortOrder=${sortOrder}">
                                            ${p}
                                        </a>
                                    </li>
                                </c:forEach>

                                <!-- Next -->
                                <li class="page-item ${userPage.hasNext() ? '' : 'disabled'}">
                                    <a class="page-link"
                                       href="${ctx}/admin/users?page=${userPage.page + 1}&keyword=${keyword}&role=${role}&status=${status}&sortBy=${sortBy}&sortOrder=${sortOrder}">
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
