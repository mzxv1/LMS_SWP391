<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<c:set var="pageTitle" value="Khóa học" scope="request" />

<jsp:include page="/WEB-INF/views/common/header.jsp" />

<main class="flex-grow-1">

    <section class="bg-primary text-white py-5">
        <div class="container">

            <h1 class="fw-bold mb-2">
                Khóa học
            </h1>

            <p class="mb-0 opacity-75">
                Khám phá các khóa học đang được mở trên hệ thống LMS.
            </p>

        </div>
    </section>


    <section class="container py-4">

        <form method="get"
              action="${pageContext.request.contextPath}/courses">

            <div class="row g-3 align-items-end">

                <div class="col-lg-5">

                    <label for="keyword"
                           class="form-label fw-semibold">
                        Tìm kiếm
                    </label>

                    <div class="input-group">

                        <span class="input-group-text">
                            <i class="bi bi-search"></i>
                        </span>

                        <input type="text"
                               class="form-control"
                               id="keyword"
                               name="keyword"
                               value="${fn:escapeXml(keyword)}"
                               placeholder="Tìm kiếm khóa học..." />

                    </div>

                </div>


                <div class="col-md-4 col-lg-2">

                    <label for="category"
                           class="form-label fw-semibold">
                        Danh mục
                    </label>

                    <select class="form-select"
                            id="category"
                            name="category">

                        <option value="">
                            Tất cả danh mục
                        </option>

                        <c:forEach var="cat"
                                   items="${categories}">

                            <c:choose>
                                <c:when test="${selectedCategory eq cat}">
                                    <option value="${fn:escapeXml(cat)}" selected>
                                            ${fn:escapeXml(cat)}
                                    </option>
                                </c:when>

                                <c:otherwise>
                                    <option value="${fn:escapeXml(cat)}">
                                            ${fn:escapeXml(cat)}
                                    </option>
                                </c:otherwise>
                            </c:choose>

                        </c:forEach>

                    </select>

                </div>


                <div class="col-md-4 col-lg-2">

                    <label for="sort"
                           class="form-label fw-semibold">
                        Sắp xếp
                    </label>

                    <select class="form-select"
                            id="sort"
                            name="sort">

                        <c:choose>
                            <c:when test="${selectedSort eq 'oldest'}">
                                <option value="newest">
                                    Mới nhất
                                </option>
                                <option value="oldest" selected>
                                    Cũ nhất
                                </option>
                                <option value="price_asc">
                                    Giá thấp → cao
                                </option>
                                <option value="price_desc">
                                    Giá cao → thấp
                                </option>
                            </c:when>

                            <c:when test="${selectedSort eq 'price_asc'}">
                                <option value="newest">
                                    Mới nhất
                                </option>
                                <option value="oldest">
                                    Cũ nhất
                                </option>
                                <option value="price_asc" selected>
                                    Giá thấp → cao
                                </option>
                                <option value="price_desc">
                                    Giá cao → thấp
                                </option>
                            </c:when>

                            <c:when test="${selectedSort eq 'price_desc'}">
                                <option value="newest">
                                    Mới nhất
                                </option>
                                <option value="oldest">
                                    Cũ nhất
                                </option>
                                <option value="price_asc">
                                    Giá thấp → cao
                                </option>
                                <option value="price_desc" selected>
                                    Giá cao → thấp
                                </option>
                            </c:when>

                            <c:otherwise>
                                <option value="newest" selected>
                                    Mới nhất
                                </option>
                                <option value="oldest">
                                    Cũ nhất
                                </option>
                                <option value="price_asc">
                                    Giá thấp → cao
                                </option>
                                <option value="price_desc">
                                    Giá cao → thấp
                                </option>
                            </c:otherwise>
                        </c:choose>

                    </select>

                </div>


                <div class="col-md-2 col-lg-1">

                    <button type="submit"
                            class="btn btn-primary w-100"
                            title="Tìm kiếm">

                        <i class="bi bi-search"></i>

                    </button>

                </div>


                <div class="col-md-2 col-lg-2">

                    <a href="${pageContext.request.contextPath}/courses"
                       class="btn btn-outline-secondary w-100">

                        <i class="bi bi-x-circle"></i>
                        Xóa bộ lọc

                    </a>

                </div>

            </div>

        </form>

    </section>


    <section class="container pb-5">

        <div class="d-flex justify-content-between align-items-center mb-4">

            <div>

                <h2 class="h4 fw-bold mb-1">
                    Danh sách khóa học
                </h2>

                <c:choose>
                    <c:when test="${coursePage.totalElements > 0}">
                        <p class="text-muted mb-0">
                            Hiển thị
                                ${fn:length(coursePage.content)}
                            khóa học trong tổng số
                                ${coursePage.totalElements}
                            khóa học
                        </p>
                    </c:when>

                    <c:otherwise>
                        <p class="text-muted mb-0">
                            Không tìm thấy khóa học phù hợp.
                        </p>
                    </c:otherwise>
                </c:choose>

            </div>


            <c:if test="${coursePage.totalPages > 0}">
                <div class="text-muted small">
                    Trang
                    <strong>${coursePage.page}</strong>
                    /
                        ${coursePage.totalPages}
                </div>
            </c:if>

        </div>


        <c:choose>
            <c:when test="${not empty coursePage.content}">

                <div class="row g-4">

                    <c:forEach var="course"
                               items="${coursePage.content}">

                        <div class="col-md-6 col-lg-4">

                            <div class="card h-100 shadow-sm border-0">

                                <div class="card-body d-flex flex-column p-4">

                                    <div class="mb-3">

                                        <span class="badge bg-primary-subtle text-primary">

                                            <i class="bi bi-bookmark"></i>

                                            ${fn:escapeXml(course.category)}

                                        </span>

                                    </div>


                                    <h3 class="h5 fw-bold mb-2">
                                            ${fn:escapeXml(course.title)}
                                    </h3>


                                    <p class="text-muted mb-4">

                                        <c:choose>
                                            <c:when test="${empty course.description}">
                                                Chưa có mô tả khóa học.
                                            </c:when>

                                            <c:when test="${fn:length(course.description) > 140}">
                                                ${fn:escapeXml(fn:substring(course.description, 0, 140))}...
                                            </c:when>

                                            <c:otherwise>
                                                ${fn:escapeXml(course.description)}
                                            </c:otherwise>
                                        </c:choose>

                                    </p>


                                    <div class="mb-4">

                                        <div class="d-flex align-items-center mb-2">

                                            <i class="bi bi-person-circle me-2 text-muted"></i>

                                            <span class="small">
                                                    ${fn:escapeXml(course.expertName)}
                                            </span>

                                        </div>


                                        <div class="d-flex align-items-center mb-2">

                                            <i class="bi bi-clock me-2 text-muted"></i>

                                            <span class="small">
                                                ${course.durationHours} giờ học
                                            </span>

                                        </div>


                                        <div class="d-flex align-items-center">

                                            <i class="bi bi-calendar3 me-2 text-muted"></i>

                                            <span class="small">
                                                Cập nhật:
                                                <fmt:formatDate
                                                        value="${course.updatedAt}"
                                                        pattern="dd/MM/yyyy" />
                                            </span>

                                        </div>

                                    </div>


                                    <div class="mt-auto">

                                        <div class="border-top pt-3 d-flex justify-content-between align-items-end">

                                            <div>

                                                <div class="small text-muted mb-1">
                                                    Học phí
                                                </div>

                                                <c:choose>
                                                    <c:when test="${course.price == null or course.price == 0}">
                                                        <span class="fw-bold text-success">
                                                            Miễn phí
                                                        </span>
                                                    </c:when>

                                                    <c:otherwise>
                                                        <span class="fw-bold text-primary fs-5">

                                                            <fmt:formatNumber
                                                                    value="${course.price}"
                                                                    type="number"
                                                                    groupingUsed="true"
                                                                    minFractionDigits="0"
                                                                    maxFractionDigits="0" />

                                                            ₫

                                                        </span>
                                                    </c:otherwise>
                                                </c:choose>

                                            </div>


                                            <a href="${pageContext.request.contextPath}/courses/detail?id=${course.id}"
                                               class="btn btn-outline-primary btn-sm">

                                                Xem chi tiết
                                                <i class="bi bi-arrow-right"></i>

                                            </a>

                                        </div>

                                    </div>

                                </div>

                            </div>

                        </div>

                    </c:forEach>

                </div>


                <c:if test="${coursePage.totalPages > 1}">

                    <nav class="mt-5"
                         aria-label="Course pagination">

                        <ul class="pagination justify-content-center">

                            <c:choose>
                                <c:when test="${coursePage.hasPrevious()}">

                                    <li class="page-item">

                                        <a class="page-link"
                                           href="${pageContext.request.contextPath}/courses?keyword=${fn:escapeXml(keyword)}&category=${fn:escapeXml(selectedCategory)}&sort=${fn:escapeXml(selectedSort)}&page=${coursePage.page - 1}&size=${coursePage.size}">

                                            <i class="bi bi-chevron-left"></i>

                                        </a>

                                    </li>

                                </c:when>

                                <c:otherwise>

                                    <li class="page-item disabled">

                                        <span class="page-link">

                                            <i class="bi bi-chevron-left"></i>

                                        </span>

                                    </li>

                                </c:otherwise>
                            </c:choose>


                            <c:forEach begin="1"
                                       end="${coursePage.totalPages}"
                                       var="pageNumber">

                                <c:choose>

                                    <c:when test="${pageNumber == coursePage.page}">

                                        <li class="page-item active">

                                            <span class="page-link">
                                                    ${pageNumber}
                                            </span>

                                        </li>

                                    </c:when>

                                    <c:otherwise>

                                        <li class="page-item">

                                            <a class="page-link"
                                               href="${pageContext.request.contextPath}/courses?keyword=${fn:escapeXml(keyword)}&category=${fn:escapeXml(selectedCategory)}&sort=${fn:escapeXml(selectedSort)}&page=${pageNumber}&size=${coursePage.size}">

                                                    ${pageNumber}

                                            </a>

                                        </li>

                                    </c:otherwise>

                                </c:choose>

                            </c:forEach>


                            <c:choose>

                                <c:when test="${coursePage.hasNext()}">

                                    <li class="page-item">

                                        <a class="page-link"
                                           href="${pageContext.request.contextPath}/courses?keyword=${fn:escapeXml(keyword)}&category=${fn:escapeXml(selectedCategory)}&sort=${fn:escapeXml(selectedSort)}&page=${coursePage.page + 1}&size=${coursePage.size}">

                                            <i class="bi bi-chevron-right"></i>

                                        </a>

                                    </li>

                                </c:when>

                                <c:otherwise>

                                    <li class="page-item disabled">

                                        <span class="page-link">

                                            <i class="bi bi-chevron-right"></i>

                                        </span>

                                    </li>

                                </c:otherwise>

                            </c:choose>

                        </ul>

                    </nav>

                </c:if>

            </c:when>


            <c:otherwise>

                <div class="text-center py-5">

                    <div class="mb-3">

                        <i class="bi bi-search"
                           style="font-size: 4rem;"></i>

                    </div>

                    <h3 class="h5 fw-bold">
                        Không tìm thấy khóa học
                    </h3>

                    <p class="text-muted mb-4">
                        Thử thay đổi từ khóa tìm kiếm hoặc bộ lọc.
                    </p>

                    <a href="${pageContext.request.contextPath}/courses"
                       class="btn btn-outline-primary">

                        <i class="bi bi-arrow-counterclockwise"></i>
                        Xóa bộ lọc

                    </a>

                </div>

            </c:otherwise>

        </c:choose>

    </section>

</main>

<jsp:include page="/WEB-INF/views/common/footer.jsp" />