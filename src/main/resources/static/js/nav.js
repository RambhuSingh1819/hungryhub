document.addEventListener('DOMContentLoaded', function () {
  var toggles = document.querySelectorAll('.nav-toggle');

  toggles.forEach(function (btn) {
    btn.addEventListener('click', function () {
      var navbar = btn.closest('.navbar');
      if (!navbar) return;

      var isOpen = navbar.classList.toggle('nav-open');
      btn.setAttribute('aria-expanded', isOpen ? 'true' : 'false');
    });
  });
});

