(function () {
    function initThemeToggle() {
        const topbar = document.querySelector('.swagger-ui .topbar-wrapper') || document.querySelector('.swagger-ui .topbar');
        if (!topbar) {
            setTimeout(initThemeToggle, 300);
            return;
        }

        if (document.getElementById('theme-toggle-btn')) {
            return; // Already initialized
        }

        const btn = document.createElement('button');
        btn.id = 'theme-toggle-btn';
        btn.className = 'theme-toggle-btn';

        const savedTheme = localStorage.getItem('qershi-swagger-theme') || 'dark';
        if (savedTheme === 'dark') {
            document.body.classList.add('dark-mode');
            btn.innerHTML = '💡 Dark Mode';
        } else {
            document.body.classList.remove('dark-mode');
            btn.innerHTML = '☀️ Light Mode';
        }

        btn.addEventListener('click', function () {
            const isDark = document.body.classList.toggle('dark-mode');
            if (isDark) {
                btn.innerHTML = '💡 Dark Mode';
                localStorage.setItem('qershi-swagger-theme', 'dark');
            } else {
                btn.innerHTML = '☀️ Light Mode';
                localStorage.setItem('qershi-swagger-theme', 'light');
            }
        });

        topbar.appendChild(btn);
    }

    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', initThemeToggle);
    } else {
        initThemeToggle();
    }
})();
