<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>

<c:set var="pageTitle" value="Kết quả: ${quiz.title}" scope="request" />

<jsp:include page="/WEB-INF/views/common/header.jsp" />

<div class="container py-5" style="min-height: 90vh;">
    <!-- Breadcrumb -->
    <nav aria-label="breadcrumb" class="mb-4">
        <ol class="breadcrumb bg-transparent p-0 mb-0">
            <li class="breadcrumb-item">
                <a href="${pageContext.request.contextPath}/home" class="text-decoration-none">Trang chủ</a>
            </li>
            <li class="breadcrumb-item text-truncate" style="max-width: 200px;">
                <a href="${pageContext.request.contextPath}/courses/detail?id=${course.id}" class="text-decoration-none">${course.title}</a>
            </li>
            <li class="breadcrumb-item">
                <a href="${pageContext.request.contextPath}/courses/quizzes?courseId=${course.id}" class="text-decoration-none">Bài kiểm tra</a>
            </li>
            <li class="breadcrumb-item text-truncate" style="max-width: 200px;">
                <a href="${pageContext.request.contextPath}/courses/quizzes/detail?id=${quiz.id}" class="text-decoration-none">${quiz.title}</a>
            </li>
            <li class="breadcrumb-item active" aria-current="page">Kết quả lượt thi #${attempt.id}</li>
        </ol>
    </nav>

    <!-- Quiz Result Summary Card -->
    <div class="card border-0 shadow-sm p-4 mb-4">
        <div class="row align-items-center g-4">
            <!-- Score Circle -->
            <div class="col-md-3 text-center">
                <div class="d-inline-flex flex-column align-items-center justify-content-center rounded-circle border border-5 ${attempt.score >= quiz.passScore ? 'border-success text-success bg-success-subtle' : 'border-primary text-primary bg-primary-subtle'}" 
                     style="width: 140px; height: 140px;">
                    <span class="fs-1 fw-bold">${attempt.score}%</span>
                    <span class="small fw-semibold text-uppercase" style="font-size: 0.75rem; letter-spacing: 0.5px;">Điểm số</span>
                </div>
            </div>

            <!-- Quiz Attempt Stats -->
            <div class="col-md-6">
                <span class="badge bg-secondary-subtle text-secondary mb-2 px-3 py-2 rounded-pill fw-semibold">
                    <i class="bi bi-clock-history me-1"></i> Báo cáo kết quả bài làm
                </span>
                <h3 class="fw-bold mb-2 text-dark">${quiz.title}</h3>
                
                <div class="row g-2 text-muted small mt-2">
                    <div class="col-sm-6">
                        <i class="bi bi-calendar-event me-1"></i> Bắt đầu: 
                        <strong><fmt:formatDate value="${attempt.startedAt}" pattern="dd/MM/yyyy HH:mm" /></strong>
                    </div>
                    <div class="col-sm-6">
                        <i class="bi bi-hourglass-split me-1"></i> Thời gian làm: 
                        <strong>${durationStr}</strong>
                    </div>
                    <div class="col-sm-6">
                        <i class="bi bi-check-circle-fill text-success me-1"></i> Điểm gợi ý đạt: 
                        <strong>${quiz.passScore}%</strong>
                    </div>
                    <div class="col-sm-6">
                        <i class="bi bi-award-fill me-1"></i> Kết quả: 
                        <strong class="${attempt.score >= quiz.passScore ? 'text-success' : 'text-primary'}">
                            ${attempt.score >= quiz.passScore ? 'Đã vượt qua ' : 'Hãy cố gắng thêm'}
                        </strong>
                    </div>
                </div>
            </div>

            <!-- Retry & Review Navigation Buttons -->
            <div class="col-md-3 text-center text-md-end">
                <div class="d-flex flex-column gap-2">
                    <a href="${pageContext.request.contextPath}/courses/quizzes/detail?id=${quiz.id}" class="btn btn-primary py-2.5 rounded shadow-sm fw-bold">
                        <i class="bi bi-arrow-counterclockwise me-1"></i> Thi lại / Quay về
                    </a>
                    <a href="${pageContext.request.contextPath}/courses/quizzes?courseId=${course.id}" class="btn btn-outline-secondary btn-sm py-2 rounded">
                        <i class="bi bi-list-task me-1"></i> Xem danh sách bài
                    </a>
                </div>
            </div>
        </div>
    </div>

    <!-- Questions Details with Explanation -->
    <div id="questions-top-anchor" style="scroll-margin-top: 24px;"></div>
    <div class="row">
        <div class="col-12">
            <h5 class="fw-bold text-dark mb-4"><i class="bi bi-file-earmark-ruled-fill text-primary me-1"></i> Chi tiết bài làm và đáp án</h5>
            
            <c:forEach var="q" items="${questions}" varStatus="status">
                <c:set var="userAnswerId" value="${selectedAnswers[q.id]}" />

                <div class="card border-0 shadow-sm mb-4 question-card">
                    <div class="card-body p-4">
                        <h5 class="fw-bold text-dark mb-3 d-flex align-items-start">
                            <span class="badge bg-primary text-white me-2 px-2 py-1 rounded">Câu ${status.index + 1}</span>
                            <span class="flex-grow-1">${q.content}</span>
                        </h5>

                        <!-- List of Options -->
                        <div class="options-list d-flex flex-column gap-2 mt-3">
                            <c:forEach var="opt" items="${q.options}">
                                <c:set var="isUserSelected" value="${userAnswerId != null && userAnswerId == opt.id}" />
                                
                                <c:choose>
                                    <%-- Option is correct AND user selected it (Success highlight) --%>
                                    <c:when test="${opt.correct && isUserSelected}">
                                        <div class="option-item d-flex align-items-center p-3 rounded border border-success bg-success-subtle text-success">
                                            <i class="bi bi-check-circle-fill me-3 fs-5"></i>
                                            <span class="small fw-semibold flex-grow-1">${opt.optionText}</span>
                                            <span class="badge bg-success text-white small rounded-pill px-3 py-1">Chính xác</span>
                                        </div>
                                    </c:when>
                                    
                                    <%-- Option is correct BUT user did NOT select it (Green indicator) --%>
                                    <c:when test="${opt.correct && !isUserSelected}">
                                        <div class="option-item d-flex align-items-center p-3 rounded border border-success bg-success-subtle text-success">
                                            <i class="bi bi-check-circle me-3 fs-5"></i>
                                            <span class="small fw-semibold flex-grow-1">${opt.optionText}</span>
                                            <span class="badge bg-success text-white small rounded-pill px-3 py-1">Đáp án đúng</span>
                                        </div>
                                    </c:when>

                                    <%-- Option is incorrect AND user selected it (Danger highlight) --%>
                                    <c:when test="${!opt.correct && isUserSelected}">
                                        <div class="option-item d-flex align-items-center p-3 rounded border border-danger bg-danger-subtle text-danger">
                                            <i class="bi bi-x-circle-fill me-3 fs-5"></i>
                                            <span class="small fw-semibold flex-grow-1">${opt.optionText}</span>
                                            <span class="badge bg-danger text-white small rounded-pill px-3 py-1">Bạn chọn sai</span>
                                        </div>
                                    </c:when>

                                    <%-- Standard incorrect option --%>
                                    <c:otherwise>
                                        <div class="option-item d-flex align-items-center p-3 rounded border border-light-subtle bg-light text-muted opacity-75">
                                            <i class="bi bi-circle me-3 fs-5 text-secondary"></i>
                                            <span class="small flex-grow-1">${opt.optionText}</span>
                                        </div>
                                    </c:otherwise>
                                </c:choose>
                            </c:forEach>
                        </div>

                        <!-- User skipped/unanswered check warning -->
                        <c:if var="skipped" test="${empty userAnswerId}">
                            <div class="alert alert-warning py-2 px-3 mt-3 mb-0 small d-flex align-items-center">
                                <i class="bi bi-exclamation-triangle-fill me-2 fs-6"></i> Bạn đã bỏ qua không chọn đáp án cho câu hỏi này.
                            </div>
                        </c:if>

                        <!-- Question Explanation -->
                        <c:if test="${not empty q.explanation}">
                            <div class="p-3 bg-light rounded mt-3 border-start border-4 border-warning">
                                <div class="fw-bold text-dark small mb-1">
                                    <i class="bi bi-lightbulb-fill text-warning me-1"></i> Giải thích đáp án:
                                </div>
                                <div class="text-muted small">${q.explanation}</div>
                            </div>
                        </c:if>
                    </div>
                </div>
            </c:forEach>
            
            <!-- Pagination Controls -->
            <div id="pagination-container" class="d-flex justify-content-center align-items-center gap-2 mt-4 mb-3"></div>
        </div>
    </div>
</div>

<script>
    document.addEventListener("DOMContentLoaded", function() {
        const itemsPerPage = 10;
        const questionCards = document.querySelectorAll(".question-card");
        const totalQuestions = questionCards.length;
        const totalPages = Math.ceil(totalQuestions / itemsPerPage);
        let currentPage = 1;

        const paginationContainer = document.getElementById("pagination-container");
        const topAnchor = document.getElementById("questions-top-anchor");

        function showPage(page) {
            currentPage = page;
            const start = (page - 1) * itemsPerPage;
            const end = start + itemsPerPage;

            questionCards.forEach((card, index) => {
                if (index >= start && index < end) {
                    card.style.display = "block";
                } else {
                    card.style.display = "none";
                }
            });

            renderPaginationControls();
        }

        function renderPaginationControls() {
            if (totalPages <= 1) {
                paginationContainer.innerHTML = "";
                return;
            }

            let html = "";
            
            // Previous button
            html += `<button type="button" class="btn btn-outline-primary btn-sm px-3 rounded-pill \${currentPage === 1 ? 'disabled' : ''}" onclick="changePage(\${currentPage - 1})">
                        <i class="bi bi-chevron-left me-1"></i> Trước
                     </button>`;

            // Page numbers
            for (let i = 1; i <= totalPages; i++) {
                html += `<button type="button" class="btn btn-sm px-3 rounded-circle \${currentPage === i ? 'btn-primary text-white shadow-sm' : 'btn-outline-primary'}" style="width: 36px; height: 36px;" onclick="changePage(\${i})">
                            \${i}
                         </button>`;
            }

            // Next button
            html += `<button type="button" class="btn btn-outline-primary btn-sm px-3 rounded-pill \${currentPage === totalPages ? 'disabled' : ''}" onclick="changePage(\${currentPage + 1})">
                        Sau <i class="bi bi-chevron-right ms-1"></i>
                     </button>`;

            paginationContainer.innerHTML = html;
        }

        window.changePage = function(page) {
            if (page < 1 || page > totalPages) return;
            showPage(page);
            // Smooth scroll to top of questions
            topAnchor.scrollIntoView({ behavior: 'smooth', block: 'start' });
        };

        // Initialize page 1
        showPage(1);
    });
</script>

<jsp:include page="/WEB-INF/views/common/footer.jsp" />

