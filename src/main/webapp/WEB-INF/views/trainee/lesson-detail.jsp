<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<c:set var="pageTitle" value="${lesson.title}" scope="request" />

<jsp:include page="/WEB-INF/views/common/header.jsp" />

<div class="d-flex flex-grow-1">

    <jsp:include page="/WEB-INF/views/common/sidebar.jsp" />

    <main class="flex-grow-1 p-4">

        <!-- =====================================================
             BACK TO COURSE
             ===================================================== -->

        <div class="mb-3">

            <a href="${pageContext.request.contextPath}/trainee/courses/lessons?id=${lesson.courseId}"
               class="text-decoration-none">

                <i class="bi bi-arrow-left"></i>
                Quay lại danh sách bài học

            </a>

        </div>


        <!-- =====================================================
             LESSON DETAIL
             ===================================================== -->

        <div class="card card-lms">

            <div class="card-body p-4 p-lg-5">

                <!-- Lesson type -->

                <c:if test="${not empty lesson.lessonType}">

                    <div class="mb-3">

                        <span class="badge bg-primary">

                            <c:choose>

                                <c:when test="${lesson.lessonType == 'VIDEO'}">
                                    <i class="bi bi-play-circle"></i>
                                    VIDEO
                                </c:when>

                                <c:when test="${lesson.lessonType == 'DOCUMENT'}">
                                    <i class="bi bi-file-earmark-text"></i>
                                    DOCUMENT
                                </c:when>

                                <c:otherwise>
                                    ${lesson.lessonType}
                                </c:otherwise>

                            </c:choose>

                        </span>

                    </div>

                </c:if>


                <!-- Title -->

                <h2 class="mb-3">
                    ${lesson.title}
                </h2>

                <p class="text-muted mb-3">
                    Khóa học: ${lesson.courseTitle}
                </p>


                <!-- Lesson information -->

                <div class="d-flex flex-wrap gap-4
                            text-muted mb-4">

                    <span>

                        <i class="bi bi-clock"></i>

                        ${lesson.durationMinutes} phút

                    </span>


                    <span>

                        <i class="bi bi-list-ol"></i>

                        Bài ${lesson.orderIndex}

                    </span>

                </div>


                <hr>


                <!-- Lesson status -->

                <div class="mt-4">

                    <h5 class="mb-3">
                        Thông tin bài học
                    </h5>

                    <div class="row g-3">

                        <div class="col-md-6">

                            <div class="border rounded p-3 h-100">

                                <div class="text-muted small mb-1">
                                    Loại bài học
                                </div>

                                <div class="fw-semibold">

                                    <c:choose>

                                        <c:when test="${lesson.lessonType == 'VIDEO'}">
                                            Video
                                        </c:when>

                                        <c:when test="${lesson.lessonType == 'DOCUMENT'}">
                                            Tài liệu
                                        </c:when>

                                        <c:otherwise>
                                            ${lesson.lessonType}
                                        </c:otherwise>

                                    </c:choose>

                                </div>

                            </div>

                        </div>

                        <div class="col-md-6">

                            <div class="border rounded p-3 h-100">

                                <div class="text-muted small mb-1">
                                    Trạng thái
                                </div>

                                <div class="fw-semibold">
                                    <c:choose>
                                        <c:when test="${lesson.completed}">
                                            <span class="text-success">
                                                <i class="bi bi-check-circle-fill"></i> Đã hoàn thành
                                            </span>
                                        </c:when>
                                        <c:otherwise>Chưa hoàn thành</c:otherwise>
                                    </c:choose>
                                </div>

                            </div>

                        </div>


                        <div class="col-md-6">

                            <div class="border rounded p-3 h-100">

                                <div class="text-muted small mb-1">
                                    Thời lượng
                                </div>

                                <div class="fw-semibold">

                                    ${lesson.durationMinutes} phút

                                </div>

                            </div>

                        </div>

                    </div>

                </div>


                <!-- =================================================
                     CONTENT STATUS
                     ================================================= -->

                <div class="alert alert-light border mt-4">

                    <div class="d-flex align-items-center">

                        <i class="bi bi-info-circle me-2"></i>

                        <span>
                            Nội dung bài học được mở trong Lesson Viewer.
                        </span>

                    </div>

                </div>


                <!-- =================================================
                     OPEN LESSON VIEWER
                     ================================================= -->

                <div class="d-flex justify-content-between
                            align-items-center mt-4">

                    <a href="${pageContext.request.contextPath}/trainee/courses/lessons?id=${lesson.courseId}"
                       class="btn btn-outline-secondary">

                        <i class="bi bi-arrow-left"></i>
                        Danh sách bài học

                    </a>


                    <a href="${pageContext.request.contextPath}/trainee/lessons/viewer?id=${lesson.id}&courseId=${lesson.courseId}"
                       class="btn btn-primary">

                        <i class="bi bi-play-circle"></i>

                        <c:choose>
                            <c:when test="${lesson.completed}">Xem lại bài học</c:when>
                            <c:otherwise>Bắt đầu học</c:otherwise>
                        </c:choose>

                    </a>

                </div>

            </div>

        </div>

    </main>

</div>

<jsp:include page="/WEB-INF/views/common/footer.jsp" />
