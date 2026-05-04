// MarketPlace - Main JavaScript

document.addEventListener('DOMContentLoaded', function () {

    // Auto-hide flash messages after 5 seconds
    const alerts = document.querySelectorAll('.alert');
    alerts.forEach(function (alert) {
        setTimeout(function () {
            alert.style.opacity = '0';
            alert.style.transform = 'translateY(-10px)';
            alert.style.transition = 'all 0.4s ease';
            setTimeout(function () { alert.remove(); }, 400);
        }, 5000);
    });

    // Confirm delete actions
    document.querySelectorAll('[data-confirm]').forEach(function (el) {
        el.addEventListener('click', function (e) {
            if (!confirm(el.dataset.confirm)) {
                e.preventDefault();
            }
        });
    });

    // Cart quantity increment/decrement
    document.querySelectorAll('.qty-btn').forEach(function (btn) {
        btn.addEventListener('click', function () {
            const input = this.parentElement.querySelector('.qty-input');
            if (!input) return;
            const min = parseInt(input.min) || 1;
            const max = parseInt(input.max) || 9999;
            let val = parseInt(input.value) || 1;
            if (this.textContent.trim() === '+') {
                if (val < max) input.value = val + 1;
            } else {
                if (val > min) input.value = val - 1;
            }
        });
    });

    // Product image preview on hover
    const productImages = document.querySelectorAll('.product-image');
    productImages.forEach(function (img) {
        img.addEventListener('error', function () {
            this.src = 'https://via.placeholder.com/400x300/e5e7eb/9ca3af?text=Sin+Imagen';
        });
    });

    // Search form keyboard shortcut (/)
    document.addEventListener('keydown', function (e) {
        if (e.key === '/' && document.activeElement.tagName !== 'INPUT' &&
            document.activeElement.tagName !== 'TEXTAREA') {
            e.preventDefault();
            const searchInput = document.querySelector('.search-input');
            if (searchInput) {
                searchInput.focus();
            }
        }
    });

    // Smooth scroll for anchor links
    document.querySelectorAll('a[href^="#"]').forEach(function (anchor) {
        anchor.addEventListener('click', function (e) {
            const target = document.querySelector(this.getAttribute('href'));
            if (target) {
                e.preventDefault();
                target.scrollIntoView({ behavior: 'smooth' });
            }
        });
    });
});

// Quantity helper functions for product detail page
function decrementQty() {
    const input = document.getElementById('quantity');
    if (input && parseInt(input.value) > 1) {
        input.value = parseInt(input.value) - 1;
    }
}

function incrementQty() {
    const input = document.getElementById('quantity');
    if (input) {
        const max = parseInt(input.getAttribute('max')) || 9999;
        if (parseInt(input.value) < max) {
            input.value = parseInt(input.value) + 1;
        }
    }
}
