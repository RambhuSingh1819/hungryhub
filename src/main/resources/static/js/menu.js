// ========== Menu JavaScript (with toasts & subtle animations) ==========

// ---------- Toast helpers (re-use .message, .success, .error CSS) ----------
function getMenuToast() {
    let toast = document.getElementById("menuToast");
    if (!toast) {
        toast = document.createElement("div");
        toast.id = "menuToast";
        toast.style.position = "fixed";
        toast.style.top = "20px";
        toast.style.right = "20px";
        toast.style.zIndex = "2000";
        toast.style.minWidth = "220px";
        document.body.appendChild(toast);
    }
    return toast;
}

function showMenuToast(message, type = "success") {
    const toast = getMenuToast();
    toast.textContent = message;
    toast.className = "message " + type;   // uses .message.success / .message.error from CSS
    toast.style.animation = "fadeInUp 0.35s ease-out";

    clearTimeout(toast._timer);
    toast._timer = setTimeout(() => {
        toast.className = "message";
        toast.textContent = "";
    }, 3000);
}

// small helper to “bump” an element (uses existing badgePop keyframes)
function bumpElement(el) {
    if (!el) return;
    el.style.animation = "badgePop 0.25s ease-out";
    setTimeout(() => {
        el.style.animation = "";
    }, 260);
}

// ---------- SEARCH ----------
function searchItems() {
    const input = document.getElementById("searchInput");
    if (!input) return;

    const searchQuery = input.value.trim();

    // If empty, just animate input + toast
    if (!searchQuery) {
        showMenuToast("Type something to search 😋", "error");
        bumpElement(input);
        return;
    }

    bumpElement(input); // nice tap animation

    // Small delay so animation is visible
    setTimeout(() => {
        window.location.href = "/user/menu?search=" + encodeURIComponent(searchQuery);
    }, 150);
}

// Allow Enter key to trigger search
const searchInputEl = document.getElementById("searchInput");
if (searchInputEl) {
    searchInputEl.addEventListener("keypress", function (e) {
        if (e.key === "Enter") {
            e.preventDefault();
            searchItems();
        }
    });
}

// ---------- ADD TO CART ----------
function addToCart(itemId) {
    const quantity = 1; // Default quantity

    // Try to find any related card to animate (optional, safe if not found)
    let cardToAnimate = null;
    try {
        // if you later add data-item-id on cards, this will start working automatically
        cardToAnimate = document.querySelector('.menu-item[data-item-id="' + itemId + '"]');
    } catch (e) {
        cardToAnimate = null;
    }

    fetch("/user/cart/add?itemId=" + encodeURIComponent(itemId) + "&quantity=" + quantity, {
        method: "POST",
        headers: {
            "Content-Type": "application/x-www-form-urlencoded"
        }
    })
        .then(response => response.json())
        .then(data => {
            if (data.success) {
                showMenuToast("Item added to cart 🛒", "success");
                bumpElement(cardToAnimate);

                // Optional: update cart count in navbar if element exists
                const cartCountEl = document.getElementById("cartCount");
                if (cartCountEl) {
                    const current = parseInt(cartCountEl.textContent || "0", 10) || 0;
                    cartCountEl.textContent = current + 1;
                    bumpElement(cartCountEl);
                }
            } else {
                showMenuToast(data.message || "Error adding to cart", "error");
            }
        })
        .catch(error => {
            console.error("Error:", error);
            showMenuToast("Error adding to cart", "error");
        });
}
