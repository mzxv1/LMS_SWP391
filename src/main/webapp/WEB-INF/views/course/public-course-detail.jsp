<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<c:set var="ctx" value="${pageContext.request.contextPath}" />
<c:set var="currentUser" value="${sessionScope.currentUser}" />
<c:set var="pageTitle" value="${course.title}" />

<jsp:include page="/WEB-INF/views/common/header.jsp" />

<div class="container py-5">

    <c:if test="${not empty sessionScope.errorMsg}">
        <div class="alert alert-danger alert-dismissible fade show mb-4" role="alert">
            <i class="bi bi-exclamation-triangle-fill me-2"></i>
            ${sessionScope.errorMsg}
            <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
        </div>
        <c:remove var="errorMsg" scope="session" />
    </c:if>

    <!-- =========================================================
         BREADCRUMB
         ========================================================= -->
    <nav aria-label="breadcrumb" class="mb-4">
        <ol class="breadcrumb">
            <li class="breadcrumb-item">
                <a href="${ctx}/home" class="text-decoration-none">
                    <i class="bi bi-house-door"></i>
                    Trang chủ
                </a>
            </li>

            <li class="breadcrumb-item">
                <a href="${ctx}/courses" class="text-decoration-none">
                    Khóa học
                </a>
            </li>

            <li class="breadcrumb-item active" aria-current="page">
                Chi tiết khóa học
            </li>
        </ol>
    </nav>


    <!-- =========================================================
         COURSE HEADER
         ========================================================= -->
    <div class="card border-0 shadow-sm mb-4">
        <div class="card-body p-4 p-lg-5">

            <div class="row g-4">

                <!-- LEFT: COURSE INFORMATION -->
                <div class="col-lg-8">

                    <!-- CATEGORY -->
                    <c:if test="${not empty course.category}">
                        <span class="badge bg-primary-subtle text-primary mb-3">
                            <i class="bi bi-folder"></i>
                            ${course.category}
                        </span>
                    </c:if>


                    <!-- TITLE -->
                    <h1 class="fw-bold mb-3">
                        ${course.title}
                    </h1>


                    <!-- DESCRIPTION -->
                    <p class="text-muted fs-5 mb-4">
                        ${course.description}
                    </p>


                    <!-- COURSE META -->
                    <div class="d-flex flex-wrap gap-4 text-muted">

                        <!-- EXPERT -->
                        <div>
                            <i class="bi bi-person-circle text-primary"></i>
                            <span class="ms-1">
                                ${course.expertName}
                            </span>
                        </div>


                        <!-- DURATION -->
                        <div>
                            <i class="bi bi-clock text-primary"></i>
                            <span class="ms-1">
                                ${course.durationHours} giờ
                            </span>
                        </div>


                        <!-- LESSON COUNT -->
                        <div>
                            <i class="bi bi-play-circle text-primary"></i>
                            <span class="ms-1">
                                ${course.lessonCount} bài học
                            </span>
                        </div>

                    </div>


                    <!-- RATING -->
                    <div class="mt-4">

                        <c:choose>

                            <c:when test="${course.reviewCount > 0}">

                                <span class="fw-semibold me-2">
                                        ${course.averageRating}
                                </span>

                                <span class="text-warning">
                                    <c:choose>

                                        <c:when test="${course.averageRating >= 5}">
                                            ★★★★★
                                        </c:when>

                                        <c:when test="${course.averageRating >= 4}">
                                            ★★★★☆
                                        </c:when>

                                        <c:when test="${course.averageRating >= 3}">
                                            ★★★☆☆
                                        </c:when>

                                        <c:when test="${course.averageRating >= 2}">
                                            ★★☆☆☆
                                        </c:when>

                                        <c:otherwise>
                                            ★☆☆☆☆
                                        </c:otherwise>

                                    </c:choose>
                                </span>

                                <span class="text-muted ms-2">
                                    (${course.reviewCount} đánh giá)
                                </span>

                            </c:when>

                            <c:otherwise>

                                <span class="text-muted">
                                    <i class="bi bi-star"></i>
                                    Chưa có đánh giá
                                </span>

                            </c:otherwise>

                        </c:choose>

                    </div>

                </div>


                <!-- RIGHT: PRICE / CTA -->
                <div class="col-lg-4">

                    <div class="card border shadow-sm h-100">

                        <div class="card-body p-4">

                            <!-- PRICE -->
                            <div class="mb-4">

                                <div class="text-muted small mb-1">
                                    Học phí
                                </div>

                                <div class="fs-2 fw-bold text-primary">
                                    <c:choose>

                                        <c:when test="${course.price != null}">
                                            ${course.price} ₫
                                        </c:when>

                                        <c:otherwise>
                                            Miễn phí
                                        </c:otherwise>

                                    </c:choose>
                                </div>

                            </div>


                            <!-- CTA -->
                            <c:choose>

                                <c:when test="${not empty currentUser}">

                                    <c:choose>

                                        <c:when test="${currentUser.role == 'STUDENT'}">

                                            <c:choose>
                                                <c:when test="${isEnrolled}">
                                                    <a href="${ctx}/trainee/courses/lessons?id=${course.id}" class="btn btn-primary btn-lg w-100 mb-2">
                                                        <i class="bi bi-play-circle"></i>
                                                        Vào học
                                                    </a>
                                                    <a href="${ctx}/courses/quizzes?courseId=${course.id}" class="btn btn-outline-primary btn-lg w-100 mb-2">
                                                        <i class="bi bi-clipboard2-check"></i>
                                                        Làm quiz
                                                    </a>
                                                    <div class="text-center text-success small">
                                                        Bạn đã đăng ký khóa học này
                                                    </div>
                                                </c:when>
                                                <c:otherwise>
                                                    <a href="${ctx}/checkout?id=${course.id}" class="btn btn-primary btn-lg w-100 mb-2">
                                                        <i class="bi bi-cart-plus"></i>
                                                        Đăng ký khóa học
                                                    </a>
                                                    <div class="text-center text-muted small">
                                                        Đăng ký để bắt đầu học
                                                    </div>
                                                </c:otherwise>
                                            </c:choose>

                                        </c:when>

                                        <c:otherwise>

                                            <div class="alert alert-info mb-0">
                                                <i class="bi bi-info-circle"></i>
                                                Bạn đang đăng nhập với vai trò
                                                <strong>${currentUser.role}</strong>.
                                            </div>

                                        </c:otherwise>

                                    </c:choose>

                                </c:when>


                                <c:otherwise>

                                    <a
                                            href="${ctx}/login"
                                            class="btn btn-primary btn-lg w-100 mb-2">
                                        <i class="bi bi-box-arrow-in-right"></i>
                                        Đăng nhập để đăng ký
                                    </a>

                                    <div class="text-center text-muted small">
                                        Chưa có tài khoản?
                                        <a href="${ctx}/register">
                                            Đăng ký ngay
                                        </a>
                                    </div>

                                </c:otherwise>

                            </c:choose>

                        </div>

                    </div>

                </div>

            </div>

        </div>
    </div>


    <!-- =========================================================
         COURSE CONTENT
         ========================================================= -->
    <div class="row g-4">

        <!-- LESSON LIST -->
        <div class="col-lg-8">

            <div class="card border-0 shadow-sm">

                <div class="card-body p-4">

                    <div class="d-flex justify-content-between align-items-center mb-4 flex-wrap gap-2">

                        <div>
                            <h3 class="fw-bold mb-1">
                                Nội dung khóa học
                            </h3>

                            <div class="text-muted">
                                ${course.lessonCount} bài học
                            </div>
                        </div>

                        <div class="d-flex align-items-center gap-3">
                            <c:if test="${not empty currentUser}">
                                <c:if test="${isEnrolled}">
                                    <a href="${ctx}/courses/quizzes?courseId=${course.id}" class="btn btn-outline-primary btn-sm">
                                        <i class="bi bi-clipboard2-check"></i> Làm quiz
                                    </a>
                                </c:if>
                            </c:if>
                            <i class="bi bi-journal-play fs-2 text-primary"></i>
                        </div>

                    </div>


                    <!-- =================================================
                         PUBLIC LESSON PREVIEW
                         Backend already returns only the first 3 lessons.
                         ================================================= -->

                    <c:choose>

                        <c:when test="${not empty course.lessons}">

                            <div class="list-group list-group-flush">

                                <c:forEach
                                        var="lesson"
                                        items="${course.lessons}"
                                        varStatus="status">

                                    <div class="list-group-item px-0 py-3">

                                        <div class="d-flex align-items-center">

                                            <!-- LESSON NUMBER -->
                                            <div
                                                    class="rounded-circle bg-primary-subtle text-primary
                                                           d-flex align-items-center justify-content-center
                                                           flex-shrink-0"
                                                    style="width: 40px; height: 40px;">

                                                    ${status.index + 1}

                                            </div>


                                            <!-- LESSON INFO -->
                                            <div class="ms-3 flex-grow-1">

                                                <div class="fw-semibold">
                                                        ${lesson.title}
                                                </div>

                                                <div class="text-muted small mt-1">

                                                    <i class="bi bi-clock"></i>

                                                        ${lesson.durationMinutes} phút

                                                </div>

                                            </div>


                                            <!-- LOCK -->
                                            <div class="text-muted">

                                                <i class="bi bi-lock"></i>

                                            </div>

                                        </div>

                                    </div>

                                </c:forEach>

                            </div>


                            <!-- LOCKED LESSONS -->
                            <c:if test="${course.lessonCount > 3}">

                                <div class="mt-4 p-4 bg-light rounded text-center">

                                    <div class="mb-2">
                                        <i class="bi bi-lock-fill fs-3 text-secondary"></i>
                                    </div>

                                    <div class="fw-semibold mb-1">
                                        Còn ${course.lessonCount - 3} bài học
                                    </div>

                                    <c:choose>
                                        <c:when test="${isEnrolled}">
                                            <div class="text-muted small mb-3">
                                                Bạn đã đăng ký khóa học này.
                                            </div>
                                        </c:when>
                                        <c:otherwise>
                                            <div class="text-muted small mb-3">
                                                Đăng ký khóa học để truy cập toàn bộ nội dung.
                                            </div>
                                        </c:otherwise>
                                    </c:choose>


                                    <c:choose>

                                        <c:when test="${currentUser.role == 'STUDENT'}">
                                            <c:choose>
                                                <c:when test="${isEnrolled}">
                                                    <a href="${ctx}/trainee/courses/lessons?id=${course.id}" class="btn btn-primary btn-lg w-100 mb-2">
                                                        <i class="bi bi-play-circle"></i>
                                                        Vào học
                                                    </a>
                                                </c:when>
                                                <c:otherwise>
                                                    <a href="${ctx}/checkout?id=${course.id}" class="btn btn-primary btn-lg w-100 mb-2">
                                                        <i class="bi bi-cart-plus"></i>
                                                        Đăng ký khóa học
                                                    </a>
                                                    <div class="text-center text-muted small">
                                                        Đăng ký để bắt đầu học
                                                    </div>
                                                </c:otherwise>
                                            </c:choose>
                                        </c:when>


                                    </c:choose>

                                </div>

                            </c:if>


                            <!-- ALL LESSONS ARE VISIBLE -->
                            <c:if test="${course.lessonCount <= 3}">

                                <div class="mt-4 p-3 bg-light rounded text-center">

                                    <span class="text-muted small">
                                        <i class="bi bi-check-circle"></i>
                                        Đây là toàn bộ nội dung bài học hiện có.
                                    </span>

                                </div>

                            </c:if>

                        </c:when>


                        <c:otherwise>

                            <div class="text-center py-5">

                                <i class="bi bi-journal-x fs-1 text-muted"></i>

                                <p class="text-muted mt-3 mb-0">
                                    Khóa học chưa có bài học.
                                </p>

                            </div>

                        </c:otherwise>

                    </c:choose>

                </div>

            </div>

        </div>


        <!-- COURSE SUMMARY -->
        <div class="col-lg-4">

            <div class="card border-0 shadow-sm">

                <div class="card-body p-4">

                    <h5 class="fw-bold mb-4">
                        <i class="bi bi-info-circle text-primary"></i>
                        Thông tin khóa học
                    </h5>


                    <!-- CATEGORY -->
                    <div class="d-flex justify-content-between py-2 border-bottom">

                        <span class="text-muted">
                            Danh mục
                        </span>

                        <span class="fw-semibold text-end">
                            ${course.category}
                        </span>

                    </div>


                    <!-- DURATION -->
                    <div class="d-flex justify-content-between py-2 border-bottom">

                        <span class="text-muted">
                            Thời lượng
                        </span>

                        <span class="fw-semibold">
                            ${course.durationHours} giờ
                        </span>

                    </div>


                    <!-- LESSONS -->
                    <div class="d-flex justify-content-between py-2 border-bottom">

                        <span class="text-muted">
                            Số bài học
                        </span>

                        <span class="fw-semibold">
                            ${course.lessonCount}
                        </span>

                    </div>


                    <!-- EXPERT -->
                    <div class="d-flex justify-content-between py-2">

                        <span class="text-muted">
                            Giảng viên
                        </span>

                        <span class="fw-semibold text-end">
                            ${course.expertName}
                        </span>

                    </div>

                </div>

            </div>


            <!-- BACK TO COURSE LIST -->
            <div class="mt-3">

                <a
                        href="${ctx}/courses"
                        class="btn btn-outline-secondary w-100">

                    <i class="bi bi-arrow-left"></i>
                    Quay lại danh sách khóa học

                </a>

            </div>

        </div>

    </div>

</div>


<!-- =============================================================
FOOTER
============================================================= -->
<jsp:include page="/WEB-INF/views/common/footer.jsp" />