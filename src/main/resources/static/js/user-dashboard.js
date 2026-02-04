// src/main/resources/static/js/user-dashboard.js
(function () {
  'use strict';

  /* ------------------- Utilities ------------------- */

  // Respect reduced-motion
  const prefersReducedMotion = window.matchMedia && window.matchMedia('(prefers-reduced-motion: reduce)').matches;

  // Simple toast (re-usable)
  if (typeof window.showUserToast !== 'function') {
    window.showUserToast = function (message, type = 'success') {
      let toast = document.getElementById('userToast');
      if (!toast) {
        toast = document.createElement('div');
        toast.id = 'userToast';
        document.body.appendChild(toast);
      }
      toast.className = 'message ' + (type === 'error' ? 'error' : 'success');
      toast.textContent = message;
      toast.style.opacity = '1';
      toast.style.transform = 'translateY(0)';
      clearTimeout(toast._hideTimer);
      toast._hideTimer = setTimeout(function () {
        toast.style.opacity = '0';
        toast.textContent = '';
      }, 3500);
    };
  }

  // tiny "bump" animation for elements (buttons / icons)
  if (typeof window.bumpElement !== 'function') {
    window.bumpElement = function (el) {
      if (!el) return;
      el.classList.add('bump-anim');
      setTimeout(function () {
        el.classList.remove('bump-anim');
      }, 350);
    };
  }

  /* ------------------- Counters ------------------- */
  function animateNumberNode(node, duration = 900) {
    if (!node) return;
    const raw = node.getAttribute('data-counter') || node.textContent;
    const target = Number(raw) || 0;
    if (prefersReducedMotion) {
      node.textContent = Number.isInteger(target) ? target : (Math.round(target * 10) / 10);
      return;
    }

    const stepTime = 25;
    const steps = Math.max(1, Math.floor(duration / stepTime));
    const increment = target / steps;
    let current = 0;
    let step = 0;

    const timer = setInterval(() => {
      step++;
      current += increment;
      if (step >= steps) {
        clearInterval(timer);
        node.textContent = Number.isInteger(target) ? target : (Math.round(target * 10) / 10);
      } else {
        node.textContent = Number.isInteger(target) ? Math.round(current) : (Math.round(current * 10) / 10);
      }
    }, stepTime);
  }

  function initCounters() {
    const els = document.querySelectorAll('.dashboard-stat-number, .dashboard-stat-number[data-counter], .dashboard-stats-card [data-counter], .dashboard-stat-number');
    // But our markup uses .dashboard-stat-number and data-counter
    const counters = document.querySelectorAll('[data-counter]');
    if (!counters || counters.length === 0) return;

    // Use IntersectionObserver to animate when visible
    if (prefersReducedMotion) {
      counters.forEach(c => {
        const raw = c.getAttribute('data-counter') || c.textContent;
        c.textContent = raw;
      });
      return;
    }
    if ('IntersectionObserver' in window) {
      const io = new IntersectionObserver((entries, obs) => {
        entries.forEach(entry => {
          if (entry.isIntersecting) {
            animateNumberNode(entry.target);
            obs.unobserve(entry.target);
          }
        });
      }, { threshold: 0.35 });
      counters.forEach(c => io.observe(c));
    } else {
      counters.forEach(c => animateNumberNode(c));
    }
  }

  /* ------------------- Reveal on scroll ------------------- */

  function initReveal() {
    const targets = document.querySelectorAll('.reveal, .reveal-grid > *');
    if (!targets || targets.length === 0) return;

    if (prefersReducedMotion) {
      targets.forEach(t => t.classList.add('revealed'));
      return;
    }
    if ('IntersectionObserver' in window) {
      const io = new IntersectionObserver((entries, obs) => {
        entries.forEach(entry => {
          if (entry.isIntersecting) {
            entry.target.classList.add('revealed');
            obs.unobserve(entry.target);
          }
        });
      }, { threshold: 0.18 });
      targets.forEach(t => io.observe(t));
    } else {
      targets.forEach(t => t.classList.add('revealed'));
    }
  }

  /* ------------------- Testimonials rotation (if any) ------------------- */
  function initTestimonials() {
    const wrapper = document.querySelector('.testimonials-wrapper');
    if (!wrapper) return;
    const items = Array.from(wrapper.querySelectorAll('.testimonial'));
    if (items.length < 2) {
      if (items[0]) items[0].classList.add('active');
      return;
    }
    let index = 0;
    items.forEach((it, i) => it.classList.toggle('active', i === 0));
    if (!prefersReducedMotion) {
      setInterval(() => {
        index = (index + 1) % items.length;
        items.forEach((it, i) => it.classList.toggle('active', i === index));
      }, 6000);
    }
  }

  /* ------------------- addToCart function (global) ------------------- */
  // This function performs a POST to your backend route and handles responses.
  window.addToCart = function (itemId) {
    var quantity = 1;

    // Find the button for some micro-feedback
    var button = document.querySelector('button[data-item-id="' + itemId + '"]') ||
                 document.querySelector('button[onclick*="' + itemId + '"]');

    var oldText = null;
    if (button) {
      oldText = button.textContent;
      button.disabled = true;
      button.textContent = 'Adding...';
      bumpElement(button);
    }

    // Use form-urlencoded since likely same backend expects it; adjust if your API expects JSON
    var body = 'itemId=' + encodeURIComponent(itemId) + '&quantity=' + encodeURIComponent(quantity);

    fetch('/user/cart/add', {
      method: 'POST',
      headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
      body: body,
      credentials: 'same-origin'
    }).then(function (res) {
      // try parse JSON safely
      return res.json().catch(function () { return { success: false, message: 'Invalid server response' }; });
    }).then(function (data) {
      if (button) {
        button.disabled = false;
        button.textContent = oldText || 'Add to Cart';
      }

      if (data && data.success) {
        showUserToast('Item added to cart!', 'success');

        // update cart count if provided
        if (typeof data.cartCount !== 'undefined') {
          var countEl = document.getElementById('cartCount');
          if (countEl) {
            countEl.textContent = data.cartCount;
            bumpElement(countEl);
          }
        }

        // animate navbar cart icon if any
        var cartIcon = document.querySelector('.nav-cart-icon') || document.querySelector('#cartCount');
        bumpElement(cartIcon);
      } else {
        showUserToast((data && data.message) ? data.message : 'Could not add item to cart', 'error');
      }
    }).catch(function (err) {
      if (button) {
        button.disabled = false;
        button.textContent = oldText || 'Add to Cart';
      }
      console.error('addToCart error:', err);
      showUserToast('Network error: could not add item', 'error');
    });
  };

  /* ------------------- CTA microstate for hero button ------------------- */
  function attachCtaMicro() {
    const cta = document.getElementById('browseMenuCta');
    if (!cta) return;
    function down() { cta.classList.add('hero-cta-pressed'); }
    function up() { cta.classList.remove('hero-cta-pressed'); }
    if (window.PointerEvent) {
      cta.addEventListener('pointerdown', down, { passive: true });
      document.addEventListener('pointerup', up, { passive: true });
    } else {
      cta.addEventListener('mousedown', down, { passive: true });
      document.addEventListener('mouseup', up, { passive: true });
    }
    cta.addEventListener('keydown', function (e) {
      if (e.key === ' ' || e.key === 'Enter') {
        bumpElement(cta);
      }
    });
  }

  /* ------------------- Init ------------------- */
  function init() {
    initReveal();
    initCounters();
    initTestimonials();
    attachCtaMicro();
  }

  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', init);
  } else {
    init();
  }

})();
