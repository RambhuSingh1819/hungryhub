console.log("💳 checkout.js loaded");

document.addEventListener("DOMContentLoaded", function () {

  const form = document.getElementById("checkoutForm");
  if (!form) {
    console.error("❌ checkoutForm not found");
    return;
  }

  function showToast(msg, type = "success") {
    let toast = document.getElementById("checkoutToast");

    // fallback if toast div is missing
    if (!toast) {
      alert(msg);
      return;
    }

    toast.textContent = msg;
    toast.className = "message " + type;

    setTimeout(() => {
      toast.textContent = "";
      toast.className = "message";
    }, 3000);
  }

  // ---------- FORM SUBMIT ----------
  form.addEventListener("submit", async function (e) {
    e.preventDefault();

    const deliveryAddress =
      document.getElementById("deliveryAddress").value.trim();

    const specialInstructions =
      document.getElementById("specialInstructions").value.trim();

    if (!deliveryAddress) {
      showToast("Please enter delivery address", "error");
      return;
    }

    try {
      // 1️⃣ CREATE APP ORDER
      const orderRes = await fetch("/user/orders/create-from-cart", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          deliveryAddress,
          specialInstructions
        })
      });

      const orderData = await orderRes.json();

      if (!orderRes.ok || !orderData.success) {
        throw new Error(orderData.message || "Order creation failed");
      }

      showToast("Order created. Opening payment gateway...");

      // 2️⃣ CREATE RAZORPAY ORDER
      const rpRes = await fetch("/payment/create-order", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          appOrderId: orderData.orderId,
          amount: orderData.amount
        })
      });

      const rpData = await rpRes.json();

      if (!rpRes.ok) {
        throw new Error("Payment initialization failed");
      }

      // 3️⃣ OPEN RAZORPAY
      // 3️⃣ OPEN RAZORPAY OR SIMULATOR
      if (rpData.razorpayOrderId && rpData.razorpayOrderId.startsWith("rzp_mock_")) {
          openMockPaymentSimulator(rpData, async function (response) {
              const verifyRes = await fetch("/payment/verify", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify(response)
              });

              if (verifyRes.ok) {
                window.location.href = "/payment/success";
              } else {
                window.location.href = "/payment/failed";
              }
          });
          return;
      }

      const options = {
        key: rpData.razorpayKeyId,
        amount: rpData.amountInPaise,
        currency: rpData.currency,
        name: "Food Delivery App",
        order_id: rpData.razorpayOrderId,

        handler: async function (response) {
          const verifyRes = await fetch("/payment/verify", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({
              razorpayPaymentId: response.razorpay_payment_id,
              razorpayOrderId: response.razorpay_order_id,
              razorpaySignature: response.razorpay_signature,
              appOrderId: rpData.appOrderId
            })
          });

          if (verifyRes.ok) {
            window.location.href = "/payment/success";
          } else {
            window.location.href = "/payment/failed";
          }
        }
      };

      new Razorpay(options).open();

    } catch (err) {
      console.error("❌ Checkout error:", err);
      showToast(err.message || "Checkout failed", "error");
    }
  });
});

// Dynamic payment simulator for local testing
function openMockPaymentSimulator(rpData, onComplete) {
    const modalId = "mockPaymentModal_" + Date.now();
    const modalHtml = `
      <div class="modal fade" id="${modalId}" tabindex="-1" aria-labelledby="${modalId}Label" aria-hidden="true" data-bs-backdrop="static">
        <div class="modal-dialog modal-dialog-centered">
          <div class="modal-content border-0 shadow-lg" style="border-radius: 16px; background: rgba(255, 255, 255, 0.98); backdrop-filter: blur(10px);">
            <div class="modal-header bg-primary text-white border-0 py-3" style="border-top-left-radius: 16px; border-top-right-radius: 16px;">
              <h5 class="modal-title fw-bold" id="${modalId}Label">
                <i class="bi bi-wallet2 me-2"></i> HungryHub Payment Simulator
              </h5>
            </div>
            <div class="modal-body p-4 text-center">
              <div class="mb-4">
                <span class="display-4 text-primary"><i class="bi bi-shield-check"></i></span>
                <h4 class="mt-2 fw-bold text-dark text-center">Mock Transaction</h4>
                <p class="text-muted text-center" style="font-size:0.9rem;">Simulate Razorpay payment gateway response for testing purposes.</p>
              </div>
              
              <div class="card bg-light border-0 p-3 mb-4 text-start" style="border-radius: 12px; font-size:0.9rem;">
                <div class="d-flex justify-content-between mb-2">
                  <span class="text-muted">Order ID:</span>
                  <span class="fw-semibold text-dark">${rpData.appOrderId || rpData.razorpayOrderId}</span>
                </div>
                <div class="d-flex justify-content-between">
                  <span class="text-muted">Total Payable:</span>
                  <span class="fw-bold text-success fs-5">₹ ${(rpData.amountInPaise / 100).toFixed(2)}</span>
                </div>
              </div>
              
              <div class="d-grid gap-3">
                <button type="button" class="btn btn-success py-2 fw-bold rounded-pill btn-simulate-success">
                  <i class="bi bi-check-circle-fill me-2"></i> Simulate Payment Success
                </button>
                <button type="button" class="btn btn-danger py-2 fw-bold rounded-pill btn-simulate-failed">
                  <i class="bi bi-x-circle-fill me-2"></i> Simulate Payment Failure
                </button>
              </div>
            </div>
            <div class="modal-footer border-0 justify-content-center pb-4 pt-0">
              <small class="text-muted"><i class="bi bi-info-circle me-1"></i> Under mock mode: everything works except the real payment gateway.</small>
            </div>
          </div>
        </div>
      </div>
    `;

    const div = document.createElement('div');
    div.innerHTML = modalHtml;
    const modalEl = div.firstElementChild;
    document.body.appendChild(modalEl);

    const bsModal = new bootstrap.Modal(modalEl);
    bsModal.show();

    modalEl.querySelector('.btn-simulate-success').addEventListener('click', () => {
        bsModal.hide();
        modalEl.remove();
        onComplete({
            razorpayPaymentId: "pay_mock_" + Math.random().toString(36).substring(2, 12),
            razorpayOrderId: rpData.razorpayOrderId,
            razorpaySignature: "mock_signature",
            appOrderId: rpData.appOrderId
        });
    });

    modalEl.querySelector('.btn-simulate-failed').addEventListener('click', () => {
        bsModal.hide();
        modalEl.remove();
        window.location.href = "/payment/failed";
    });
}
