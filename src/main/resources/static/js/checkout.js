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
