# AcademiaX — React 19 Frontend SPA

This directory contains the modern Single Page Application (SPA) frontend for **AcademiaX**, built with **React 19**, **Vite 8**, **React Router v7**, and **Recharts 3**.

---

## 🛠 Tech Stack

- **React 19**: Component-driven UI development
- **Vite 8**: Modern frontend toolchain & dev server
- **React Router DOM v7**: Client-side routing with protected routes (`ProtectedRoute`)
- **Recharts 3**: Interactive charts for academic analytics and grade distributions
- **Lucide React**: Modern UI icons
- **Vanilla CSS**: Custom glassmorphism design system supporting light/dark theme modes

---

## 📂 Folder Structure

```
frontend/
├── public/                # Static public assets
├── src/
│   ├── components/        # Reusable UI components (Modal, Sidebar, StatCard)
│   ├── context/           # React Contexts (AuthContext, ThemeContext, ToastContext)
│   ├── layouts/           # DashboardLayout wrapper
│   ├── pages/             # Route pages (Dashboard, Courses, Students, Enrollments, Grades, Reports, Login)
│   ├── utils/             # Helper utilities
│   ├── api.js             # Centralized REST API client (communicates with Spring Boot backend)
│   ├── App.jsx            # Routing and Provider context tree
│   ├── index.css          # Design system, CSS variables & theme tokens
│   └── main.jsx           # React app mounting point
├── package.json           # Dependencies and scripts
└── vite.config.js         # Vite configuration with API proxy to http://localhost:8080
```

---

## 🚀 Getting Started

1. **Install Dependencies**
   ```bash
   npm install
   ```

2. **Start Development Server**
   ```bash
   npm run dev
   ```
   Access the frontend at **http://localhost:3000**.
   > *Note: Ensure the Spring Boot backend server is running at `http://localhost:8080` for API endpoints to function.*

3. **Build for Production**
   ```bash
   npm run build
   ```
   Outputs production assets into the `dist/` directory.
