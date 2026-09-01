<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>

<footer class="text-center text-muted py-3 border-top mt-auto small">
    &copy; <%= java.time.Year.now() %> LMS - Hệ thống quản lý học tập. All rights reserved.
</footer>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
<script src="<%= request.getContextPath() %>/js/main.js"></script>
</body>
</html>
