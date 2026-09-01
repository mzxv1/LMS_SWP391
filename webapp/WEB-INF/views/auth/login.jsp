<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Đăng nhập" scope="request" />
<jsp:include page="/WEB-INF/views/common/header.jsp" />

<main class="container auth-wrapper">
    <div class="row w-100 justify-content-center">
        <div class="col-md-5 col-lg-4">
            <div class="card card-lms p-4">
                <h4 class="mb-3 text-center fw-bold">Đăng nhập</h4>

                <c:if test="${not empty sessionScope.flashSuccess}">
                    <div class="alert alert-success alert-auto-dismiss" role="alert">
                            ${sessionScope.flashSuccess}
                    </div>
                    <c:remove var="flashSuccess" scope="session" />
                </c:if>

                <c:if test="${not empty error}">
                    <div class="alert alert-danger" role="alert">${error}</div>
                </c:if>

                <form method="post" action="${pageContext.request.contextPath}/login">
                    <c:if test="${not empty param.next}">
                        <input type="hidden" name="next" value="${param.next}" />
                    </c:if>
                    <div class="mb-3">
                        <label class="form-label">Tên đăng nhập</label>
                        <input type="text" name="username" class="form-control"
                               value="${username}" required autofocus>
                    </div>
                    <div class="mb-3">
                        <label class="form-label">Mật khẩu</label>
                        <input type="password" name="password" class="form-control" required>
                    </div>
                    <div class="text-end mb-3">
                        <a href="${pageContext.request.contextPath}/forgot-password" class="small">Quên mật khẩu?</a>
                    </div>
                    <button type="submit" class="btn btn-primary w-100">Đăng nhập</button>
                </form>

                <c:if test="${googleEnabled}">
                    <div class="d-flex align-items-center my-3">
                        <hr class="flex-grow-1">
                        <span class="px-2 text-muted small">hoặc</span>
                        <hr class="flex-grow-1">
                    </div>
                    <a href="${pageContext.request.contextPath}/login/google<c:if test="${not empty param.next}">?next=${param.next}</c:if>"
                       class="btn btn-outline-secondary w-100">
                        <i class="bi bi-google"></i>
                        Đăng nhập với Google
                    </a>
                </c:if>

                <p class="text-center mt-3 mb-0">
                    Chưa có tài khoản?
                    <a href="${pageContext.request.contextPath}/register">Đăng ký ngay</a>
                </p>
            </div>
        </div>
    </div>
</main>

<jsp:include page="/WEB-INF/views/common/footer.jsp" />
