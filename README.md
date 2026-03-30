# Boontory

Boontory is a personal book library MVP built as a small monorepo:

- `frontend/`: Vue 3 + TypeScript + Vue Router client
- `backend/`: Kotlin + Spring Boot REST API with SQLite persistence
- `docker-compose.coolify.yml`: multi-service deployment entrypoint for Coolify

## MVP scope

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
cd backend
./gradlew build

cd ../frontend
npm run build
```

## Notes

- SQLite data is stored at `backend/data/boontory.db` locally or `/app/data/boontory.db` in containers.
- The scanner uses ZXing in the browser and requires camera permission.
- Open Library requests are made through the backend so the frontend stays simple and the API contract remains stable.
