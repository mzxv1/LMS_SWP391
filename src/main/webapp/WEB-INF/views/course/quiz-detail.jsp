<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>

<c:set var="pageTitle" value="${quiz.title}" scope="request" />

<jsp:include page="/WEB-INF/views/common/header.jsp" />

<div class="container py-5" style="min-height: 80vh;">
    <!-- Breadcrumb -->
    <nav aria-label="breadcrumb" class="mb-4">
        <ol class="breadcrumb bg-transparent p-0 mb-0">
            <li class="breadcrumb-item">
                <a href="${pageContext.request.contextPath}/home" class="text-decoration-none">
                    <i class="bi bi-house-door-fill text-muted"></i> Trang chủ
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
            <li class="breadcrumb-item">
                <a href="${pageContext.request.contextPath}/courses/quizzes?courseId=${course.id}" class="text-decoration-none">
                    Bài kiểm tra
                </a>
            </li>
            <li class="breadcrumb-item active text-truncate" aria-current="page" style="max-width: 250px;">
                ${quiz.title}
            </li>
        </ol>
    </nav>

    <!-- Quiz Header Card -->
    <div class="card border-0 shadow-sm p-4 mb-4">
        <div class="d-flex justify-content-between align-items-center flex-wrap gap-3">
            <div>
                <span class="badge bg-primary-subtle text-primary mb-2 px-3 py-2 rounded-pill fw-semibold">
                    <i class="bi bi-clipboard2-check-fill me-1"></i> Trắc nghiệm tự ôn luyện
                </span>
                <h3 class="fw-bold mb-1 text-dark">${quiz.title}</h3>
                <p class="text-muted mb-0">Luyện tập nâng cao kiến thức, hệ thống hỗ trợ ôn tập không giới hạn số lần làm bài.</p>
            </div>
            <div>
                <a href="${pageContext.request.contextPath}/courses/quizzes?courseId=${course.id}" class="btn btn-outline-secondary btn-sm">
                    <i class="bi bi-arrow-left me-1"></i> Quay lại danh sách
                </a>
            </div>
        </div>
    </div>

    <div class="row g-4">
        <!-- Left: Quiz Info & Guide -->
        <div class="col-lg-8">
            <!-- Guidelines & Info Card -->
            <div class="card border-0 shadow-sm mb-4">
                <div class="card-body p-4">
                    <h5 class="fw-bold text-dark mb-4 pb-2 border-bottom">
                        <i class="bi bi-file-earmark-text text-primary me-2"></i> Hướng dẫn & Thông tin bài làm
                    </h5>

                    <!-- Statistics grid -->
                    <div class="row g-3 mb-4">
                        <div class="col-sm-6 col-md-3">
                            <div class="p-3 bg-light rounded text-center h-100">
                                <i class="bi bi-clock text-primary fs-3 d-block mb-1"></i>
                                <span class="text-muted small d-block">Thời lượng</span>
                                <strong class="text-dark fs-5">${quiz.timeLimitMin} phút</strong>
                            </div>
                        </div>
                        <div class="col-sm-6 col-md-3">
                            <div class="p-3 bg-light rounded text-center h-100">
                                <i class="bi bi-question-circle text-success fs-3 d-block mb-1"></i>
                                <span class="text-muted small d-block">Số câu hỏi</span>
                                <strong class="text-dark fs-5">${quiz.totalQuestions} câu</strong>
                            </div>
                        </div>
                        <div class="col-sm-6 col-md-3">
                            <div class="p-3 bg-light rounded text-center h-100">
                                <i class="bi bi-check-circle-fill text-warning fs-3 d-block mb-1"></i>
                                <span class="text-muted small d-block">Điểm vượt qua</span>
                                <strong class="text-dark fs-5">${quiz.passScore}%</strong>
                            </div>
                        </div>
                        <div class="col-sm-6 col-md-3">
                            <div class="p-3 bg-light rounded text-center h-100">
                                <i class="bi bi-arrow-repeat text-info fs-3 d-block mb-1"></i>
                                <span class="text-muted small d-block">Số lần làm</span>
                                <strong class="text-dark fs-5">${quiz.attemptCount}</strong>
                            </div>
                        </div>
                    </div>

                    <!-- Guidelines List -->
                    <div class="bg-primary-subtle p-3 rounded mb-2">
                        <h6 class="fw-bold text-primary mb-2"><i class="bi bi-info-circle-fill me-1"></i> Lưu ý quan trọng:</h6>
                        <ul class="mb-0 text-muted small ps-3">
                            <li class="mb-1">Học tập nâng cao kiến thức không bị giới hạn số lần làm bài. Hãy ôn luyện đến khi đạt kết quả mong muốn.</li>
                            <li class="mb-1">Khi bắt đầu làm bài, đồng hồ đếm ngược sẽ tự kích hoạt. Hết thời gian hệ thống sẽ tự động thu và nộp bài.</li>
                            <li class="mb-1">Bạn có thể rời trang thi khi bài làm dở dang và quay lại bấm nút <strong>"Làm tiếp"</strong> để tiếp tục làm phần còn lại.</li>
                        </ul>
                    </div>
                </div>
            </div>

            <!-- History of attempts Card -->
            <div class="card border-0 shadow-sm">
                <div class="card-body p-4">
                    <h5 class="fw-bold text-dark mb-4 pb-2 border-bottom">
                        <i class="bi bi-clock-history text-primary me-2"></i> Lịch sử làm bài
                    </h5>

                    <c:choose>
                        <c:when test="${empty attempts}">
                            <div class="text-center py-5 text-muted">
                                <i class="bi bi-clipboard2-x fs-1 d-block mb-2 text-secondary"></i>
                                <h6>Bạn chưa thực hiện lượt thi nào đối với bài kiểm tra này.</h6>
                                <p class="mb-0 small">Bấm nút ở thanh bên phải để bắt đầu làm bài kiểm tra lần đầu tiên.</p>
                            </div>
                        </c:when>
                        <c:otherwise>
                            <div class="table-responsive">
                                <table class="table table-hover align-middle mb-0 text-center">
                                    <thead class="table-light">
                                        <tr>
                                            <th>Lần làm</th>
                                            <th>Thời gian bắt đầu</th>
                                            <th>Thời gian nộp bài</th>
                                            <th>Điểm số</th>
                                            <th>Trạng thái</th>
                                            <th>Thao tác</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        <c:forEach var="attempt" items="${attempts}" varStatus="status">
                                            <tr>
                                                <td class="fw-semibold">Lần ${attempts.size() - status.index}</td>
                                                <td class="small">
                                                    <fmt:formatDate value="${attempt.startedAt}" pattern="dd/MM/yyyy HH:mm:ss" />
                                                </td>
                                                <td class="small">
                                                    <c:choose>
                                                        <c:when test="${attempt.submittedAt != null}">
                                                            <fmt:formatDate value="${attempt.submittedAt}" pattern="dd/MM/yyyy HH:mm:ss" />
                                                        </c:when>
                                                        <c:otherwise>
                                                            <span class="text-warning fw-semibold"><i class="bi bi-clock me-1"></i> Chưa nộp</span>
                                                        </c:otherwise>
                                                    </c:choose>
                                                </td>
                                                <td class="fw-bold">
                                                    <c:choose>
                                                        <c:when test="${attempt.score != null}">
                                                            <span class="fs-6 ${attempt.score >= quiz.passScore ? 'text-success' : 'text-danger'}">
                                                                ${attempt.score}%
                                                            </span>
                                                        </c:when>
                                                        <c:otherwise>
                                                            <span class="text-muted">-</span>
                                                        </c:otherwise>
                                                    </c:choose>
                                                </td>
                                                <td>
                                                    <c:choose>
                                                        <c:when test="${attempt.submittedAt == null}">
                                                            <span class="badge bg-warning text-dark">Chưa hoàn thành</span>
                                                        </c:when>
                                                        <c:otherwise>
                                                            <span class="badge bg-success">Đã hoàn thành</span>
                                                        </c:otherwise>
                                                    </c:choose>
                                                </td>
                                                <td>
                                                    <c:choose>
                                                        <c:when test="${attempt.submittedAt == null}">
                                                            <a href="${pageContext.request.contextPath}/courses/quizzes/taking?attemptId=${attempt.id}" class="btn btn-warning text-dark btn-sm rounded px-3 fw-semibold">
                                                                Làm tiếp <i class="bi bi-arrow-right-short ms-1"></i>
                                                            </a>
                                                        </c:when>
                                                        <c:otherwise>
                                                            <a href="${pageContext.request.contextPath}/courses/quizzes/result?attemptId=${attempt.id}" class="btn btn-outline-primary btn-sm rounded px-3">
                                                                Xem chi tiết <i class="bi bi-chevron-right ms-1"></i>
                                                            </a>
                                                        </c:otherwise>
                                                    </c:choose>
                                                </td>
                                            </tr>
                                        </c:forEach>
                                    </tbody>
                                </table>
                            </div>
                        </c:otherwise>
                    </c:choose>
                </div>
            </div>
        </div>

        <!-- Right: Action Button Card -->
        <div class="col-lg-4">
            <div class="card border-0 shadow-sm position-sticky" style="top: 24px;">
                <div class="card-body p-4 text-center">
                    <img src="${pageContext.request.contextPath}/assets/images/default-course.png" 
                         onerror="this.src='https://placehold.co/600x400/e9ecef/6c757d?text=Quiz'"
                         class="img-fluid rounded mb-4" alt="Quiz Image" style="max-height: 160px; width: 100%; object-fit: cover;">
                    
                    <c:choose>
                        <c:when test="${inProgressAttempt != null}">
                            <div class="alert alert-warning mb-4 text-start">
                                <i class="bi bi-exclamation-triangle-fill me-2"></i> Bạn có lượt thi chưa hoàn thành từ ngày 
                                <strong><fmt:formatDate value="${inProgressAttempt.startedAt}" pattern="dd/MM/yyyy HH:mm" /></strong>.
                            </div>
                            <a href="${pageContext.request.contextPath}/courses/quizzes/taking?attemptId=${inProgressAttempt.id}" 
                               class="btn btn-warning btn-lg w-100 py-3 rounded shadow-sm fw-bold mb-3 text-dark">
                                <i class="bi bi-play-circle-fill me-1"></i> Làm tiếp bài kiểm tra
                            </a>
                            <p class="text-muted small mb-0">Nhấn nút trên để tiếp tục làm bài thi đang dang dở.</p>
                        </c:when>
                        <c:otherwise>
                            <form action="${pageContext.request.contextPath}/courses/quizzes/start" method="post" class="mb-3">
                                <input type="hidden" name="quizId" value="${quiz.id}">
                                <button type="submit" class="btn btn-primary btn-lg w-100 py-3 rounded shadow-sm fw-bold">
                                    <i class="bi bi-play-fill me-1"></i> 
                                    <c:choose>
                                        <c:when test="${quiz.attemptCount > 0}">
                                            Làm bài thi mới <i class="bi bi-chevron-right ms-1"></i>
                                        </c:when>
                                        <c:otherwise>
                                            Bắt đầu làm bài <i class="bi bi-chevron-right ms-1"></i>
                                        </c:otherwise>
                                    </c:choose>
                                </button>
                            </form>
                            <p class="text-muted small mb-0">Bạn có thể thi thử bao nhiêu lần tùy ý để tự ôn luyện kiến thức.</p>
                        </c:otherwise>
                    </c:choose>
                </div>
            </div>
        </div>
    </div>
</div>

<jsp:include page="/WEB-INF/views/common/footer.jsp" />
