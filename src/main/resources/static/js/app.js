// Main React App Component (for index.html)
const { useState, useEffect } = React;

/* ===== Small animated counter for stats ===== */
function StatCounter({ target, label, suffix = "" }) {
    const [value, setValue] = useState(0);

    useEffect(() => {
        // defensive: convert target to number
        const numericTarget = Number(target) || 0;
        let start = 0;
        const duration = 900; // ms
        const stepTime = 25; // ms
        const steps = Math.max(1, Math.floor(duration / stepTime));
        const increment = numericTarget / steps;

        const timer = setInterval(() => {
            start += increment;
            if (start >= numericTarget) {
                clearInterval(timer);
                setValue(numericTarget);
            } else {
                // show one decimal place for non-integers, else integer
                if (!Number.isInteger(numericTarget)) {
                    // round to one decimal (avoid long floats)
                    setValue(Math.round(start * 10) / 10);
                } else {
                    setValue(Math.round(start));
                }
            }
        }, stepTime);

        return () => clearInterval(timer);
    }, [target]);

    // Format displayed value: if target is non-integer, ensure one decimal
    const displayValue = !Number.isInteger(Number(target))
        ? (typeof value === 'number' ? (Math.round(value * 10) / 10) : value)
        : (typeof value === 'number' ? Math.round(value) : value);

    return (
        <div className="hero-stat" aria-hidden="true">
            <div className="hero-stat-number">
                {displayValue}{suffix}
            </div>
            <div className="hero-stat-label">{label}</div>
        </div>
    );
}

function App() {
    const [ctaPressed, setCtaPressed] = useState(false);

    const handleCtaClick = (e) => {
        // small click animation, then let navigation proceed
        setCtaPressed(true);
        // ensure we remove pressed state shortly after
        setTimeout(() => setCtaPressed(false), 180);
        // navigation will continue normally (anchor)
    };

    return (
        <div>
            {/* NAVBAR */}
            <nav className="navbar" role="navigation" aria-label="Main navigation">
                <div className="container">
                    <div className="nav-brand">🍔 Food Delivery</div>
                    <div className="nav-links" role="menubar" aria-hidden="false">
                        <a href="/">Home</a>
                        <a href="/user/menu">Menu</a>
                        <a href="/user/login">Login</a>
                        <a href="/user/register">Register</a>
                        <a href="/admin/login">Admin</a>
                    </div>
                </div>
            </nav>

            {/* HERO SECTION (uses your hero styles + animations) */}
            <section className="hero" aria-labelledby="hero-heading">
                <div className="hero-image-overlay" aria-hidden="true" />

                <div className="container">
                    {/* LEFT SIDE TEXT */}
                    <div className="hero-left">
                        <div className="hero-badges" aria-hidden="true">
                            <span className="hero-badge">⚡ 30-minute average delivery</span>
                            <span className="hero-badge">🔥 Handpicked local favourites</span>
                            <span className="hero-badge">🛡️ Secure online payments</span>
                        </div>

                        <h1 id="hero-heading" className="hero-title">
                            Your favourite food, delivered faster than ever.
                        </h1>

                        <p className="hero-subtitle">
                            Browse curated menus from nearby restaurants, track your order in real-time,
                            and enjoy fresh meals at your doorstep.
                        </p>

                        <div className="hero-actions">
                            <a
                                href="/user/menu"
                                className={
                                    "btn btn-primary hero-cta" +
                                    (ctaPressed ? " hero-cta-pressed" : "")
                                }
                                onClick={handleCtaClick}
                                role="button"
                                aria-label="Browse Menu"
                            >
                                Browse Menu
                            </a>
                            <a href="/user/register" className="btn btn-outline-light" role="button" aria-label="Create free account">
                                Create free account
                            </a>
                        </div>

                        <p className="hero-note">
                            No hidden delivery charges. Live order updates, secure checkout, and quick support.
                        </p>

                        <div className="hero-stats" aria-hidden="true">
                            <StatCounter target={250} label="+ Partner Restaurants" />
                            <StatCounter target={12} label="min Avg. Prep Time" suffix="+" />
                            <StatCounter target={4.8} label="★ App Rating" />
                        </div>
                    </div>

                    {/* RIGHT SIDE ILLUSTRATION CARD */}
                    <div className="hero-right">
                        {/* added id so hero.js can find it for parallax */}
                        <div className="hero-card" id="heroCard" tabIndex="0" aria-label="Live order preview">
                            <div className="hero-card-header">
                                <div>
                                    <div className="hero-card-title">Live Order Preview</div>
                                    <small>Example of an active delivery</small>
                                </div>
                                <span className="hero-chip">On the way</span>
                            </div>

                            <div className="hero-card-items" aria-live="polite">
                                <div>• Spicy Paneer Wrap x 1</div>
                                <div>• Loaded Fries x 1</div>
                                <div>• Cold Coffee x 2</div>
                            </div>

                            <div className="hero-card-footer">
                                <div>
                                    <div style={{ fontSize: "0.8rem" }}>ETA</div>
                                    <strong>18–22 min</strong>
                                </div>
                                <div>
                                    <div style={{ fontSize: "0.8rem" }}>Payable</div>
                                    <strong>₹ 529</strong>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </section>
        </div>
    );
}

// Mount React app
// Ensure an element with id="root" exists in the page.
const rootEl = document.getElementById('root');
if (rootEl) {
    const root = ReactDOM.createRoot(rootEl);
    root.render(<App />);
} else {
    // if root is missing, log a helpful error for debugging
    // eslint-disable-next-line no-console
    console.error('React mount failed: <div id="root"></div> not found.');
}
/* =========================================================
   UI ENHANCEMENTS – SAFE WITH REACT
   (Runs AFTER React renders)
========================================================= */

document.addEventListener("DOMContentLoaded", () => {

	/* ===== SCROLL REVEAL ===== */
	const revealObserver = new IntersectionObserver(
		(entries) => {
			entries.forEach(entry => {
				if (entry.isIntersecting) {
					entry.target.classList.add("active");
					revealObserver.unobserve(entry.target);
				}
			});
		},
		{ threshold: 0.15 }
	);

	document.querySelectorAll(".reveal").forEach(el => {
		revealObserver.observe(el);
	});

	/* ===== HERO CARD PARALLAX (VERY LIGHT) ===== */
	const heroCard = document.getElementById("heroCard");
	if (heroCard && window.matchMedia("(prefers-reduced-motion: no-preference)").matches) {
		const strength = 6;

		heroCard.addEventListener("mousemove", (e) => {
			const rect = heroCard.getBoundingClientRect();
			const x = e.clientX - rect.left - rect.width / 2;
			const y = e.clientY - rect.top - rect.height / 2;

			heroCard.style.transform = `
				translateY(-6px)
				rotateX(${(-y / rect.height) * strength}deg)
				rotateY(${(x / rect.width) * strength}deg)
			`;
		});

		heroCard.addEventListener("mouseleave", () => {
			heroCard.style.transform = "translateY(0) rotateX(0) rotateY(0)";
		});
	}

	/* ===== MAGNETIC BUTTON (CTA ONLY) ===== */
	const magneticBtns = document.querySelectorAll(".hero-cta");

	magneticBtns.forEach(btn => {
		btn.addEventListener("mousemove", (e) => {
			const rect = btn.getBoundingClientRect();
			const x = e.clientX - rect.left - rect.width / 2;
			const y = e.clientY - rect.top - rect.height / 2;

			btn.style.transform = `translate(${x * 0.15}px, ${y * 0.15}px)`;
		});

		btn.addEventListener("mouseleave", () => {
			btn.style.transform = "";
		});
	});
});
