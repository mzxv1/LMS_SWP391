<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<c:set var="pageTitle" value="Setting List" scope="request" />
<jsp:include page="/WEB-INF/views/common/header.jsp" />

<div class="d-flex flex-grow-1">
    <jsp:include page="/WEB-INF/views/common/sidebar.jsp" />

    <main class="flex-grow-1 p-4 bg-light">
        <div class="container-fluid">

            <!-- Title & Alerts -->
            <div class="d-flex justify-content-between align-items-center mb-3">
                <h2 class="h3 fw-bold text-dark mb-0">Setting List</h2>
                <a href="${ctx}/admin/settings/detail" class="btn btn-primary shadow-sm">
                    <i class="bi bi-plus-circle me-1"></i> New Setting
                </a>
            </div>

            <c:if test="${not empty message}">
                <div class="alert alert-success alert-dismissible fade show" role="alert">
                    <i class="bi bi-check-circle-fill me-2"></i>${message}
                    <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
                </div>
            </c:if>

            <c:if test="${not empty error}">
                <div class="alert alert-danger alert-dismissible fade show" role="alert">
                    <i class="bi bi-exclamation-triangle-fill me-2"></i>${error}
                    <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
                </div>
            </c:if>

            <!-- Filter & Search Toolbar (SRS 3.1.1 / Iteration 3) -->
            <div class="card border-0 shadow-sm rounded-3 mb-4">
                <div class="card-body p-3">
                    <form method="get" action="${ctx}/admin/settings" class="row g-2 align-items-center">
                        <input type="hidden" name="sortBy" value="${sortBy}">
                        <input type="hidden" name="sortOrder" value="${sortOrder}">

                        <!-- (1) Type filter dropdown (User Role, Subject, Semester, etc.) -->
                        <div class="col-md-3">
                            <select name="type" class="form-select" title="Setting Type">
                                <option value="">All Types</option>
                                <c:forEach var="t" items="${distinctTypes}">
                                    <option value="${t}" ${type == t ? 'selected' : ''}>${t}</option>
                                </c:forEach>
                            </select>
                        </div>

                        <!-- (2) Status filter dropdown -->
                        <div class="col-md-2">
                            <select name="status" class="form-select" title="Setting Status">
                                <option value="">All Statuses</option>
                                <option value="Active" ${status == 'Active' ? 'selected' : ''}>Active</option>
                                <option value="Inactive" ${status == 'Inactive' ? 'selected' : ''}>Inactive</option>
                            </select>
                        </div>

                        <!-- Search keyword input -->
                        <div class="col-md-5">
                            <div class="input-group">
                                <input type="text" name="keyword" class="form-control"
                                       placeholder="Enter keyword(s) to search..." value="${keyword}">
                                <button type="submit" class="btn btn-secondary">
                                    <i class="bi bi-search me-1"></i> Search
                                </button>
                            </div>
                        </div>

                        <div class="col-md-2 text-end">
                            <a href="${ctx}/admin/settings" class="btn btn-outline-secondary w-100">
                                <i class="bi bi-arrow-counterclockwise"></i> Reset
                            </a>
                        </div>
                    </form>
                </div>
            </div>

            <!-- Table of 7 Columns matching SRS Wireframe & Mockup -->
            <div class="card border-0 shadow-sm rounded-3 overflow-hidden">
                <div class="table-responsive">
                    <table class="table table-hover align-middle mb-0">
                        <thead class="table-secondary text-secondary">
                            <tr>
                                <th scope="col" style="width: 80px;">
                                    <a href="${ctx}/admin/settings?keyword=${keyword}&type=${type}&status=${status}&sortBy=id&sortOrder=${sortBy == 'id' && sortOrder == 'ASC' ? 'DESC' : 'ASC'}"
                                       class="text-decoration-none text-dark fw-bold d-inline-flex align-items-center">
                                        Id
                                        <i class="bi bi-arrow-down-up ms-1 text-muted small"></i>
                                    </a>
                                </th>
                                <th scope="col">
                                    <a href="${ctx}/admin/settings?keyword=${keyword}&type=${type}&status=${status}&sortBy=name&sortOrder=${sortBy == 'name' && sortOrder == 'ASC' ? 'DESC' : 'ASC'}"
                                       class="text-decoration-none text-dark fw-bold d-inline-flex align-items-center">
                                        Name
                                        <i class="bi bi-arrow-down-up ms-1 text-muted small"></i>
                                    </a>
                                </th>
                                <th scope="col">
                                    <a href="${ctx}/admin/settings?keyword=${keyword}&type=${type}&status=${status}&sortBy=type&sortOrder=${sortBy == 'type' && sortOrder == 'ASC' ? 'DESC' : 'ASC'}"
                                       class="text-decoration-none text-dark fw-bold d-inline-flex align-items-center">
                                        Type
                                        <i class="bi bi-arrow-down-up ms-1 text-muted small"></i>
                                    </a>
                                </th>
                                <th scope="col">
                                    <a href="${ctx}/admin/settings?keyword=${keyword}&type=${type}&status=${status}&sortBy=value&sortOrder=${sortBy == 'value' && sortOrder == 'ASC' ? 'DESC' : 'ASC'}"
                                       class="text-decoration-none text-dark fw-bold d-inline-flex align-items-center">
                                        Value
                                        <i class="bi bi-arrow-down-up ms-1 text-muted small"></i>
                                    </a>
                                </th>
                                <th scope="col" style="width: 100px;">
                                    <a href="${ctx}/admin/settings?keyword=${keyword}&type=${type}&status=${status}&sortBy=priority&sortOrder=${sortBy == 'priority' && sortOrder == 'ASC' ? 'DESC' : 'ASC'}"
                                       class="text-decoration-none text-dark fw-bold d-inline-flex align-items-center">
                                        Priority
                                        <i class="bi bi-arrow-down-up ms-1 text-muted small"></i>
                                    </a>
                                </th>
                                <th scope="col" style="width: 120px;">
                                    <a href="${ctx}/admin/settings?keyword=${keyword}&type=${type}&status=${status}&sortBy=status&sortOrder=${sortBy == 'status' && sortOrder == 'ASC' ? 'DESC' : 'ASC'}"
                                       class="text-decoration-none text-dark fw-bold d-inline-flex align-items-center">
                                        Status
                                        <i class="bi bi-arrow-down-up ms-1 text-muted small"></i>
                                    </a>
                                </th>
                                <th scope="col" class="text-center" style="width: 180px;">Action</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:choose>
                                <c:when test="${not empty settingPage.content}">
                                    <c:forEach var="s" items="${settingPage.content}">
                                        <tr>
                                            <td class="text-muted fw-semibold">${s.id}</td>
                                            <td class="fw-semibold text-dark">
                                                <a href="${ctx}/admin/settings/detail?id=${s.id}" class="text-decoration-none text-primary">
                                                    ${s.name}
                                                </a>
                                            </td>
                                            <td>
                                                <c:choose>
                                                    <c:when test="${s.type == 'User Role'}">
                                                        <span class="badge bg-primary-subtle text-primary border border-primary-subtle">${s.type}</span>
                                                    </c:when>
                                                    <c:when test="${s.type == 'Subject'}">
                                                        <span class="badge bg-success-subtle text-success border border-success-subtle">${s.type}</span>
                                                    </c:when>
                                                    <c:when test="${s.type == 'Semester'}">
                                                        <span class="badge bg-warning-subtle text-warning-emphasis border border-warning-subtle">${s.type}</span>
                                                    </c:when>
                                                    <c:when test="${not empty s.type}">
                                                        <span class="badge bg-light text-dark border">${s.type}</span>
                                                    </c:when>
                                                    <c:otherwise>
                                                        <span class="text-muted italic">-- Setting Type --</span>
                                                    </c:otherwise>
                                                </c:choose>
                                            </td>
                                            <td><code>${s.value}</code></td>
                                            <td><span class="badge bg-secondary-subtle text-secondary px-2 py-1">${s.priority}</span></td>
                                            <td>
                                                <c:choose>
                                                    <c:when test="${s.status == 'Active'}">
                                                        <span class="badge bg-success-subtle text-success border border-success-subtle">Active</span>
                                                    </c:when>
                                                    <c:otherwise>
                                                        <span class="badge bg-secondary-subtle text-secondary border border-secondary-subtle">Inactive</span>
                                                    </c:otherwise>
                                                </c:choose>
                                            </td>
                                            <td class="text-center">
                                                <div class="d-inline-flex gap-2">
                                                    <!-- Edit Link -->
                                                    <a href="${ctx}/admin/settings/detail?id=${s.id}"
                                                       class="btn btn-sm btn-outline-primary" title="Edit Setting">
                                                        <i class="bi bi-pencil-square"></i> Edit
                                                    </a>

                                                    <!-- Activate / Deactivate Toggle -->
                                                    <form method="post" action="${ctx}/admin/settings/status" class="d-inline">
                                                        <input type="hidden" name="id" value="${s.id}">
                                                        <input type="hidden" name="returnUrl"
                                                               value="${ctx}/admin/settings?page=${settingPage.page}&keyword=${keyword}&type=${type}&status=${status}&sortBy=${sortBy}&sortOrder=${sortOrder}">
                                                        <c:choose>
                                                            <c:when test="${s.status == 'Active'}">
                                                                <button type="submit" class="btn btn-sm btn-outline-danger"
                                                                        title="Deactivate this setting"
                                                                        onclick="return confirm('Bạn có chắc muốn vô hiệu hóa cấu hình \'${s.name}\'?');">
                                                                    Deactivate
                                                                </button>
                                                            </c:when>
                                                            <c:otherwise>
                                                                <button type="submit" class="btn btn-sm btn-outline-success"
                                                                        title="Activate this setting">
                                                                    Activate
                                                                </button>
                                                            </c:otherwise>
                                                        </c:choose>
                                                    </form>
                                                </div>
                                            </td>
                                        </tr>
                                    </c:forEach>
                                </c:when>
                                <c:otherwise>
                                    <tr>
                                        <td colspan="7" class="text-center py-4 text-muted">
                                            <i class="bi bi-inbox fs-2 d-block mb-2"></i>
                                            Không tìm thấy cấu hình nào phù hợp với bộ lọc.
                                        </td>
                                    </tr>
                                </c:otherwise>
                            </c:choose>
                        </tbody>
                    </table>
                </div>

                <!-- Pagination Footer (SRS Wireframe < 1 2 ... 9 10 >) -->
                <c:if test="${settingPage.totalPages > 1}">
                    <div class="card-footer bg-white border-0 py-3 d-flex justify-content-between align-items-center">
                        <div class="text-muted small">
                            Hiển thị trang <strong>${settingPage.page}</strong> / <strong>${settingPage.totalPages}</strong>
                            (Tổng số <strong>${settingPage.totalElements}</strong> bản ghi)
                        </div>
                        <nav aria-label="Setting pagination">
                            <ul class="pagination pagination-sm mb-0">
                                <!-- Previous -->
                                <li class="page-item ${settingPage.hasPrevious() ? '' : 'disabled'}">
                                    <a class="page-link"
                                       href="${ctx}/admin/settings?page=${settingPage.page - 1}&keyword=${keyword}&type=${type}&status=${status}&sortBy=${sortBy}&sortOrder=${sortOrder}">
                                        &laquo;
                                    </a>
                                </li>

                                <!-- Page Numbers -->
                                <c:forEach begin="1" end="${settingPage.totalPages}" var="p">
                                    <li class="page-item ${p == settingPage.page ? 'active' : ''}">
                                        <a class="page-link"
                                           href="${ctx}/admin/settings?page=${p}&keyword=${keyword}&type=${type}&status=${status}&sortBy=${sortBy}&sortOrder=${sortOrder}">
                                            ${p}
                                        </a>
                                    </li>
                                </c:forEach>

                                <!-- Next -->
                                <li class="page-item ${settingPage.hasNext() ? '' : 'disabled'}">
                                    <a class="page-link"
                                       href="${ctx}/admin/settings?page=${settingPage.page + 1}&keyword=${keyword}&type=${type}&status=${status}&sortBy=${sortBy}&sortOrder=${sortOrder}">
                                        &raquo;
                                    </a>
                                </li>
                            </ul>
                        </nav>
                    </div>
                </c:if>
            </div>
        </div>
    </main>
</div>

<jsp:include page="/WEB-INF/views/common/footer.jsp" />
