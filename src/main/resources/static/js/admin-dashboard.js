// ========== Small UI Helpers ==========

function getToastElement() {
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
    const toast = getToastElement();
    toast.textContent = message;
    toast.className = 'message ' + (type || 'success');
    toast.style.animation = 'fadeInUp 0.35s ease-out';

    setTimeout(() => {
        toast.style.animation = '';
    }, 400);

    // auto hide after some time
    clearTimeout(toast._hideTimer);
    toast._hideTimer = setTimeout(() => {
        toast.className = 'message';
        toast.textContent = '';
    }, 4000);
}

function animateOrderCard(card) {
    if (!card) return;
    card.style.animation = 'fadeInUp 0.4s ease-out';
    setTimeout(() => {
        card.style.animation = '';
    }, 450);
}

function setCardLoading(card, isLoading) {
    if (!card) return;
    if (isLoading) {
        card.style.opacity = '0.7';
        card.style.pointerEvents = 'none';
        card._prevBoxShadow = card.style.boxShadow;
        card.style.boxShadow = '0 0 0 1px rgba(148,163,184,0.5)';
    } else {
        card.style.opacity = '';
        card.style.pointerEvents = '';
        card.style.boxShadow = card._prevBoxShadow || '';
    }
}

// ========== Update Order Status ==========

function updateOrderStatus(selectElement) {
    const orderId = selectElement.id.replace('statusSelect_', '');
    const status = selectElement.value;

    const card = selectElement.closest('.order-card');
    setCardLoading(card, true);

    fetch('/admin/orders/update-status?orderId=' + encodeURIComponent(orderId) +
          '&status=' + encodeURIComponent(status), {
        method: 'POST',
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded',
        }
    })
    .then(response => response.json())
    .then(data => {
        setCardLoading(card, false);

        if (data.success) {
            // Update visible status text in header if needed
            const statusSpan = card.querySelector('.order-status');
            if (statusSpan) {
                statusSpan.textContent = status.replace(/_/g, ' ');
            }

            showAdminToast('Order status updated successfully', 'success');
            animateOrderCard(card);
        } else {
            showAdminToast(data.message || 'Error updating order status', 'error');
        }
    })
    .catch(error => {
        setCardLoading(card, false);
        showAdminToast('Error updating order status', 'error');
        console.error('Error:', error);
    });
}

// ========== Set Estimated Time ==========

function setEstimatedTime(orderId) {
    const timeInput = document.getElementById('timeInput_' + orderId);
    const card = timeInput ? timeInput.closest('.order-card') : null;

    const estimatedTimeMinutes = parseInt(timeInput && timeInput.value, 10);

    if (!estimatedTimeMinutes || estimatedTimeMinutes < 1) {
        showAdminToast('Please enter a valid estimated time (in minutes)', 'error');
        return;
    }

    setCardLoading(card, true);

    fetch('/admin/orders/set-time?orderId=' + encodeURIComponent(orderId) +
          '&estimatedTimeMinutes=' + encodeURIComponent(estimatedTimeMinutes), {
        method: 'POST',
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded',
        }
    })
    .then(response => response.json())
    .then(data => {
        setCardLoading(card, false);

        if (data.success) {
            // If there is a text node somewhere displaying the time, you can adjust it here.
            // For example, if your template shows:
            // <span th:text="${order.estimatedTimeMinutes} + ' minutes'">
            // you could refresh that via AJAX in future; for now we just give UX feedback.
            showAdminToast('Estimated time set to ' + estimatedTimeMinutes + ' minutes', 'success');
            animateOrderCard(card);
        } else {
            showAdminToast(data.message || 'Error setting estimated time', 'error');
        }
    })
    .catch(error => {
        setCardLoading(card, false);
        showAdminToast('Error setting estimated time', 'error');
        console.error('Error:', error);
    });
}
