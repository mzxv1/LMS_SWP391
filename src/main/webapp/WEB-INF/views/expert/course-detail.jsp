<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<c:set var="pageTitle" value="Chi tiết khóa học" scope="request" />
<jsp:include page="/WEB-INF/views/common/header.jsp" />

<div class="d-flex flex-grow-1">
    <jsp:include page="/WEB-INF/views/common/sidebar.jsp" />

    <main class="flex-grow-1 p-4">
        <div class="d-flex justify-content-between align-items-center mb-4">
            <h3 class="mb-0">Chi tiết khóa học</h3>
            <a href="${pageContext.request.contextPath}/expert/courses" class="btn btn-outline-secondary">
                <i class="bi bi-arrow-left"></i> Quay lại danh sách
            </a>
        </div>

        <c:if test="${not empty error}">
            <div class="alert alert-danger">${error}</div>
        </c:if>

        <c:if test="${not empty course}">
            <div class="card card-lms p-4" style="max-width: 720px;">
                <form method="post" action="${pageContext.request.contextPath}/expert/courses/detail">
                    <input type="hidden" name="id" value="${course.id}">

                    <div class="mb-3">
                        <label class="form-label">Tên khóa học</label>
                        <input type="text" name="title" class="form-control"
                               value="${course.title}" required minlength="3">
                    </div>
                     <div class="row">
                        <div class="col-md-6 mb-3">
                            <label class="form-label">Danh mục</label>
                            <input type="text" name="category" class="form-control" value="${course.category}">
                        </div>
                        <div class="col-md-6 mb-3">
                            <label class="form-label">Giá tiền (VND)</label>
                            <input type="number" name="price" class="form-control" min="0" step="1000"
                                   value="${course.price}" required>
                        </div>
                    </div>
                    <div class="row">
                        <div class="col-md-6 mb-3">
                            <label class="form-label">Thời lượng (giờ)</label>
                            <input type="number" name="durationHours" class="form-control" min="1"
                                   value="${course.durationHours}" required>
                        </div>
                        <div class="col-md-6 mb-3">
                            <label class="form-label">Trạng thái</label>
                            <select name="status" class="form-select">
                                <option value="DRAFT" ${course.status == 'DRAFT' ? 'selected' : ''}>DRAFT</option>
                                <option value="PUBLISHED" ${course.status == 'PUBLISHED' ? 'selected' : ''}>PUBLISHED</option>
                                <option value="ARCHIVED" ${course.status == 'ARCHIVED' ? 'selected' : ''}>ARCHIVED</option>
                            </select>
                        </div>
                    </div>
                    <div class="mb-3">
                        <label class="form-label">Mô tả</label>
                        <textarea name="description" class="form-control" rows="4">${course.description}</textarea>
                    </div>

                    <p class="text-muted small">
                        Giảng viên: ${course.expertName} &middot;
                        Tạo lúc: <fmt:formatDate value="${course.createdAt}" pattern="dd/MM/yyyy HH:mm" /> &middot;
                        Cập nhật: <fmt:formatDate value="${course.updatedAt}" pattern="dd/MM/yyyy HH:mm" />
                    </p>

                    <div class="d-flex gap-2">
                        <button type="submit" class="btn btn-primary">
                            <i class="bi bi-save"></i> Lưu thay đổi
                        </button>
                        <a href="${pageContext.request.contextPath}/expert/lessons?action=list&courseId=${course.id}" class="btn btn-outline-info">
                            <i class="bi bi-journal-text"></i> Quản lý bài học
                        </a>
                    </div>
                </form>
            </div>
        </c:if>
    </main>
</div>

<jsp:include page="/WEB-INF/views/common/footer.jsp" />
