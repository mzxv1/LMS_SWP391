<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<c:set var="pageTitle" value="Quản lý học liệu & đề thi" scope="request" />
<jsp:include page="/WEB-INF/views/common/header.jsp" />

<div class="d-flex flex-grow-1">
    <jsp:include page="/WEB-INF/views/common/sidebar.jsp" />

    <main class="flex-grow-1 p-4">
        <div class="d-flex justify-content-between align-items-center mb-4">
            <h3 class="mb-0 fw-bold text-dark">Quản lý học liệu & đề thi</h3>
        </div>

        <c:if test="${not empty sessionScope.successMsg}">
            <div class="alert alert-success alert-dismissible fade show shadow-sm" role="alert">
                <i class="bi bi-check-circle-fill me-2"></i> ${sessionScope.successMsg}
                <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
            </div>
            <% session.removeAttribute("successMsg"); %>
        </c:if>
        <c:if test="${not empty sessionScope.errorMsg}">
            <div class="alert alert-danger alert-dismissible fade show shadow-sm" role="alert">
                <i class="bi bi-exclamation-triangle-fill me-2"></i> ${sessionScope.errorMsg}
                <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
            </div>
            <% session.removeAttribute("errorMsg"); %>
        </c:if>

        <!-- Course Selector Card (Shared) -->
        <div class="card border-0 shadow-sm p-3 mb-4">
            <form method="get" action="${pageContext.request.contextPath}/expert/questions" id="courseForm">
                <input type="hidden" name="tab" id="courseFormTab" value="questions">
                <div class="row align-items-center">
                    <div class="col-md-6">
                        <label class="form-label fw-bold text-muted mb-1 small">CHỌN KHÓA HỌC QUẢN LÝ</label>
                        <select name="courseId" class="form-select border-primary-subtle" onchange="document.getElementById('courseFormTab').value = activeTab; this.form.submit()">
                            <c:forEach var="c" items="${courses}">
                                <option value="${c.id}" ${c.id == selectedCourseId ? 'selected' : ''}>
                                    ${c.title}
                                </option>
                            </c:forEach>
                            <c:if test="${empty courses}">
                                <option value="">-- Bạn chưa có khóa học nào --</option>
                            </c:if>
                        </select>
                    </div>
                    <div class="col-md-6 text-end pt-3 mt-1">
                        <span class="badge bg-primary-subtle text-primary border border-primary px-3 py-2 fs-6">Chuyên gia: ${sessionScope.currentUser.fullName}</span>
                    </div>
                </div>
            </form>
        </div>

        <!-- Navigation Tabs -->
        <ul class="nav nav-tabs mb-4 border-bottom" id="expertTabs" role="tablist">
            <li class="nav-item" role="presentation">
                <button class="nav-link active fw-bold text-secondary px-4 py-2.5" id="questions-tab" data-bs-toggle="tab" data-bs-target="#questions-pane" type="button" role="tab" aria-controls="questions-pane" aria-selected="true" onclick="setActiveTab('questions')">
                    <i class="bi bi-question-circle-fill me-1"></i> Ngân hàng câu hỏi
                </button>
            </li>
            <li class="nav-item" role="presentation">
                <button class="nav-link fw-bold text-secondary px-4 py-2.5" id="quizzes-tab" data-bs-toggle="tab" data-bs-target="#quizzes-pane" type="button" role="tab" aria-controls="quizzes-pane" aria-selected="false" onclick="setActiveTab('quizzes')">
                    <i class="bi bi-file-earmark-ruled-fill me-1"></i> Đề kiểm tra (Quiz)
                </button>
            </li>
        </ul>

        <!-- Tab Panes Content -->
        <div class="tab-content" id="expertTabsContent">
            
            <!-- Tab 1: Questions Bank -->
            <div class="tab-pane fade show active" id="questions-pane" role="tabpanel" aria-labelledby="questions-tab">
                <div class="d-flex justify-content-between align-items-center mb-3">
                    <h5 class="fw-bold mb-0 text-dark">Danh sách câu hỏi</h5>
                    <c:if test="${selectedChapterId != -1}">
                        <div class="d-flex gap-2">
                            <a href="${pageContext.request.contextPath}/expert/questions/template" class="btn btn-outline-success btn-sm">
                                <i class="bi bi-download"></i> Tải file mẫu
                            </a>
                            <button type="button" class="btn btn-outline-primary btn-sm" data-bs-toggle="modal" data-bs-target="#importModal">
                                <i class="bi bi-upload"></i> Nhập Excel
                            </button>
                            <a href="${pageContext.request.contextPath}/expert/questions/export?chapterId=${selectedChapterId}" class="btn btn-outline-info btn-sm">
                                <i class="bi bi-file-earmark-excel"></i> Xuất Excel
                            </a>
                            <a href="${pageContext.request.contextPath}/expert/questions/new?chapterId=${selectedChapterId}" class="btn btn-primary btn-sm fw-bold">
                                <i class="bi bi-plus-lg"></i> Thêm câu hỏi
                            </a>
                        </div>
                    </c:if>
                </div>

                <!-- Filter Selector & Search -->
                <div class="card border-0 shadow-sm p-3 mb-3 bg-light-subtle">
                    <form method="get" action="${pageContext.request.contextPath}/expert/questions" class="row g-3" id="filterForm">
                        <input type="hidden" name="courseId" value="${selectedCourseId}">
                        <input type="hidden" name="tab" value="questions">
                        <div class="col-md-6">
                            <label class="form-label fw-bold text-muted small">CHỌN CHƯƠNG HỌC (CHAPTER)</label>
                            <select name="chapterId" class="form-select" id="chapterSelect" onchange="this.form.submit()">
                                <c:forEach var="ch" items="${chapters}">
                                    <option value="${ch.id}" ${ch.id == selectedChapterId ? 'selected' : ''}>
                                        ${ch.title}
                                    </option>
                                </c:forEach>
                                <c:if test="${empty chapters}">
                                    <option value="">-- Chưa có chương học nào --</option>
                                </c:if>
                            </select>
                        </div>
                        <div class="col-md-6">
                            <label class="form-label fw-bold text-muted small">TÌM KIẾM CÂU HỎI</label>
                            <div class="input-group">
                                <input type="text" name="keyword" class="form-control"
                                       placeholder="Tìm nội dung câu hỏi hoặc đáp án..." value="${keyword}">
                                <button type="submit" class="btn btn-outline-primary">
                                    <i class="bi bi-search"></i>
                                </button>
                            </div>
                        </div>
                    </form>
                </div>

                <!-- Question List Table -->
                <div class="card border-0 shadow-sm p-0">
                    <div class="table-responsive">
                        <table class="table table-hover mb-0 align-middle">
                            <thead class="table-light">
                                <tr>
                                    <th style="width: 5%">#</th>
                                    <th style="width: 40%">Nội dung câu hỏi</th>
                                    <th style="width: 20%">Đáp án đúng</th>
                                    <th style="width: 25%">Giải thích đáp án</th>
                                    <th style="width: 5%">Ngày tạo</th>
                                    <th style="width: 5%" class="text-end">Thao tác</th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:forEach var="q" items="${questions}" varStatus="status">
                                    <tr>
                                        <td>${(currentPage - 1) * 10 + status.index + 1}</td>
                                        <td>
                                            <div class="fw-semibold text-dark text-wrap" style="max-width: 400px; line-height: 1.5; font-size: 0.95rem;">
                                                ${q.content}
                                            </div>
                                        </td>
                                        <td>
                                            <div class="small">
                                                <c:forEach var="opt" items="${q.options}" varStatus="optStatus">
                                                    <c:if test="${opt.correct}">
                                                        <c:set var="optLetter" value="A" />
                                                        <c:if test="${optStatus.index == 1}"><c:set var="optLetter" value="B" /></c:if>
                                                        <c:if test="${optStatus.index == 2}"><c:set var="optLetter" value="C" /></c:if>
                                                        <c:if test="${optStatus.index == 3}"><c:set var="optLetter" value="D" /></c:if>
                                                        <span class="badge bg-success me-1">${optLetter}</span>
                                                        <span class="text-dark fw-semibold">${opt.optionText}</span>
                                                    </c:if>
                                                </c:forEach>
                                            </div>
                                        </td>
                                        <td>
                                            <div class="text-muted text-wrap" style="max-width: 250px; font-size: 0.85rem;">
                                                <c:choose>
                                                    <c:when test="${empty q.explanation}">
                                                        <span class="text-muted small"><em>Không có giải thích</em></span>
                                                    </c:when>
                                                    <c:otherwise>
                                                        ${q.explanation}
                                                    </c:otherwise>
                                                </c:choose>
                                            </div>
                                        </td>
                                        <td class="small text-muted"><fmt:formatDate value="${q.createdAt}" pattern="dd/MM/yyyy" /></td>
                                        <td class="text-end text-nowrap">
                                            <a href="${pageContext.request.contextPath}/expert/questions/detail?id=${q.id}"
                                               class="btn btn-sm btn-outline-primary me-1" title="Sửa câu hỏi">
                                                <i class="bi bi-pencil"></i>
                                            </a>
                                            <form method="post" class="d-inline confirm-delete"
                                                  data-confirm-message="Bạn chắc chắn muốn xóa câu hỏi này?"
                                                  action="${pageContext.request.contextPath}/expert/questions/delete">
                                                <input type="hidden" name="id" value="${q.id}">
                                                <input type="hidden" name="chapterId" value="${selectedChapterId}">
                                                <button type="submit" class="btn btn-sm btn-outline-danger" title="Xóa câu hỏi">
                                                    <i class="bi bi-trash"></i>
                                                </button>
                                            </form>
                                        </td>
                                    </tr>
                                </c:forEach>

                                <c:if test="${empty questions}">
                                    <tr>
                                        <td colspan="6" class="text-center text-muted py-5">
                                            <i class="bi bi-inbox fs-2 d-block mb-2 text-secondary"></i>
                                            <c:choose>
                                                <c:when test="${empty courses}">
                                                    Bạn chưa có khóa học nào.
                                                </c:when>
                                                <c:when test="${selectedChapterId == -1}">
                                                    Vui lòng chọn hoặc tạo chương học để quản lý câu hỏi.
                                                </c:when>
                                                <c:otherwise>
                                                    Chưa có câu hỏi nào trong chương học này.
                                                </c:otherwise>
                                            </c:choose>
                                        </td>
                                    </tr>
                                </c:if>
                            </tbody>
                        </table>
                    </div>
                </div>

                <!-- Pagination -->
                <c:if test="${totalPages > 1}">
                    <nav aria-label="Page navigation" class="mt-4">
                        <ul class="pagination justify-content-center">
                            <li class="page-item ${currentPage == 1 ? 'disabled' : ''}">
                                <a class="page-link" href="?courseId=${selectedCourseId}&chapterId=${selectedChapterId}&keyword=${keyword}&page=${currentPage - 1}&tab=questions">&lt;</a>
                            </li>
                            <c:forEach var="p" begin="1" end="${totalPages}">
                                <li class="page-item ${currentPage == p ? 'active' : ''}">
                                    <a class="page-link" href="?courseId=${selectedCourseId}&chapterId=${selectedChapterId}&keyword=${keyword}&page=${p}&tab=questions">${p}</a>
                                </li>
                            </c:forEach>
                            <li class="page-item ${currentPage == totalPages ? 'disabled' : ''}">
                                <a class="page-link" href="?courseId=${selectedCourseId}&chapterId=${selectedChapterId}&keyword=${keyword}&page=${currentPage + 1}&tab=questions">&gt;</a>
                            </li>
                        </ul>
                    </nav>
                </c:if>
            </div>

            <!-- Tab 2: Quizzes List -->
            <div class="tab-pane fade" id="quizzes-pane" role="tabpanel" aria-labelledby="quizzes-tab">
                <div class="d-flex justify-content-between align-items-center mb-3">
                    <h5 class="fw-bold mb-0 text-dark">Danh sách đề kiểm tra (Quizzes)</h5>
                    <c:if var="hasCourse" test="${selectedCourseId != -1}">
                        <a href="${pageContext.request.contextPath}/expert/quizzes/new?courseId=${selectedCourseId}" class="btn btn-primary btn-sm fw-bold">
                            <i class="bi bi-plus-lg"></i> Thêm đề thi mới
                        </a>
                    </c:if>
                </div>

                <!-- Quizzes Table -->
                <div class="card border-0 shadow-sm p-0">
                    <div class="table-responsive">
                        <table class="table table-hover mb-0 align-middle">
                            <thead class="table-light">
                                <tr>
                                    <th style="width: 5%">#</th>
                                    <th style="width: 40%">Tên đề kiểm tra</th>
                                    <th style="width: 15%">Thời lượng</th>
                                    <th style="width: 15%">Số câu hỏi</th>
                                    <th style="width: 15%">Điểm đạt yêu cầu</th>
                                    <th style="width: 10%" class="text-end">Thao tác</th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:forEach var="qz" items="${quizzes}" varStatus="status">
                                    <tr>
                                        <td>${status.index + 1}</td>
                                        <td>
                                            <strong class="text-dark text-wrap d-block" style="max-width: 400px;">${qz.title}</strong>
                                        </td>
                                        <td>
                                            <span class="text-muted"><i class="bi bi-clock me-1"></i>${qz.timeLimitMin} phút</span>
                                        </td>
                                        <td>
                                            <span class="badge bg-info-subtle text-info-emphasis border border-info px-2.5 py-1.5 fw-bold rounded-pill">
                                                ${qz.totalQuestions} câu hỏi
                                            </span>
                                        </td>
                                        <td class="fw-semibold text-success">${qz.passScore}%</td>
                                        <td class="text-end text-nowrap">
                                            <a href="${pageContext.request.contextPath}/expert/quizzes/edit?id=${qz.id}"
                                               class="btn btn-sm btn-outline-primary me-1" title="Sửa đề thi">
                                                <i class="bi bi-pencil"></i>
                                            </a>
                                            <form method="post" class="d-inline confirm-delete"
                                                  data-confirm-message="Bạn chắc chắn muốn xóa đề thi này? Hành động này sẽ xóa toàn bộ lịch sử thi của học viên!"
                                                  action="${pageContext.request.contextPath}/expert/quizzes/delete">
                                                <input type="hidden" name="id" value="${qz.id}">
                                                <input type="hidden" name="courseId" value="${selectedCourseId}">
                                                <button type="submit" class="btn btn-sm btn-outline-danger" title="Xóa đề thi">
                                                    <i class="bi bi-trash"></i>
                                                </button>
                                            </form>
                                        </td>
                                    </tr>
                                </c:forEach>

                                <c:if test="${empty quizzes}">
                                    <tr>
                                        <td colspan="6" class="text-center text-muted py-5">
                                            <i class="bi bi-inbox fs-2 d-block mb-2 text-secondary"></i>
                                            Chưa có đề kiểm tra nào cho khóa học này. Click "Thêm đề thi mới" để bắt đầu thiết lập.
                                        </td>
                                    </tr>
                                </c:if>
                            </tbody>
                        </table>
                    </div>
                </div>
            </div>

        </div>

        <!-- Import Excel Modal -->
        <div class="modal fade" id="importModal" tabindex="-1" aria-labelledby="importModalLabel" aria-hidden="true">
            <div class="modal-dialog">
                <div class="modal-content border-0 shadow">
                    <form method="post" action="${pageContext.request.contextPath}/expert/questions/import?chapterId=${selectedChapterId}" enctype="multipart/form-data">
                        <div class="modal-header border-0 pb-0">
                            <h5 class="modal-title fw-bold" id="importModalLabel">Nhập câu hỏi từ file Excel</h5>
                            <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
                        </div>
                        <div class="modal-body py-3">
                            <div class="mb-3">
                                <label class="form-label fw-bold">Chọn file Excel (.xlsx) <span class="text-danger">*</span></label>
                                <input type="file" name="file" class="form-control" accept=".xlsx" required>
                                <div class="form-text mt-2 text-muted small">
                                    Hệ thống sẽ tự động phân tích và gán các câu hỏi trong file Excel vào đúng Chương học đang chọn ở bộ lọc.
                                </div>
                            </div>
                        </div>
                        <div class="modal-footer border-0 pt-0">
                            <button type="button" class="btn btn-light" data-bs-dismiss="modal">Hủy</button>
                            <button type="submit" class="btn btn-primary fw-bold">Bắt đầu Import</button>
                        </div>
                    </form>
                </div>
            </div>
        </div>
    </main>
</div>

<script>
    let activeTab = "questions"; // default

    document.addEventListener("DOMContentLoaded", function() {
        // Read tab parameter from URL
        const urlParams = new URLSearchParams(window.location.search);
        let tabParam = urlParams.get('tab');
        
        // If not in URL, read from localStorage
        if (!tabParam) {
            tabParam = localStorage.getItem('expert_active_tab');
        }

        if (tabParam === 'quizzes') {
            activeTab = 'quizzes';
            const quizzesTabBtn = document.getElementById('quizzes-tab');
            if (quizzesTabBtn) {
                // Show quizzes tab using Bootstrap API
                const tab = bootstrap.Tab.getOrCreateInstance(quizzesTabBtn);
                tab.show();
            }
        }
        
        // Sync initial value of courseForm tab
        const activeTabInputs = document.querySelectorAll('#courseFormTab');
        activeTabInputs.forEach(input => {
            input.value = activeTab;
        });
    });

    function setActiveTab(tabName) {
        activeTab = tabName;
        localStorage.setItem('expert_active_tab', tabName);
        
        // Sync courseForm tab hidden value
        const activeTabInputs = document.querySelectorAll('#courseFormTab');
        activeTabInputs.forEach(input => {
            input.value = tabName;
        });
    }
</script>

<jsp:include page="/WEB-INF/views/common/footer.jsp" />
