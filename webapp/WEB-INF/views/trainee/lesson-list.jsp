<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<c:set var="pageTitle" value="Bài học - ${course.title}" scope="request" />

<jsp:include page="/WEB-INF/views/common/header.jsp" />

<div class="d-flex flex-grow-1">
    <jsp:include page="/WEB-INF/views/common/sidebar.jsp" />

    <main class="flex-grow-1 p-4">
        <div class="mb-3">
            <a href="${pageContext.request.contextPath}/trainee/courses/detail?id=${course.courseId}"
               class="text-decoration-none">
                <i class="bi bi-arrow-left"></i> Quay lại khóa học
            </a>
        </div>

        <div class="d-flex justify-content-between align-items-end mb-4">
            <div>
                <h3 class="mb-1">Bài học: ${course.title}</h3>
                <p class="text-muted mb-0">
                    ${course.completedLessons} / ${course.totalLessons} bài đã hoàn thành · ${course.progressPercent}%
                </p>
            </div>
        </div>

        <!-- Tabs navigation -->
        <ul class="nav nav-tabs mb-4" id="courseContentTab" role="tablist">
            <li class="nav-item" role="presentation">
                <button class="nav-link active fw-semibold" id="lessons-tab" data-bs-toggle="tab" 
                        data-bs-target="#lessons-tab-pane" type="button" role="tab" 
                        aria-controls="lessons-tab-pane" aria-selected="true">
                    <i class="bi bi-journal-play me-1"></i> Bài học
                </button>
            </li>
            <li class="nav-item" role="presentation">
                <button class="nav-link fw-semibold" id="quizzes-tab" data-bs-toggle="tab" 
                        data-bs-target="#quizzes-tab-pane" type="button" role="tab" 
                        aria-controls="quizzes-tab-pane" aria-selected="false">
                    <i class="bi bi-clipboard2-check me-1"></i> Bài kiểm tra
                    <c:set var="totalQuizzes" value="0" />
                    <c:forEach var="chap" items="${course.chapters}">
                        <c:set var="totalQuizzes" value="${totalQuizzes + chap.quizzes.size()}" />
                    </c:forEach>
                    <c:if test="${totalQuizzes > 0}">
                        <span class="badge bg-warning text-dark ms-1">${totalQuizzes}</span>
                    </c:if>
                </button>
            </li>
        </ul>

        <div class="tab-content" id="courseContentTabContent">
            <!-- TAB 1: BÀI HỌC -->
            <div class="tab-pane fade show active" id="lessons-tab-pane" role="tabpanel" aria-labelledby="lessons-tab" tabindex="0">
                <c:forEach var="chapter" items="${course.chapters}" varStatus="chapterStatus">
                    <div class="card card-lms mb-3">
                        <div class="card-header bg-white">
                            <span class="fw-semibold">Chương ${chapterStatus.index + 1}: ${chapter.title}</span>
                        </div>
                        <div class="list-group list-group-flush">
                            <c:forEach var="lesson" items="${chapter.lessons}">
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
                                        <span class="text-muted small ms-2">${lesson.durationMinutes} phút</span>
                                    </div>
                                    <a href="${pageContext.request.contextPath}/trainee/lessons/detail?id=${lesson.id}&courseId=${course.courseId}"
                                       class="btn btn-sm btn-outline-primary">
                                        <c:choose>
                                            <c:when test="${lesson.completed}">Xem lại</c:when>
                                            <c:otherwise>Bắt đầu học</c:otherwise>
                                        </c:choose>
                                    </a>
                                </div>
                            </c:forEach>

                            <c:if test="${empty chapter.lessons}">
                                <div class="list-group-item text-muted text-center py-4">Chương này chưa có bài học.</div>
                            </c:if>
                        </div>
                    </div>
                </c:forEach>

                <c:if test="${empty course.chapters}">
                    <div class="card card-lms">
                        <div class="card-body text-center text-muted py-5">
                            <i class="bi bi-journal-x display-5"></i>
                            <p class="mt-3 mb-0">Khóa học chưa có bài học.</p>
                        </div>
                    </div>
                </c:if>
            </div>

            <!-- TAB 2: BÀI KIỂM TRA -->
            <div class="tab-pane fade" id="quizzes-tab-pane" role="tabpanel" aria-labelledby="quizzes-tab" tabindex="0">
                <c:set var="hasQuizzes" value="false" />
                <c:forEach var="chapter" items="${course.chapters}" varStatus="chapterStatus">
                    <c:if test="${not empty chapter.quizzes}">
                        <c:set var="hasQuizzes" value="true" />
                        <div class="card card-lms mb-3">
                            <div class="card-header bg-white d-flex justify-content-between align-items-center">
                                <span class="fw-semibold">Chương ${chapterStatus.index + 1}: ${chapter.title}</span>
                                <span class="badge bg-light text-secondary border">${chapter.quizzes.size()} bài kiểm tra</span>
                            </div>
                            <div class="list-group list-group-flush">
                                <c:forEach var="quiz" items="${chapter.quizzes}">
                                    <div class="list-group-item d-flex justify-content-between align-items-center py-3">
                                        <div>
                                            <i class="bi bi-clipboard2-check-fill text-warning me-2" style="font-size: 1.1rem;"></i>
                                            <span class="fw-bold text-dark">Bài kiểm tra: ${quiz.title}</span>
                                            <span class="text-muted small ms-2">(${quiz.totalQuestions} câu hỏi · ${quiz.timeLimitMin} phút)</span>
                                            <c:if test="${quiz.attemptCount > 0}">
                                                <span class="badge bg-secondary ms-2">Đã làm ${quiz.attemptCount} lần</span>
                                                <c:choose>
                                                    <c:when test="${quiz.lastAttemptPassed}">
                                                        <span class="badge bg-success ms-1">Đạt (${quiz.lastAttemptScore}%)</span>
                                                    </c:when>
                                                    <c:otherwise>
                                                        <span class="badge bg-danger ms-1">Chưa đạt (${quiz.lastAttemptScore}%)</span>
                                                    </c:otherwise>
                                                </c:choose>
                                            </c:if>
                                        </div>
                                        <a href="${pageContext.request.contextPath}/courses/quizzes/detail?id=${quiz.id}"
                                           class="btn btn-sm btn-warning fw-semibold text-dark">
                                            <c:choose>
                                                <c:when test="${quiz.attemptCount > 0}">Làm lại</c:when>
                                                <c:otherwise>Bắt đầu làm</c:otherwise>
                                            </c:choose>
                                        </a>
                                    </div>
                                </c:forEach>
                            </div>
                        </div>
                    </c:if>
                </c:forEach>

                <c:if test="${not hasQuizzes}">
                    <div class="card card-lms">
                        <div class="card-body text-center text-muted py-5">
                            <i class="bi bi-clipboard2-x display-5 text-warning"></i>
                            <p class="mt-3 mb-0">Khóa học này chưa có bài kiểm tra nào.</p>
                        </div>
                    </div>
                </c:if>
            </div>
        </div>
    </main>
</div>

<jsp:include page="/WEB-INF/views/common/footer.jsp" />
