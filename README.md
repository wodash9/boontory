# Boontory

Boontory is a personal book library MVP built as a small monorepo:

- `frontend/`: Vue 3 + TypeScript + Vue Router client
- `backend/`: Kotlin + Spring Boot REST API with SQLite persistence
- `Dockerfile` (repo root): single-container build path used by live Coolify app

## MVP scope

- Public landing page at `/landing` for positioning and acquisition
- Scan ISBN-13 barcodes with the device camera
- Look up book metadata through Open Library
- Add, edit, delete, search, and filter books
- Track reading status, rating, notes, and read date
- Store data in SQLite on the backend

## Local development

### Backend

```bash
cd backend
./gradlew bootRun
```

The API runs on `http://localhost:8080`.

### Frontend

```bash
cd frontend
npm install
npm run dev
```

The Vite dev server runs on `http://localhost:5173` and proxies `/api` to the backend.

## Production build

```bash
docker build \
  --build-arg VITE_KEYCLOAK_URL=https://auth.etharlia.com \
  --build-arg VITE_KEYCLOAK_REALM=etharlia \
  --build-arg VITE_KEYCLOAK_CLIENT_ID=boontory-frontend \
  --build-arg VITE_SSO_LOGOUT_URL=https://oauth.etharlia.com/oauth2/sign_out \
  -t boontory-auth .
```

## Authentication

This is the target auth rollout for Boontory:

1. `oauth2-proxy` on `https://oauth.etharlia.com` protects public `*.etharlia.com` hosts at the Traefik edge.
2. The Vue app uses `keycloak-js` with the `boontory-frontend` public client so API calls carry a bearer token once the app auth slice is deployed.

### Frontend env

Use `frontend/.env.example` for local development. The production Docker build reads:

- `VITE_KEYCLOAK_URL`
- `VITE_KEYCLOAK_REALM`
- `VITE_KEYCLOAK_CLIENT_ID`
- `VITE_SSO_LOGOUT_URL` (oauth2-proxy sign_out base URL)

### Backend env

Use `backend/.env.example` for local development. The backend runtime reads:

- `KEYCLOAK_ISSUER_URI`
- `FRONTEND_ORIGIN_PATTERNS`
- `BOONTORY_DB_PATH`

### Logout behavior (target state)

- Frontend uses `VITE_SSO_LOGOUT_URL` (`https://oauth.etharlia.com/oauth2/sign_out`) as the oauth2-proxy sign_out base URL.
- Chained logout: hit oauth2-proxy `sign_out`, redirect to Keycloak logout, then return to app post-logout URI.

## Notes

- SQLite data is stored at `backend/data/boontory.db` locally or `/app/data/boontory.db` in containers.
- The scanner uses ZXing in the browser and requires camera permission.
- Open Library requests are made through the backend so the frontend stays simple and the API contract remains stable.
