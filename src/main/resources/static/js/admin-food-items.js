// Admin Food Items JavaScript
console.log("admin-food-items.js loaded");

/* ========== SMALL UI HELPERS (toast + animations) ========== */

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

// animate a card (e.g., after save)
function animateCard(card) {
    if (!card) return;
    card.style.animation = 'fadeInUp 0.4s ease-out';
    setTimeout(() => {
        card.style.animation = '';
    }, 450);
}

// soft delete animation before removing card
function fadeOutAndRemove(el) {
    if (!el) return;
    el.style.transition = 'opacity 0.25s ease, transform 0.25s ease';
    el.style.opacity = '0';
    el.style.transform = 'translateY(8px)';
    setTimeout(() => {
        if (el.parentNode) {
            el.parentNode.removeChild(el);
        }
    }, 260);
}

// modal helpers
function openModal() {
    const modal = document.getElementById('itemModal');
    if (!modal) return;

    modal.classList.remove('hidden');
    document.body.style.overflow = 'hidden'; // lock background scroll
}

function closeModal() {
    const modal = document.getElementById('itemModal');
    if (!modal) return;

    modal.classList.add('hidden');
    document.body.style.overflow = ''; // restore scroll
}

/* ========== GLOBAL FUNCTIONS (used by HTML) ========== */

window.showAddItemModal = function () {
    document.getElementById('modalTitle').textContent = 'Add Food Item';
    document.getElementById('itemForm').reset();
    document.getElementById('itemId').value = '';

    // Reset preview & AI hint
    const wrapper = document.getElementById('imagePreviewWrapper');
    const img = document.getElementById('imagePreview');
    if (wrapper && img) {
        wrapper.style.display = 'none';
        img.src = '';
    }
    const hint = document.getElementById('aiHint');
    if (hint) hint.textContent = '';

    openModal();
};

window.closeItemModal = function () {
    closeModal();
};

// Edit button now passes the button element
window.editItem = function (buttonEl) {
    const id = buttonEl.getAttribute('data-id');
    const name = buttonEl.getAttribute('data-name');
    const description = buttonEl.getAttribute('data-description');
    const price = buttonEl.getAttribute('data-price');
    const category = buttonEl.getAttribute('data-category');
    const imageUrl = buttonEl.getAttribute('data-imageurl');

    document.getElementById('modalTitle').textContent = 'Edit Food Item';
    document.getElementById('itemId').value = id;
    document.getElementById('itemName').value = name || '';
    document.getElementById('itemDescription').value = description || '';
    document.getElementById('itemPrice').value = price || '';
    document.getElementById('itemCategory').value = category || '';
    document.getElementById('itemImageUrl').value = imageUrl || '';

    // Show preview if we have image
    if (imageUrl) {
        const img = document.getElementById('imagePreview');
        const wrapper = document.getElementById('imagePreviewWrapper');
        if (img && wrapper) {
            img.src = imageUrl;
            wrapper.style.display = 'block';
        }
    }

    const hint = document.getElementById('aiHint');
    if (hint) hint.textContent = '';

    openModal();
};

window.deleteItem = function (itemId) {
    if (!confirm('Are you sure you want to delete this item?')) {
        return;
    }

    // find card for animation
	const card = document.querySelector(
	    '.admin-food-card button[data-id="' + itemId + '"]'
	)?.closest('.admin-food-card');
 // fallback

    fetch('/admin/food-items/delete?id=' + encodeURIComponent(itemId), {
        method: 'POST',
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded',
        }
    })
        .then(response => response.json())
        .then(data => {
            if (data.success) {
                showAdminToast('Item deleted successfully', 'success');
                if (card) {
                    fadeOutAndRemove(card);
                } else {
                    // as a fallback
                    location.reload();
                }
            } else {
                showAdminToast(data.message || 'Error deleting item', 'error');
            }
        })
        .catch(error => {
            showAdminToast('Error deleting item', 'error');
            console.error('Error:', error);
        });
};

// Close modal when clicking outside
document.addEventListener('click', function (e) {
    const modal = document.getElementById('itemModal');
    if (!modal || modal.classList.contains('hidden')) return;

    // click on dark overlay closes modal
    if (e.target === modal) {
        closeModal();
    }
});


// IMAGE PREVIEW
window.updateImagePreview = function () {
    const url = document.getElementById('itemImageUrl').value.trim();
    const wrapper = document.getElementById('imagePreviewWrapper');
    const img = document.getElementById('imagePreview');

    if (!wrapper || !img) return;

    if (url) {
        img.src = url;
        wrapper.style.display = 'block';
    } else {
        wrapper.style.display = 'none';
    }
};

// ✨ AI Suggest Button
window.suggestWithAI = async function () {
    console.log("suggestWithAI called");

    const name = document.getElementById("itemName").value.trim();
    const category = document.getElementById("itemCategory").value.trim();
    const descInput = document.getElementById("itemDescription");
    const imageInput = document.getElementById("itemImageUrl");
    const hint = document.getElementById("aiHint");

    if (!name) {
        showAdminToast("Please enter the item name first.", "error");
        return;
    }

    if (hint) {
        hint.textContent = "Generating description & image suggestion...";
    }

    try {
        const response = await fetch("/admin/food-items/ai-suggest", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ name, category })
        });

        console.log("AI status:", response.status);

        if (!response.ok) {
            const errorText = await response.text();
            console.error("AI error body:", errorText);
            if (hint) {
                hint.textContent = "AI request failed: " + response.status;
            }
            showAdminToast("AI request failed, check server logs.", "error");
            return;
        }

        const data = await response.json();
        console.log("AI data:", data);

        if (data.description && descInput) {
            descInput.value = data.description;
        }

        if (data.imageUrl && imageInput) {
            imageInput.value = data.imageUrl;
            window.updateImagePreview();
        }

        if (hint) {
            if (data.suggestions && data.suggestions.length > 0) {
                hint.textContent = "AI ideas: " + data.suggestions.join(", ");
            } else {
                hint.textContent = "AI suggestion applied!";
            }
        }

        showAdminToast("AI suggestion applied", "success");
    } catch (err) {
        console.error("AI call error:", err);
        if (hint) {
            hint.textContent = "AI could not generate data. Try again.";
        }
        showAdminToast("AI could not generate data", "error");
    }
};

// FORM SUBMIT HANDLER (Save & Update)
if (document.getElementById('itemForm')) {
    document.getElementById('itemForm').addEventListener('submit', function (e) {
        e.preventDefault();

        const itemId = document.getElementById('itemId').value;
        const name = document.getElementById('itemName').value;
        const description = document.getElementById('itemDescription').value;
        const price = document.getElementById('itemPrice').value;
        const category = document.getElementById('itemCategory').value;
        const imageUrl = document.getElementById('itemImageUrl').value;

        const url = itemId ? '/admin/food-items/update' : '/admin/food-items/add';
        const params = new URLSearchParams({
            name: name,
            description: description,
            price: price,
            category: category,
            imageUrl: imageUrl
        });

        if (itemId) {
            params.append('id', itemId);
        }

        // loading state on submit button
        const submitBtn = this.querySelector('button[type="submit"]');
        const originalText = submitBtn ? submitBtn.textContent : '';
        if (submitBtn) {
            submitBtn.disabled = true;
            submitBtn.textContent = 'Saving...';
        }

        fetch(url + '?' + params.toString(), {
            method: 'POST',
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded',
            }
        })
            .then(response => response.json())
            .then(data => {
                if (submitBtn) {
                    submitBtn.disabled = false;
                    submitBtn.textContent = originalText || 'Save Item';
                }

                if (data.success) {
                    showAdminToast('Item saved successfully', 'success');
                    window.closeItemModal();

                    // For now, reload to reflect list updates (simpler than manually updating DOM)
                    setTimeout(() => {
                        location.reload();
                    }, 300);
                } else {
                    showAdminToast(data.message || 'Error saving item', 'error');
                }
            })
            .catch(error => {
                if (submitBtn) {
                    submitBtn.disabled = false;
                    submitBtn.textContent = originalText || 'Save Item';
                }
                showAdminToast('Error saving item', 'error');
                console.error('Error:', error);
            });
    });
}
