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

/* ===== Collapsible, Animated FAQ Item ===== */
function FaqItem({ question, answer }) {
    const [isOpen, setIsOpen] = useState(false);

    return (
        <div style={{
            borderBottom: '1px solid #f1f5f9',
            paddingBlock: '16px',
            cursor: 'pointer'
        }} onClick={() => setIsOpen(!isOpen)}>
            <div style={{
                display: 'flex',
                justifyContent: 'space-between',
                alignItems: 'center',
                fontWeight: 700,
                color: isOpen ? '#2563eb' : '#0f172a',
                fontSize: '1.05rem',
                transition: 'color 0.2s ease'
            }}>
                <span>{question}</span>
                <i className={`bi bi-chevron-${isOpen ? 'up' : 'down'}`} style={{
                    fontSize: '0.9rem',
                    color: isOpen ? '#2563eb' : '#64748b',
                    transition: 'transform 0.2s ease'
                }}></i>
            </div>
            <div style={{
                maxHeight: isOpen ? '200px' : '0px',
                overflow: 'hidden',
                transition: 'all 0.3s cubic-bezier(0.4, 0, 0.2, 1)',
                opacity: isOpen ? 1 : 0,
                marginTop: isOpen ? '8px' : '0px',
                color: '#475569',
                fontSize: '0.95rem',
                lineHeight: '1.6'
            }}>
                {answer}
            </div>
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

            {/* HERO SECTION */}
            <section className="hero" aria-labelledby="hero-heading">
                <div className="hero-image-overlay" aria-hidden="true" />

                <div className="container hero-inner">
                    {/* LEFT SIDE TEXT */}
                    <div className="hero-left">
                        <div className="hero-badges" aria-hidden="true">
                            <span className="hero-badge">⚡ 30-minute average delivery</span>
                            <span className="hero-badge">🔥 Handpicked local favourites</span>
                            <span className="hero-badge">🛡️ Secure online payments</span>
                        </div>

                        <h1 id="hero-heading" className="hero-title">
                            Your favourite food, delivered <span style={{ color: '#2563eb' }}>faster than ever</span>.
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

            {/* POPULAR DISHES SHOWCASE */}
            <section className="section" style={{ backgroundColor: '#ffffff', paddingBlock: '80px' }}>
                <div className="container">
                    <div className="section-header text-center" style={{ marginBottom: '45px' }}>
                        <h2 className="section-title" style={{ fontSize: '2.2rem', fontWeight: 900, color: '#0f172a' }}>Popular Dishes</h2>
                        <p className="section-subtitle" style={{ marginInline: 'auto', color: '#64748b' }}>
                            Top-rated choices handpicked by our culinary experts. Freshly baked, grilled, and prepared for you.
                        </p>
                    </div>

                    <div className="menu-grid grid-auto-fit-lg">
                        {/* Burger */}
                        <div className="menu-item card-3d card-media-zoom">
                            <img src="https://images.unsplash.com/photo-1568901346375-23c9450c58cd?q=80&w=600&auto=format&fit=crop" alt="Classic Veg Burger" />
                            <div className="menu-item-info">
                                <span style={{ fontSize: '0.75rem', fontWeight: 700, textTransform: 'uppercase', color: '#3b82f6', letterSpacing: '0.05em', marginBottom: '4px' }}>Burgers</span>
                                <h3>Classic Veg Burger</h3>
                                <p>Crispy vegetable patty with fresh lettuce, sliced tomatoes, onions, and creamy burger mayo.</p>
                                <div className="item-footer">
                                    <span className="price">₹129.00</span>
                                    <a href="/user/menu" className="btn btn-primary btn-sm">Order Now</a>
                                </div>
                            </div>
                        </div>

                        {/* Pizza */}
                        <div className="menu-item card-3d card-media-zoom">
                            <img src="https://images.unsplash.com/photo-1604382355076-af4b0eb60143?q=80&w=600&auto=format&fit=crop" alt="Margherita Pizza" />
                            <div className="menu-item-info">
                                <span style={{ fontSize: '0.75rem', fontWeight: 700, textTransform: 'uppercase', color: '#ef4444', letterSpacing: '0.05em', marginBottom: '4px' }}>Pizza</span>
                                <h3>Cheesy Margherita Pizza</h3>
                                <p>Classic thin crust pizza topped with fresh tomato sauce, loaded with mozzarella cheese and fresh basil.</p>
                                <div className="item-footer">
                                    <span className="price">₹249.00</span>
                                    <a href="/user/menu" className="btn btn-primary btn-sm">Order Now</a>
                                </div>
                            </div>
                        </div>

                        {/* Lava Cake */}
                        <div className="menu-item card-3d card-media-zoom">
                            <img src="https://images.unsplash.com/photo-1606313564200-e75d5e30476c?q=80&w=600&auto=format&fit=crop" alt="Chocolate Lava Cake" />
                            <div className="menu-item-info">
                                <span style={{ fontSize: '0.75rem', fontWeight: 700, textTransform: 'uppercase', color: '#f59e0b', letterSpacing: '0.05em', marginBottom: '4px' }}>Desserts</span>
                                <h3>Chocolate Lava Cake</h3>
                                <p>Decadent individual chocolate cake with a warm, molten liquid chocolate center.</p>
                                <div className="item-footer">
                                    <span className="price">₹119.00</span>
                                    <a href="/user/menu" className="btn btn-primary btn-sm">Order Now</a>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </section>

            {/* WHY CHOOSE US - FEATURES */}
            <section className="section" style={{ backgroundColor: '#f8fafc', paddingBlock: '80px', borderTop: '1px solid #f1f5f9', borderBottom: '1px solid #f1f5f9' }}>
                <div className="container">
                    <div className="section-header text-center" style={{ marginBottom: '45px' }}>
                        <h2 className="section-title" style={{ fontSize: '2.2rem', fontWeight: 900, color: '#0f172a' }}>Why choose us?</h2>
                        <p className="section-subtitle" style={{ marginInline: 'auto', color: '#64748b' }}>
                            Fast delivery, verified reviews, handpicked local partners, and secure checkouts — all in one app.
                        </p>
                    </div>

                    <div className="feature-grid grid-auto-fit">
                        <div className="feature-card card-3d" style={{ display: 'flex', flexDirection: 'column', gap: '8px' }}>
                            <div className="feature-icon" style={{ display: 'flex', width: '45px', height: '45px', borderRadius: '12px', backgroundColor: '#fef3c7', alignItems: 'center', justifyContent: 'center', fontSize: '1.4rem', color: '#d97706' }}>⏱</div>
                            <h3 style={{ fontSize: '1.15rem', fontWeight: 700, color: '#1e293b', marginTop: '10px' }}>Lightning-fast delivery</h3>
                            <p style={{ fontSize: '0.92rem', color: '#64748b', margin: 0 }}>Optimised courier routes ensure your food arrives steaming hot and fresh.</p>
                        </div>

                        <div className="feature-card card-3d" style={{ display: 'flex', flexDirection: 'column', gap: '8px' }}>
                            <div className="feature-icon" style={{ display: 'flex', width: '45px', height: '45px', borderRadius: '12px', backgroundColor: '#dcfce7', alignItems: 'center', justifyContent: 'center', fontSize: '1.4rem', color: '#15803d' }}>🥗</div>
                            <h3 style={{ fontSize: '1.15rem', fontWeight: 700, color: '#1e293b', marginTop: '10px' }}>Quality you can trust</h3>
                            <p style={{ fontSize: '0.92rem', color: '#64748b', margin: 0 }}>Every kitchen is verified for hygiene and ingredients quality.</p>
                        </div>

                        <div className="feature-card card-3d" style={{ display: 'flex', flexDirection: 'column', gap: '8px' }}>
                            <div className="feature-icon" style={{ display: 'flex', width: '45px', height: '45px', borderRadius: '12px', backgroundColor: '#e0f2fe', alignItems: 'center', justifyContent: 'center', fontSize: '1.4rem', color: '#0369a1' }}>📍</div>
                            <h3 style={{ fontSize: '1.15rem', fontWeight: 700, color: '#1e293b', marginTop: '10px' }}>Live order tracking</h3>
                            <p style={{ fontSize: '0.92rem', color: '#64748b', margin: 0 }}>Watch your rider on the map from the restaurant kitchen to your front door.</p>
                        </div>
                    </div>
                </div>
            </section>

            {/* CUSTOMER TESTIMONIALS */}
            <section className="section" style={{ backgroundColor: '#ffffff', paddingBlock: '80px' }}>
                <div className="container">
                    <div className="section-header text-center" style={{ marginBottom: '45px' }}>
                        <h2 className="section-title" style={{ fontSize: '2.2rem', fontWeight: 900, color: '#0f172a' }}>What our customers say</h2>
                        <p className="section-subtitle" style={{ marginInline: 'auto', color: '#64748b' }}>
                            We serve thousands of hungry customers every day. Here is what they think of our platform.
                        </p>
                    </div>

                    <div className="grid-auto-fit">
                        {/* Testimonial 1 */}
                        <div className="feature-card" style={{ padding: '24px', position: 'relative' }}>
                            <div style={{ color: '#fbbf24', fontSize: '1.1rem', marginBottom: '10px' }}>★★★★★</div>
                            <p style={{ fontStyle: 'italic', fontSize: '0.95rem', color: '#475569', marginBottom: '20px' }}>
                                "HungryHub has completely changed our family dinners! The delivery is always on time, and the Margherita Pizza is hot enough to burn your mouth."
                            </p>
                            <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
                                <div style={{ width: '40px', height: '40px', borderRadius: '50%', backgroundColor: '#3b82f6', color: '#ffffff', display: 'flex', alignItems: 'center', justifyContent: 'center', fontWeight: 700 }}>A</div>
                                <div>
                                    <strong style={{ fontSize: '0.9rem', color: '#1e293b' }}>Anjali Sharma</strong>
                                    <div style={{ fontSize: '0.75rem', color: '#64748b' }}>Verified Foodie</div>
                                </div>
                            </div>
                        </div>

                        {/* Testimonial 2 */}
                        <div className="feature-card" style={{ padding: '24px', position: 'relative' }}>
                            <div style={{ color: '#fbbf24', fontSize: '1.1rem', marginBottom: '10px' }}>★★★★★</div>
                            <p style={{ fontStyle: 'italic', fontSize: '0.95rem', color: '#475569', marginBottom: '20px' }}>
                                "Highly recommend the Cold Coffee and the Veg Burger! The mock checkout feature works flawlessly and making orders is so clean and painless."
                            </p>
                            <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
                                <div style={{ width: '40px', height: '40px', borderRadius: '50%', backgroundColor: '#10b981', color: '#ffffff', display: 'flex', alignItems: 'center', justifyContent: 'center', fontWeight: 700 }}>R</div>
                                <div>
                                    <strong style={{ fontSize: '0.9rem', color: '#1e293b' }}>Rohit Kumar</strong>
                                    <div style={{ fontSize: '0.75rem', color: '#64748b' }}>Daily Customer</div>
                                </div>
                            </div>
                        </div>

                        {/* Testimonial 3 */}
                        <div className="feature-card" style={{ padding: '24px', position: 'relative' }}>
                            <div style={{ color: '#fbbf24', fontSize: '1.1rem', marginBottom: '10px' }}>★★★★★</div>
                            <p style={{ fontStyle: 'italic', fontSize: '0.95rem', color: '#475569', marginBottom: '20px' }}>
                                "Perfect tracking! I can see exactly when the rider picks up the parcel. The chocolate lava cake is soft, hot, and gooey inside. Ten stars!"
                            </p>
                            <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
                                <div style={{ width: '40px', height: '40px', borderRadius: '50%', backgroundColor: '#8b5cf6', color: '#ffffff', display: 'flex', alignItems: 'center', justifyContent: 'center', fontWeight: 700 }}>P</div>
                                <div>
                                    <strong style={{ fontSize: '0.9rem', color: '#1e293b' }}>Pooja Mehta</strong>
                                    <div style={{ fontSize: '0.75rem', color: '#64748b' }}>Sweet Lover</div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </section>

            {/* FREQUENTLY ASKED QUESTIONS */}
            <section className="section" style={{ backgroundColor: '#f8fafc', paddingBlock: '80px', borderTop: '1px solid #f1f5f9' }}>
                <div className="container" style={{ maxWidth: '750px' }}>
                    <div className="section-header text-center" style={{ marginBottom: '40px' }}>
                        <h2 className="section-title" style={{ fontSize: '2.2rem', fontWeight: 900, color: '#0f172a' }}>Got questions?</h2>
                        <p className="section-subtitle" style={{ marginInline: 'auto', color: '#64748b' }}>
                            Find quick answers below or drop us a line in support.
                        </p>
                    </div>

                    <div style={{ backgroundColor: '#ffffff', borderRadius: '16px', padding: '24px', border: '1px solid #e2e8f0', boxShadow: '0 4px 12px rgba(0,0,0,0.02)' }}>
                        <FaqItem 
                            question="How fast is the standard delivery?" 
                            answer="We maintain an average delivery time of 30 minutes from cooking completion. Times may vary slightly depending on your distance and weather."
                        />
                        <FaqItem 
                            question="Can I pay using online payment methods?" 
                            answer="Yes! We support secure debit/credit cards, UPI, net banking, and secure simulated checkouts for testing and trial orders."
                        />
                        <FaqItem 
                            question="Is there a minimum order requirement?" 
                            answer="No! You can order as little as a single beverage or cake. There is no minimum restriction on local food deliveries."
                        />
                        <FaqItem 
                            question="How do I register my restaurant on the app?" 
                            answer="You can register as an Admin user, buy a monthly subscription, and immediately upload your menus, edit prices, and view customer orders."
                        />
                    </div>
                </div>
            </section>

            {/* FOOTER */}
            <footer style={{ backgroundColor: '#0f172a', color: '#94a3b8', paddingBlock: '60px 30px', borderTop: '1px solid #1e293b' }}>
                <div className="container">
                    <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))', gap: '40px', marginBottom: '40px' }}>
                        <div>
                            <div style={{ fontSize: '1.25rem', fontWeight: 900, color: '#ffffff', marginBottom: '16px' }}>🍔 HungryHub</div>
                            <p style={{ fontSize: '0.9rem', lineHeight: 1.6 }}>
                                Providing lightning-fast, premium food delivery services from your favorite local dining options straight to your door.
                            </p>
                        </div>
                        <div>
                            <div style={{ fontSize: '1rem', fontWeight: 700, color: '#ffffff', marginBottom: '16px' }}>Quick Links</div>
                            <ul style={{ listStyle: 'none', padding: 0, margin: 0, display: 'flex', flexDirection: 'column', gap: '10px', fontSize: '0.9rem' }}>
                                <li><a href="/" style={{ color: '#94a3b8', textDecoration: 'none' }}>Home</a></li>
                                <li><a href="/user/menu" style={{ color: '#94a3b8', textDecoration: 'none' }}>Menu</a></li>
                                <li><a href="/user/login" style={{ color: '#94a3b8', textDecoration: 'none' }}>Customer Login</a></li>
                                <li><a href="/admin/login" style={{ color: '#94a3b8', textDecoration: 'none' }}>Merchant Panel</a></li>
                            </ul>
                        </div>
                        <div>
                            <div style={{ fontSize: '1rem', fontWeight: 700, color: '#ffffff', marginBottom: '16px' }}>Support</div>
                            <ul style={{ listStyle: 'none', padding: 0, margin: 0, display: 'flex', flexDirection: 'column', gap: '10px', fontSize: '0.9rem' }}>
                                <li>Email: support@hungryhub.com</li>
                                <li>Phone: +1 (800) 555-FOOD</li>
                                <li>Office: 456 Main Street, Tech City</li>
                            </ul>
                        </div>
                        <div>
                            <div style={{ fontSize: '1rem', fontWeight: 700, color: '#ffffff', marginBottom: '16px' }}>Join our newsletter</div>
                            <p style={{ fontSize: '0.9rem', marginBottom: '12px' }}>Get discount coupons and updates weekly.</p>
                            <div style={{ display: 'flex', gap: '8px' }}>
                                <input type="email" placeholder="Your email..." style={{ padding: '8px 12px', borderRadius: '6px', border: '1px solid #334155', backgroundColor: '#1e293b', color: '#ffffff', fontSize: '0.85rem', width: '100%' }} />
                                <button className="btn btn-primary btn-sm" style={{ paddingInline: '16px' }}>Go</button>
                            </div>
                        </div>
                    </div>
                    <div style={{ borderTop: '1px solid #1e293b', paddingTop: '20px', display: 'flex', justifyContent: 'space-between', flexWrap: 'wrap', gap: '10px', fontSize: '0.8rem' }}>
                        <div>&copy; {new Date().getFullYear()} HungryHub. All rights reserved.</div>
                        <div style={{ display: 'flex', gap: '20px' }}>
                            <a href="#" style={{ color: '#64748b', textDecoration: 'none' }}>Privacy Policy</a>
                            <a href="#" style={{ color: '#64748b', textDecoration: 'none' }}>Terms of Service</a>
                        </div>
                    </div>
                </div>
            </footer>
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
