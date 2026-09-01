<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<c:set var="pageTitle" value="Trainee Dashboard" scope="request" />

<jsp:include page="/WEB-INF/views/common/header.jsp" />

<div class="d-flex flex-grow-1">

    <jsp:include page="/WEB-INF/views/common/sidebar.jsp" />

    <main class="flex-grow-1 p-4">

        <!-- =====================================================
             PAGE HEADER
             ===================================================== -->

        <div class="mb-4">
            <h3 class="mb-1">Trainee Dashboard</h3>
            <p class="text-muted mb-0">
                Theo dõi tiến độ học tập và tiếp tục các khóa học đã đăng ký.
            </p>
        </div>


        <!-- =====================================================
             SUMMARY STATISTICS
             ===================================================== -->

        <div class="row g-3 mb-4">

            <div class="col-sm-6 col-xl-3">
                <div class="card card-lms h-100">
                    <div class="card-body">
                        <div class="text-muted small mb-2">Khóa học đã đăng ký</div>
                        <div class="fs-3 fw-bold">${dashboard.enrolledCourses}</div>
                    </div>
                </div>
            </div>

            <div class="col-sm-6 col-xl-3">
                <div class="card card-lms h-100">
                    <div class="card-body">
                        <div class="text-muted small mb-2">Đang học</div>
                        <div class="fs-3 fw-bold text-primary">${dashboard.inProgressCourses}</div>
                    </div>
                </div>
            </div>

            <div class="col-sm-6 col-xl-3">
                <div class="card card-lms h-100">
                    <div class="card-body">
                        <div class="text-muted small mb-2">Đã hoàn thành</div>
                        <div class="fs-3 fw-bold text-success">${dashboard.completedCourses}</div>
                    </div>
                </div>
            </div>

            <div class="col-sm-6 col-xl-3">
                <div class="card card-lms h-100">
                    <div class="card-body">
                        <div class="text-muted small mb-2">Tiến độ tổng thể</div>
                        <div class="fs-3 fw-bold">${dashboard.overallProgressPercent}%</div>
                    </div>
                </div>
            </div>

        </div>


        <div class="d-flex justify-content-between align-items-center mb-3">
            <h4 class="mb-0">Khóa học của tôi</h4>
            <span class="text-muted small">${dashboard.enrolledCourses} khóa học</span>
        </div>


        <!-- =====================================================
             COURSE LIST CONTROLS
             ===================================================== -->

        <div class="card card-lms mb-4">
            <div class="card-body p-3">
                <form method="get"
                      action="${pageContext.request.contextPath}/trainee/dashboard"
                      class="row g-2 align-items-center">

                    <input type="hidden" name="page" value="1" />

                    <div class="col-md-4">
                        <div class="input-group">
                            <input type="text"
                                   name="search"
                                   class="form-control"
                                   value="${fn:escapeXml(search)}"
                                   placeholder="Tìm theo tên khóa học..." />
                            <button type="submit" class="btn btn-primary">
                                <i class="bi bi-search"></i>
                                Tìm kiếm
                            </button>
                        </div>
                    </div>

                    <div class="col-md-3">
                        <select name="status" class="form-select">
                            <option value="ALL" ${status == 'ALL' ? 'selected' : ''}>Tất cả trạng thái</option>
                            <option value="IN_PROGRESS" ${status == 'IN_PROGRESS' ? 'selected' : ''}>Đang học</option>
                            <option value="COMPLETED" ${status == 'COMPLETED' ? 'selected' : ''}>Đã hoàn thành</option>
                        </select>
                    </div>

                    <div class="col-md-3">
                        <select name="sort" class="form-select">
                            <option value="newest" ${sort == 'newest' ? 'selected' : ''}>Đăng ký mới nhất</option>
                            <option value="oldest" ${sort == 'oldest' ? 'selected' : ''}>Đăng ký lâu nhất</option>
                            <option value="name_asc" ${sort == 'name_asc' ? 'selected' : ''}>Tên khóa học: A–Z</option>
                            <option value="name_desc" ${sort == 'name_desc' ? 'selected' : ''}>Tên khóa học: Z–A</option>
                            <option value="progress_asc" ${sort == 'progress_asc' ? 'selected' : ''}>Tiến độ: thấp đến cao</option>
                            <option value="progress_desc" ${sort == 'progress_desc' ? 'selected' : ''}>Tiến độ: cao đến thấp</option>
                        </select>
                    </div>

                    <div class="col-md-2">
                        <a href="${pageContext.request.contextPath}/trainee/dashboard"
                           class="btn btn-outline-secondary w-100">
                            <i class="bi bi-arrow-counterclockwise"></i>
                            Đặt lại
                        </a>
                    </div>

                </form>
            </div>
        </div>


        <!-- =====================================================
             COURSE LIST
             ===================================================== -->

        <div class="row g-4">

            <c:forEach var="course" items="${coursePage.content}">

                <div class="col-md-6 col-xl-4">

                    <div class="card card-lms h-100">

                        <div class="card-body d-flex flex-column">

                            <!-- Course title -->

                            <h5 class="card-title mb-2">
                                    ${course.title}
                            </h5>


                            <!-- Category -->

                            <c:if test="${not empty course.category}">
                                <div class="mb-3">
                                    <span class="badge bg-light text-dark border">
                                            ${course.category}
                                    </span>
                                    <span class="badge bg-primary">
                                            ${course.enrollmentStatus}
                                    </span>
                                </div>
                            </c:if>

                            <c:if test="${empty course.category}">
                                <div class="mb-3">
                                    <span class="badge bg-primary">
                                            ${course.enrollmentStatus}
                                    </span>
                                </div>
                            </c:if>


                            <!-- Description -->

                            <p class="card-text text-muted flex-grow-1">
                                    ${course.description}
                            </p>


                            <!-- Course information -->

                            <div class="small text-muted mb-3">

                                <div class="mb-1">
                                    <i class="bi bi-clock"></i>
                                        ${course.durationHours} giờ
                                </div>

                                <div>
                                    <i class="bi bi-calendar3"></i>
                                    Đăng ký:
                                    <fmt:formatDate
                                            value="${course.enrolledAt}"
                                            pattern="dd/MM/yyyy"
                                    />
                                </div>

                            </div>


                            <!-- Progress -->

                            <div class="mb-3">

                                <div class="d-flex justify-content-between
                                            align-items-center mb-1">

                                    <span class="small">
                                        Tiến độ
                                    </span>

                                    <span class="small fw-semibold">
                                        ${course.progressPercent}%
                                    </span>

                                </div>

                                <div class="progress" style="height: 8px;">

                                    <div class="progress-bar"
                                         role="progressbar"
                                         style="width: ${course.progressPercent}%"
                                         aria-valuenow="${course.progressPercent}"
                                         aria-valuemin="0"
                                         aria-valuemax="100">
                                    </div>

                                </div>

                            </div>


                            <!-- Action -->

                            <div class="mt-auto">

                                <a href="${pageContext.request.contextPath}/trainee/courses/detail?id=${course.courseId}"
                                   class="btn btn-primary w-100">

                                    <i class="bi bi-book"></i>

                                    <c:choose>

                                        <c:when test="${course.progressPercent > 0}">
                                            Tiếp tục học
                                        </c:when>

                                        <c:otherwise>
                                            Bắt đầu học
                                        </c:otherwise>

                                    </c:choose>

                                </a>

                            </div>

                        </div>

                    </div>

                </div>

            </c:forEach>


            <!-- =================================================
                 EMPTY STATE
                 ================================================= -->

            <c:if test="${empty coursePage.content}">

                <div class="col-12">

                    <div class="card card-lms">

                        <div class="card-body text-center py-5">

                            <i class="bi bi-book display-5 text-muted"></i>

                            <c:choose>
                                <c:when test="${dashboard.enrolledCourses == 0}">
                                    <h5 class="mt-3">Bạn chưa đăng ký khóa học nào</h5>
                                    <p class="text-muted mb-4">
                                        Hãy khám phá các khóa học và bắt đầu hành trình học tập của bạn.
                                    </p>
                                </c:when>
                                <c:otherwise>
                                    <h5 class="mt-3">Không tìm thấy khóa học phù hợp</h5>
                                    <p class="text-muted mb-4">
                                        Hãy thử thay đổi từ khóa hoặc bộ lọc của bạn.
                                    </p>
                                </c:otherwise>
                            </c:choose>

                            <a href="${pageContext.request.contextPath}/courses"
                               class="btn btn-outline-primary">

                                <i class="bi bi-search"></i>
                                Khám phá khóa học

                            </a>

                        </div>

                    </div>

                </div>

            </c:if>

        </div>


        <!-- =====================================================
             PAGINATION
             ===================================================== -->

        <c:if test="${coursePage.totalPages > 1}">
            <div class="d-flex justify-content-between align-items-center mt-4">
                <span class="text-muted small">
                    Trang ${coursePage.page} / ${coursePage.totalPages}
                    (${coursePage.totalElements} khóa học)
                </span>

                <nav aria-label="My enrolled courses pagination">
                    <ul class="pagination pagination-sm mb-0">
                        <c:url var="previousUrl" value="/trainee/dashboard">
                            <c:param name="page" value="${coursePage.page - 1}" />
                            <c:param name="search" value="${search}" />
                            <c:param name="status" value="${status}" />
                            <c:param name="sort" value="${sort}" />
                        </c:url>
                        <li class="page-item ${coursePage.hasPrevious() ? '' : 'disabled'}">
                            <a class="page-link" href="${previousUrl}">&laquo;</a>
                        </li>

                        <c:forEach begin="1" end="${coursePage.totalPages}" var="p">
                            <c:url var="pageUrl" value="/trainee/dashboard">
                                <c:param name="page" value="${p}" />
                                <c:param name="search" value="${search}" />
                                <c:param name="status" value="${status}" />
                                <c:param name="sort" value="${sort}" />
                            </c:url>
                            <li class="page-item ${p == coursePage.page ? 'active' : ''}">
                                <a class="page-link" href="${pageUrl}">${p}</a>
                            </li>
                        </c:forEach>

                        <c:url var="nextUrl" value="/trainee/dashboard">
                            <c:param name="page" value="${coursePage.page + 1}" />
                            <c:param name="search" value="${search}" />
                            <c:param name="status" value="${status}" />
                            <c:param name="sort" value="${sort}" />
                        </c:url>
                        <li class="page-item ${coursePage.hasNext() ? '' : 'disabled'}">
                            <a class="page-link" href="${nextUrl}">&raquo;</a>
                        </li>
                    </ul>
                </nav>
            </div>
        </c:if>

    </main>

</div>

<jsp:include page="/WEB-INF/views/common/footer.jsp" />
