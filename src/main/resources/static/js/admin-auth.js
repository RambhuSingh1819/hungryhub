// ========== Helper Functions ==========

function showMessage(message, type) {
    const messageDiv = document.getElementById('message');
    if (!messageDiv) return;

    messageDiv.textContent = message;
    messageDiv.className = 'message ' + type;

    messageDiv.style.animation = 'fadeInUp 0.35s ease-out';
    setTimeout(() => messageDiv.style.animation = '', 400);

    const offset = messageDiv.getBoundingClientRect().top + window.scrollY - 80;
    window.scrollTo({ top: offset < 0 ? 0 : offset, behavior: 'smooth' });

    setTimeout(() => {
        messageDiv.textContent = '';
        messageDiv.className = 'message';
    }, 5000);
}

function animateBlock(el) {
    if (!el) return;
    el.style.animation = 'fadeInUp 0.35s ease-out';
    setTimeout(() => el.style.animation = '', 400);
}

// ========== EMAIL OTP ONLY ==========

function sendEmailOtp() {
    const email = document.getElementById('email')?.value || '';

    if (!email.trim()) {
        showMessage('Please enter your email', 'error');
        return;
    }

    fetch('/admin/send-otp', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
            email: email.trim(),
            type: 'admin'
        })
    })
    .then(res => res.json())
    .then(data => {
        if (data.success) {
            showMessage('OTP sent to your email', 'success');
            const group = document.getElementById('emailOtpGroup');
            if (group) {
                group.style.display = 'block';
                animateBlock(group);
            }
        } else {
            showMessage(data.message || 'Error sending email OTP', 'error');
        }
    })
    .catch(() => showMessage('Error sending OTP', 'error'));
}

// ========== LOGIN HANDLER (PHONE LOGIN DISABLED) ==========

const adminLoginForm = document.getElementById('adminLoginForm');

if (adminLoginForm) {
    adminLoginForm.addEventListener('submit', function (e) {
        e.preventDefault();

        const identifier = document.getElementById('identifier')?.value || '';
        const password = document.getElementById('password')?.value || '';

        if (!identifier.trim() || !password.trim()) {
            showMessage('Please enter both identifier and password', 'error');
            return;
        }

        const loginData = { password: password, type: 'admin' };

        // Email login
        if (identifier.includes('@')) {
            loginData.email = identifier.trim();
        }
        // Admin ID login (ADM...)
        else if (identifier.startsWith('ADM')) {
            loginData.adminId = identifier.trim();
        }
        // PHONE LOGIN DISABLED
        else {
            showMessage(
                'Please login using your Admin ID (starting with ADM...) or registered email address.',
                'error'
            );
            return;
        }

        fetch('/admin/login', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(loginData)
        })
        .then(res => res.json())
        .then(data => {
            if (data.success) {
                showMessage('Login successful! Redirecting...', 'success');
                const card = adminLoginForm.closest('.auth-card');
                if (card) animateBlock(card);
                setTimeout(() => window.location.href = data.redirect || '/admin/dashboard', 1000);
            } else {
                showMessage(data.message || 'Login failed', 'error');
            }
        })
        .catch(() => showMessage('Error during login', 'error'));
    });
}

// ========== ADMIN REGISTRATION HANDLER (NO PHONE OTP) ==========

const adminRegisterForm = document.getElementById('adminRegisterForm');

if (adminRegisterForm) {
    adminRegisterForm.addEventListener('submit', function (e) {
        e.preventDefault();

        const fullName = document.getElementById('fullName')?.value || '';
        const email = document.getElementById('email')?.value || '';
        const emailOtp = document.getElementById('emailOtp')?.value || '';
        const phoneNumber = document.getElementById('phoneNumber')?.value || '';
        const password = document.getElementById('password')?.value || '';

        // Required fields
        if (!fullName.trim() || !email.trim() || !password.trim()) {
            showMessage('Please fill all required fields', 'error');
            return;
        }

        // Email OTP only
        if (!emailOtp.trim()) {
            showMessage('Please enter your email OTP', 'error');
            animateBlock(document.getElementById('emailOtpGroup'));
            return;
        }

        // Verify email OTP
        fetch('/admin/verify-otp', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                email: email.trim(),
                otp: emailOtp.trim(),
                type: 'admin'
            })
        })
        .then(res => res.json())
        .then(verify => {
            if (!verify.success) {
                showMessage('Invalid email OTP', 'error');
                return;
            }

            // Register admin
            const data = {
                fullName: fullName.trim(),
                email: email.trim(),
                phoneNumber: phoneNumber.trim(),
                password: password
            };

            return fetch('/admin/register', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(data)
            });
        })
        .then(res => res?.json())
        .then(data => {
            if (!data) return;

            if (data.success) {
                showMessage(
                    'Registration successful! Your Admin ID: ' +
                    data.adminId +
                    '. Redirecting...',
                    'success'
                );

                const card = adminRegisterForm.closest('.auth-card');
                if (card) animateBlock(card);

                setTimeout(() => window.location.href = '/admin/login', 3000);
            } else {
                showMessage(data.message || 'Registration failed', 'error');
            }
        })
        .catch(() => showMessage('Error during registration', 'error'));
    });
}
