<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<c:set var="pageTitle" value="${lesson.title}" scope="request" />

<jsp:include page="/WEB-INF/views/common/header.jsp" />

<div class="d-flex flex-grow-1">

    <jsp:include page="/WEB-INF/views/common/sidebar.jsp" />

    <main class="flex-grow-1 p-4">

        <!-- =====================================================
             BACK TO LESSON DETAIL
             ===================================================== -->

        <div class="mb-3">

            <a href="${pageContext.request.contextPath}/trainee/lessons/detail?id=${lesson.id}&courseId=${lesson.courseId}"
               class="text-decoration-none">

                <i class="bi bi-arrow-left"></i>
                Quay lại bài học

            </a>

        </div>


        <!-- =====================================================
             LESSON HEADER
             ===================================================== -->

        <div class="mb-4">

            <h3 class="mb-2">
                ${lesson.title}
            </h3>

            <div class="d-flex flex-wrap gap-3 text-muted small">

                <span>
                    <i class="bi bi-play-circle"></i>
                    Video
                </span>

                <span>
                    <i class="bi bi-clock"></i>
                    ${lesson.durationMinutes} phút
                </span>

            </div>

        </div>


        <!-- =====================================================
             VIDEO PLAYER
             ===================================================== -->

        <div class="card card-lms mb-4">

            <div class="card-body p-0">

                <div class="ratio ratio-16x9 bg-dark">

                    <video
                            class="w-100 h-100"
                            controls
                            preload="metadata"
                            src="${pageContext.request.contextPath}/${lesson.contentUrl}">

                        Trình duyệt của bạn không hỗ trợ phát video.

                    </video>

                </div>

            </div>

        </div>


        <!-- =====================================================
             LESSON INFORMATION
             ===================================================== -->

        <div class="card card-lms">

            <div class="card-body">

                <h5 class="mb-3">
                    ${lesson.title}
                </h5>

                <div class="row g-3">

                    <!-- Lesson Type -->

                    <div class="col-md-4">

                        <div class="text-muted small">
                            Loại bài học
                        </div>

                        <div class="fw-semibold">

                            <i class="bi bi-play-circle me-1"></i>
                            Video

                        </div>

                    </div>


                    <!-- Duration -->

                    <div class="col-md-4">

                        <div class="text-muted small">
                            Thời lượng
                        </div>

                        <div class="fw-semibold">

                            <i class="bi bi-clock me-1"></i>
                            ${lesson.durationMinutes} phút

                        </div>

                    </div>


                    <!-- Status -->

                    <div class="col-md-4">

                        <div class="text-muted small">
                            Trạng thái
                        </div>

                        <div class="fw-semibold">

                            <c:choose>

                                <c:when test="${lesson.completed}">

                                    <span class="text-success">

                                        <i class="bi bi-check-circle-fill me-1"></i>
                                        Đã hoàn thành

                                    </span>

                                </c:when>

                                <c:otherwise>

                                    <form method="post"
                                          action="${pageContext.request.contextPath}/trainee/lessons/complete"
                                          class="d-inline">

                                        <input type="hidden"
                                               name="id"
                                               value="${lesson.id}">

                                        <input type="hidden"
                                               name="courseId"
                                               value="${lesson.courseId}">

                                        <button type="submit"
                                                class="btn btn-success btn-sm">

                                            <i class="bi bi-check-circle me-1"></i>
                                            Đánh dấu hoàn thành

                                        </button>

                                    </form>

                                </c:otherwise>

                            </c:choose>

                        </div>

                    </div>

                </div>

            </div>

        </div>

    </main>

</div>

<jsp:include page="/WEB-INF/views/common/footer.jsp" />
