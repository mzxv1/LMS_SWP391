<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>

<c:set var="pageTitle" value="Quản lý bài học" scope="request" />

<jsp:include page="/WEB-INF/views/common/header.jsp" />

<div class="d-flex flex-grow-1">

    <jsp:include page="/WEB-INF/views/common/sidebar.jsp" />

    <main class="flex-grow-1 p-4">

        <div class="d-flex justify-content-between align-items-center mb-4">
            <div>
                <h3 class="mb-1">Quản lý Bài học</h3>
                <p class="text-muted mb-0">
                    Khóa học: <strong>${course != null ? course.title : 'Chọn một khóa học'}</strong>
                </p>
            </div>
            <c:if test="${course != null}">
                <a href="${pageContext.request.contextPath}/expert/lessons?action=add&courseId=${course.id}" class="btn btn-primary">
                    <i class="bi bi-plus-circle"></i> Thêm bài học mới
                </a>
            </c:if>
        </div>

        <c:if test="${not empty error}">
            <div class="alert alert-danger">${error}</div>
        </c:if>

        <c:choose>
            <c:when test="${course == null}">
                <div class="alert alert-info">Vui lòng chọn một khóa học để quản lý bài học.</div>
            </c:when>
            <c:when test="${empty lessons}">
                <div class="card card-lms">
                    <div class="card-body text-center p-5">
                        <h5 class="text-muted">Khóa học này chưa có bài học nào.</h5>
                        <p class="mb-0">Hãy bắt đầu bằng cách thêm bài học mới!</p>
                    </div>
                </div>
            </c:when>
            <c:otherwise>
                <div class="card card-lms">
                    <div class="card-body p-0">
                        <div class="table-responsive">
                            <table class="table table-hover align-middle mb-0">
                                <thead class="table-light">
                                    <tr>
                                        <th class="ps-3">Thứ tự</th>
                                        <th>Tên bài học</th>
                                        <th>Chapter</th>
                                        <th>Loại</th>
                                        <th>Thời lượng</th>
                                        <th class="text-end pe-3">Hành động</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    <c:forEach var="lesson" items="${lessons}">
                                        <tr>
                                            <td class="ps-3 fw-bold">${lesson.orderIndex}</td>
                                            <td>${lesson.title}</td>
                                            <td>
                                                <c:set var="chapterFound" value="false" />
                                                <c:forEach var="chapter" items="${chapters}">
                                                    <c:if test="${chapter.id == lesson.chapterId}">
                                                        ${chapter.title}
                                                        <c:set var="chapterFound" value="true" />
                                                    </c:if>
                                                </c:forEach>
                                                <c:if test="${!chapterFound}">
                                                    <span class="text-muted fst-italic">Không có</span>
                                                </c:if>
                                            </td>
                                            <td>
                                                <span class="badge bg-secondary">${lesson.lessonType}</span>
                                            </td>
                                            <td>${lesson.durationMinutes} phút</td>
                                            <td class="text-end pe-3">
                                                <a href="${pageContext.request.contextPath}/expert/lessons?action=edit&id=${lesson.id}" class="btn btn-sm btn-outline-primary">
                                                    <i class="bi bi-pencil"></i> Sửa
                                                </a>
                                                <form action="${pageContext.request.contextPath}/expert/lessons?action=delete&id=${lesson.id}" method="post" class="d-inline" onsubmit="return confirm('Bạn có chắc chắn muốn xóa bài học này không?');">
                                                    <button type="submit" class="btn btn-sm btn-outline-danger">
                                                        <i class="bi bi-trash"></i> Xóa
                                                    </button>
                                                </form>
                                            </td>
                                        </tr>
                                    </c:forEach>
                                </tbody>
                            </table>
                        </div>
                    </div>
                </div>
            </c:otherwise>
        </c:choose>

    </main>

</div>

<jsp:include page="/WEB-INF/views/common/footer.jsp" />
