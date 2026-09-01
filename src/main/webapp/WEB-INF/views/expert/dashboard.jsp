<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>

<c:set var="pageTitle" value="Expert Dashboard" scope="request" />

<jsp:include page="/WEB-INF/views/common/header.jsp" />

<div class="d-flex flex-grow-1">

    <jsp:include page="/WEB-INF/views/common/sidebar.jsp" />

    <main class="flex-grow-1 p-4">

        <!-- =====================================================
             PAGE HEADER
             ===================================================== -->

        <div class="mb-4">
            <h3 class="mb-1">Expert Dashboard</h3>
            <p class="text-muted mb-0">
                Tổng quan các khóa học của bạn: học viên, doanh thu và hoạt động gần đây.
            </p>
        </div>


        <!-- =====================================================
             SUMMARY STATISTICS
             ===================================================== -->

        <div class="row g-3 mb-3">

            <div class="col-sm-6 col-xl-3">
                <div class="card card-lms h-100">
                    <div class="card-body">
                        <div class="text-muted small mb-2">Khóa học của tôi</div>
                        <div class="fs-3 fw-bold">${dashboard.totalCourses}</div>
                        <div class="small text-muted mt-1">
                            ${dashboard.publishedCourses} đã xuất bản
                        </div>
                    </div>
                </div>
            </div>

            <div class="col-sm-6 col-xl-3">
                <div class="card card-lms h-100">
                    <div class="card-body">
                        <div class="text-muted small mb-2">Học viên</div>
                        <div class="fs-3 fw-bold text-primary">${dashboard.totalStudents}</div>
                        <div class="small text-muted mt-1">học viên duy nhất</div>
                    </div>
                </div>
            </div>

            <div class="col-sm-6 col-xl-3">
                <div class="card card-lms h-100">
                    <div class="card-body">
                        <div class="text-muted small mb-2">Tổng lượt đăng ký</div>
                        <div class="fs-3 fw-bold text-success">${dashboard.totalEnrollments}</div>
                    </div>
                </div>
            </div>

            <div class="col-sm-6 col-xl-3">
                <div class="card card-lms h-100">
                    <div class="card-body">
                        <div class="text-muted small mb-2">Doanh thu</div>
                        <div class="fs-3 fw-bold">
                            <fmt:formatNumber value="${dashboard.totalRevenue}" type="number" groupingUsed="true" maxFractionDigits="0" /> đ
                        </div>
                    </div>
                </div>
            </div>

        </div>


        <!-- =====================================================
             COURSE STATUS BREAKDOWN
             ===================================================== -->

        <div class="card card-lms mb-4">
            <div class="card-body">
                <div class="text-muted small mb-3 text-uppercase">Khóa học theo trạng thái</div>
                <div class="row text-center g-3">
                    <div class="col-4">
                        <div class="fs-4 fw-bold text-success">${dashboard.publishedCourses}</div>
                        <span class="badge bg-success">PUBLISHED</span>
                    </div>
                    <div class="col-4">
                        <div class="fs-4 fw-bold text-secondary">${dashboard.draftCourses}</div>
                        <span class="badge bg-secondary">DRAFT</span>
                    </div>
                    <div class="col-4">
                        <div class="fs-4 fw-bold text-dark">${dashboard.archivedCourses}</div>
                        <span class="badge bg-dark">ARCHIVED</span>
                    </div>
                </div>
            </div>
        </div>


        <div class="row g-3">

            <!-- =================================================
                 TOP COURSES BY ENROLLMENT
                 ================================================= -->

            <div class="col-lg-6">
                <div class="card card-lms h-100">
                    <div class="card-body">
                        <div class="d-flex justify-content-between align-items-center mb-3">
                            <h6 class="mb-0">Khóa học nổi bật của tôi</h6>
                            <a href="${pageContext.request.contextPath}/expert/courses" class="small">Xem tất cả</a>
                        </div>

                        <c:choose>
                            <c:when test="${empty dashboard.topCourses}">
                                <p class="text-muted small mb-0">Bạn chưa có khóa học nào.</p>
                            </c:when>
                            <c:otherwise>
                                <div class="table-responsive">
                                    <table class="table table-sm align-middle mb-0">
                                        <thead>
                                            <tr class="text-muted small">
                                                <th>Khóa học</th>
                                                <th>Trạng thái</th>
                                                <th class="text-end">Học viên</th>
                                            </tr>
                                        </thead>
                                        <tbody>
                                            <c:forEach var="course" items="${dashboard.topCourses}">
                                                <tr>
                                                    <td>
                                                        <a href="${pageContext.request.contextPath}/expert/courses/detail?id=${course.id}">
                                                                ${course.title}
                                                        </a>
                                                    </td>
                                                    <td>
                                                        <span class="badge bg-light text-dark border">${course.status}</span>
                                                    </td>
                                                    <td class="text-end fw-semibold">${course.enrollmentCount}</td>
                                                </tr>
                                            </c:forEach>
                                        </tbody>
                                    </table>
                                </div>
                            </c:otherwise>
                        </c:choose>
                    </div>
                </div>
            </div>


            <!-- =================================================
                 RECENT ENROLLMENTS
                 ================================================= -->

            <div class="col-lg-6">
                <div class="card card-lms h-100">
                    <div class="card-body">
                        <h6 class="mb-3">Đăng ký gần đây</h6>

                        <c:choose>
                            <c:when test="${empty dashboard.recentEnrollments}">
                                <p class="text-muted small mb-0">Chưa có lượt đăng ký nào.</p>
                            </c:when>
                            <c:otherwise>
                                <div class="table-responsive">
                                    <table class="table table-sm align-middle mb-0">
                                        <thead>
                                            <tr class="text-muted small">
                                                <th>Học viên</th>
                                                <th>Khóa học</th>
                                                <th>Trạng thái</th>
                                                <th class="text-end">Ngày</th>
                                            </tr>
                                        </thead>
                                        <tbody>
                                            <c:forEach var="enr" items="${dashboard.recentEnrollments}">
                                                <tr>
                                                    <td>${enr.studentName}</td>
                                                    <td class="text-muted small">${enr.courseTitle}</td>
                                                    <td>
                                                        <span class="badge bg-light text-dark border">${enr.status}</span>
                                                    </td>
                                                    <td class="text-end small text-muted">
                                                        <fmt:formatDate value="${enr.enrolledAt}" pattern="dd/MM/yyyy" />
                                                    </td>
                                                </tr>
                                            </c:forEach>
                                        </tbody>
                                    </table>
                                </div>
                            </c:otherwise>
                        </c:choose>
                    </div>
                </div>
            </div>

        </div>

    </main>

</div>

<jsp:include page="/WEB-INF/views/common/footer.jsp" />
