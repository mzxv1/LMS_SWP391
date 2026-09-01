<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>

<c:set var="pageTitle" value="Admin Dashboard" scope="request" />

<jsp:include page="/WEB-INF/views/common/header.jsp" />

<div class="d-flex flex-grow-1">

    <jsp:include page="/WEB-INF/views/common/sidebar.jsp" />

    <main class="flex-grow-1 p-4">

        <!-- =====================================================
             PAGE HEADER
             ===================================================== -->

        <div class="mb-4">
            <h3 class="mb-1">Admin Dashboard</h3>
            <p class="text-muted mb-0">
                Tổng quan toàn hệ thống: người dùng, khóa học, đăng ký và doanh thu.
            </p>
        </div>


        <!-- =====================================================
             SUMMARY STATISTICS - USERS & COURSES
             ===================================================== -->

        <div class="row g-3 mb-3">

            <div class="col-sm-6 col-xl-3">
                <div class="card card-lms h-100">
                    <div class="card-body">
                        <div class="text-muted small mb-2">Tổng người dùng</div>
                        <div class="fs-3 fw-bold">${dashboard.totalUsers}</div>
                        <div class="small text-muted mt-1">
                            ${dashboard.activeUsers} hoạt động &middot; ${dashboard.inactiveUsers} ngừng hoạt động
                        </div>
                    </div>
                </div>
            </div>

            <div class="col-sm-6 col-xl-3">
                <div class="card card-lms h-100">
                    <div class="card-body">
                        <div class="text-muted small mb-2">Tổng khóa học</div>
                        <div class="fs-3 fw-bold text-primary">${dashboard.totalCourses}</div>
                        <div class="small text-muted mt-1">
                            ${dashboard.publishedCourses} đã xuất bản
                        </div>
                    </div>
                </div>
            </div>

            <div class="col-sm-6 col-xl-3">
                <div class="card card-lms h-100">
                    <div class="card-body">
                        <div class="text-muted small mb-2">Tổng lượt đăng ký</div>
                        <div class="fs-3 fw-bold text-success">${dashboard.totalEnrollments}</div>
                        <div class="small text-muted mt-1">
                            ${dashboard.activeEnrollments} đang hoạt động
                        </div>
                    </div>
                </div>
            </div>

            <div class="col-sm-6 col-xl-3">
                <div class="card card-lms h-100">
                    <div class="card-body">
                        <div class="text-muted small mb-2">Tổng doanh thu</div>
                        <div class="fs-3 fw-bold">
                            <fmt:formatNumber value="${dashboard.totalRevenue}" type="number" groupingUsed="true" maxFractionDigits="0" /> đ
                        </div>
                    </div>
                </div>
            </div>

        </div>


        <!-- =====================================================
             BREAKDOWN - USER ROLES & COURSE STATUS
             ===================================================== -->

        <div class="row g-3 mb-4">

            <div class="col-md-6">
                <div class="card card-lms h-100">
                    <div class="card-body">
                        <div class="text-muted small mb-3 text-uppercase">Người dùng theo vai trò</div>
                        <div class="d-flex justify-content-between mb-2">
                            <span><i class="bi bi-shield-lock"></i> Admin</span>
                            <span class="fw-semibold">${dashboard.totalAdmins}</span>
                        </div>
                        <div class="d-flex justify-content-between mb-2">
                            <span><i class="bi bi-person-workspace"></i> Chuyên gia</span>
                            <span class="fw-semibold">${dashboard.totalExperts}</span>
                        </div>
                        <div class="d-flex justify-content-between">
                            <span><i class="bi bi-mortarboard"></i> Học viên</span>
                            <span class="fw-semibold">${dashboard.totalStudents}</span>
                        </div>
                    </div>
                </div>
            </div>

            <div class="col-md-6">
                <div class="card card-lms h-100">
                    <div class="card-body">
                        <div class="text-muted small mb-3 text-uppercase">Khóa học theo trạng thái</div>
                        <div class="d-flex justify-content-between mb-2">
                            <span><span class="badge bg-success">PUBLISHED</span></span>
                            <span class="fw-semibold">${dashboard.publishedCourses}</span>
                        </div>
                        <div class="d-flex justify-content-between mb-2">
                            <span><span class="badge bg-secondary">DRAFT</span></span>
                            <span class="fw-semibold">${dashboard.draftCourses}</span>
                        </div>
                        <div class="d-flex justify-content-between">
                            <span><span class="badge bg-dark">ARCHIVED</span></span>
                            <span class="fw-semibold">${dashboard.archivedCourses}</span>
                        </div>
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
                            <h6 class="mb-0">Khóa học nổi bật</h6>
                            <a href="${pageContext.request.contextPath}/admin/courses" class="small">Xem tất cả</a>
                        </div>

                        <c:choose>
                            <c:when test="${empty dashboard.topCourses}">
                                <p class="text-muted small mb-0">Chưa có khóa học nào được đăng ký.</p>
                            </c:when>
                            <c:otherwise>
                                <div class="table-responsive">
                                    <table class="table table-sm align-middle mb-0">
                                        <thead>
                                            <tr class="text-muted small">
                                                <th>Khóa học</th>
                                                <th>Chuyên gia</th>
                                                <th class="text-end">Học viên</th>
                                            </tr>
                                        </thead>
                                        <tbody>
                                            <c:forEach var="course" items="${dashboard.topCourses}">
                                                <tr>
                                                    <td>
                                                        <a href="${pageContext.request.contextPath}/admin/courses/detail?id=${course.id}">
                                                                ${course.title}
                                                        </a>
                                                    </td>
                                                    <td class="text-muted small">${course.expertName}</td>
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


        <!-- =====================================================
             RECENTLY REGISTERED USERS
             ===================================================== -->

        <div class="card card-lms mt-3">
            <div class="card-body">
                <div class="d-flex justify-content-between align-items-center mb-3">
                    <h6 class="mb-0">Người dùng mới đăng ký</h6>
                    <a href="${pageContext.request.contextPath}/admin/users" class="small">Xem tất cả</a>
                </div>

                <c:choose>
                    <c:when test="${empty dashboard.recentUsers}">
                        <p class="text-muted small mb-0">Chưa có người dùng nào.</p>
                    </c:when>
                    <c:otherwise>
                        <div class="table-responsive">
                            <table class="table table-sm align-middle mb-0">
                                <thead>
                                    <tr class="text-muted small">
                                        <th>Họ tên</th>
                                        <th>Email</th>
                                        <th>Vai trò</th>
                                        <th>Trạng thái</th>
                                        <th class="text-end">Ngày tạo</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    <c:forEach var="u" items="${dashboard.recentUsers}">
                                        <tr>
                                            <td>
                                                <a href="${pageContext.request.contextPath}/admin/users/detail?id=${u.id}">
                                                        ${u.fullName}
                                                </a>
                                            </td>
                                            <td class="text-muted small">${u.email}</td>
                                            <td><span class="badge bg-light text-dark border">${u.role}</span></td>
                                            <td>
                                                <c:choose>
                                                    <c:when test="${u.active}">
                                                        <span class="badge bg-success">Hoạt động</span>
                                                    </c:when>
                                                    <c:otherwise>
                                                        <span class="badge bg-secondary">Ngừng hoạt động</span>
                                                    </c:otherwise>
                                                </c:choose>
                                            </td>
                                            <td class="text-end small text-muted">
                                                <fmt:formatDate value="${u.createdAt}" pattern="dd/MM/yyyy" />
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

    </main>

</div>

<jsp:include page="/WEB-INF/views/common/footer.jsp" />
