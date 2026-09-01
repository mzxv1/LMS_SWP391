<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Thêm khóa học" scope="request" />
<jsp:include page="/WEB-INF/views/common/header.jsp" />

<div class="d-flex flex-grow-1">
    <jsp:include page="/WEB-INF/views/common/sidebar.jsp" />

    <main class="flex-grow-1 p-4">
        <div class="d-flex justify-content-between align-items-center mb-4">
            <h3 class="mb-0">Thêm khóa học mới</h3>
            <a href="${pageContext.request.contextPath}/expert/courses" class="btn btn-outline-secondary">
                <i class="bi bi-arrow-left"></i> Quay lại danh sách
            </a>
        </div>

        <c:if test="${not empty error}">
            <div class="alert alert-danger">${error}</div>
        </c:if>

        <div class="card card-lms p-4" style="max-width: 720px;">
            <form method="post" action="${pageContext.request.contextPath}/expert/courses/new">
                <div class="mb-3">
                    <label class="form-label">Tên khóa học</label>
                    <input type="text" name="title" class="form-control"
                           value="${formData.title}" required minlength="3">
                </div>
                <div class="row">
                    <div class="col-md-6 mb-3">
                        <label class="form-label">Danh mục</label>
                        <input type="text" name="category" class="form-control" value="${formData.category}">
                    </div>
                    <div class="col-md-6 mb-3">
                        <label class="form-label">Giá tiền (VND)</label>
                        <input type="number" name="price" class="form-control" min="0" step="1000"
                               value="${formData.price != null ? formData.price : 0}" required>
                    </div>
                </div>
                <div class="row">
                    <div class="col-md-6 mb-3">
                        <label class="form-label">Thời lượng (giờ)</label>
                        <input type="number" name="durationHours" class="form-control" min="1"
                               value="${formData.durationHours}" required>
                    </div>
                    <div class="col-md-6 mb-3">
                        <label class="form-label">Trạng thái</label>
                        <select name="status" class="form-select">
                            <option value="DRAFT" ${formData.status == 'DRAFT' ? 'selected' : ''}>DRAFT</option>
                            <option value="PUBLISHED" ${formData.status == 'PUBLISHED' ? 'selected' : ''}>PUBLISHED</option>
                            <option value="ARCHIVED" ${formData.status == 'ARCHIVED' ? 'selected' : ''}>ARCHIVED</option>
                        </select>
                    </div>
                </div>
                <div class="mb-3">
                    <label class="form-label">Mô tả</label>
                    <textarea name="description" class="form-control" rows="4">${formData.description}</textarea>
                </div>
                <button type="submit" class="btn btn-primary">
                    <i class="bi bi-check-lg"></i> Tạo khóa học
                </button>
            </form>
        </div>
    </main>
</div>

<jsp:include page="/WEB-INF/views/common/footer.jsp" />
