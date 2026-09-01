<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Quên mật khẩu" scope="request" />
<jsp:include page="/WEB-INF/views/common/header.jsp" />

<main class="container auth-wrapper">
    <div class="row w-100 justify-content-center">
        <div class="col-md-5 col-lg-4">
            <div class="card card-lms p-4">
                <h4 class="mb-3 text-center fw-bold">Quên mật khẩu</h4>
                <p class="text-muted small text-center">
                    Nhập email đã đăng ký, chúng tôi sẽ gửi cho bạn một liên kết để đặt lại mật khẩu.
                </p>

                <c:if test="${not empty error}">
                    <div class="alert alert-danger" role="alert">${error}</div>
                </c:if>

                <c:if test="${not empty success}">
                    <div class="alert alert-success" role="alert">${success}</div>
                </c:if>

                <form method="post" action="${pageContext.request.contextPath}/forgot-password">
                    <div class="mb-3">
                        <label class="form-label">Email</label>
                        <input type="email" name="email" class="form-control" required autofocus>
                    </div>
                    <button type="submit" class="btn btn-primary w-100">Gửi liên kết đặt lại mật khẩu</button>
                </form>

                <p class="text-center mt-3 mb-0">
                    <a href="${pageContext.request.contextPath}/login">Quay lại đăng nhập</a>
                </p>
            </div>
        </div>
    </div>
</main>

<jsp:include page="/WEB-INF/views/common/footer.jsp" />
