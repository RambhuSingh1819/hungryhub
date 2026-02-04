// Cart JavaScript (CLEAN – Cart page only)
console.log("🛒 cart.js loaded");

// ---------- TOAST UI ----------
function getCartToast() {
    let toast = document.getElementById("cartToast");
    if (!toast) {
        toast = document.createElement("div");
        toast.id = "cartToast";
        toast.style.position = "fixed";
        toast.style.top = "20px";
        toast.style.right = "20px";
        toast.style.zIndex = "2000";
        document.body.appendChild(toast);
    }
    return toast;
}

function showCartToast(msg, type = "success") {
    const toast = getCartToast();
    toast.textContent = msg;
    toast.className = "message " + type;

    clearTimeout(toast.timer);
    toast.timer = setTimeout(() => {
        toast.textContent = "";
        toast.className = "message";
    }, 3000);
}

// ---------- UPDATE QUANTITY ----------
function updateQuantity(cartItemId, newQuantity) {
    if (newQuantity < 1) {
        return removeFromCart(cartItemId);
    }

    fetch(`/user/cart/update?cartItemId=${cartItemId}&quantity=${newQuantity}`, {
        method: "POST"
    })
        .then(res => res.json())
        .then(data => {
            if (data.success) {
                location.reload();
            } else {
                showCartToast(data.message || "Error updating cart", "error");
            }
        })
        .catch(() => showCartToast("Error updating cart", "error"));
}

// ---------- REMOVE ITEM ----------
function removeFromCart(cartItemId) {
    if (!confirm("Remove item from cart?")) return;

    fetch(`/user/cart/remove?cartItemId=${cartItemId}`, {
        method: "POST"
    })
        .then(res => res.json())
        .then(data => {
            if (data.success) {
                location.reload();
            } else {
                showCartToast(data.message || "Error removing item", "error");
            }
        })
        .catch(() => showCartToast("Error removing item", "error"));
}
