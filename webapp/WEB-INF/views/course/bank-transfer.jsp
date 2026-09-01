<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />

<jsp:include page="/WEB-INF/views/common/header.jsp" />

<div class="container py-5">
    <div class="row justify-content-center">
        <div class="col-md-8 col-lg-6">
            <div class="card shadow-sm border-0 rounded-4 overflow-hidden">
                <div class="card-header bg-primary text-white text-center py-4 border-0">
                    <h4 class="mb-0 fw-bold"><i class="bi bi-qr-code-scan me-2"></i>Thanh toán chuyển khoản</h4>
                    <p class="mb-0 mt-2 text-white-50 small">Vui lòng quét mã QR hoặc chuyển khoản theo thông tin bên dưới</p>
                </div>
                
                <div class="card-body p-4 p-md-5">
                    <div class="text-center mb-4">
                        <div class="p-3 bg-white rounded-3 shadow-sm d-inline-block border">
                            <img src="${qrUrl}" alt="Mã QR" class="img-fluid" style="max-width: 250px; width: 100%;" />
                        </div>
                    </div>

                    <div class="bg-light p-4 rounded-3 mb-4 border">
                        <div class="row mb-2">
                            <div class="col-sm-5 text-muted fw-semibold">Ngân hàng:</div>
                            <div class="col-sm-7 fw-bold text-dark">${bankName}</div>
                        </div>
                        <div class="row mb-2">
                            <div class="col-sm-5 text-muted fw-semibold">Số tài khoản:</div>
                            <div class="col-sm-7 fw-bold text-primary d-flex align-items-center">
                                ${accountNumber}
                            </div>
                        </div>
                        <div class="row mb-2">
                            <div class="col-sm-5 text-muted fw-semibold">Số tiền:</div>
                            <div class="col-sm-7 fw-bold text-danger fs-5">
                                <fmt:formatNumber value="${amount}" type="number" maxFractionDigits="0"/> VNĐ
                            </div>
                        </div>
                        <div class="row">
                            <div class="col-sm-5 text-muted fw-semibold">Nội dung chuyển khoản:</div>
                            <div class="col-sm-7 fw-bold text-dark">
                                <span class="bg-warning-subtle text-warning-emphasis px-2 py-1 rounded border border-warning-subtle">${description}</span>
                            </div>
                        </div>
                    </div>

                    <div class="alert alert-info border-info-subtle d-flex align-items-center mb-0">
                        <div class="spinner-border spinner-border-sm text-info me-3 flex-shrink-0" role="status">
                            <span class="visually-hidden">Loading...</span>
                        </div>
                        <div id="status-text" class="fw-medium text-info-emphasis mb-0">
                            Hệ thống đang tự động kiểm tra giao dịch... Vui lòng không đóng trang này.
                        </div>
                    </div>
                </div>
                <div class="card-footer bg-white border-top py-3 d-flex justify-content-between align-items-center">
                    <a href="${ctx}/checkout?id=${courseId}" class="btn btn-outline-danger px-4 rounded-pill">
                        <i class="bi bi-x-circle me-1"></i>Hủy thanh toán
                    </a>
                    <div class="fw-bold text-secondary">
                        <i class="bi bi-clock me-1"></i>Thời gian còn lại: <span id="countdown" class="text-danger">01:30</span>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>

<jsp:include page="/WEB-INF/views/common/footer.jsp" />

<script>
    const paymentId = '${paymentId}';
    const contextPath = '${pageContext.request.contextPath}';
    const statusTextEl = document.getElementById('status-text');
    let pollingInterval;
    let countdownInterval;
    
    // Countdown logic: 1 minute 30 seconds = 90 seconds
    let timeLeft = 90;
    const countdownEl = document.getElementById('countdown');

    function updateCountdown() {
        if (timeLeft <= 0) {
            clearInterval(countdownInterval);
            clearInterval(pollingInterval);
            statusTextEl.parentElement.className = 'alert alert-danger border-danger-subtle d-flex align-items-center mb-0';
            statusTextEl.innerHTML = '<i class="bi bi-x-circle-fill text-danger fs-4 me-3"></i><span class="fw-bold text-danger-emphasis">Hết thời gian thanh toán! Đang chuyển hướng...</span>';
            setTimeout(function() {
                window.location.href = contextPath + '/payment-status?status=FAILED';
            }, 2000);
            return;
        }

        const m = Math.floor(timeLeft / 60);
        const s = timeLeft % 60;
        countdownEl.innerText = (m < 10 ? '0' : '') + m + ':' + (s < 10 ? '0' : '') + s;
        timeLeft--;
    }

    function checkPaymentStatus() {
        fetch(contextPath + '/sepay?action=check&paymentId=' + paymentId)
            .then(response => response.json())
            .then(data => {
                if (data.status === 'SUCCESS') {
                    clearInterval(pollingInterval);
                    clearInterval(countdownInterval);
                    statusTextEl.parentElement.className = 'alert alert-success border-success-subtle d-flex align-items-center mb-0';
                    statusTextEl.innerHTML = '<i class="bi bi-check-circle-fill text-success fs-4 me-3"></i><span class="fw-bold text-success-emphasis">Thanh toán thành công! Đang chuyển hướng...</span>';
                    
                    setTimeout(function() {
                        window.location.href = contextPath + '/payment-status?status=SUCCESS';
                    }, 2000);
                } else if (data.status === 'FAILED') {
                    clearInterval(pollingInterval);
                    clearInterval(countdownInterval);
                    statusTextEl.parentElement.className = 'alert alert-danger border-danger-subtle d-flex align-items-center mb-0';
                    statusTextEl.innerHTML = '<i class="bi bi-x-circle-fill text-danger fs-4 me-3"></i><span class="fw-bold text-danger-emphasis">Giao dịch thất bại hoặc đã bị hủy! Đang chuyển hướng...</span>';
                    
                    setTimeout(function() {
                        window.location.href = contextPath + '/payment-status?status=FAILED';
                    }, 2000);
                }
            })
            .catch(error => console.error('Lỗi khi kiểm tra trạng thái:', error));
    }

    updateCountdown();
    countdownInterval = setInterval(updateCountdown, 1000);
    pollingInterval = setInterval(checkPaymentStatus, 5000);
</script>
