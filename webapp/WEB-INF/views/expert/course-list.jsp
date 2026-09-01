<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<c:set var="pageTitle" value="Quản lý khóa học" scope="request" />
<jsp:include page="/WEB-INF/views/common/header.jsp" />

<div class="d-flex flex-grow-1">
    <jsp:include page="/WEB-INF/views/common/sidebar.jsp" />

    <main class="flex-grow-1 p-4">
        <div class="d-flex justify-content-between align-items-center mb-4">
            <h3 class="mb-0">Quản lý khóa học</h3>
            <a href="${pageContext.request.contextPath}/expert/courses/new" class="btn btn-primary">
                <i class="bi bi-plus-lg"></i> Thêm khóa học
            </a>
        </div>

        <div class="card card-lms p-3 mb-3">
            <form method="get" action="${pageContext.request.contextPath}/expert/courses" class="row g-2">
                <div class="col-md-6">
                    <input type="text" name="keyword" class="form-control"
                           placeholder="Tìm theo tên khóa học, danh mục..." value="${keyword}">
                </div>
                <div class="col-md-3">
                    <select name="status" class="form-select">
                        <option value="">-- Tất cả trạng thái --</option>
                        <option value="DRAFT" ${status == 'DRAFT' ? 'selected' : ''}>DRAFT</option>
                        <option value="PUBLISHED" ${status == 'PUBLISHED' ? 'selected' : ''}>PUBLISHED</option>
                        <option value="ARCHIVED" ${status == 'ARCHIVED' ? 'selected' : ''}>ARCHIVED</option>
                    </select>
                </div>
                <div class="col-md-3">
                    <button type="submit" class="btn btn-outline-primary w-100">
                        <i class="bi bi-search"></i> Tìm kiếm
                    </button>
                </div>
            </form>
        </div>

        <div class="card card-lms p-0">
            <div class="table-responsive">
                <table class="table table-hover mb-0 align-middle">
                    <thead class="table-light">
                        <tr>
                            <th>#</th>
                            <th>Tên khóa học</th>
                            <th>Danh mục</th>
                            <th>Thời lượng (giờ)</th>
                            <th>Trạng thái</th>
                            <th>Cập nhật</th>
                            <th class="text-end">Thao tác</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:forEach var="c" items="${courses}">
                            <tr>
                                <td>${c.id}</td>
                                <td>${c.title}</td>
                                <td>${c.category}</td>
                                <td>${c.durationHours}</td>
                                <td>
                                    <c:choose>
                                        <c:when test="${c.status == 'PUBLISHED'}">
                                            <span class="badge bg-success">PUBLISHED</span>
                                        </c:when>
                                        <c:when test="${c.status == 'DRAFT'}">
                                            <span class="badge bg-warning text-dark">DRAFT</span>
                                        </c:when>
                                        <c:otherwise>
                                            <span class="badge bg-secondary">ARCHIVED</span>
                                        </c:otherwise>
                                    </c:choose>
                                </td>
                                <td><fmt:formatDate value="${c.updatedAt}" pattern="dd/MM/yyyy" /></td>
                                <td class="text-end">
                                    <a href="${pageContext.request.contextPath}/expert/courses/detail?id=${c.id}"
                                       class="btn btn-sm btn-outline-primary">
                                        <i class="bi bi-eye"></i> Chi tiết
                                    </a>
                                    <form method="post" class="d-inline confirm-delete"
                                          data-confirm-message="Xóa khóa học '${c.title}'?"
                                          action="${pageContext.request.contextPath}/expert/courses/delete">
                                        <input type="hidden" name="id" value="${c.id}">
                                        <button type="submit" class="btn btn-sm btn-outline-danger">
                                            <i class="bi bi-trash"></i>
                                        </button>
                                    </form>
                                </td>
                            </tr>
                        </c:forEach>
                        <c:if test="${empty courses}">
                            <tr>
                                <td colspan="7" class="text-center text-muted py-4">Bạn chưa có khóa học nào.</td>
                            </tr>
                        </c:if>
                    </tbody>
                </table>
            </div>
        </div>
    </main>
</div>

<jsp:include page="/WEB-INF/views/common/footer.jsp" />
