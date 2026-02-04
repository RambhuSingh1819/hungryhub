// admin-pay.js – handle admin subscription payment via Razorpay

function showAdminPayToast(message, type = "success") {
    const toast = document.getElementById("adminPayToast");
    if (!toast) return;

    toast.textContent = message;
    toast.className = "message " + type;

    setTimeout(() => {
        toast.textContent = "";
        toast.className = "message";
    }, 4000);
}

document.addEventListener("DOMContentLoaded", () => {
    console.log("admin-pay.js loaded");

    const btn = document.getElementById("adminPayBtn");
    if (!btn) return;

    btn.addEventListener("click", async () => {
        try {
            showAdminPayToast("Creating subscription order...", "success");

            const res = await fetch("/payment/admin/create-subscription-order", {
                method: "POST"
            });

            let data = null;
            try {
                data = await res.json();
            } catch (e) {
                data = null;
            }

            if (!res.ok || !data || !data.success) {
                const msg =
                    (data && data.message) ||
                    "Failed to create subscription order";
                throw new Error(msg);
            }

            const options = {
                key: data.razorpayKeyId,
                amount: data.amountInPaise,
                currency: data.currency,
                name: "Food Delivery - Admin Subscription",
                description: "Admin monthly subscription",
                order_id: data.razorpayOrderId,
                handler: async function (response) {
                    try {
                        const verifyRes = await fetch("/payment/verify", {
                            method: "POST",
                            headers: { "Content-Type": "application/json" },
                            body: JSON.stringify({
                                razorpayPaymentId: response.razorpay_payment_id,
                                razorpayOrderId: response.razorpay_order_id,
                                razorpaySignature: response.razorpay_signature,
                                appOrderId: data.appOrderId
                            })
                        });

                        if (verifyRes.ok) {
                            showAdminPayToast(
                                "Payment successful! Redirecting...",
                                "success"
                            );
                            setTimeout(() => {
                                window.location.href = "/admin/dashboard";
                            }, 1500);
                        } else {
                            showAdminPayToast(
                                "Payment verification failed.",
                                "error"
                            );
                        }
                    } catch (err) {
                        console.error(err);
                        showAdminPayToast(
                            "Payment verification error: " + err.message,
                            "error"
                        );
                    }
                },
                theme: {
                    color: "#3399cc"
                }
            };

            const rzp = new Razorpay(options);
            rzp.open();
        } catch (err) {
            console.error(err);
            showAdminPayToast("Payment start error: " + err.message, "error");
        }
    });
});
