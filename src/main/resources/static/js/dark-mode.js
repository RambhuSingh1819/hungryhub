// dark-mode.js — DISABLED (Forced Light Mode)

(function () {
  const root = document.documentElement;

  // Always force light mode
  root.classList.remove("dark");
  localStorage.setItem("theme", "light");

  // Optional: hide toggle if it exists
  const toggle = document.getElementById("themeToggle");
  if (toggle) {
    toggle.style.display = "none";
  }

  console.log("🌞 Light mode enforced. Dark mode disabled.");
})();
