// ================== User Authentication JavaScript (with animations & toasts) ==================

// ---------- Toast helpers ----------
function getUserToastElement() {
    let toast = document.getElementById("userToast");
    if (!toast) {
        toast = document.createElement("div");
        toast.id = "userToast";
        toast.style.position = "fixed";
        toast.style.top = "20px";
        toast.style.right = "20px";
        toast.style.zIndex = "2000";
        toast.style.minWidth = "220px";
        document.body.appendChild(toast);
    }
    return toast;
}

function showUserToast(message, type = "success") {
    const toast = getUserToastElement();
    toast.textContent = message;
    toast.className = "message " + type;   // uses .message.success / .message.error from CSS
    toast.style.animation = "fadeInUp 0.35s ease-out";

    clearTimeout(toast._timer);
    toast._timer = setTimeout(() => {
        toast.className = "message";
        toast.textContent = "";
    }, 3500);
}

// small helper to “bump” an element (uses badgePop keyframes from CSS)
function bumpElement(el) {
    if (!el) return;
    el.style.animation = "badgePop 0.25s ease-out";
    setTimeout(() => {
        el.style.animation = "";
    }, 260);
}

// shared helper to get auth card
function getAuthCard() {
    return document.querySelector(".auth-card");
}

// ---------- Core message helper (keeps your old behaviour + toast) ----------
function showMessage(message, type) {
    const messageDiv = document.getElementById("message");
    if (messageDiv) {
        messageDiv.textContent = message;
        messageDiv.className = "message " + type;
        bumpElement(messageDiv);
        setTimeout(() => {
            messageDiv.textContent = "";
            messageDiv.className = "message";
        }, 5000);
    }
    showUserToast(message, type); // floating toast
}

// ================== OTP SENDERS ================== //

// EMAIL OTP — ACTIVE
function sendEmailOtp() {
    const emailInput = document.getElementById("email");
    const email = emailInput ? emailInput.value.trim() : "";

    if (!email) {
        showMessage("Please enter your email", "error");
        bumpElement(emailInput);
        return;
    }

    // optional: bump button
    const btn = document.querySelector('button[onclick*="sendEmailOtp"]');
    if (btn) {
        bumpElement(btn);
        btn.disabled = true;
    }

    fetch("/user/send-otp", {
        method: "POST",
        headers: {
            "Content-Type": "application/json",
        },
        body: JSON.stringify({
            email: email,
            type: "user",
        }),
    })
        .then((response) => response.json())
        .then((data) => {
            if (btn) btn.disabled = false;

            if (data.success) {
                showMessage("OTP sent to your email", "success");
                const group = document.getElementById("emailOtpGroup");
                if (group) {
                    group.style.display = "block";
                    bumpElement(group);
                }
            } else {
                showMessage(data.message || "Error sending OTP", "error");
            }
        })
        .catch((error) => {
            if (btn) btn.disabled = false;
            showMessage("Error sending OTP", "error");
            console.error("Error:", error);
        });
}

/*  ================== PHONE OTP — DISABLED ==================

function sendPhoneOtp() {
    const phoneInput = document.getElementById("phoneNumber");
    const phoneNumber = phoneInput ? phoneInput.value.trim() : "";

    if (!phoneNumber) {
        showMessage("Please enter your phone number", "error");
        bumpElement(phoneInput);
        return;
    }

    const btn = document.querySelector('button[onclick*="sendPhoneOtp"]');
    if (btn) {
        bumpElement(btn);
        btn.disabled = true;
    }

    fetch("/user/send-otp", {
        method: "POST",
        headers: {
            "Content-Type": "application/json",
        },
        body: JSON.stringify({
            phoneNumber: phoneNumber,
            type: "user",
        }),
    })
        .then((response) => response.json())
        .then((data) => {
            if (btn) btn.disabled = false;

            if (data.success) {
                showMessage("OTP sent to your phone", "success");
                const group = document.getElementById("phoneOtpGroup");
                if (group) {
                    group.style.display = "block";
                    bumpElement(group);
                }
            } else {
                showMessage(data.message || "Error sending OTP", "error");
            }
        })
        .catch((error) => {
            if (btn) btn.disabled = false;
            showMessage("Error sending OTP", "error");
            console.error("Error:", error);
        });
}

   ========================================================= */

// ================== LOGIN FORM HANDLER ================== //

const loginForm = document.getElementById("loginForm");
if (loginForm) {
    loginForm.addEventListener("submit", function (e) {
        e.preventDefault();

        const identifierInput = document.getElementById("identifier");
        const passwordInput = document.getElementById("password");
        const identifier = identifierInput ? identifierInput.value.trim() : "";
        const password = passwordInput ? passwordInput.value : "";

        if (!identifier || !password) {
            showMessage("Please enter your credentials", "error");
            bumpElement(identifierInput || passwordInput);
            return;
        }

        const authCard = getAuthCard();
        bumpElement(authCard);

        const loginData = {
            password: password,
            type: "user",
        };

        // Email or phone (backend will ignore phone login if disabled)
        if (identifier.includes("@")) {
            loginData.email = identifier;
        } else {
            loginData.phoneNumber = identifier;
        }

        const submitBtn = loginForm.querySelector('button[type="submit"]');
        let originalText = submitBtn ? submitBtn.textContent : "";

        if (submitBtn) {
            submitBtn.disabled = true;
            submitBtn.textContent = "Logging in...";
        }

        fetch("/user/login", {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
            },
            body: JSON.stringify(loginData),
        })
            .then((response) => response.json())
            .then((data) => {
                if (submitBtn) {
                    submitBtn.disabled = false;
                    submitBtn.textContent = originalText || "Login";
                }

                if (data.success) {
                    showMessage("Login successful! Redirecting...", "success");
                    bumpElement(authCard);
                    setTimeout(() => {
                        window.location.href = data.redirect || "/user/dashboard";
                    }, 900);
                } else {
                    showMessage(data.message || "Login failed", "error");
                }
            })
            .catch((error) => {
                if (submitBtn) {
                    submitBtn.disabled = false;
                    submitBtn.textContent = originalText || "Login";
                }
                showMessage("Error during login", "error");
                console.error("Error:", error);
            });
    });
}

// ================== REGISTRATION FORM HANDLER (EMAIL OTP ONLY) ================== //

const registerForm = document.getElementById("registerForm");
if (registerForm) {
    registerForm.addEventListener("submit", function (e) {
        e.preventDefault();

        const emailEl = document.getElementById("email");
        const phoneEl = document.getElementById("phoneNumber");
        const emailOtpEl = document.getElementById("emailOtp");

        const email = emailEl ? emailEl.value.trim() : "";
        const phoneNumber = phoneEl ? phoneEl.value.trim() : "";
        const emailOtp = emailOtpEl ? emailOtpEl.value.trim() : "";

        const fullName = (document.getElementById("fullName") || {}).value || "";
        const password = (document.getElementById("password") || {}).value || "";
        const confirmPassword = (document.getElementById("confirmPassword") || {}).value || "";
        const address = (document.getElementById("address") || {}).value || "";

        const authCard = getAuthCard();
        bumpElement(authCard);

        // Basic validations
        if (!fullName.trim()) {
            showMessage("Please enter your full name", "error");
            bumpElement(document.getElementById("fullName"));
            return;
        }

        if (!email) {
            showMessage("Email is required", "error");
            bumpElement(emailEl);
            return;
        }

        // Phone is OPTIONAL now
        // if you want, you can keep a soft check on length/format, but not required.

        if (!password || !confirmPassword) {
            showMessage("Please enter and confirm your password", "error");
            bumpElement(document.getElementById("password"));
            return;
        }

        if (password !== confirmPassword) {
            showMessage("Passwords do not match", "error");
            bumpElement(document.getElementById("confirmPassword"));
            return;
        }

        // Email OTP is required (since we are verifying only via email)
        if (!emailOtp) {
            showMessage("Please enter your email OTP", "error");
            bumpElement(emailOtpEl);
            return;
        }

        // Disable submit while verifying
        const submitBtn = registerForm.querySelector('button[type="submit"]');
        let originalText = submitBtn ? submitBtn.textContent : "";
        if (submitBtn) {
            submitBtn.disabled = true;
            submitBtn.textContent = "Verifying OTP...";
        }

        // 1) Verify EMAIL OTP only
        fetch("/user/verify-otp", {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
            },
            body: JSON.stringify({
                email: email,
                otp: emailOtp,
                type: "user",
            }),
        })
            .then((res) => res.json())
            .then((result) => {
                if (!result.success) {
                    showMessage("Invalid or expired email OTP", "error");
                    if (submitBtn) {
                        submitBtn.disabled = false;
                        submitBtn.textContent = originalText || "Register";
                    }
                    return null;
                }

                // 2) Proceed with registration (email verified)
                if (submitBtn) {
                    submitBtn.textContent = "Creating account...";
                }

                const registrationData = {
                    email: email,
                    phoneNumber: phoneNumber, // optional; backend can accept empty string
                    password: password,
                    fullName: fullName,
                    address: address,
                };

                return fetch("/user/register", {
                    method: "POST",
                    headers: {
                        "Content-Type": "application/json",
                    },
                    body: JSON.stringify(registrationData),
                });
            })
            .then((response) => (response ? response.json() : null))
            .then((data) => {
                if (!submitBtn) return;

                submitBtn.disabled = false;
                submitBtn.textContent = originalText || "Register";

                if (!data) return;

                if (data.success) {
                    showMessage("Registration successful! Redirecting to login...", "success");
                    bumpElement(authCard);
                    setTimeout(() => {
                        window.location.href = "/user/login";
                    }, 1800);
                } else {
                    showMessage(data.message || "Registration failed", "error");
                }
            })
            .catch((error) => {
                if (submitBtn) {
                    submitBtn.disabled = false;
                    submitBtn.textContent = originalText || "Register";
                }
                showMessage("Error during registration", "error");
                console.error("Error:", error);
            });
    });
}
