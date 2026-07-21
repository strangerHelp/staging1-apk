# StrangerHelp Frontend

Frontend source code for [strangerhelp.com](https://strangerhelp.com) — a hyperlocal task marketplace where strangers help strangers.

Use this repo to understand the UI structure, screens, and flows when building the **Android/iOS native app**.

## Contents

```
├── API.md              # Complete API documentation with all endpoints
├── DESIGN.md           # Design system (colors, typography, spacing)
├── pages/              # All page templates (53 pages)
│   ├── index.astro     # Homepage
│   ├── tasks/          # Task feed, detail, posting, voice
│   ├── chat/           # Messaging (list + detail)
│   ├── ask/            # Community Q&A
│   ├── meets/          # StrangerMeet events
│   ├── pulse.astro     # Live map
│   ├── dashboard/      # User dashboard, profile, settings
│   ├── admin/          # Admin panel
│   ├── blog/           # Blog posts
│   ├── api/            # API route handlers (backend logic)
│   └── ...             # Auth, legal, help pages
├── components/         # Reusable UI components
│   ├── Navbar.astro
│   ├── Footer.astro
│   ├── TaskCard.astro
│   ├── ChatWindow.tsx
│   └── ...
├── layouts/            # Page layouts
│   ├── Layout.astro        # Main layout (navbar + footer)
│   ├── AccountLayout.astro # Dashboard layout (sidebar)
│   └── PublicLayout.astro
├── styles/             # Global CSS (Tailwind)
└── public/             # Static assets (icons, scripts)
```

## Key Screens (for native app reference)

| Screen | File | Purpose |
|--------|------|---------|
| Task Feed | `pages/tasks/index.astro` | Browse tasks with search, sort, location filter |
| Task Detail | `pages/tasks/[id].astro` | View task, claim, share, edit, report |
| Post Task | `pages/tasks/new.astro` | Create task with voice, photos, location picker |
| Chat List | `pages/chat/index.astro` | Conversations list |
| Chat Detail | `pages/chat/[id].astro` | Messages with send, images, audio |
| Profile | `pages/dashboard/profile.astro` | Edit profile, trust stats, verify email |
| Login/Register | `pages/login.astro`, `pages/register.astro` | Auth with Google OAuth |
| Meets | `pages/meets/index.astro` | Browse/create events |
| Pulse Map | `pages/pulse.astro` | Live map with MapLibre |
| Admin | `pages/admin/index.astro` | Full admin panel |

## Design System

See `DESIGN.md` for complete design tokens:
- **Primary:** #171717 (near black)
- **Accent:** #29bc9b (teal/cyan)
- **Link:** #0070f3 (blue)
- **Font:** Geist Sans
- **Cards:** 16px radius, 1px border
- **Buttons:** Pill shape (rounded-full)

## API Documentation

See `API.md` for complete API docs with:
- All endpoints (auth, tasks, messages, meets, notifications, etc.)
- Request/response formats
- Error codes
- Rate limits
- Authentication flow

## Tech Stack (Web)

- **Framework:** Astro 6 (SSR)
- **UI:** Tailwind CSS 4, React 19 (islands)
- **Maps:** MapLibre GL JS + OpenFreeMap
- **Backend:** Cloudflare Workers + D1 (SQLite)
- **Auth:** HMAC-signed cookies + Google OAuth

## For Native App Developers

1. Read `API.md` for all backend endpoints
2. Study `pages/tasks/index.astro` for feed logic (search, sort, location)
3. Study `pages/tasks/[id].astro` for task detail flow (claim, edit, share, proof)
4. Study `pages/chat/[id].astro` for messaging implementation
5. Study `DESIGN.md` for exact colors and typography
6. The `pages/api/` folder shows backend logic — useful for understanding data flow

## Live Site

- Website: https://strangerhelp.com
- API Base: https://strangerhelp.com/api/
