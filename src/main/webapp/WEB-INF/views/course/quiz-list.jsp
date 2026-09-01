<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>

<c:set var="pageTitle" value="Danh sách bài kiểm tra" scope="request" />
<jsp:include page="/WEB-INF/views/common/header.jsp" />

<!-- Calculate progress statistics -->
<c:set var="totalQuizzes" value="0" />
<c:set var="completedQuizzes" value="0" />
<c:forEach var="quiz" items="${quizzes}">
    <c:set var="totalQuizzes" value="${totalQuizzes + 1}" />
    <c:if test="${quiz.lastAttemptPassed != null && quiz.lastAttemptPassed}">
        <c:set var="completedQuizzes" value="${completedQuizzes + 1}" />
    </c:if>
</c:forEach>
<c:set var="progressPercent" value="0" />
<c:if test="${totalQuizzes > 0}">
    <c:set var="progressPercent" value="${(completedQuizzes * 100) / totalQuizzes}" />
</c:if>

<div class="container py-5" style="min-height: 80vh;">
    <!-- Breadcrumb -->
    <nav aria-label="breadcrumb" class="mb-4">
        <ol class="breadcrumb bg-transparent p-0 mb-0">
            <li class="breadcrumb-item">
                <a href="${pageContext.request.contextPath}/home" class="text-decoration-none">
                    <i class="bi bi-house-door"></i> Trang chủ
                </a>
            </li>
            <li class="breadcrumb-item">
                <a href="${pageContext.request.contextPath}/courses" class="text-decoration-none">
                    Khóa học
                </a>
            </li>
            <li class="breadcrumb-item">
                <a href="${pageContext.request.contextPath}/courses/detail?id=${course.id}" class="text-decoration-none">
                    ${course.title}
                </a>
            </li>
            <li class="breadcrumb-item active" aria-current="page">Bài kiểm tra</li>
        </ol>
    </nav>

    <div class="row g-4">
        <!-- Left: Quizzes List (col-md-8) -->
        <div class="col-lg-8">
            <!-- Header & Tabs Filter -->
            <div class="card border-0 shadow-sm p-4 mb-4">
                <div class="d-flex justify-content-between align-items-center flex-wrap gap-3">
                    <div>
                        <h4 class="fw-bold mb-1 text-dark">Danh sách bài kiểm tra</h4>
                        <p class="text-muted small mb-0">Học và đánh giá năng lực của bạn thông qua các bài kiểm tra trắc nghiệm dưới đây.</p>
                    </div>
                    <div class="btn-group" role="group" aria-label="Filter status buttons">
                        <button type="button" class="btn btn-outline-primary active btn-sm px-3" onclick="filterQuizzes('all', this)">Tất cả</button>
                        <button type="button" class="btn btn-outline-primary btn-sm px-3" onclick="filterQuizzes('not-started', this)">Chưa làm</button>
                        <button type="button" class="btn btn-outline-primary btn-sm px-3" onclick="filterQuizzes('completed', this)">Đã hoàn thành</button>
                    </div>
                </div>
            </div>

            <!-- Quizzes Container -->
            <div class="quiz-container">
                <c:forEach var="quiz" items="${quizzes}">
                    <c:set var="status" value="not-started" />
                    <c:choose>
                        <c:when test="${quiz.lastAttemptStartedAt == null}">
                            <c:set var="status" value="not-started" />
                        </c:when>
                        <c:when test="${quiz.lastAttemptSubmittedAt == null}">
                            <c:set var="status" value="in-progress" />
                        </c:when>
                        <c:when test="${quiz.lastAttemptPassed != null && quiz.lastAttemptPassed}">
                            <c:set var="status" value="completed-passed" />
                        </c:when>
                        <c:otherwise>
                            <c:set var="status" value="completed-failed" />
                        </c:otherwise>
                    </c:choose>

                    <div class="card border-0 shadow-sm mb-4 quiz-card" data-status="${status}">
                        <div class="card-body p-4">
                            <!-- Title -->
                            <h5 class="fw-bold text-dark mb-3">
                                <i class="bi bi-file-earmark-ruled-fill text-primary me-2"></i>${quiz.title}
                            </h5>
                            
                            <!-- Detail Info Row -->
                            <div class="d-flex flex-wrap gap-4 text-muted small mb-3 align-items-center">
                                <div>
                                    <i class="bi bi-clock me-1"></i>${quiz.timeLimitMin} phút
                                </div>
                                <div class="vr"></div>
                                <div>
                                    <i class="bi bi-question-circle me-1"></i>${quiz.totalQuestions} câu hỏi
                                </div>
                                <div class="vr"></div>
                                <div>
                                    <i class="bi bi-arrow-repeat me-1"></i>Đã làm: 
                                    <strong>
                                        <c:choose>
                                            <c:when test="${quiz.attemptCount > 0}">${quiz.attemptCount} lần</c:when>
                                            <c:otherwise>Chưa làm</c:otherwise>
                                        </c:choose>
                                    </strong>
                                </div>
                            </div>
                            
                            <hr class="my-3 text-black-50" style="opacity: 0.12;">
                            
                            <!-- Card Footer Action Row -->
                            <div class="d-flex justify-content-between align-items-center flex-wrap gap-3">
                                <!-- Status Badge & Best Score -->
                                <div class="d-flex align-items-center gap-3">
                                    <c:choose>
                                        <c:when test="${status == 'not-started'}">
                                            <span class="badge bg-secondary-subtle text-secondary rounded-pill px-3 py-2 fw-semibold">
                                                <i class="bi bi-circle me-1"></i>Chưa thi
                                            </span>
                                        </c:when>
                                        <c:when test="${status == 'in-progress'}">
                                            <span class="badge bg-warning-subtle text-warning-emphasis rounded-pill px-3 py-2 fw-semibold">
                                                <i class="bi bi-hourglass-split me-1"></i>Đang làm dở
                                            </span>
                                        </c:when>
                                        <c:when test="${status == 'completed-passed'}">
                                            <span class="badge bg-success-subtle text-success rounded-pill px-3 py-2 fw-semibold">
                                                <i class="bi bi-check-circle-fill me-1"></i>Đã đạt
                                            </span>
                                        </c:when>
                                        <c:otherwise>
                                            <span class="badge bg-danger-subtle text-danger rounded-pill px-3 py-2 fw-semibold">
                                                <i class="bi bi-exclamation-circle-fill me-1"></i>Chưa đạt
                                            </span>
                                        </c:otherwise>
                                    </c:choose>
                                    
                                    <c:if test="${quiz.lastAttemptScore != null}">
                                        <span class="fw-bold text-primary small">
                                            <i class="bi bi-trophy me-1 text-warning"></i>Điểm cao nhất: ${quiz.lastAttemptScore}%
                                        </span>
                                    </c:if>
                                </div>
                                
                                <!-- Buttons -->
                                <div>
                                    <c:choose>
                                        <c:when test="${status == 'not-started'}">
                                            <a href="${pageContext.request.contextPath}/courses/quizzes/detail?id=${quiz.id}" class="btn btn-primary px-4 rounded fw-bold text-white shadow-sm">
                                                Bắt đầu làm bài thi <i class="bi bi-chevron-right ms-1"></i>
                                            </a>
                                        </c:when>
                                        <c:when test="${status == 'in-progress'}">
                                            <a href="${pageContext.request.contextPath}/courses/quizzes/detail?id=${quiz.id}" class="btn btn-warning text-dark px-4 rounded fw-bold shadow-sm">
                                                Làm tiếp <i class="bi bi-arrow-right-short ms-1"></i>
                                            </a>
                                        </c:when>
                                        <c:otherwise>
                                            <div class="d-flex gap-2">
                                                <a href="${pageContext.request.contextPath}/courses/quizzes/result?quizId=${quiz.id}" class="btn btn-outline-success px-3 rounded fw-bold">
                                                    Kết quả <i class="bi bi-eye ms-1"></i>
                                                </a>
                                                <a href="${pageContext.request.contextPath}/courses/quizzes/detail?id=${quiz.id}" class="btn btn-primary px-3 rounded fw-bold text-white shadow-sm">
                                                    Làm lại <i class="bi bi-arrow-counterclockwise ms-1"></i>
                                                </a>
                                            </div>
                                        </c:otherwise>
                                    </c:choose>
                                </div>
                            </div>
                        </div>
                    </div>
                </c:forEach>

                <c:if test="${empty quizzes}">
                    <div class="card border-0 shadow-sm p-5 text-center text-muted">
                        <i class="bi bi-clipboard2-x fs-1 d-block mb-3 text-secondary"></i>
                        <h5>Khóa học này hiện tại chưa có bài kiểm tra nào.</h5>
                        <p class="mb-0">Vui lòng quay lại sau khi giảng viên bổ sung bài đánh giá.</p>
                    </div>
                </c:if>
            </div>
        </div>

        <!-- Right: Progress Dashboard & Top Scores (col-md-4) -->
        <div class="col-lg-4">
            <div class="position-sticky" style="top: 24px;">
                <!-- Completion Progress Card -->
                <div class="card border-0 shadow-sm mb-4">
                    <div class="card-body p-4">
                        <h6 class="fw-bold text-dark mb-3">
                            <i class="bi bi-graph-up-arrow text-primary me-2"></i>Tiến độ hoàn thành
                        </h6>
                        <fmt:formatNumber value="${progressPercent}" maxFractionDigits="0" var="formattedProgressPercent" />
                        <div class="d-flex align-items-center justify-content-between mb-3">
                            <div class="p-3 bg-light rounded text-center" style="min-width: 80px;">
                                <span class="fs-4 fw-bold text-dark">${completedQuizzes} / ${totalQuizzes}</span>
                            </div>
                            <div class="text-end">
                                <span class="text-muted small d-block">Tỷ lệ hoàn thành</span>
                                <strong class="fs-5 text-success">${formattedProgressPercent}%</strong>
                            </div>
                        </div>
                        <div class="progress rounded-pill" style="height: 8px;">
                            <div class="progress-bar bg-success rounded-pill" role="progressbar" style="width: ${formattedProgressPercent}%" aria-valuenow="${formattedProgressPercent}" aria-valuemin="0" aria-valuemax="100"></div>
                        </div>
                    </div>
                </div>

                <!-- Best Scores Card -->
                <div class="card border-0 shadow-sm">
                    <div class="card-body p-4">
                        <h6 class="fw-bold text-dark mb-3">
                            <i class="bi bi-trophy-fill text-warning me-2"></i>Thành tích cao nhất
                        </h6>
                        <ul class="list-group list-group-flush">
                            <c:forEach var="q" items="${quizzes}">
                                <li class="list-group-item d-flex justify-content-between align-items-center px-0 py-3 border-light-subtle">
                                    <div class="text-truncate me-2" style="max-width: 200px;">
                                        <span class="fw-semibold text-dark small">${q.title}</span>
                                    </div>
                                    <c:choose>
                                        <c:when test="${q.lastAttemptScore != null}">
                                            <span class="badge bg-success-subtle text-success fw-bold rounded-pill px-2.5 py-1.5">${q.lastAttemptScore}%</span>
                                        </c:when>
                                        <c:otherwise>
                                            <span class="badge bg-light text-muted fw-normal rounded-pill px-2.5 py-1.5">Chưa làm</span>
                                        </c:otherwise>
                                    </c:choose>
                                </li>
                            </c:forEach>
                        </ul>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>

<script>
    function filterQuizzes(status, btn) {
        // Toggle active class on buttons
        const buttons = btn.parentElement.querySelectorAll('.btn');
        buttons.forEach(b => b.classList.remove('active'));
        btn.classList.add('active');

        // Filter quiz cards
        const cards = document.querySelectorAll('.quiz-card');
        cards.forEach(card => {
            const cardStatus = card.getAttribute('data-status');
            if (status === 'all') {
                card.style.display = 'block';
            } else if (status === 'not-started') {
                if (cardStatus === 'not-started' || cardStatus === 'in-progress') {
                    card.style.display = 'block';
                } else {
                    card.style.display = 'none';
                }
            } else if (status === 'completed') {
                if (cardStatus === 'completed-passed' || cardStatus === 'completed-failed') {
                    card.style.display = 'block';
                } else {
                    card.style.display = 'none';
                }
            }
        });
    }
</script>

<jsp:include page="/WEB-INF/views/common/footer.jsp" />
