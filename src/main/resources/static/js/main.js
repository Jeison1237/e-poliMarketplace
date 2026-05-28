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

    // Dashboard enhancements
    initDashboardEnhancements();
});

/**
 * Initialize dashboard animations and interactivity
 */
function initDashboardEnhancements() {
    // Animate stat cards on scroll
    const statCards = document.querySelectorAll('.stat-card');
    if (statCards.length > 0) {
        observeElements(statCards);
    }

    // Animate dashboard sections on scroll
    const dashboardSections = document.querySelectorAll('.dashboard-section');
    if (dashboardSections.length > 0) {
        observeElements(dashboardSections);
    }

    // Animate empty state
    const emptyState = document.querySelector('.empty-state');
    if (emptyState) {
        observeElements([emptyState]);
    }

    // Add hover effect to table rows
    const tableRows = document.querySelectorAll('.products-table tbody tr');
    tableRows.forEach(function (row) {
        row.addEventListener('mouseenter', function () {
            this.style.transition = 'all 0.2s ease';
        });
    });

    // Button ripple effect
    document.querySelectorAll('.btn').forEach(function (btn) {
        btn.addEventListener('click', function (e) {
            const ripple = document.createElement('span');
            const rect = this.getBoundingClientRect();
            const size = Math.max(rect.width, rect.height);
            const x = e.clientX - rect.left - size / 2;
            const y = e.clientY - rect.top - size / 2;

            ripple.style.width = ripple.style.height = size + 'px';
            ripple.style.left = x + 'px';
            ripple.style.top = y + 'px';
            ripple.classList.add('ripple');

            this.appendChild(ripple);
            setTimeout(() => ripple.remove(), 600);
        });
    });
}

/**
 * Observe elements for intersection and add animation class
 */
function observeElements(elements) {
    const observer = new IntersectionObserver(function (entries) {
        entries.forEach(function (entry) {
            if (entry.isIntersecting) {
                entry.target.style.animation = 'none';
                entry.target.offsetHeight; // Trigger reflow
                entry.target.style.animation = '';
                observer.unobserve(entry.target);
            }
        });
    }, {
        threshold: 0.1,
        rootMargin: '0px 0px -100px 0px'
    });

    elements.forEach(function (el) {
        observer.observe(el);
    });
}

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
