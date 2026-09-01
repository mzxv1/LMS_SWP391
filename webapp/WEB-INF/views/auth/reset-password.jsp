<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Đặt lại mật khẩu" scope="request" />
<jsp:include page="/WEB-INF/views/common/header.jsp" />

<main class="container auth-wrapper">
    <div class="row w-100 justify-content-center">
        <div class="col-md-5 col-lg-4">
            <div class="card card-lms p-4">
                <h4 class="mb-3 text-center fw-bold">Đặt lại mật khẩu</h4>

                <c:if test="${not empty error}">
                    <div class="alert alert-danger" role="alert">${error}</div>
                </c:if>

                <c:choose>
                    <c:when test="${not empty token}">
                        <form method="post" action="${pageContext.request.contextPath}/reset-password">
                            <input type="hidden" name="token" value="${token}" />
                            <div class="mb-3">
                                <label class="form-label">Mật khẩu mới</label>
                                <input type="password" name="newPassword" class="form-control"
                                       required minlength="6" autofocus>
                            </div>
                            <div class="mb-3">
                                <label class="form-label">Xác nhận mật khẩu mới</label>
                                <input type="password" name="confirmPassword" class="form-control"
                                       required minlength="6">
                            </div>
                            <button type="submit" class="btn btn-primary w-100">Đặt lại mật khẩu</button>
                        </form>
                    </c:when>
                    <c:otherwise>
                        <p class="text-center mb-0">
                            <a href="${pageContext.request.contextPath}/forgot-password">Yêu cầu liên kết mới</a>
                        </p>
                    </c:otherwise>
                </c:choose>

                <p class="text-center mt-3 mb-0">
                    <a href="${pageContext.request.contextPath}/login">Quay lại đăng nhập</a>
                </p>
            </div>
        </div>
    </div>
</main>

<jsp:include page="/WEB-INF/views/common/footer.jsp" />
