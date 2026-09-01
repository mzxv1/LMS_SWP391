<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<c:set var="currentUser" value="${sessionScope.currentUser}" />
<c:set var="uri" value="${pageContext.request.requestURI}" />

<aside class="sidebar bg-dark text-white p-3">
    <ul class="nav nav-pills flex-column gap-1">
        <li class="nav-item">
            <a class="nav-link ${fn:contains(uri, '/profile') ? 'active' : 'text-white'}"
               href="${ctx}/profile">
                <i class="bi bi-person-badge"></i> Hồ sơ của tôi
            </a>
        </li>

        <c:if test="${currentUser.role == 'ADMIN'}">
            <li class="mt-2"><small class="text-uppercase text-white-50">Quản trị</small></li>
            <li class="nav-item">
                <a class="nav-link ${fn:endsWith(uri, '/admin/dashboard') ? 'active' : 'text-white'}"
                   href="${ctx}/admin/dashboard">
                    <i class="bi bi-speedometer2"></i> Dashboard
                </a>
            </li>
            <li class="nav-item">
                <a class="nav-link ${fn:contains(uri, '/admin/users') ? 'active' : 'text-white'}"
                   href="${ctx}/admin/users">
                    <i class="bi bi-people"></i> Quản lý người dùng
                </a>
            </li>
            <li class="nav-item">
                <a class="nav-link ${fn:contains(uri, '/admin/courses') ? 'active' : 'text-white'}"
                   href="${ctx}/admin/courses">
                    <i class="bi bi-journal-bookmark"></i> Quản lý khóa học
                </a>
            </li>
            <li class="nav-item">
                <a class="nav-link ${fn:contains(uri, '/admin/settings') ? 'active' : 'text-white'}"
                   href="${ctx}/admin/settings">
                    <i class="bi bi-sliders"></i> Quản lý Cấu hình
                </a>
            <li class="nav-item">
                <a class="nav-link ${fn:contains(uri, '/admin/registrations') ? 'active' : 'text-white'}"
                   href="${ctx}/admin/registrations">
                    <i class="bi bi-card-list"></i> Quản lý đăng ký
                </a>
            </li>
        </c:if>

        <c:if test="${currentUser.role == 'EXPERT'}">
            <li class="mt-2"><small class="text-uppercase text-white-50">Chuyên gia</small></li>
            <li class="nav-item">
                <a class="nav-link ${fn:endsWith(uri, '/expert/dashboard') ? 'active' : 'text-white'}"
                   href="${ctx}/expert/dashboard">
                    <i class="bi bi-speedometer2"></i> Dashboard
                </a>
            </li>
            <li class="nav-item">
                <a class="nav-link ${fn:contains(uri, '/expert/courses') ? 'active' : 'text-white'}"
                   href="${ctx}/expert/courses">
                    <i class="bi bi-journal-bookmark"></i> Quản lý khóa học
                </a>
            </li>
            <li class="nav-item">
                <a class="nav-link ${fn:contains(uri, '/expert/questions') ? 'active' : 'text-white'}"
                   href="${ctx}/expert/questions">
                    <i class="bi bi-question-circle"></i> Quản lý học liệu & đề thi
                </a>
            </li>
        </c:if>

        <c:if test="${currentUser.role == 'STUDENT'}">
            <li class="mt-2"><small class="text-uppercase text-white-50">Học viên</small></li>
            <li class="nav-item">
                <a class="nav-link ${fn:endsWith(uri, '/trainee/dashboard') ? 'active' : 'text-white'}"
                   href="${ctx}/trainee/dashboard">
                    <i class="bi bi-speedometer2"></i> Dashboard
                </a>
            </li>
            <li class="nav-item">
                <a class="nav-link ${fn:contains(uri, '/enrollment-history') ? 'active' : 'text-white'}"
                   href="${ctx}/enrollment-history">
                    <i class="bi bi-journal-bookmark"></i> Lịch sử đăng ký
                </a>
            </li>
        </c:if>
    </ul>
</aside>
