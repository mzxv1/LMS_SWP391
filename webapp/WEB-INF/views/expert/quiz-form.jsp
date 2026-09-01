<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>

<c:set var="titleWord" value="${isEdit ? 'Chỉnh sửa' : 'Thêm mới'}" />
<c:set var="pageTitle" value="${titleWord} đề thi" scope="request" />
<jsp:include page="/WEB-INF/views/common/header.jsp" />

<div class="d-flex flex-grow-1">
    <jsp:include page="/WEB-INF/views/common/sidebar.jsp" />

    <main class="flex-grow-1 p-4">
        <div class="d-flex justify-content-between align-items-center mb-4">
            <h3 class="mb-0 fw-bold text-dark">${titleWord} đề thi</h3>
            <a href="${pageContext.request.contextPath}/expert/questions?courseId=${course.id}&tab=quizzes" class="btn btn-outline-secondary">
                <i class="bi bi-arrow-left"></i> Quay lại
            </a>
        </div>

        <c:if test="${not empty error}">
            <div class="alert alert-danger shadow-sm mb-4">
                <i class="bi bi-exclamation-triangle-fill me-2"></i> ${error}
            </div>
        </c:if>

        <div class="card border-0 shadow-sm p-4">
            <h5 class="fw-bold text-primary mb-3">Khóa học: ${course.title}</h5>
            <hr class="mb-4">

            <form method="post" action="${pageContext.request.contextPath}/expert/quizzes/${isEdit ? 'edit' : 'new'}" id="quizForm">
                <input type="hidden" name="courseId" value="${course.id}">
                <c:if test="${isEdit}">
                    <input type="hidden" name="id" value="${quiz.id}">
                </c:if>

                <div class="row">
                    <!-- Title -->
                    <div class="col-md-6 mb-3">
                        <label class="form-label fw-bold">Tên đề kiểm tra <span class="text-danger">*</span></label>
                        <input type="text" name="title" class="form-control border-primary-subtle" 
                               value="${quiz.title}" required minlength="3" placeholder="Ví dụ: Bài kiểm tra Giữa khóa Java">
                    </div>

                    <!-- Time Limit -->
                    <div class="col-md-3 mb-3">
                        <label class="form-label fw-bold">Thời lượng làm bài (phút) <span class="text-danger">*</span></label>
                        <input type="number" name="timeLimitMin" class="form-control border-primary-subtle" 
                               value="${quiz.timeLimitMin > 0 ? quiz.timeLimitMin : 30}" required min="1" max="180">
                    </div>

                    <!-- Pass Score -->
                    <div class="col-md-3 mb-3">
                        <label class="form-label fw-bold">Điểm đạt yêu cầu (%) <span class="text-danger">*</span></label>
                        <input type="number" name="passScore" class="form-control border-primary-subtle" 
                               value="${quiz.passScore > 0 ? quiz.passScore : 80}" required min="0" max="100" placeholder="Ví dụ: 80">
                    </div>
                </div>

                <div class="mt-4 mb-4">
                    <h5 class="fw-bold text-dark mb-3"><i class="bi bi-diagram-3 me-2 text-primary"></i>Phân bổ câu hỏi theo Chương học</h5>
                    <p class="text-muted small">Nhập số lượng câu hỏi hệ thống sẽ bốc ngẫu nhiên từ mỗi chương học để tạo đề thi cho học sinh.</p>

                    <div class="table-responsive border rounded">
                        <table class="table table-hover align-middle mb-0">
                            <thead class="table-light">
                                <tr>
                                    <th style="width: 5%">#</th>
                                    <th style="width: 50%">Tên Chương học</th>
                                    <th style="width: 25%">Số câu hỏi có sẵn trong ngân hàng</th>
                                    <th style="width: 20%">Số câu hỏi bốc vào đề thi</th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:forEach var="dist" items="${distributions}" varStatus="status">
                                    <tr>
                                        <td>${status.index + 1}</td>
                                        <td>
                                            <span class="fw-semibold text-dark">${dist.chapterName}</span>
                                        </td>
                                        <td>
                                            <span class="badge bg-secondary-subtle text-secondary-emphasis rounded-pill px-3 py-1.5 fw-semibold border">
                                                ${dist.maxQuestionsAvailable} câu hỏi
                                            </span>
                                        </td>
                                        <td>
                                            <div class="input-group input-group-sm" style="max-width: 140px;">
                                                <input type="number" name="chapter_count_${dist.chapterId}" 
                                                       class="form-control chapter-count-input border-primary-subtle" 
                                                       value="${dist.questionCount}" min="0" max="${dist.maxQuestionsAvailable}" 
                                                       data-max="${dist.maxQuestionsAvailable}" data-chapter-name="${dist.chapterName}" required>
                                                <span class="input-group-text">câu</span>
                                            </div>
                                        </td>
                                    </tr>
                                </c:forEach>

                                <c:if test="${empty distributions}">
                                    <tr>
                                        <td colspan="4" class="text-center py-4 text-muted">
                                            <i class="bi bi-exclamation-circle fs-3 d-block mb-2"></i>
                                            Khóa học này chưa có chương học nào được tạo. Vui lòng tạo chương học trước khi thiết lập đề thi.
                                        </td>
                                    </tr>
                                </c:if>
                            </tbody>
                        </table>
                    </div>
                </div>

                <!-- Summary Row -->
                <div class="row align-items-center bg-light p-3 rounded mb-4 mx-0 border">
                    <div class="col-md-8">
                        <strong class="text-dark"><i class="bi bi-info-circle text-primary me-1"></i> Lưu ý:</strong> 
                        <span class="text-muted small">Tổng số câu hỏi của đề thi sẽ được tự động cộng dồn từ cấu hình các chương ở trên.</span>
                    </div>
                    <div class="col-md-4 text-end">
                        <div class="d-flex justify-content-end align-items-center gap-2">
                            <span class="fw-bold text-dark fs-5">TỔNG SỐ CÂU HỎI:</span>
                            <input type="text" id="totalQuestionsDisplay" class="form-control text-center fw-bold bg-white text-primary fs-5 border border-primary-subtle" 
                                   style="width: 100px;" value="0" readonly>
                        </div>
                    </div>
                </div>

                <!-- Submit buttons -->
                <div class="d-flex gap-2 justify-content-end">
                    <a href="${pageContext.request.contextPath}/expert/questions?courseId=${course.id}&tab=quizzes" class="btn btn-light px-4">Hủy</a>
                    <button type="submit" class="btn btn-primary px-4 fw-bold"><i class="bi bi-check-lg me-1"></i>Lưu cấu hình đề thi</button>
                </div>
            </form>
        </div>
    </main>
</div>

<script>
    function calculateTotalQuestions() {
        let total = 0;
        const inputs = document.querySelectorAll('.chapter-count-input');
        inputs.forEach(input => {
            const val = parseInt(input.value) || 0;
            total += val;
        });
        document.getElementById('totalQuestionsDisplay').value = total;
    }

    document.addEventListener("DOMContentLoaded", function() {
        const inputs = document.querySelectorAll('.chapter-count-input');
        inputs.forEach(input => {
            input.addEventListener('input', function() {
                // Force limits in UI
                const val = parseInt(this.value) || 0;
                const max = parseInt(this.getAttribute('data-max')) || 0;
                const chapterName = this.getAttribute('data-chapter-name');
                if (val < 0) {
                    this.value = 0;
                } else if (val > max) {
                    alert("Chương \"" + chapterName + "\" chỉ có tối đa " + max + " câu hỏi trong ngân hàng!");
                    this.value = max;
                }
                calculateTotalQuestions();
            });
        });
        calculateTotalQuestions();
    });
</script>

<jsp:include page="/WEB-INF/views/common/footer.jsp" />
