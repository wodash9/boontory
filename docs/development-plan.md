# Development Plan: Boontory — Personal Book Library Web App

> **Generated from:** Project description (no PRD file)
> **Created:** 2026-03-16
> **Last synced:** 2026-03-16
> **Status:** Active Planning Document
> **VibeKanban Project ID:** To be assigned

## Overview

Boontory is a mobile-first Progressive Web App that lets a single user scan book barcodes (ISBN-13) with their phone camera or webcam, manage a personal book collection, and search/fetch book metadata from the Open Library API. All data is stored locally in the browser via IndexedDB — no backend or authentication required.

## Tech Stack

- **Backend:** None (frontend-only)
- **Frontend:** React 18, TypeScript, Vite, TailwindCSS, React Router v6
- **Database:** IndexedDB via Dexie.js (browser-local)
- **APIs:** Open Library API (free, no key required)
- **Barcode Scanning:** ZXing-js (`@zxing/library`)
- **Infrastructure:** PWA (Vite PWA plugin + service worker), deployable to Vercel/Netlify

---

## Completion Status Summary

| Epic | Status | Progress |
|------|--------|----------|
| 1. Foundation & Infrastructure | Not Started | 0% |
| 2. Book Library Management | Not Started | 0% |
| 3. Open Library Integration | Not Started | 0% |
| 4. ISBN Barcode Scanner | Not Started | 0% |
| 5. PWA & UX Polish | Not Started | 0% |

---

## Epic 1: Foundation & Infrastructure (NOT STARTED)

Set up the project scaffold, install dependencies, configure routing, define the database schema, and build the base layout shell. Everything downstream depends on this epic being solid.

### Acceptance Criteria

- [ ] `npm run dev` starts the app without errors
- [ ] `npm run build` produces a production bundle without errors
- [ ] All 4 routes are navigable without 404s
- [ ] IndexedDB database initialises on first load with correct schema
- [ ] Bottom navigation bar renders correctly on mobile and desktop

### Tasks

| ID | Title | Description | Priority | Complexity | Depends On | Status |
|----|-------|-------------|----------|------------|------------|--------|
| 1.1 | Initialize Vite + React + TypeScript project | Scaffold project with `npm create vite`, add TypeScript template, configure `tsconfig.json` | High | S | — | <!-- vk: --> |
| 1.2 | Set up TailwindCSS | Install and configure Tailwind with PostCSS; verify utility classes render | High | S | 1.1 | <!-- vk: --> |
| 1.3 | Set up React Router navigation | Install React Router v6; define routes for `/`, `/library`, `/scan`, `/search` | High | S | 1.1 | <!-- vk: --> |
| 1.4 | Implement IndexedDB schema with Dexie.js | Install Dexie.js; define `books` table with all fields; export typed db instance | High | M | 1.1 | <!-- vk: --> |
| 1.5 | Create base layout with bottom navigation bar | Build `Layout` component with header and mobile-first bottom nav (Library, Scan, Search icons) | High | M | 1.2, 1.3 | <!-- vk: --> |

### Task Details

**1.1 - Initialize Vite + React + TypeScript project**
- [ ] `npm run dev` starts dev server on localhost without errors
- [ ] `npm run build` completes successfully and outputs to `dist/`
- [ ] `src/` folder contains `main.tsx`, `App.tsx`, and `index.css`
- [ ] `.gitignore` includes `node_modules/` and `dist/`

**1.2 - Set up TailwindCSS**
- [ ] `tailwind.config.ts` exists and `content` paths cover `src/**/*.{ts,tsx}`
- [ ] A Tailwind utility class (e.g. `bg-blue-500`) applied to a test element renders the correct color
- [ ] PostCSS config includes `tailwindcss` and `autoprefixer`

**1.3 - Set up React Router navigation**
- [ ] Navigating to `/library`, `/scan`, `/search` renders the correct placeholder page component
- [ ] Unknown routes (`/xyz`) render a 404 or redirect to `/`
- [ ] `BrowserRouter` wraps the app in `main.tsx`

**1.4 - Implement IndexedDB schema with Dexie.js**
- [ ] `src/db.ts` exports a typed `Dexie` instance with a `books` table
- [ ] `books` table has fields: `id`, `isbn`, `title`, `authors`, `cover`, `description`, `status`, `rating`, `notes`, `dateAdded`, `dateRead`
- [ ] Opening the app in browser shows the `boontory` database in DevTools → Application → IndexedDB
- [ ] A test `db.books.add({...})` call resolves without error in the browser console

**1.5 - Create base layout with bottom navigation bar**
- [ ] `Layout` component renders `<Outlet />` (React Router) for child routes
- [ ] Bottom nav has 3 tabs: Library, Scan, Search — each with an icon and label
- [ ] Active tab is visually highlighted based on current route
- [ ] Layout is usable on 375px (iPhone SE) and 1440px (desktop) widths

---

## Epic 2: Book Library Management (NOT STARTED)

Full CRUD for the personal book collection: listing, viewing, adding, editing, deleting, tracking reading status, rating, and searching/filtering within the library.

### Acceptance Criteria

- [ ] User can add a book manually and see it appear in the library list
- [ ] User can edit all book fields and changes persist after page refresh
- [ ] User can delete a book; it no longer appears in the list
- [ ] Reading status, star rating, and notes are saved to IndexedDB
- [ ] Library search/filter returns accurate results
- [ ] Statistics page displays correct counts from real data

### Tasks

| ID | Title | Description | Priority | Complexity | Depends On | Status |
|----|-------|-------------|----------|------------|------------|--------|
| 2.1 | Book list view (grid/list toggle) | Display all books from IndexedDB; toggle between grid and list layout; sort by date added | High | M | 1.4, 1.5 | <!-- vk: --> |
| 2.2 | Book detail page | Show full book details: cover, title, authors, description, status, rating, notes | High | M | 2.1 | <!-- vk: --> |
| 2.3 | Add book manually form | Form to add a book with all fields; validates required fields (title); saves to IndexedDB | High | M | 1.4 | <!-- vk: --> |
| 2.4 | Edit book form | Pre-populated form to edit existing book; saves changes to IndexedDB | High | M | 2.3 | <!-- vk: --> |
| 2.5 | Delete book with confirmation | Delete button on detail page; confirmation dialog before deletion | High | S | 2.2 | <!-- vk: --> |
| 2.6 | Reading status selector | Inline selector (Want to Read / Reading / Read) on detail page; persists to DB | High | S | 2.2 | <!-- vk: --> |
| 2.7 | Star rating component | Reusable 1–5 star rating component; clicking saves rating to DB; shows on list and detail | Medium | S | 2.2 | <!-- vk: --> |
| 2.8 | Personal notes textarea | Editable notes field on detail page; auto-saves or saves on blur; persists to DB | Medium | S | 2.2 | <!-- vk: --> |
| 2.9 | Library search and filter | Search bar (title/author); filter chips for status; updates list in real time | High | M | 2.1 | <!-- vk: --> |
| 2.10 | Library statistics view | Stats page: total books, count per status, books read per year (bar or number display) | Low | M | 2.1 | <!-- vk: --> |

### Task Details

**2.1 - Book list view (grid/list toggle)**
- [ ] Library page fetches all books from `db.books.toArray()` and renders them
- [ ] Grid view shows cover thumbnail, title, and author; list view shows same in a row layout
- [ ] Toggle button switches between views and preference is remembered (localStorage)
- [ ] Empty library shows an empty state message instead of a blank page

**2.2 - Book detail page**
- [ ] Navigating to `/library/:id` with a valid book ID renders that book's details
- [ ] Cover image displays (or placeholder if no cover URL)
- [ ] All fields (title, authors, description, status, rating, notes) are visible

**2.3 - Add book manually form**
- [ ] Form has inputs for: ISBN, title, authors, description, status
- [ ] Submitting with an empty title shows a validation error
- [ ] On valid submit, book is saved to IndexedDB and user is redirected to the new book's detail page
- [ ] New book appears in the library list immediately

**2.4 - Edit book form**
- [ ] Editing a book pre-fills all fields with current values from IndexedDB
- [ ] Saving updates the record in IndexedDB (`db.books.update()`)
- [ ] Changes are visible on the detail page after save without a full page refresh

**2.5 - Delete book with confirmation**
- [ ] Clicking delete opens a modal/dialog asking "Are you sure?"
- [ ] Confirming deletion removes the book from IndexedDB and redirects to `/library`
- [ ] Cancelling the dialog leaves the book intact

**2.6 - Reading status selector**
- [ ] Status selector shows 3 options: "Want to Read", "Reading", "Read"
- [ ] Selecting a status immediately updates the book in IndexedDB
- [ ] Status is reflected in the library list view (e.g. badge or icon)

**2.7 - Star rating component**
- [ ] Clicking a star sets the rating (1–5) and saves to IndexedDB
- [ ] Hovering over stars shows a preview of the would-be rating
- [ ] Current rating is visually highlighted on page load
- [ ] Rating of 0 (unrated) is the default and renders as empty stars

**2.8 - Personal notes textarea**
- [ ] Notes textarea is editable on the detail page
- [ ] Notes save to IndexedDB on blur or via an explicit "Save" button
- [ ] Saved notes persist after a page refresh

**2.9 - Library search and filter**
- [ ] Typing in the search bar filters books by title or author in real time (no submit needed)
- [ ] Status filter chips ("All", "Want to Read", "Reading", "Read") filter the list
- [ ] Search and status filter can be combined (e.g. "show only 'Read' books matching 'Tolkien'")
- [ ] Clearing search/filters restores the full library list

**2.10 - Library statistics view**
- [ ] Stats are calculated from live IndexedDB data (not hardcoded)
- [ ] Displays: total books, number in each status category
- [ ] Displays books marked as "Read" grouped by year (based on `dateRead`)
- [ ] Page renders without error when library is empty

---

## Epic 3: Open Library Integration (NOT STARTED)

A service layer for the Open Library API that powers both the search page and the post-scan lookup. Handles metadata fetching, cover image URL construction, and graceful degradation when data is missing.

### Acceptance Criteria

- [ ] Search by title or author returns results from Open Library
- [ ] ISBN lookup returns full book metadata
- [ ] Cover images load from Open Library CDN
- [ ] Missing data (no cover, no description) is handled gracefully with UI fallbacks
- [ ] A book found via search or ISBN lookup can be added to the local library in one tap

### Tasks

| ID | Title | Description | Priority | Complexity | Depends On | Status |
|----|-------|-------------|----------|------------|------------|--------|
| 3.1 | Open Library API service module | Create `src/services/openLibrary.ts` with functions: `searchBooks(query)`, `lookupByISBN(isbn)`, `getCoverUrl(coverId)` | High | M | 1.1 | <!-- vk: --> |
| 3.2 | Book search page UI | Search input + results list on `/search` route; calls `searchBooks` on submit | High | M | 3.1, 1.5 | <!-- vk: --> |
| 3.3 | Search result card component | Card showing cover thumbnail, title, authors, publication year, and "Add to Library" button | High | S | 3.2 | <!-- vk: --> |
| 3.4 | "Add to Library" from search result | On click: open status selector sheet, then save to IndexedDB and confirm to user | High | M | 3.3, 1.4 | <!-- vk: --> |
| 3.5 | ISBN direct lookup function | `lookupByISBN(isbn)` fetches `/api/books?bibkeys=ISBN:...` and maps response to internal `Book` type | High | M | 3.1 | <!-- vk: --> |
| 3.6 | Graceful missing-data handling | Show placeholder cover SVG if no cover; show "No description available" if empty; allow manual field editing before save | Medium | S | 3.4 | <!-- vk: --> |

### Task Details

**3.1 - Open Library API service module**
- [ ] `searchBooks(query: string)` fetches `https://openlibrary.org/search.json?q=...` and returns a typed array of results
- [ ] `lookupByISBN(isbn: string)` fetches the ISBN API endpoint and returns a mapped `Book` object or `null`
- [ ] `getCoverUrl(coverId: number, size: 'S'|'M'|'L')` returns the correct Open Library cover CDN URL
- [ ] All functions handle network errors and return empty results (no unhandled rejections)

**3.2 - Book search page UI**
- [ ] Search input is focused on page load
- [ ] Submitting the form (Enter or button click) triggers `searchBooks` and shows results
- [ ] A loading spinner shows while the API request is in flight
- [ ] Empty results show a "No books found" message

**3.3 - Search result card component**
- [ ] Card displays: cover image (or placeholder), title, authors joined by comma, first publish year
- [ ] "Add to Library" button is visible and tappable on mobile
- [ ] Card is keyboard-accessible (focusable, Enter triggers Add)

**3.4 - "Add to Library" from search result**
- [ ] Tapping "Add to Library" opens a bottom sheet with status selector (Want to Read / Reading / Read)
- [ ] Confirming saves the book to IndexedDB with all available metadata from the API response
- [ ] A success toast/snackbar confirms the book was added
- [ ] Duplicate ISBN check: if book already exists in library, show "Already in library" instead

**3.5 - ISBN direct lookup function**
- [ ] `lookupByISBN('9780140328721')` returns an object with title, authors, description, coverId
- [ ] Returns `null` for ISBNs not found in Open Library (does not throw)
- [ ] Response is mapped to the app's internal `Book` TypeScript interface

**3.6 - Graceful missing-data handling**
- [ ] If `coverId` is absent, a grey placeholder SVG with a book icon renders in place of the cover
- [ ] If `description` is absent, "No description available" is shown in the description field
- [ ] Before adding a book, user can tap any field to edit it manually (inline editing or edit form)

---

## Epic 4: ISBN Barcode Scanner (NOT STARTED)

Real-time ISBN-13 barcode detection from the device camera using ZXing-js. Includes camera access, scanning UI, successful detection flow, and manual fallback.

### Acceptance Criteria

- [ ] Scanner page opens the rear camera on mobile and any available camera on desktop
- [ ] Scanning a book's ISBN-13 barcode successfully detects the code within 5 seconds under normal lighting
- [ ] Detected ISBN triggers Open Library lookup and shows a book preview card
- [ ] User can add the detected book to their library from the scanner page
- [ ] Manual ISBN entry works as a fallback when camera is unavailable or denied

### Tasks

| ID | Title | Description | Priority | Complexity | Depends On | Status |
|----|-------|-------------|----------|------------|------------|--------|
| 4.1 | Integrate ZXing-js library | Install `@zxing/library`; create `src/services/scanner.ts` wrapping `BrowserMultiFormatReader` | High | M | 1.1 | <!-- vk: --> |
| 4.2 | Camera access component | `<CameraFeed>` component: calls `getUserMedia`, prefers `facingMode: environment` on mobile, renders `<video>` | High | M | 4.1 | <!-- vk: --> |
| 4.3 | Scanner UI (overlay + feedback) | Scanner page with video feed, rectangular scanning overlay, and success flash animation | High | M | 4.2, 1.5 | <!-- vk: --> |
| 4.4 | Barcode detection loop | Continuous decode loop on video frames using ZXing; extracts ISBN string on detection | High | M | 4.2 | <!-- vk: --> |
| 4.5 | Post-detection flow | On ISBN detected: pause scanner, call `lookupByISBN`, show book preview card (or "not found" state) | High | M | 4.4, 3.5 | <!-- vk: --> |
| 4.6 | Book preview card after scan | Card showing fetched book details with "Add to Library" and "Scan Again" CTAs | High | M | 4.5, 3.4 | <!-- vk: --> |
| 4.7 | Manual ISBN entry fallback | Text input + "Look Up" button on scanner page; triggers same `lookupByISBN` flow | High | S | 3.5 | <!-- vk: --> |
| 4.8 | Camera permission denied handling | Detect `NotAllowedError`; show instructions to enable camera + offer manual entry fallback | High | S | 4.2 | <!-- vk: --> |

### Task Details

**4.1 - Integrate ZXing-js library**
- [ ] `@zxing/library` is listed in `package.json` dependencies
- [ ] `src/services/scanner.ts` exports a `createScanner()` function returning a configured `BrowserMultiFormatReader`
- [ ] Scanner is configured to detect `EAN_13` format (ISBN-13 barcodes)

**4.2 - Camera access component**
- [ ] `<CameraFeed ref={videoRef} />` renders a `<video>` element with `autoPlay` and `playsInline`
- [ ] On mobile, `getUserMedia` requests `{ video: { facingMode: 'environment' } }` (rear camera)
- [ ] On desktop, uses default camera device
- [ ] Component cleans up the media stream on unmount (`stream.getTracks().forEach(t => t.stop())`)

**4.3 - Scanner UI (overlay + feedback)**
- [ ] Scanner page fills the full viewport with the video feed as background
- [ ] A semi-transparent overlay with a rectangular cut-out indicates the scan area
- [ ] On successful detection, the overlay flashes green briefly before transitioning to the result card
- [ ] A "Cancel" or back button stops the scanner and navigates away

**4.4 - Barcode detection loop**
- [ ] ZXing `decodeFromVideoElement` runs continuously while the scanner is active
- [ ] On first successful decode, the raw barcode string is returned/emitted
- [ ] Loop stops automatically after a successful decode (no duplicate triggers)
- [ ] Loop is cleaned up when the component unmounts

**4.5 - Post-detection flow**
- [ ] After decode, `lookupByISBN(isbn)` is called and a loading state is shown
- [ ] If book found: transitions to book preview card
- [ ] If book not found: shows "Book not found in Open Library" with option to add manually or scan again
- [ ] Detected ISBN is shown to the user (for verification)

**4.6 - Book preview card after scan**
- [ ] Card shows: cover image, title, authors, year, description snippet
- [ ] "Add to Library" button triggers the same status-selector → save flow as Epic 3
- [ ] "Scan Again" button resets the scanner to active detection mode
- [ ] Card is scrollable if content overflows on small screens

**4.7 - Manual ISBN entry fallback**
- [ ] A text input accepts numeric ISBN input (13 digits)
- [ ] "Look Up" button is disabled until input is 10 or 13 characters
- [ ] Submitting triggers `lookupByISBN` and shows the same preview card as a camera scan
- [ ] Input is visible without camera being active (e.g. user denied camera permission)

**4.8 - Camera permission denied handling**
- [ ] `getUserMedia` rejection with `NotAllowedError` is caught without crashing
- [ ] User sees a clear message: "Camera access denied. Enable it in browser settings to scan."
- [ ] Manual ISBN entry input is automatically shown when camera is unavailable
- [ ] No error appears in the console (graceful handling)

---

## Epic 5: PWA & UX Polish (NOT STARTED)

Make the app installable as a PWA, ensure it works offline, add dark mode, improve perceived performance with skeletons and empty states, and implement library export.

### Acceptance Criteria

- [ ] App can be installed to home screen on Android Chrome and iOS Safari
- [ ] Library page loads and works with no internet connection
- [ ] Dark mode matches system preference on first load and can be toggled manually
- [ ] All API-loading states show skeletons instead of blank areas
- [ ] Export produces a valid JSON or CSV file containing all books
- [ ] App passes Lighthouse PWA audit with score ≥ 90

### Tasks

| ID | Title | Description | Priority | Complexity | Depends On | Status |
|----|-------|-------------|----------|------------|------------|--------|
| 5.1 | Web App Manifest | Create `manifest.json`: name "Boontory", icons (192×192, 512×512), `display: standalone`, theme color | High | S | 1.1 | <!-- vk: --> |
| 5.2 | Vite PWA plugin + service worker | Install `vite-plugin-pwa`; configure Workbox to cache app shell and static assets | High | M | 5.1 | <!-- vk: --> |
| 5.3 | Offline support for library | Ensure library reads from IndexedDB when offline; covers served from cache; no broken UI when offline | High | M | 5.2, 2.1 | <!-- vk: --> |
| 5.4 | Dark/light mode toggle | Detect `prefers-color-scheme`; toggle button in header; persist preference to localStorage | Medium | M | 1.5 | <!-- vk: --> |
| 5.5 | Empty states | Design and add empty state illustrations/messages for: empty library, no search results, book not found | Medium | S | 2.1, 3.2 | <!-- vk: --> |
| 5.6 | Loading skeletons | Skeleton components for book list cards and search results; shown during API/DB fetch | Medium | S | 2.1, 3.2 | <!-- vk: --> |
| 5.7 | Export library to JSON/CSV | Button in settings or library page; serialises all IndexedDB books to JSON and CSV download | Low | M | 1.4, 2.1 | <!-- vk: --> |
| 5.8 | Cross-browser and device testing | Manual test on: Chrome Android, iOS Safari, desktop Chrome, desktop Firefox; fix any layout/API issues | Medium | M | 5.1, 5.2, 5.3 | <!-- vk: --> |

### Task Details

**5.1 - Web App Manifest**
- [ ] `public/manifest.json` is linked in `index.html` via `<link rel="manifest">`
- [ ] Manifest includes `name`, `short_name`, `icons` (192 and 512px PNG), `display: "standalone"`, `background_color`, `theme_color`
- [ ] Chrome DevTools → Application → Manifest shows no errors

**5.2 - Vite PWA plugin + service worker**
- [ ] `vite-plugin-pwa` is installed and configured in `vite.config.ts`
- [ ] After `npm run build`, a service worker file is present in `dist/`
- [ ] App shell (HTML, JS, CSS) is precached by Workbox
- [ ] Chrome DevTools → Application → Service Workers shows the SW as "activated and running"

**5.3 - Offline support for library**
- [ ] With DevTools → Network set to "Offline", the library page loads and displays books
- [ ] Book cover images already viewed load from cache when offline
- [ ] A subtle "You are offline" banner appears when network is unavailable
- [ ] Attempting Open Library search while offline shows a "No internet connection" error gracefully

**5.4 - Dark/light mode toggle**
- [ ] On first load, app theme matches `window.matchMedia('(prefers-color-scheme: dark)')` result
- [ ] Toggle button in the header switches between dark and light mode instantly
- [ ] Theme preference is saved to localStorage and restored on next visit
- [ ] All pages (library, scan, search, detail) are readable in both modes

**5.5 - Empty states**
- [ ] Empty library renders an illustration/icon + "Your library is empty. Scan or search for a book to add one."
- [ ] No search results renders "No books found for '[query]'. Try different keywords."
- [ ] Book not found after ISBN scan renders "This book wasn't found in Open Library. Add it manually."

**5.6 - Loading skeletons**
- [ ] Book list shows grey animated skeleton cards while IndexedDB is loading
- [ ] Search results show skeleton cards while the Open Library API request is in flight
- [ ] Skeletons match the approximate shape/size of the real content cards
- [ ] Skeletons disappear and are replaced by real content once data loads

**5.7 - Export library to JSON/CSV**
- [ ] Export button triggers a browser file download without opening a new tab
- [ ] JSON export contains an array of all books with all fields
- [ ] CSV export contains one header row and one row per book, with correct column names
- [ ] Export works on mobile browsers (uses `<a download>` pattern)

**5.8 - Cross-browser and device testing**
- [ ] Library CRUD (add, edit, delete) works on Chrome Android 120+ and iOS Safari 17+
- [ ] Camera scanner works on Chrome Android and shows the manual fallback on iOS (if `getUserMedia` is restricted)
- [ ] Dark mode renders correctly on all tested browsers
- [ ] No console errors on any tested platform

---

## Dependencies

- `react` + `react-dom` v18
- `react-router-dom` v6
- `typescript`
- `vite` + `vite-plugin-pwa`
- `tailwindcss` + `postcss` + `autoprefixer`
- `dexie` (IndexedDB ORM)
- `@zxing/library` (barcode scanning)
- Open Library API — `https://openlibrary.org` (free, no key, CORS-enabled)

## Out of Scope

- User authentication / multi-user support
- Backend server or cloud database
- Social features (sharing, recommendations)
- Goodreads or other third-party library sync
- Push notifications
- In-app purchases or monetisation

## Open Questions

- [ ] Should the app support ISBN-10 in addition to ISBN-13?
- [ ] Should cover images be cached locally (base64 in IndexedDB) or always loaded from Open Library URLs?
- [ ] Is there a preferred icon/brand style for the app?
- [ ] Should reading status changes log a timestamp (e.g. `dateStarted`, `dateFinished`)?
- [ ] Should there be a "Lent to" field to track books lent to friends?

## Related Documents

| Document | Purpose | Status |
|----------|---------|--------|
| docs/development-plan.md | Development Plan | Current |

---

## Changelog

- **2026-03-16**: Initial development plan created from project description
