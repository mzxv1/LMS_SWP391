<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<c:set var="isEdit" value="${mode == 'edit'}" />
<c:set var="pageTitle" value="${isEdit ? 'Setting Details' : 'New Setting'}" scope="request" />
<jsp:include page="/WEB-INF/views/common/header.jsp" />

<div class="d-flex flex-grow-1">
    <jsp:include page="/WEB-INF/views/common/sidebar.jsp" />

    <main class="flex-grow-1 p-4 bg-light">
        <div class="container" style="max-width: 720px;">

            <!-- Breadcrumb Navigation -->
            <nav aria-label="breadcrumb" class="mb-3">
                <ol class="breadcrumb">
                    <li class="breadcrumb-item"><a href="${ctx}/admin/settings" class="text-decoration-none">System Settings</a></li>
                    <li class="breadcrumb-item active" aria-current="page">${isEdit ? 'Setting Details' : 'New Setting'}</li>
                </ol>
            </nav>

            <div class="card border-0 shadow-sm rounded-3">
                <div class="card-header bg-white border-bottom py-3">
                    <h2 class="h4 fw-bold text-dark mb-0">
                        ${isEdit ? 'Setting Details' : 'New Setting'}
                    </h2>
                </div>

                <div class="card-body p-4">

                    <!-- Error Alert -->
                    <c:if test="${not empty error}">
                        <div class="alert alert-danger alert-dismissible fade show mb-4" role="alert">
                            <i class="bi bi-exclamation-triangle-fill me-2"></i>${error}
                            <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
                        </div>
                    </c:if>

                    <!-- Form (SRS 3.1.2 / Iteration 3) -->
                    <form method="post" action="${ctx}/admin/settings/detail" class="needs-validation" novalidate>
                        <c:if test="${isEdit}">
                            <input type="hidden" name="id" value="${setting.id}">
                        </c:if>

                        <div class="row g-3">
                            <!-- 1. Name* (non-digit string, max length 20) -->
                            <div class="col-md-6">
                                <label for="name" class="form-label fw-semibold">
                                    Name <span class="text-danger">*</span>
                                </label>
                                <input type="text" id="name" name="name" class="form-control"
                                       maxlength="20" required value="${setting.name}"
                                       placeholder="e.g. Manager">
                                <div class="form-text text-muted small">
                                    Tối đa 20 ký tự, không chứa chữ số (SRS).
                                </div>
                            </div>

                            <!-- 2. Type (Dropdown of Master Data Types: User Role, Course Category, Course Level, Subject, Semester) -->
                            <div class="col-md-6">
                                <label for="type" class="form-label fw-semibold">
                                    Type <span class="text-danger">*</span>
                                </label>
                                <select id="type" name="type" class="form-select" required>
                                    <option value="">-- Select Type --</option>
                                    <c:forEach var="t" items="${settingTypes}">
                                        <option value="${t}" ${setting.type == t ? 'selected' : ''}>
                                            ${t}
                                        </option>
                                    </c:forEach>
                                    <c:if test="${not empty setting.type}">
                                        <c:set var="matched" value="false" />
                                        <c:forEach var="t" items="${settingTypes}">
                                            <c:if test="${t == setting.type}"><c:set var="matched" value="true" /></c:if>
                                        </c:forEach>
                                        <c:if test="${not matched}">
                                            <option value="${setting.type}" selected>${setting.type}</option>
                                        </c:if>
                                    </c:if>
                                </select>
                                <div class="form-text text-muted small">
                                    Nhóm cấu hình danh mục hệ thống.
                                </div>
                            </div>

                            <!-- 3. Value (any string, max length 100) -->
                            <div class="col-md-6">
                                <label for="value" class="form-label fw-semibold">
                                    Value <span class="text-danger">*</span>
                                </label>
                                <input type="text" id="value" name="value" class="form-control"
                                       maxlength="100" required value="${setting.value}"
                                       placeholder="e.g. MANAGER">
                                <div class="form-text text-muted small">
                                    Mã định danh logic cho hệ thống (tối đa 100 ký tự).
                                </div>
                            </div>

                            <!-- 4. Priority (positive integer) -->
                            <div class="col-md-6">
                                <label for="priority" class="form-label fw-semibold">
                                    Priority <span class="text-danger">*</span>
                                </label>
                                <input type="number" id="priority" name="priority" class="form-control"
                                       min="1" required value="${empty setting.priority ? 1 : setting.priority}">
                                <div class="form-text text-muted small">
                                    Số nguyên dương thứ tự hiển thị (&ge; 1).
                                </div>
                            </div>

                            <!-- 5. Status (Active / Inactive radio) -->
                            <div class="col-12">
                                <label class="form-label fw-semibold d-block">Status</label>
                                <div class="form-check form-check-inline">
                                    <input class="form-check-input" type="radio" name="status" id="statusActive"
                                           value="Active" ${empty setting.status || setting.status == 'Active' ? 'checked' : ''}>
                                    <label class="form-check-label text-success fw-medium" for="statusActive">
                                        <i class="bi bi-check-circle me-1"></i> Active
                                    </label>
                                </div>
                                <div class="form-check form-check-inline">
                                    <input class="form-check-input" type="radio" name="status" id="statusInactive"
                                           value="Inactive" ${setting.status == 'Inactive' ? 'checked' : ''}>
                                    <label class="form-check-label text-secondary fw-medium" for="statusInactive">
                                        <i class="bi bi-dash-circle me-1"></i> Inactive
                                    </label>
                                </div>
                            </div>

                            <!-- 6. Description (max length 200) -->
                            <div class="col-12">
                                <label for="description" class="form-label fw-semibold">Description</label>
                                <textarea id="description" name="description" class="form-control"
                                          rows="3" maxlength="200"
                                          placeholder="Mô tả chi tiết cấu hình...">${setting.description}</textarea>
                                <div class="form-text text-muted small">
                                    Mô tả ý nghĩa cấu hình (tối đa 200 ký tự).
                                </div>
                            </div>

                            <!-- Action Buttons -->
                            <div class="col-12 mt-4 pt-2 border-top d-flex gap-2 justify-content-end">
                                <a href="${ctx}/admin/settings" class="btn btn-outline-secondary px-4">
                                    <i class="bi bi-x-circle me-1"></i> Cancel
                                </a>
                                <button type="submit" class="btn btn-primary px-4">
                                    <i class="bi bi-check2-circle me-1"></i> Submit
                                </button>
                            </div>
                        </div>
                    </form>
                </div>
            </div>
        </div>
    </main>
</div>

<jsp:include page="/WEB-INF/views/common/footer.jsp" />
