<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<c:set var="pageTitle" value="Trang chủ" scope="request" />
<jsp:include page="/WEB-INF/views/common/header.jsp" />

<main class="flex-grow-1">
    <div class="bg-primary text-white py-5">
        <div class="container">
            <h1 class="fw-bold mb-2"><i class="bi bi-mortarboard-fill"></i> Khám phá khóa học</h1>
            <p class="mb-0 fs-5 opacity-75">
                Top khóa học được đăng ký nhiều nhất trên hệ thống LMS.
            </p>
        </div>
    </div>

    <div class="container py-4">
        <c:if test="${not empty error}">
            <div class="alert alert-danger">${error}</div>
        </c:if>

        <div class="d-flex justify-content-between align-items-center mb-3">
            <h4 class="mb-0">Khóa học nổi bật </h4>
        </div>

        <div class="row g-4">
            <c:forEach var="c" items="${courses}">
                <div class="col-md-6 col-lg-4">
                    <a href="${pageContext.request.contextPath}/courses/detail?id=${c.id}"
                       class="text-decoration-none text-reset d-block h-100">
                        <div class="card card-lms h-100 p-3">
                            <div class="d-flex justify-content-between align-items-start mb-2">
                                <span class="badge bg-success">PUBLISHED</span>
                                <span class="text-muted small">${c.durationHours} giờ</span>
                            </div>
                            <h5 class="mb-1">${c.title}</h5>
                            <p class="text-muted small mb-2">
                                <i class="bi bi-bookmark"></i>
                                <c:choose>
                                    <c:when test="${not empty c.category}">${c.category}</c:when>
                                    <c:otherwise>Chưa phân loại</c:otherwise>
                                </c:choose>
                            </p>
                            <p class="mb-3" style="min-height: 3em;">
                                <c:choose>
                                    <c:when test="${fn:length(c.description) > 120}">
                                        ${fn:substring(c.description, 0, 120)}...
                                    </c:when>
                                    <c:otherwise>${c.description}</c:otherwise>
                                </c:choose>
                            </p>
                            <div class="mt-auto d-flex justify-content-between align-items-center small text-muted mb-2">
                                <span><i class="bi bi-person-workspace"></i> ${c.expertName}</span>
                                <span><i class="bi bi-people-fill"></i> ${c.enrollmentCount} học viên</span>
                            </div>
                            <div class="border-top pt-2 text-primary small fw-semibold">
                                Xem chi tiết <i class="bi bi-arrow-right"></i>
                            </div>
                        </div>
                    </a>
                </div>
            </c:forEach>

            <c:if test="${empty courses}">
                <div class="col-12">
                    <div class="text-center text-muted py-5">
                        <i class="bi bi-inbox" style="font-size: 3rem;"></i>
                        <p class="mt-2 mb-0">Hiện chưa có khóa học nào đang hoạt động.</p>
                    </div>
                </div>
            </c:if>
        </div>
    </div>
</main>

<jsp:include page="/WEB-INF/views/common/footer.jsp" />
