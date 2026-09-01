<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />

<jsp:include page="/WEB-INF/views/common/header.jsp" />

<div class="container py-5">
    <h2 class="fw-bold mb-4">Thanh toán khóa học</h2>

    <form action="${ctx}/checkout/process" method="POST" id="checkoutForm">
        <input type="hidden" name="courseId" value="${course.id}" />
        <div class="row g-4">
            <!-- Cột trái: Thông tin người dùng & Phương thức thanh toán -->
            <div class="col-lg-8">
                <!-- Thông báo lỗi nếu có -->
                <c:if test="${not empty param.error}">
                    <div class="alert alert-danger">
                        <c:choose>
                            <c:when test="${param.error == 'InvalidMethod'}">Phương thức thanh toán không hợp lệ.</c:when>
                            <c:otherwise>${param.error}</c:otherwise>
                        </c:choose>
                    </div>
                </c:if>

                <!-- Thông tin người dùng -->
                <div class="card shadow-sm border-0 mb-4">
                    <div class="card-body p-4">
                        <h5 class="fw-bold mb-3">Thông tin cá nhân</h5>
                        <div class="mb-3">
                            <label class="form-label fw-semibold">Email người học</label>
                            <input type="email" class="form-control" name="email" value="${sessionScope.currentUser.email}" required>
                            <div class="form-text">Nhập email của người sẽ tham gia khóa học (phải là email đã đăng ký trên hệ thống). Mặc định là email của bạn.</div>
                        </div>
                    </div>
                </div>

                <!-- Phương thức thanh toán -->
                <div class="card shadow-sm border-0">
                    <div class="card-body p-4">
                        <h5 class="fw-bold mb-3">Chọn phương thức thanh toán</h5>

                        <!-- VNPay -->
                        <div class="form-check border rounded p-3 mb-3 d-flex align-items-center">
                            <input class="form-check-input ms-2 me-3" type="radio" name="paymentMethod" id="vnpay" value="VNPAY" checked>
                            <label class="form-check-label w-100 fw-semibold" for="vnpay">
                                Thanh toán qua VNPay (Thẻ ATM, Visa, Master)
                            </label>
                        </div>

                        <!-- SePay (Chuyển khoản) -->
                        <div class="form-check border rounded p-3 mb-3 d-flex align-items-center">
                            <input class="form-check-input ms-2 me-3" type="radio" name="paymentMethod" id="sepay" value="SEPAY">
                            <label class="form-check-label w-100 fw-semibold" for="sepay">
                                Chuyển khoản ngân hàng (Xác nhận tự động)
                            </label>
                        </div>
                    </div>
                </div>
            </div>

            <!-- Cột phải: Tóm tắt đơn hàng & Nút thanh toán -->
            <div class="col-lg-4">
                <div class="card shadow-sm border-0 position-sticky" style="top: 20px;">
                    <div class="card-body p-4">
                        <h5 class="fw-bold mb-3">Tóm tắt đơn hàng</h5>
                        <div class="d-flex align-items-center mb-3">
                            <img src="${not empty course.thumbnail ? course.thumbnail : ctx += '/assets/images/default-course.png'}" alt="Thumbnail" class="img-fluid rounded me-3" style="width: 80px; height: 60px; object-fit: cover;" onerror="this.src='https://placehold.co/80x60/EEE/31343C?text=Course'">
                            <div>
                                <div class="fw-semibold">${course.title}</div>
                                <div class="text-muted small">Giảng viên: ${course.expertName}</div>
                            </div>
                        </div>
                        <hr>
                        <div class="d-flex justify-content-between mb-2">
                            <span class="text-muted">Học phí</span>
                            <span class="fw-semibold">${course.price} ₫</span>
                        </div>
                        <div class="d-flex justify-content-between mb-3 fs-5 fw-bold">
                            <span>Tổng cộng</span>
                            <span class="text-primary">${course.price} ₫</span>
                        </div>
                        <button type="submit" class="btn btn-primary w-100 btn-lg mt-3">Tiến hành thanh toán</button>
                    </div>
                </div>
            </div>
        </div>
    </form>
</div>

<jsp:include page="/WEB-INF/views/common/footer.jsp" />
