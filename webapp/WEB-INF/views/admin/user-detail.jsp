<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<c:set var="currentUser" value="${sessionScope.currentUser}" />
<c:set var="isSelf" value="${currentUser != null && currentUser.id == user.id}" />
<c:set var="pageTitle" value="Chi tiết người dùng" scope="request" />
<jsp:include page="/WEB-INF/views/common/header.jsp" />

<div class="d-flex flex-grow-1">
    <jsp:include page="/WEB-INF/views/common/sidebar.jsp" />

    <main class="flex-grow-1 p-4 bg-light">
        <div class="container" style="max-width: 760px;">

            <!-- Breadcrumb Navigation -->
            <nav aria-label="breadcrumb" class="mb-3">
                <ol class="breadcrumb">
                    <li class="breadcrumb-item"><a href="${ctx}/admin/users">Quản lý người dùng</a></li>
                    <li class="breadcrumb-item active" aria-current="page">Chi tiết người dùng #${user.id}</li>
                </ol>
            </nav>

            <div class="d-flex justify-content-between align-items-center mb-3">
                <h2 class="h4 fw-bold text-dark mb-0">
                    <i class="bi bi-person-badge text-primary me-2"></i>Chi tiết tài khoản: <span class="text-primary">${user.username}</span>
                </h2>
                <a href="${ctx}/admin/users" class="btn btn-outline-secondary">
                    <i class="bi bi-arrow-left me-1"></i> Quay lại danh sách
                </a>
            </div>

            <!-- Alerts -->
            <c:if test="${not empty message}">
                <div class="alert alert-success alert-dismissible fade show mb-3" role="alert">
                    <i class="bi bi-check-circle-fill me-2"></i>${message}
                    <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
                </div>
            </c:if>

            <c:if test="${not empty error}">
                <div class="alert alert-danger alert-dismissible fade show mb-3" role="alert">
                    <i class="bi bi-exclamation-triangle-fill me-2"></i>${error}
                    <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
                </div>
            </c:if>

            <c:if test="${isSelf}">
                <div class="alert alert-info d-flex align-items-center mb-3" role="alert">
                    <i class="bi bi-shield-lock-fill fs-4 me-3 text-info"></i>
                    <div>
                        <strong>Tài khoản đang đăng nhập của bạn:</strong>
                        Vai trò và trạng thái tài khoản được khóa bảo vệ, không thể tự giáng quyền hoặc tự vô hiệu hóa để đảm bảo an toàn hệ thống.
                    </div>
                </div>
            </c:if>

            <c:if test="${not empty user}">
                <div class="card border-0 shadow-sm rounded-3">
                    <div class="card-body p-4">
                        <form method="post" action="${ctx}/admin/users/detail" class="needs-validation" novalidate>
                            <input type="hidden" name="id" value="${user.id}">

                            <div class="row g-3">
                                <!-- 1. Username (Read-only / Identity) -->
                                <div class="col-md-6">
                                    <label class="form-label fw-semibold">Tên đăng nhập</label>
                                    <input type="text" class="form-control bg-light" value="${user.username}" disabled>
                                    <input type="hidden" name="username" value="${user.username}">
                                    <div class="form-text text-muted small">Tên đăng nhập là định danh cố định.</div>
                                </div>

                                <!-- 2. Full Name* -->
                                <div class="col-md-6">
                                    <label for="fullName" class="form-label fw-semibold">
                                        Họ và tên <span class="text-danger">*</span>
                                    </label>
                                    <input type="text" id="fullName" name="fullName" class="form-control"
                                           value="${user.fullName}" required>
                                </div>

                                <!-- 3. Email* -->
                                <div class="col-md-6">
                                    <label for="email" class="form-label fw-semibold">
                                        Email <span class="text-danger">*</span>
                                    </label>
                                    <input type="email" id="email" name="email" class="form-control"
                                           value="${user.email}" required>
                                </div>

                                <!-- 4. Phone -->
                                <div class="col-md-6">
                                    <label for="phone" class="form-label fw-semibold">Số điện thoại</label>
                                    <input type="tel" id="phone" name="phone" class="form-control"
                                           value="${user.phone}" placeholder="0901234567">
                                </div>

                                <!-- 5. Role -->
                                <div class="col-md-6">
                                    <label for="role" class="form-label fw-semibold">
                                        Vai trò <span class="text-danger">*</span>
                                    </label>
                                    <c:choose>
                                        <c:when test="${isSelf}">
                                            <input type="hidden" name="role" value="${user.role}">
                                            <select id="role" class="form-select bg-light" disabled>
                                                <option value="ADMIN" selected>ADMIN (Quản trị viên)</option>
                                            </select>
                                            <div class="form-text text-muted small">Không thể đổi vai trò của chính mình.</div>
                                        </c:when>
                                        <c:otherwise>
                                            <select id="role" name="role" class="form-select" required>
                                                <option value="STUDENT" ${user.role == 'STUDENT' ? 'selected' : ''}>STUDENT (Học viên)</option>
                                                <option value="EXPERT" ${user.role == 'EXPERT' ? 'selected' : ''}>EXPERT (Chuyên gia)</option>
                                                <option value="ADMIN" ${user.role == 'ADMIN' ? 'selected' : ''}>ADMIN (Quản trị viên)</option>
                                            </select>
                                        </c:otherwise>
                                    </c:choose>
                                </div>

                                <!-- 6. Status (Radio Active / Inactive) -->
                                <div class="col-md-6">
                                    <label class="form-label fw-semibold d-block">Trạng thái tài khoản</label>
                                    <c:choose>
                                        <c:when test="${isSelf}">
                                            <input type="hidden" name="status" value="Active">
                                            <div class="form-check form-check-inline">
                                                <input class="form-check-input" type="radio" checked disabled>
                                                <label class="form-check-label text-success fw-medium">
                                                    <i class="bi bi-check-circle me-1"></i> Active (Hoạt động)
                                                </label>
                                            </div>
                                            <div class="form-text text-muted small">Không thể tự khóa tài khoản hiện tại.</div>
                                        </c:when>
                                        <c:otherwise>
                                            <div class="form-check form-check-inline">
                                                <input class="form-check-input" type="radio" name="status" id="statusActive"
                                                       value="Active" ${user.active ? 'checked' : ''}>
                                                <label class="form-check-label text-success fw-medium" for="statusActive">
                                                    <i class="bi bi-check-circle me-1"></i> Active (Hoạt động)
                                                </label>
                                            </div>
                                            <div class="form-check form-check-inline">
                                                <input class="form-check-input" type="radio" name="status" id="statusInactive"
                                                       value="Inactive" ${!user.active ? 'checked' : ''}>
                                                <label class="form-check-label text-danger fw-medium" for="statusInactive">
                                                    <i class="bi bi-dash-circle me-1"></i> Inactive (Đã khóa)
                                                </label>
                                            </div>
                                        </c:otherwise>
                                    </c:choose>
                                </div>

                                <!-- 7. Reset Password (Optional) -->
                                <div class="col-12">
                                    <label for="password" class="form-label fw-semibold">
                                        Đặt lại mật khẩu mới
                                    </label>
                                    <div class="input-group">
                                        <span class="input-group-text"><i class="bi bi-key"></i></span>
                                        <input type="password" id="password" name="password" class="form-control"
                                               minlength="6" placeholder="Nhập mật khẩu mới nếu muốn thay đổi">
                                    </div>
                                    <div class="form-text text-muted small">
                                        Để trống nếu giữ nguyên mật khẩu cũ (nếu đổi, mật khẩu phải từ 6 ký tự trở lên).
                                    </div>
                                </div>

                                <!-- 8. Metadata -->
                                <div class="col-12 mt-3 pt-3 border-top">
                                    <p class="text-muted small mb-0">
                                        <i class="bi bi-calendar3 me-1"></i> Ngày khởi tạo tài khoản:
                                        <strong><fmt:formatDate value="${user.createdAt}" pattern="dd/MM/yyyy HH:mm:ss" /></strong>
                                    </p>
                                </div>

                                <!-- Action Buttons -->
                                <div class="col-12 mt-4 pt-2 border-top d-flex gap-2 justify-content-end">
                                    <a href="${ctx}/admin/users" class="btn btn-outline-secondary px-4">
                                        <i class="bi bi-x-circle me-1"></i> Hủy bỏ
                                    </a>
                                    <button type="submit" class="btn btn-primary px-4 shadow-sm">
                                        <i class="bi bi-check2-circle me-1"></i> Lưu thay đổi
                                    </button>
                                </div>
                            </div>
                        </form>
                    </div>
                </div>
            </c:if>
        </div>
    </main>
</div>

<jsp:include page="/WEB-INF/views/common/footer.jsp" />
