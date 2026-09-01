<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<c:set var="pageTitle" value="Hồ sơ của tôi" scope="request" />
<jsp:include page="/WEB-INF/views/common/header.jsp" />

<div class="d-flex flex-grow-1">
    <jsp:include page="/WEB-INF/views/common/sidebar.jsp" />

    <main class="flex-grow-1 p-4">
        <h3 class="mb-4">Hồ sơ của tôi</h3>

        <c:if test="${not empty success}">
            <div class="alert alert-success alert-auto-dismiss">${success}</div>
        </c:if>
        <c:if test="${not empty error}">
            <div class="alert alert-danger">${error}</div>
        </c:if>

        <div class="row">
            <div class="col-lg-4 mb-4">
                <div class="card card-lms p-4 text-center">
                    <i class="bi bi-person-circle" style="font-size: 4rem; color:#0d6efd;"></i>
                    <h5 class="mt-2 mb-0">${user.fullName}</h5>
                    <span class="text-muted">@${user.username}</span>
                    <span class="badge bg-primary mt-2 align-self-center">${user.role}</span>
                    <hr>
                    <div class="text-start small">
                        <p class="mb-1"><i class="bi bi-envelope"></i> ${user.email}</p>
                        <p class="mb-0"><i class="bi bi-calendar3"></i>
                            Tham gia:
                            <fmt:formatDate value="${user.createdAt}" pattern="dd/MM/yyyy" />
                        </p>
                    </div>
                </div>
            </div>

            <div class="col-lg-8">
                <div class="card card-lms p-4">
                    <h5 class="mb-3">Cập nhật thông tin</h5>
                    <form method="post" action="${pageContext.request.contextPath}/profile">
                        <div class="mb-3">
                            <label class="form-label">Họ và tên</label>
                            <input type="text" name="fullName" class="form-control"
                                   value="${user.fullName}" required>
                        </div>
                        <div class="mb-3">
                            <label class="form-label">Email</label>
                            <input type="email" class="form-control" value="${user.email}" disabled readonly>
                            <div class="form-text text-muted small">Email không thể thay đổi.</div>
                        </div>

                        <hr>
                        <p class="text-muted small">Để trống các trường dưới đây nếu bạn không muốn đổi mật khẩu.</p>
                        <div class="row">
                            <div class="col-6 mb-3">
                                <label class="form-label">Mật khẩu mới</label>
                                <input type="password" name="newPassword" class="form-control" minlength="6">
                            </div>
                            <div class="col-6 mb-3">
                                <label class="form-label">Xác nhận mật khẩu mới</label>
                                <input type="password" name="confirmNewPassword" class="form-control" minlength="6">
                            </div>
                        </div>

                        <button type="submit" class="btn btn-primary">
                            <i class="bi bi-save"></i> Lưu thay đổi
                        </button>
                    </form>
                </div>
            </div>
        </div>
    </main>
</div>

<jsp:include page="/WEB-INF/views/common/footer.jsp" />
