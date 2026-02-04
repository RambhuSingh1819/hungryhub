// Admin Orders JavaScript (enhanced with animations, toasts & smooth UI)

/* ========== SMALL UI HELPERS (toast + highlight + loading) ========== */

function getAdminToastElement() {
    let toast = document.getElementById('adminToast');
    if (!toast) {
        toast = document.createElement('div');
        toast.id = 'adminToast';
        toast.style.position = 'fixed';
        toast.style.top = '16px';
        toast.style.right = '16px';
        toast.style.zIndex = '2000';
        toast.style.minWidth = '220px';
        document.body.appendChild(toast);
    }
    return toast;
}

function showAdminToast(message, type) {
    const toast = getAdminToastElement();
    toast.textContent = message;
    toast.className = 'message ' + (type || 'success'); // uses .message, .success, .error from CSS
    toast.style.animation = 'fadeInUp 0.35s ease-out';

    setTimeout(() => {
        toast.style.animation = '';
    }, 400);

    clearTimeout(toast._hideTimer);
    toast._hideTimer = setTimeout(() => {
        toast.className = 'message';
        toast.textContent = '';
    }, 4000);
}

function highlightOrderCard(card) {
    if (!card) return;
    card.style.animation = 'fadeInUp 0.4s ease-out';
    card.style.boxShadow = '0 12px 26px rgba(34,197,94,0.45)';

    setTimeout(() => {
        card.style.animation = '';
        card.style.boxShadow = ''; // resets to CSS default
    }, 450);
}

function addCardLoading(card) {
    if (!card) return;
    card.classList.add('order-card-updating');
    card.style.opacity = '0.7';
    card.style.filter = 'grayscale(0.1)';
}

function removeCardLoading(card) {
    if (!card) return;
    card.classList.remove('order-card-updating');
    card.style.opacity = '';
    card.style.filter = '';
}

/* ========== STATUS UPDATE ========== */

function updateOrderStatus(selectElement) {
    const orderId = selectElement.id.replace('statusSelect_', '');
    const status = selectElement.value;

    const card = selectElement.closest('.order-card');
    const statusBadge = card ? card.querySelector('.order-status') : null;

    // Loading state
    const previousDisabled = selectElement.disabled;
    selectElement.disabled = true;
    selectElement.style.opacity = '0.7';

    addCardLoading(card);

    fetch(
        '/admin/orders/update-status?orderId=' +
            encodeURIComponent(orderId) +
            '&status=' +
            encodeURIComponent(status),
        {
            method: 'POST',
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded',
            },
        }
    )
        .then((response) => response.json())
        .then((data) => {
            selectElement.disabled = previousDisabled;
            selectElement.style.opacity = '';
            removeCardLoading(card);

            if (data.success) {
                const prettyStatus = status.replace(/_/g, ' ');
                showAdminToast('Order status updated to ' + prettyStatus, 'success');

                // Update badge text & animate it
                if (statusBadge) {
                    statusBadge.textContent = prettyStatus;

                    const s = status.toUpperCase();
                    if (s === 'DELIVERED') {
                        statusBadge.style.backgroundColor = '#22c55e';
                    } else if (s === 'CANCELLED') {
                        statusBadge.style.backgroundColor = '#dc2626';
                    } else if (
                        s === 'OUT_FOR_DELIVERY' ||
                        s === 'READY' ||
                        s === 'PREPARING'
                    ) {
                        statusBadge.style.backgroundColor = '#f59e0b';
                    } else {
                        statusBadge.style.backgroundColor = '#f39c12';
                    }

                    statusBadge.style.animation = 'badgePop 0.3s ease-out';
                    setTimeout(() => {
                        statusBadge.style.animation = '';
                    }, 320);
                }

                selectElement.style.transform = 'scale(1.02)';
                selectElement.style.boxShadow =
                    '0 0 0 2px rgba(34,197,94,0.25)';

                setTimeout(() => {
                    selectElement.style.transform = '';
                    selectElement.style.boxShadow = '';
                }, 250);

                highlightOrderCard(card);
            } else {
                showAdminToast(
                    data.message || 'Error updating order status',
                    'error'
                );
            }
        })
        .catch((error) => {
            selectElement.disabled = previousDisabled;
            selectElement.style.opacity = '';
            removeCardLoading(card);
            showAdminToast('Error updating order status', 'error');
            console.error('Error:', error);
        });
}

/* ========== ESTIMATED TIME UPDATE ========== */
/**
 * Supports two usages:
 *  1) From HTML inline: setEstimatedTime('ORD123')
 *  2) From JS listener: setEstimatedTime('ORD123', buttonElement)
 */
function setEstimatedTime(orderId, buttonFromClick) {
    const timeInput = document.getElementById('timeInput_' + orderId);

    if (!timeInput) {
        showAdminToast('Time input not found for order ' + orderId, 'error');
        return;
    }

    const estimatedTimeMinutes = parseInt(timeInput.value, 10);

    if (!estimatedTimeMinutes || estimatedTimeMinutes < 1) {
        showAdminToast(
            'Please enter a valid estimated time (in minutes)',
            'error'
        );
        timeInput.style.animation = 'badgePop 0.25s ease-out';
        timeInput.style.borderColor = '#dc2626';
        setTimeout(() => {
            timeInput.style.animation = '';
            timeInput.style.borderColor = '';
        }, 260);
        return;
    }

    const card = timeInput.closest('.order-card');

    // Button: prefer the one passed from click handler, otherwise find by data-order-id, and as a last fallback find by inline onclick
    let button = buttonFromClick || null;
    if (!button && card) {
        button =
            card.querySelector('.set-time-btn[data-order-id="' + orderId + '"]') ||
            card.querySelector('button[onclick*="setEstimatedTime"]');
    }

    let originalText = '';
    if (button) {
        originalText = button.textContent;
        button.disabled = true;
        button.textContent = 'Saving...';
        button.style.opacity = '0.7';
    }

    addCardLoading(card);

    fetch(
        '/admin/orders/set-time?orderId=' +
            encodeURIComponent(orderId) +
            '&estimatedTimeMinutes=' +
            encodeURIComponent(estimatedTimeMinutes),
        {
            method: 'POST',
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded',
            },
        }
    )
        .then((response) => response.json())
        .then((data) => {
            if (button) {
                button.disabled = false;
                button.textContent = originalText || 'Set Time';
                button.style.opacity = '';
            }

            removeCardLoading(card);

            if (data.success) {
                showAdminToast(
                    'Estimated time set to ' +
                        estimatedTimeMinutes +
                        ' minutes',
                    'success'
                );

                if (card) {
                    let timeRow = null;

                    const paragraphs = card.querySelectorAll('p');
                    paragraphs.forEach((p) => {
                        if (!timeRow && /Estimated Time/i.test(p.textContent)) {
                            timeRow = p;
                        }
                    });

                    if (!timeRow) {
                        const footer =
                            card.querySelector('.order-footer') || card;
                        timeRow = document.createElement('p');
                        const strong = document.createElement('strong');
                        strong.textContent = 'Estimated Time: ';
                        const span = document.createElement('span');
                        span.textContent = estimatedTimeMinutes + ' minutes';

                        timeRow.appendChild(strong);
                        timeRow.appendChild(span);
                        footer.appendChild(timeRow);
                    } else {
                        const span = timeRow.querySelector('span');
                        if (span) {
                            span.textContent =
                                estimatedTimeMinutes + ' minutes';
                        } else {
                            timeRow.textContent =
                                'Estimated Time: ' +
                                estimatedTimeMinutes +
                                ' minutes';
                        }
                    }

                    timeInput.style.boxShadow =
                        '0 0 0 2px rgba(34,197,94,0.25)';
                    timeInput.style.borderColor = '#22c55e';
                    timeInput.style.animation = 'fadeInUp 0.28s ease-out';

                    setTimeout(() => {
                        timeInput.style.boxShadow = '';
                        timeInput.style.borderColor = '';
                        timeInput.style.animation = '';
                    }, 300);

                    highlightOrderCard(card);
                }
            } else {
                showAdminToast(
                    data.message || 'Error setting estimated time',
                    'error'
                );
            }
        })
        .catch((error) => {
            if (button) {
                button.disabled = false;
                button.textContent = originalText || 'Set Time';
                button.style.opacity = '';
            }
            removeCardLoading(card);
            showAdminToast('Error setting estimated time', 'error');
            console.error('Error:', error);
        });
}

/* ========== (Optional) Attach click handlers when using data-order-id ========== */

document.addEventListener('DOMContentLoaded', function () {
    const timeButtons = document.querySelectorAll('.set-time-btn[data-order-id]');
    timeButtons.forEach(function (btn) {
        btn.addEventListener('click', function () {
            const orderId = btn.getAttribute('data-order-id');
            setEstimatedTime(orderId, btn);
        });
    });
});
