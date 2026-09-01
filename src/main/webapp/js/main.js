document.addEventListener('DOMContentLoaded', function () {
    // Auto-dismiss success/error alerts after 5s
    document.querySelectorAll('.alert-auto-dismiss').forEach(function (el) {
        setTimeout(function () {
            var alert = bootstrap.Alert.getOrCreateInstance(el);
            alert.close();
        }, 5000);
    });

    // Confirm before any destructive form submit (delete buttons)
    document.querySelectorAll('form.confirm-delete').forEach(function (form) {
        form.addEventListener('submit', function (e) {
            var msg = form.dataset.confirmMessage || 'Bạn có chắc chắn muốn xóa mục này?';
            if (!confirm(msg)) {
                e.preventDefault();
            }
        });
    });
});
