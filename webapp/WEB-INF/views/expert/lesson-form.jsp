<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<c:set var="isEdit" value="${lesson != null}" />
<c:set var="pageTitle" value="${isEdit ? 'Chỉnh sửa bài học' : 'Thêm bài học mới'}" scope="request" />

<jsp:include page="/WEB-INF/views/common/header.jsp" />

<div class="d-flex flex-grow-1">

    <jsp:include page="/WEB-INF/views/common/sidebar.jsp" />

    <main class="flex-grow-1 p-4">

        <div class="mb-4 d-flex align-items-center">
            <a href="${pageContext.request.contextPath}/expert/lessons?action=list&courseId=${course.id}" class="btn btn-outline-secondary me-3">
                <i class="bi bi-arrow-left"></i> Quay lại
            </a>
            <div>
                <h3 class="mb-1">${pageTitle}</h3>
                <p class="text-muted mb-0">Khóa học: <strong>${course.title}</strong></p>
            </div>
        </div>

        <c:if test="${not empty error}">
            <div class="alert alert-danger alert-dismissible fade show" role="alert" style="max-width: 800px;">
                <i class="bi bi-exclamation-triangle-fill me-2"></i> ${error}
                <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
            </div>
        </c:if>

        <div class="card card-lms" style="max-width: 800px;">
            <div class="card-body">
                <form action="${pageContext.request.contextPath}/expert/lessons?action=${isEdit ? 'edit' : 'add'}" method="post">
                    
                    <c:if test="${isEdit}">
                        <input type="hidden" name="id" value="${lesson.id}">
                    </c:if>
                    <input type="hidden" name="courseId" value="${course.id}">
                    
                    <div class="mb-3">
                        <label for="title" class="form-label">Tên bài học <span class="text-danger">*</span></label>
                        <input type="text" class="form-control" id="title" name="title" value="${lesson.title}" required>
                    </div>

                    <div class="row">
                        <div class="col-md-6 mb-3">
                            <label for="chapterId" class="form-label">Chapter <span class="text-danger">*</span></label>
                            <select class="form-select" id="chapterId" name="chapterId" required>
                                <option value="" disabled ${empty lesson.chapterId or lesson.chapterId == 0 ? 'selected' : ''}>-- Chọn chapter --</option>
                                <c:forEach var="chapter" items="${chapters}">
                                    <option value="${chapter.id}" ${lesson.chapterId == chapter.id ? 'selected' : ''}>
                                        ${chapter.title}
                                    </option>
                                </c:forEach>
                            </select>
                        </div>
                        <div class="col-md-6 mb-3">
                            <label for="lessonType" class="form-label">Loại bài học</label>
                            <select class="form-select" id="lessonType" name="lessonType">
                                <option value="VIDEO" ${lesson.lessonType == 'VIDEO' ? 'selected' : ''}>Video</option>
                                <option value="ARTICLE" ${lesson.lessonType == 'ARTICLE' ? 'selected' : ''}>Bài viết</option>
                                <option value="QUIZ" ${lesson.lessonType == 'QUIZ' ? 'selected' : ''}>Bài tập trắc nghiệm</option>
                            </select>
                        </div>
                    </div>

                    <div class="mb-3">
                        <label for="contentUrl" class="form-label">URL Nội dung (Link Video / Link Bài viết)</label>
                        <input type="text" class="form-control" id="contentUrl" name="contentUrl" value="${lesson.contentUrl}" placeholder="https://...">
                    </div>

                    <div class="row">
                        <div class="col-md-6 mb-3">
                            <label for="durationMinutes" class="form-label">Thời lượng (phút)</label>
                            <input type="number" class="form-control" id="durationMinutes" name="durationMinutes" value="${lesson.durationMinutes != null ? lesson.durationMinutes : 0}" min="0">
                        </div>
                        <div class="col-md-6 mb-3">
                            <label for="orderIndex" class="form-label">Thứ tự ưu tiên (Order Index)</label>
                            <input type="number" class="form-control" id="orderIndex" name="orderIndex" value="${lesson.orderIndex != null ? lesson.orderIndex : 0}" min="0">
                        </div>
                    </div>

                    <hr class="my-4">

                    <div class="d-flex justify-content-end">
                        <button type="submit" class="btn btn-primary px-4">
                            ${isEdit ? 'Cập nhật' : 'Tạo mới'}
                        </button>
                    </div>

                </form>
            </div>
        </div>

    </main>

</div>

<jsp:include page="/WEB-INF/views/common/footer.jsp" />
