<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Đăng ký" scope="request" />
<jsp:include page="/WEB-INF/views/common/header.jsp" />

<main class="container auth-wrapper py-4">
    <div class="row w-100 justify-content-center">
        <div class="col-md-6 col-lg-5">
            <div class="card card-lms p-4">
                <h4 class="mb-3 text-center fw-bold">Tạo tài khoản học viên</h4>

                <c:if test="${not empty error}">
                    <div class="alert alert-danger" role="alert">${error}</div>
                </c:if>

                <c:if test="${not empty success}">
                    <div class="alert alert-success" role="alert">${success}</div>
                </c:if>

                <form method="post" action="${pageContext.request.contextPath}/register">
                    <div class="mb-3">
                        <label class="form-label">Họ và tên</label>
                        <input type="text" name="fullName" class="form-control"
                               value="${formData.fullName}" required>
                    </div>
                    <div class="mb-3">
                        <label class="form-label">Tên đăng nhập</label>
                        <input type="text" name="username" class="form-control"
                               value="${formData.username}" required minlength="4">
                    </div>
                    <div class="mb-3">
                        <label class="form-label">Email</label>
                        <input type="email" name="email" class="form-control"
                               value="${formData.email}" required>
                    </div>
                    <div class="row">
                        <div class="col-6 mb-3">
                            <label class="form-label">Mật khẩu</label>
                            <input type="password" name="password" class="form-control" required minlength="6">
                        </div>
                        <div class="col-6 mb-3">
                            <label class="form-label">Xác nhận mật khẩu</label>
                            <input type="password" name="confirmPassword" class="form-control" required minlength="6">
                        </div>
                    </div>
                    <button type="submit" class="btn btn-primary w-100">Đăng ký</button>
                </form>

                <c:if test="${googleEnabled}">
                    <div class="d-flex align-items-center my-3">
                        <hr class="flex-grow-1">
                        <span class="px-2 text-muted small">hoặc</span>
                        <hr class="flex-grow-1">
                    </div>
                    <a href="${pageContext.request.contextPath}/login/google" class="btn btn-outline-secondary w-100">
                        <i class="bi bi-google"></i>
                        Đăng ký với Google
                    </a>
                    <p class="text-center text-muted small mt-2 mb-0">
                        Tài khoản học viên sẽ được tạo tự động cho lần đăng nhập Google đầu tiên.
                    </p>
                </c:if>

                <p class="text-center mt-3 mb-0">
                    Đã có tài khoản?
                    <a href="${pageContext.request.contextPath}/login">Đăng nhập</a>
                </p>
            </div>
        </div>
    </div>
</main>

<jsp:include page="/WEB-INF/views/common/footer.jsp" />
