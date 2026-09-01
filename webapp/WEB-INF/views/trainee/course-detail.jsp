<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>

<c:set var="pageTitle" value="${course.title}" scope="request" />

<jsp:include page="/WEB-INF/views/common/header.jsp" />

<div class="d-flex flex-grow-1">
    <jsp:include page="/WEB-INF/views/common/sidebar.jsp" />

    <main class="flex-grow-1 p-4">
        <div class="mb-3">
            <a href="${pageContext.request.contextPath}/trainee/dashboard"
               class="text-decoration-none">
                <i class="bi bi-arrow-left"></i> Quay lại Trainee Dashboard
            </a>
        </div>

        <div class="card card-lms mb-4">
            <div class="card-body p-4">
                <div class="row align-items-center">
                    <div class="col-lg-8">
                        <c:if test="${not empty course.category}">
                            <span class="badge bg-primary mb-2">${course.category}</span>
                        </c:if>
                        <span class="badge bg-light text-dark border mb-2">${course.enrollmentStatus}</span>
                        <h2 class="mb-2">${course.title}</h2>
                        <p class="text-muted mb-3">${course.description}</p>
                        <div class="d-flex flex-wrap gap-3 text-muted small">
                            <span><i class="bi bi-clock"></i> ${course.durationHours} giờ</span>
                            <span><i class="bi bi-calendar3"></i> Đăng ký: <fmt:formatDate value="${course.enrolledAt}" pattern="dd/MM/yyyy" /></span>
                        </div>
                    </div>

                    <div class="col-lg-4 mt-4 mt-lg-0">
                        <div class="text-lg-end">
                            <div class="text-muted small mb-1">Tiến độ khóa học</div>
                            <div class="fs-2 fw-bold">${course.progressPercent}%</div>
                            <div class="progress mt-2" style="height: 10px;">
                                <div class="progress-bar" role="progressbar"
                                     style="width: ${course.progressPercent}%"
                                     aria-valuenow="${course.progressPercent}"
                                     aria-valuemin="0" aria-valuemax="100"></div>
                            </div>
                            <div class="text-muted small mt-2">
                                ${course.completedLessons} / ${course.totalLessons} bài đã hoàn thành
                            </div>
                        </div>
                    </div>
                </div>

                <div class="mt-4">
                    <a href="${pageContext.request.contextPath}/trainee/courses/lessons?id=${course.courseId}"
                       class="btn btn-primary">
                        <i class="bi bi-play-circle"></i> Tiếp tục học
                    </a>
                    <a href="${pageContext.request.contextPath}/trainee/courses/lessons?id=${course.courseId}"
                       class="btn btn-outline-primary ms-2">
                        Xem tất cả bài học
                    </a>
                    <a href="${pageContext.request.contextPath}/courses/quizzes?courseId=${course.courseId}"
                       class="btn btn-outline-success ms-2">
                        <i class="bi bi-clipboard2-check"></i> Làm quiz
                    </a>
                </div>
            </div>
        </div>

        <div class="d-flex justify-content-between align-items-center mb-3">
            <h4 class="mb-0">Xem trước nội dung khóa học</h4>
            <span class="text-muted small">${course.totalLessons} bài học</span>
        </div>

        <div class="card card-lms">
            <div class="list-group list-group-flush">
                <c:forEach var="lesson" items="${course.lessonPreview}">
                    <div class="list-group-item d-flex justify-content-between align-items-center py-3">
                        <div>
                            <c:choose>
                                <c:when test="${lesson.completed}">
                                    <i class="bi bi-check-circle-fill text-success me-2"></i>
                                </c:when>
                                <c:otherwise>
                                    <i class="bi bi-play-circle text-primary me-2"></i>
                                </c:otherwise>
                            </c:choose>
                            <span class="fw-semibold">Bài ${lesson.orderIndex}: ${lesson.title}</span>
                        </div>
                        <span class="text-muted small">${lesson.durationMinutes} phút</span>
                    </div>
                </c:forEach>

                <c:if test="${empty course.lessonPreview}">
                    <div class="text-center text-muted py-5">
                        Khóa học chưa có bài học.
                    </div>
                </c:if>
            </div>
        </div>
    </main>
</div>

<jsp:include page="/WEB-INF/views/common/footer.jsp" />
