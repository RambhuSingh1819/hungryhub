(function () {
  'use strict';

  // Helper: check reduced motion preference
  const prefersReducedMotion = () =>
    window.matchMedia &&
    window.matchMedia('(prefers-reduced-motion: reduce)').matches;

  // Attach CTA microstate (mouse + keyboard)
  function attachCtaMicrostate() {
    try {
      const cta = document.querySelector('.hero-cta');
      if (!cta) return;

      function onPointerDown() {
        cta.classList.add('hero-cta-pressed');
      }
      function onPointerUp() {
        cta.classList.remove('hero-cta-pressed');
      }

      function onKeyDown(e) {
        const isActivationKey =
          e.key === ' ' || e.key === 'Spacebar' || e.code === 'Space' || e.key === 'Enter';
        if (isActivationKey) {
          cta.classList.add('hero-cta-pressed');
          setTimeout(() => cta.classList.remove('hero-cta-pressed'), 140);
        }
      }

      if (window.PointerEvent) {
        cta.addEventListener('pointerdown', onPointerDown, { passive: true });
        document.addEventListener('pointerup', onPointerUp, { passive: true });
      } else {
        cta.addEventListener('mousedown', onPointerDown, { passive: true });
        document.addEventListener('mouseup', onPointerUp, { passive: true });
        document.addEventListener('touchend', onPointerUp, { passive: true });
      }

      cta.addEventListener('keydown', onKeyDown);
    } catch (err) {
      // eslint-disable-next-line no-console
      console.warn('hero.js: CTA microstate failed', err);
    }
  }

  // Parallax / tilt on hero card. Skip on reduced-motion or touch
  function attachHeroCardParallax() {
    try {
      if (prefersReducedMotion()) return;
      if ('ontouchstart' in window) return;

      const card = document.getElementById('heroCard');
      if (!card) return;

      const strength = 8; // degrees
      card.style.transformOrigin = 'center';
      card.style.willChange = 'transform';

      const moveEvent = window.PointerEvent ? 'pointermove' : 'mousemove';

      function onMove(e) {
        try {
          const clientX = e.clientX;
          const clientY = e.clientY;
          const r = card.getBoundingClientRect();
          if (r.width === 0 || r.height === 0) return;

          const x = (clientX - r.left) / r.width - 0.5;
          const y = (clientY - r.top) / r.height - 0.5;

          const rx = (-y * strength).toFixed(2);
          const ry = (x * strength).toFixed(2);

          card.style.transform = `perspective(900px) rotateX(${rx}deg) rotateY(${ry}deg) translateZ(6px)`;
        } catch (innerErr) {
          // eslint-disable-next-line no-console
          console.debug('hero.js: card onMove error', innerErr);
        }
      }

      function onLeave() {
        card.style.transform = '';
        card.style.transition = 'transform 260ms cubic-bezier(.2,.9,.3,1)';
        setTimeout(() => {
          card.style.transition = '';
        }, 300);
      }

      function onEnter() {
        card.style.transition = 'transform 160ms ease';
      }

      card.addEventListener(moveEvent, onMove, { passive: true });
      card.addEventListener('mouseleave', onLeave, { passive: true });
      card.addEventListener('mouseenter', onEnter, { passive: true });
      card.addEventListener('pointerleave', onLeave, { passive: true });
    } catch (err) {
      // eslint-disable-next-line no-console
      console.warn('hero.js: hero card parallax failed', err);
    }
  }

  // Init
  function init() {
    attachCtaMicrostate();
    attachHeroCardParallax();
  }

  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', init);
  } else {
    init();
  }
})();
