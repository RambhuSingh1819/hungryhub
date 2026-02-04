(function () {
  'use strict';

  const reduce = window.matchMedia && window.matchMedia('(prefers-reduced-motion: reduce)').matches;

  function animateNumber(el, target, duration = 900) {
    if (!el) return;
    target = Number(target) || 0;
    if (target === 0) {
      el.textContent = target;
      return;
    }
    if (reduce) {
      el.textContent = (Number.isInteger(target) ? target : (Math.round(target * 10) / 10));
      return;
    }

    const stepTime = 25;
    const steps = Math.max(1, Math.floor(duration / stepTime));
    const increment = target / steps;
    let current = 0;
    let count = 0;

    const timer = setInterval(() => {
      current += increment;
      count++;
      if (count >= steps) {
        clearInterval(timer);
        el.textContent = (Number.isInteger(target) ? target : (Math.round(target * 10) / 10));
      } else {
        el.textContent = (Number.isInteger(target) ? Math.round(current) : (Math.round(current * 10) / 10));
      }
    }, stepTime);
  }

  function initCounters() {
    const els = document.querySelectorAll('.hero-stat-number[data-counter]');
    if (!els || els.length === 0) return;

    if ('IntersectionObserver' in window) {
      const io = new IntersectionObserver((entries, obs) => {
        entries.forEach(entry => {
          if (entry.isIntersecting) {
            const node = entry.target;
            const target = node.getAttribute('data-counter');
            animateNumber(node, target);
            obs.unobserve(node);
          }
        });
      }, {threshold: 0.4});

      els.forEach(el => io.observe(el));
    } else {
      els.forEach(el => {
        const target = el.getAttribute('data-counter');
        animateNumber(el, target);
      });
    }
  }

  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', initCounters);
  } else {
    initCounters();
  }
})();
