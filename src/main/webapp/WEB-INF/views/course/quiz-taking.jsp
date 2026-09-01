<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<c:set var="pageTitle" value="Đang làm bài: ${quiz.title}" scope="request" />

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
            <li class="breadcrumb-item active" aria-current="page">Lượt thi #${attempt.id}</li>
        </ol>
    </nav>

    <!-- Quiz Header -->
    <div class="card border-0 shadow-sm p-4 mb-4">
        <h4 class="fw-bold text-dark m-0"><i class="bi bi-journal-bookmark-fill text-primary me-2"></i>${course.title} | ${quiz.title}</h4>
    </div>

    <!-- Quiz Form -->
    <form id="quiz-form" action="${pageContext.request.contextPath}/courses/quizzes/submit" method="post">
        <input type="hidden" name="attemptId" value="${attempt.id}">

        <div class="row g-4">
            <!-- Left Side: Controls & Navigation (col-md-4) -->
            <div class="col-md-4">
                <div class="position-sticky" style="top: 24px;">
                    <!-- Timer Box -->
                    <div class="card border-0 shadow-sm mb-3">
                        <div class="card-body text-center p-3">
                            <span class="text-uppercase fw-bold text-muted small d-block mb-1">⏱️ Thời gian còn lại</span>
                            <strong class="fs-3 fw-bold text-dark" id="countdown-timer">00:00</strong>
                        </div>
                    </div>

                    <!-- Question Grid Box -->
                    <div class="card border-0 shadow-sm mb-3">
                        <div class="card-body p-3">
                            <h6 class="fw-bold text-dark mb-3"><i class="bi bi-grid-3x3-gap-fill text-muted me-1"></i> Danh sách câu hỏi</h6>
                            <div class="d-flex flex-wrap gap-2 mb-3" id="questions-grid">
                                <c:forEach var="q" items="${questions}" varStatus="status">
                                    <button type="button" id="grid-q-${q.id}" 
                                            class="btn btn-outline-secondary d-flex align-items-center justify-content-center fw-semibold rounded grid-btn"
                                            style="width: 45px; height: 45px; font-size: 0.9rem;"
                                            onclick="showQuestion(${status.index})">
                                        ${status.index + 1}
                                    </button>
                                </c:forEach>
                            </div>
                            
                            <!-- Legend -->
                            <div class="d-flex justify-content-between text-muted small border-top pt-2 mt-2" style="font-size: 0.8rem;">
                                <span class="d-flex align-items-center"><span class="d-inline-block bg-success me-2" style="width: 24px; height: 12px; border-radius: 6px;"></span> Đã chọn</span>
                                <span class="d-flex align-items-center"><span class="d-inline-block bg-primary me-2" style="width: 24px; height: 12px; border-radius: 6px;"></span> Đang xem</span>
                            </div>
                        </div>
                    </div>

                    <!-- Prev/Next Navigation Buttons -->
                    <div class="d-flex gap-2 mb-3">
                        <button type="button" class="btn btn-outline-primary w-50 py-2.5 fw-bold" id="btn-prev" onclick="prevQuestion()">
                            <i class="bi bi-chevron-left me-1"></i> Câu trước
                        </button>
                        <button type="button" class="btn btn-outline-primary w-50 py-2.5 fw-bold" id="btn-next" onclick="nextQuestion()">
                            Câu tiếp <i class="bi bi-chevron-right ms-1"></i>
                        </button>
                    </div>

                    <!-- Submit Button -->
                    <button type="button" class="btn btn-primary btn-lg w-100 py-3 fw-bold rounded shadow-sm" onclick="confirmSubmit()">
                        🚀 Nộp bài thi
                    </button>
                </div>
            </div>

            <!-- Right Side: Active Question Display (col-md-8) -->
            <div class="col-md-8">
                <c:forEach var="q" items="${questions}" varStatus="status">
                    <div class="card border-0 shadow-sm question-container-card mb-4" id="q-card-${status.index}" style="display: none;">
                        <div class="card-body p-4">
                            <h5 class="fw-bold text-dark mb-4 d-flex align-items-start">
                                <span class="badge bg-primary text-white me-2 px-2.5 py-1.5 rounded">Câu ${status.index + 1}</span>
                                <span class="flex-grow-1 fs-5">${q.content}</span>
                            </h5>

                            <!-- Options -->
                            <div class="options-list d-flex flex-column gap-3">
                                <c:forEach var="opt" items="${q.options}" varStatus="oStatus">
                                    <c:set var="letter" value="${oStatus.index == 0 ? 'A' : (oStatus.index == 1 ? 'B' : (oStatus.index == 2 ? 'C' : 'D'))}" />
                                    <label class="option-item d-flex align-items-center p-3 rounded border border-light-subtle bg-light-subtle" 
                                           style="cursor: pointer; transition: all 0.2s ease-in-out;"
                                           onclick="selectOption(${q.id}, ${opt.id}, ${status.index})">
                                        <input type="radio" name="question_${q.id}" value="${opt.id}" class="form-check-input me-3 flex-shrink-0" style="width: 1.25rem; height: 1.25rem;">
                                        <span class="text-dark"><strong class="me-1">${letter}.</strong> ${opt.optionText}</span>
                                    </label>
                                </c:forEach>
                            </div>
                        </div>
                    </div>
                </c:forEach>
            </div>
        </div>
    </form>
</div>

<!-- Modal Confirm Submit -->
<div class="modal fade" id="submitConfirmModal" tabindex="-1" aria-labelledby="submitConfirmModalLabel" aria-hidden="true">
    <div class="modal-dialog modal-dialog-centered">
        <div class="modal-content border-0 shadow">
            <div class="modal-header border-0 pb-0">
                <h5 class="modal-title fw-bold" id="submitConfirmModalLabel"><i class="bi bi-patch-question text-warning me-1"></i> Xác nhận nộp bài</h5>
                <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
            </div>
            <div class="modal-body py-4">
                <p class="mb-0 text-muted">Bạn có chắc chắn muốn nộp bài thi trắc nghiệm này không? Hãy chắc chắn bạn đã rà soát kỹ tất cả các câu trả lời.</p>
            </div>
            <div class="modal-footer border-0 pt-0">
                <button type="button" class="btn btn-light px-4 rounded" data-bs-dismiss="modal">Hủy</button>
                <button type="button" class="btn btn-primary px-4 rounded fw-bold" onclick="executeSubmit()">Nộp bài</button>
            </div>
        </div>
    </div>
</div>

<style>
    .option-item:hover {
        background-color: var(--bs-primary-bg-subtle) !important;
        border-color: var(--bs-primary-border-subtle) !important;
    }
    .option-item input[type="radio"]:checked + span {
        font-weight: 600;
    }
    .option-item:has(input[type="radio"]:checked) {
        background-color: var(--bs-primary-bg-subtle) !important;
        border-color: var(--bs-primary-border-subtle) !important;
        box-shadow: 0 0 0 2px var(--bs-primary-border-subtle);
    }
</style>

<script>
    // 1. Countdown Timer Implementation
    let remainingSeconds = ${remainingSeconds};
    const timerElement = document.getElementById('countdown-timer');
    const formElement = document.getElementById('quiz-form');
    let autoSubmitted = false;

    function updateTimer() {
        if (remainingSeconds <= 0) {
            clearInterval(timerInterval);
            timerElement.innerHTML = "00:00";
            if (!autoSubmitted) {
                autoSubmitted = true;
                alert("Đã hết thời gian làm bài! Hệ thống tự động nộp bài của bạn.");
                formElement.submit();
            }
            return;
        }

        const mins = Math.floor(remainingSeconds / 60);
        const secs = remainingSeconds % 60;
        
        const displayMins = mins < 10 ? '0' + mins : mins;
        const displaySecs = secs < 10 ? '0' + secs : secs;
        
        timerElement.innerHTML = displayMins + ":" + displaySecs;
        
        // Color changes when 5 mins left
        if (remainingSeconds <= 300) {
            const container = timerElement.parentElement;
            container.classList.remove('bg-primary-subtle', 'text-primary');
            container.classList.add('bg-danger', 'text-white');
        }
        
        remainingSeconds--;
    }

    const timerInterval = setInterval(updateTimer, 1000);
    updateTimer();

    // 2. Active Question and Grid Navigation Implementation
    let currentQuestionIndex = 0;
    const totalQuestions = ${totalQuestions};

    const questionCards = document.querySelectorAll('.question-container-card');
    const gridButtons = document.querySelectorAll('.grid-btn');
    const prevBtn = document.getElementById('btn-prev');
    const nextBtn = document.getElementById('btn-next');

    // Keep track of which question IDs have been answered
    const answeredQuestions = new Set();

    function showQuestion(index) {
        if (index < 0 || index >= totalQuestions) return;

        // Hide current active card
        questionCards[currentQuestionIndex].style.display = 'none';

        // Update current index
        currentQuestionIndex = index;

        // Show new active card
        questionCards[currentQuestionIndex].style.display = 'block';

        // Update grid active highlights
        updateGridStates();
    }

    function updateGridStates() {
        gridButtons.forEach((btn, idx) => {
            const questionIdStr = btn.id.split('-q-')[1];
            const qId = parseInt(questionIdStr);

            // Clear styling classes
            btn.classList.remove('btn-primary', 'text-white', 'btn-success', 'btn-outline-secondary');

            // Determine if it is active (currently viewed) or answered
            const isCurrent = (idx === currentQuestionIndex);
            const isAnswered = answeredQuestions.has(qId);

            // Add correct symbols and class
            let label = (idx + 1).toString();
            if (isCurrent) {
                btn.classList.add('btn-primary', 'text-white');
            } else if (isAnswered) {
                btn.classList.add('btn-success', 'text-white');
            } else {
                btn.classList.add('btn-outline-secondary');
            }
            btn.innerText = label;
        });

        // Update disabled states of prev/next buttons
        prevBtn.disabled = (currentQuestionIndex === 0);
        nextBtn.disabled = (currentQuestionIndex === totalQuestions - 1);
    }

    function prevQuestion() {
        if (currentQuestionIndex > 0) {
            showQuestion(currentQuestionIndex - 1);
        }
    }

    function nextQuestion() {
        if (currentQuestionIndex < totalQuestions - 1) {
            showQuestion(currentQuestionIndex + 1);
        }
    }

    function selectOption(questionId, optionId, idx) {
        answeredQuestions.add(questionId);
        // Sync radio input (just to be safe, although clicking label checks it)
        const radioInput = document.querySelector(`input[name="question_${questionId}"][value="${optionId}"]`);
        if (radioInput) {
            radioInput.checked = true;
        }
        updateGridStates();
    }

    // Bind change listener for form inputs to mark answered status
    formElement.addEventListener('change', (e) => {
        if (e.target.type === 'radio') {
            const name = e.target.name; // e.g. question_123
            const questionId = parseInt(name.split('_')[1]);
            answeredQuestions.add(questionId);
            updateGridStates();
        }
    });

    // Scan already answered questions (for resumed attempts)
    const checkedRadios = document.querySelectorAll('input[type="radio"]:checked');
    checkedRadios.forEach(r => {
        const questionId = parseInt(r.name.split('_')[1]);
        answeredQuestions.add(questionId);
    });

    // Initialize Page to Question 1
    showQuestion(0);

    // 3. Submit actions
    let confirmModal;
    function confirmSubmit() {
        if (!confirmModal) {
            confirmModal = new bootstrap.Modal(document.getElementById('submitConfirmModal'));
        }
        confirmModal.show();
    }

    function executeSubmit() {
        confirmModal.hide();
        formElement.submit();
    }
</script>

<jsp:include page="/WEB-INF/views/common/footer.jsp" />
