<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<c:set var="ctx" value="${pageContext.request.contextPath}" />
<c:set var="currentUser" value="${sessionScope.currentUser}" />

<!DOCTYPE html>
<html lang="vi">

<head>
    <meta charset="UTF-8">

    <meta name="viewport"
          content="width=device-width, initial-scale=1.0">

    <title>
        <c:if test="${not empty pageTitle}">
            ${pageTitle} -
        </c:if>
        LMS - Hệ thống quản lý học tập
    </title>

    <link
            href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css"
            rel="stylesheet">

    <link
            href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.css"
            rel="stylesheet">

    <link
            href="${ctx}/css/style.css?v=2"
            rel="stylesheet">
</head>

<body>

<nav class="navbar navbar-expand-lg navbar-dark bg-primary shadow-sm">

    <div class="container-fluid">

        <a class="navbar-brand fw-bold"
           href="${ctx}/home">

            <i class="bi bi-mortarboard-fill"></i>
            LMS

        </a>

        <button
                class="navbar-toggler"
                type="button"
                data-bs-toggle="collapse"
                data-bs-target="#topNav"
                aria-controls="topNav"
                aria-expanded="false"
                aria-label="Toggle navigation">

            <span class="navbar-toggler-icon"></span>

        </button>

        <div class="collapse navbar-collapse"
             id="topNav">

            <ul class="navbar-nav me-auto">

                <!-- Trang chủ -->

                <li class="nav-item">

                    <a class="nav-link"
                       href="${ctx}/home">

                        <i class="bi bi-house-door"></i>
                        Trang chủ

                    </a>

                </li>


                <!-- Public Courses -->

                <li class="nav-item">

                    <a class="nav-link"
                       href="${ctx}/courses">

                        <i class="bi bi-book"></i>
                        Khóa học

                    </a>

                </li>


                <!-- Student: My Courses -->

                <c:if test="${currentUser.role == 'STUDENT'}">

                    <li class="nav-item">

                        <a class="nav-link"
                           href="${ctx}/trainee/dashboard">

                            <i class="bi bi-journal-bookmark"></i>
                            Khóa học của tôi

                        </a>

                    </li>

                </c:if>


                <!-- Admin: Dashboard -->

                <c:if test="${currentUser.role == 'ADMIN'}">

                    <li class="nav-item">

                        <a class="nav-link"
                           href="${ctx}/admin/dashboard">

                            <i class="bi bi-speedometer2"></i>
                            Dashboard

                        </a>

                    </li>

                </c:if>


                <!-- Expert: Dashboard -->

                <c:if test="${currentUser.role == 'EXPERT'}">

                    <li class="nav-item">

                        <a class="nav-link"
                           href="${ctx}/expert/dashboard">

                            <i class="bi bi-speedometer2"></i>
                            Dashboard

                        </a>

                    </li>

                </c:if>

            </ul>

            <ul class="navbar-nav ms-auto align-items-lg-center">

                <c:choose>

                    <c:when test="${not empty currentUser}">

                        <li class="nav-item">

                            <span class="nav-link text-white-50">

                                <i class="bi bi-person-circle"></i>

                                ${currentUser.fullName}

                                <span class="badge bg-light text-primary ms-1">
                                        ${currentUser.role}
                                </span>

                            </span>

                        </li>

                        <li class="nav-item">

                            <a class="nav-link"
                               href="${ctx}/profile">

                                <i class="bi bi-person"></i>
                                Hồ sơ

                            </a>

                        </li>

                        <li class="nav-item">

                            <a class="nav-link"
                               href="${ctx}/logout">

                                <i class="bi bi-box-arrow-right"></i>
                                Đăng xuất

                            </a>

                        </li>

                    </c:when>

                    <c:otherwise>

                        <li class="nav-item">

                            <a class="nav-link"
                               href="${ctx}/login">

                                Đăng nhập

                            </a>

                        </li>

                        <li class="nav-item">

                            <a class="nav-link"
                               href="${ctx}/register">

                                Đăng ký

                            </a>

                        </li>

                    </c:otherwise>

                </c:choose>

            </ul>

        </div>

    </div>

</nav>