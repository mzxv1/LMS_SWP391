<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<c:set var="pageTitle" value="Chi tiết câu hỏi" scope="request" />
<jsp:include page="/WEB-INF/views/common/header.jsp" />

<div class="d-flex flex-grow-1">
    <jsp:include page="/WEB-INF/views/common/sidebar.jsp" />

    <main class="flex-grow-1 p-4">
        <div class="d-flex justify-content-between align-items-center mb-4">
            <div>
                <nav aria-label="breadcrumb">
                    <ol class="breadcrumb mb-1">
                        <li class="breadcrumb-item"><a href="${pageContext.request.contextPath}/expert/questions?chapterId=${chapter.id}">Ngân hàng câu hỏi</a></li>
                        <li class="breadcrumb-item active" aria-current="page">Chi tiết câu hỏi</li>
                    </ol>
                </nav>
                <h3 class="mb-0">Chi tiết câu hỏi #${question.id}</h3>
                <p class="text-muted mb-0 mt-1">Khóa học: <strong>${course.title}</strong> &nbsp;|&nbsp; Chương học: <strong>${chapter.title}</strong></p>
            </div>
            <a href="${pageContext.request.contextPath}/expert/questions?chapterId=${chapter.id}" class="btn btn-outline-secondary">
                <i class="bi bi-arrow-left"></i> Quay lại
            </a>
        </div>

        <c:if test="${not empty error}">
            <div class="alert alert-danger alert-dismissible fade show" role="alert">
                <i class="bi bi-exclamation-triangle-fill me-2"></i> ${error}
                <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
            </div>
        </c:if>

        <div class="card card-lms p-4" style="max-width: 900px;">
            <form method="post" action="${pageContext.request.contextPath}/expert/questions/detail">
                <input type="hidden" name="id" value="${question.id}">

                <div class="row mb-3">
                    <div class="col-md-12">
                        <label class="form-label fw-bold">Dạng câu hỏi</label>
                        <select id="questionType" class="form-select" onchange="toggleQuestionType()">
                            <option value="MCQ">Trắc nghiệm (4 lựa chọn)</option>
                            <option value="TRUE_FALSE">Đúng / Sai</option>
                        </select>
                    </div>
                </div>

                <div class="mb-3">
                    <label class="form-label fw-bold">Nội dung câu hỏi <span class="text-danger">*</span></label>
                    <textarea name="content" class="form-control" rows="3" placeholder="Nhập nội dung câu hỏi..." required>${question.content}</textarea>
                </div>

                <div class="mb-4">
                    <label class="form-label fw-bold">Giải thích đáp án</label>
                    <textarea name="explanation" class="form-control" rows="2" placeholder="Nhập lời giải thích cho đáp án đúng (không bắt buộc)...">${question.explanation}</textarea>
                </div>

                <div class="mb-3">
                    <label class="form-label fw-bold text-primary">Các đáp án lựa chọn <span class="text-danger">*</span></label>
                    <div id="optionsContainer">
                        <!-- Options populated via JS -->
                    </div>
                </div>

                <p class="text-muted small mb-0">
                    Ngày tạo: <fmt:formatDate value="${question.createdAt}" pattern="dd/MM/yyyy HH:mm" />
                </p>

                <hr class="my-4">

                <div class="d-flex justify-content-end gap-2">
                    <a href="${pageContext.request.contextPath}/expert/questions?chapterId=${chapter.id}" class="btn btn-outline-secondary">Hủy bỏ</a>
                    <button type="submit" class="btn btn-primary px-4">
                        <i class="bi bi-save me-1"></i> Lưu thay đổi
                    </button>
                </div>
            </form>
        </div>
    </main>
</div>

<script>
    function toggleQuestionType() {
        const type = document.getElementById('questionType').value;
        const optionsContainer = document.getElementById('optionsContainer');
        
        let oldOptions = [];
        let oldCorrectIndex = -1;
        
        <c:forEach var="opt" items="${question.options}" varStatus="status">
            oldOptions.push('${opt.optionText}');
            if (${opt.correct}) {
                oldCorrectIndex = ${status.index};
            }
        </c:forEach>

        if (type === 'TRUE_FALSE') {
            optionsContainer.innerHTML = `
                <div class="mb-3">
                    <div class="form-check d-flex align-items-center mb-2">
                        <input class="form-check-input me-3" type="radio" name="correctIndex" value="0" id="correct0" required \${oldCorrectIndex === 0 ? "checked" : ""}>
                        <div class="input-group">
                            <span class="input-group-text fw-bold">1</span>
                            <input type="text" name="optionText" class="form-control bg-light" value="Đúng" readonly required>
                        </div>
                    </div>
                    <div class="form-check d-flex align-items-center mb-2">
                        <input class="form-check-input me-3" type="radio" name="correctIndex" value="1" id="correct1" required \${oldCorrectIndex === 1 ? "checked" : ""}>
                        <div class="input-group">
                            <span class="input-group-text fw-bold">2</span>
                            <input type="text" name="optionText" class="form-control bg-light" value="Sai" readonly required>
                        </div>
                    </div>
                </div>
            `;
        } else {
            let v0 = oldOptions[0] || '';
            let v1 = oldOptions[1] || '';
            let v2 = oldOptions[2] || '';
            let v3 = oldOptions[3] || '';
            
            optionsContainer.innerHTML = `
                <div class="mb-3">
                    <div class="form-check d-flex align-items-center mb-2">
                        <input class="form-check-input me-3" type="radio" name="correctIndex" value="0" id="correct0" required \${oldCorrectIndex === 0 ? "checked" : ""}>
                        <div class="input-group">
                            <span class="input-group-text fw-bold">A</span>
                            <input type="text" name="optionText" class="form-control" placeholder="Đáp án lựa chọn A" value="\${v0}" required>
                        </div>
                    </div>
                    <div class="form-check d-flex align-items-center mb-2">
                        <input class="form-check-input me-3" type="radio" name="correctIndex" value="1" id="correct1" required \${oldCorrectIndex === 1 ? "checked" : ""}>
                        <div class="input-group">
                            <span class="input-group-text fw-bold">B</span>
                            <input type="text" name="optionText" class="form-control" placeholder="Đáp án lựa chọn B" value="\${v1}" required>
                        </div>
                    </div>
                    <div class="form-check d-flex align-items-center mb-2">
                        <input class="form-check-input me-3" type="radio" name="correctIndex" value="2" id="correct2" required \${oldCorrectIndex === 2 ? "checked" : ""}>
                        <div class="input-group">
                            <span class="input-group-text fw-bold">C</span>
                            <input type="text" name="optionText" class="form-control" placeholder="Đáp án lựa chọn C" value="\${v2}" required>
                        </div>
                    </div>
                    <div class="form-check d-flex align-items-center mb-2">
                        <input class="form-check-input me-3" type="radio" name="correctIndex" value="3" id="correct3" required \${oldCorrectIndex === 3 ? "checked" : ""}>
                        <div class="input-group">
                            <span class="input-group-text fw-bold">D</span>
                            <input type="text" name="optionText" class="form-control" placeholder="Đáp án lựa chọn D" value="\${v3}" required>
                        </div>
                    </div>
                </div>
            `;
        }
    }

    document.addEventListener("DOMContentLoaded", function() {
        // Automatically determine question type if options count is 2
        let optCount = ${question.options.size()};
        if (optCount === 2) {
            document.getElementById('questionType').value = 'TRUE_FALSE';
        }
        toggleQuestionType();
    });
</script>

<jsp:include page="/WEB-INF/views/common/footer.jsp" />
