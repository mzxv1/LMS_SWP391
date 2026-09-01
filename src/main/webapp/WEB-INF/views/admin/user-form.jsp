<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<c:set var="pageTitle" value="Thêm người dùng mới" scope="request" />
<jsp:include page="/WEB-INF/views/common/header.jsp" />

<div class="d-flex flex-grow-1">
    <jsp:include page="/WEB-INF/views/common/sidebar.jsp" />

    <main class="flex-grow-1 p-4 bg-light">
        <div class="container" style="max-width: 760px;">

            <!-- Breadcrumb Navigation -->
            <nav aria-label="breadcrumb" class="mb-3">
                <ol class="breadcrumb">
                    <li class="breadcrumb-item"><a href="${ctx}/admin/users">Quản lý người dùng</a></li>
                    <li class="breadcrumb-item active" aria-current="page">Thêm người dùng mới</li>
                </ol>
            </nav>

            <div class="d-flex justify-content-between align-items-center mb-3">
                <h2 class="h4 fw-bold text-dark mb-0">
                    <i class="bi bi-person-plus text-primary me-2"></i>Thêm người dùng mới
                </h2>
                <a href="${ctx}/admin/users" class="btn btn-outline-secondary">
                    <i class="bi bi-arrow-left me-1"></i> Quay lại danh sách
                </a>
            </div>

            <!-- Alerts -->
            <c:if test="${not empty error}">
                <div class="alert alert-danger alert-dismissible fade show mb-3" role="alert">
                    <i class="bi bi-exclamation-triangle-fill me-2"></i>${error}
                    <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
                </div>
            </c:if>

            <div class="card border-0 shadow-sm rounded-3">
                <div class="card-body p-4">
                    <form method="post" action="${ctx}/admin/users/new" class="needs-validation" novalidate>
                        <div class="row g-3">

                            <!-- 1. Full Name* -->
                            <div class="col-md-6">
                                <label for="fullName" class="form-label fw-semibold">
                                    Họ và tên <span class="text-danger">*</span>
                                </label>
                                <input type="text" id="fullName" name="fullName" class="form-control"
                                       value="${formData.fullName}" required placeholder="vd: Nguyễn Văn A">
                            </div>

                            <!-- 2. Username* -->
                            <div class="col-md-6">
                                <label for="username" class="form-label fw-semibold">
                                    Tên đăng nhập <span class="text-danger">*</span>
                                </label>
                                <input type="text" id="username" name="username" class="form-control"
                                       value="${formData.username}" required minlength="4" placeholder="vd: user123">
                                <div class="form-text text-muted small">Tối thiểu 4 ký tự, dùng để đăng nhập.</div>
                            </div>

                            <!-- 3. Email* -->
                            <div class="col-md-6">
                                <label for="email" class="form-label fw-semibold">
                                    Email <span class="text-danger">*</span>
                                </label>
                                <input type="email" id="email" name="email" class="form-control"
                                       value="${formData.email}" required placeholder="vd: user@example.com">
                            </div>

                            <!-- 4. Phone -->
                            <div class="col-md-6">
                                <label for="phone" class="form-label fw-semibold">Số điện thoại</label>
                                <input type="tel" id="phone" name="phone" class="form-control"
                                       value="${formData.phone}" placeholder="vd: 0901234567">
                            </div>

                            <!-- 5. Password info -->
                            <div class="col-12">
                                <div class="alert alert-info mb-0 py-2 small">
                                    <i class="bi bi-info-circle me-1"></i>
                                    Mật khẩu sẽ được hệ thống tự động tạo ngẫu nhiên và hiển thị sau khi tạo tài khoản thành công.
                                </div>
                            </div>

                            <!-- 6. Role* -->
                            <div class="col-md-6">
                                <label for="role" class="form-label fw-semibold">
                                    Vai trò <span class="text-danger">*</span>
                                </label>
                                <select id="role" name="role" class="form-select" required>
                                    <option value="STUDENT" ${formData == null || formData.role == 'STUDENT' ? 'selected' : ''}>STUDENT (Học viên)</option>
                                    <option value="EXPERT" ${formData.role == 'EXPERT' ? 'selected' : ''}>EXPERT (Chuyên gia)</option>
                                    <option value="ADMIN" ${formData.role == 'ADMIN' ? 'selected' : ''}>ADMIN (Quản trị viên)</option>
                                </select>
                            </div>

                            <!-- 8. Status -->
                            <div class="col-md-6">
                                <label class="form-label fw-semibold d-block">Trạng thái ban đầu</label>
                                <div class="form-check form-check-inline">
                                    <input class="form-check-input" type="radio" name="status" id="statusActive"
                                           value="Active" ${formData == null || formData.active ? 'checked' : ''}>
                                    <label class="form-check-label text-success fw-medium" for="statusActive">
                                        <i class="bi bi-check-circle me-1"></i> Active (Hoạt động)
                                    </label>
                                </div>
                                <div class="form-check form-check-inline">
                                    <input class="form-check-input" type="radio" name="status" id="statusInactive"
                                           value="Inactive" ${formData != null && !formData.active ? 'checked' : ''}>
                                    <label class="form-check-label text-danger fw-medium" for="statusInactive">
                                        <i class="bi bi-dash-circle me-1"></i> Inactive (Đã khóa)
                                    </label>
                                </div>
                            </div>

                            <!-- Action Buttons -->
                            <div class="col-12 mt-4 pt-2 border-top d-flex gap-2 justify-content-end">
                                <a href="${ctx}/admin/users" class="btn btn-outline-secondary px-4">
                                    <i class="bi bi-x-circle me-1"></i> Hủy bỏ
                                </a>
                                <button type="submit" class="btn btn-primary px-4 shadow-sm">
                                    <i class="bi bi-person-plus-fill me-1"></i> Tạo người dùng
                                </button>
                            </div>

                        </div>
                    </form>
                </div>
            </div>
        </div>
    </main>
</div>

<jsp:include page="/WEB-INF/views/common/footer.jsp" />
