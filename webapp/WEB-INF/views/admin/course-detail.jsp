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
            <a href="${pageContext.request.contextPath}/admin/courses" class="btn btn-outline-secondary">
                <i class="bi bi-arrow-left"></i> Quay lại danh sách
            </a>
        </div>

        <c:if test="${not empty message}">
            <div class="alert alert-success">${message}</div>
        </c:if>

        <c:if test="${not empty error}">
            <div class="alert alert-danger">${error}</div>
        </c:if>

        <c:if test="${not empty course}">
            <div class="row g-4">
                <!-- Course Information (Read-only) -->
                <div class="col-lg-8">
                    <div class="card card-lms p-4">
                        <!-- 1. Tên khóa học -->
                        <div class="mb-3">
                            <label class="form-label text-muted fw-bold">Tên khóa học</label>
                            <input type="text" class="form-control" value="${course.title}" disabled>
                        </div>

                        <!-- 2. Danh mục / Thời lượng -->
                        <div class="row mb-3">
                            <div class="col-md-6 mb-3 mb-md-0">
                                <label class="form-label text-muted fw-bold">Danh mục</label>
                                <input type="text" class="form-control" value="${course.category}" disabled>
                            </div>
                            <div class="col-md-6">
                                <label class="form-label text-muted fw-bold">Thời lượng (giờ)</label>
                                <input type="text" class="form-control" value="${course.durationHours} giờ" disabled>
                            </div>
                        </div>

                        <!-- 3. Chuyên gia phụ trách / Mã Chuyên gia -->
                        <div class="row mb-3">
                            <div class="col-md-6 mb-3 mb-md-0">
                                <label class="form-label text-muted fw-bold">Chuyên gia phụ trách</label>
                                <input type="text" class="form-control" value="${course.expertName}" disabled>
                            </div>
                            <div class="col-md-6">
                                <label class="form-label text-muted fw-bold">Mã Chuyên gia (ID)</label>
                                <input type="text" class="form-control" value="${course.expertId}" disabled>
                            </div>
                        </div>

                        <!-- 4. Ngày tạo / Cập nhật lần cuối -->
                        <div class="row text-muted small mb-3">
                            <div class="col-md-6">
                                <span class="fw-bold">Ngày tạo:</span> <fmt:formatDate value="${course.createdAt}" pattern="dd/MM/yyyy HH:mm" />
                            </div>
                            <div class="col-md-6">
                                <span class="fw-bold">Cập nhật lần cuối:</span> <fmt:formatDate value="${course.updatedAt}" pattern="dd/MM/yyyy HH:mm" />
                            </div>
                        </div>

                        <!-- 5. Mô tả khóa học -->
                        <div>
                            <label class="form-label text-muted fw-bold">Mô tả khóa học</label>
                            <div class="p-3 bg-light rounded border">${not empty course.description ? course.description : 'Không có mô tả.'}</div>
                        </div>
                    </div>
                </div>

                <!-- Admin Action: Status Change Form -->
                <div class="col-lg-4">
                    <div class="card card-lms p-4">
                        <h5 class="card-title mb-3"><i class="bi bi-gear"></i> Thay đổi trạng thái</h5>
                        <form method="post" action="${pageContext.request.contextPath}/admin/courses/updateStatus">
                            <input type="hidden" name="id" value="${course.id}">

                            <div class="mb-3">
                                <label class="form-label fw-semibold">Trạng thái khóa học</label>
                                <select name="status" class="form-select" required>
                                    <option value="DRAFT" ${course.status == 'DRAFT' ? 'selected' : ''}>DRAFT (Bản nháp)</option>
                                    <option value="PUBLISHED" ${course.status == 'PUBLISHED' ? 'selected' : ''}>PUBLISHED (Đã xuất bản)</option>
                                    <option value="ARCHIVED" ${course.status == 'ARCHIVED' ? 'selected' : ''}>ARCHIVED (Lưu trữ)</option>
                                </select>
                                <div class="form-text">Admin chỉ có quyền phê duyệt/thay đổi trạng thái khóa học.</div>
                            </div>

                            <button type="submit" class="btn btn-primary w-100">
                                <i class="bi bi-save"></i> Cập nhật trạng thái
                            </button>
                        </form>
                    </div>
                </div>
            </div>
        </c:if>
    </main>
</div>

<jsp:include page="/WEB-INF/views/common/footer.jsp" />
