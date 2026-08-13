/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{js,jsx}",
  ],
  darkMode: 'class',
  theme: {
    extend: {
      colors: {
        bdae: {
          primary: 'var(--bdae-primary)',
          secondary: 'var(--bdae-secondary)',
          tertiary: 'var(--bdae-tertiary)',
          surface: 'var(--bdae-surface)',
          bg: 'var(--bdae-bg)',
        }
      }
    },
  },
  plugins: [],
}