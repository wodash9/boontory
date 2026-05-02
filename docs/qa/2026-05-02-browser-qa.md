# Boontory browser QA — 2026-05-02

## Veredicto

PASS funcional local con bypass seguro de QA.

## Entorno

- Backend local: `BOONTORY_DISABLE_AUTH=true`, SQLite temporal, `SERVER_PORT=18080`.
- Frontend local: `VITE_AUTH_MODE=mock`, `VITE_AUTH_MOCK_USERNAME=boontory-qa`, Vite en `127.0.0.1:5179`.
- Browser: Google Chrome headless con DevTools Protocol.
- Evidencias locales: `/tmp/boontory-qa/*.png` y `/tmp/boontory-qa/report.json`.

## Checks ejecutados

- Mock-auth dashboard access: PASS.
- Shelves create: PASS.
- Shelves edit: PASS.
- Manual book create + detail: PASS.
- Book shelf assignment: PASS.
- Book edit: PASS.
- Library list/search filter: PASS.
- Catalog search + add from Open Library: PASS.
- Scan/manual ISBN lookup + add: PASS.
- Dashboard stats after adds: PASS.
- Book delete: PASS.
- Shelf delete: PASS.
- Landing responsive at 390px: PASS.
- App library responsive at 390px: PASS.
- Browser console after final QA run: 0 blocking errors/warnings captured by the QA script.

## Bug encontrado y corregido

DELETE endpoints returned `200 OK` with an empty body. The frontend API client treats `204 No Content` as the empty-response case and otherwise parses JSON. This caused deletes to happen server-side while the UI could remain stale after a JSON parse failure.

Fix:

- `BookController.delete()` now returns `204 No Content`.
- `ShelfController.delete()` now returns `204 No Content`.
- Backend tests now assert `isNoContent`.

## No cubierto

- Real camera hardware / camera permission flow. Headless QA validated the manual ISBN fallback and catalog lookup, not physical barcode scanning.
- Real Keycloak login in production. QA used explicit local mock-auth mode. Production defaults remain Keycloak/auth enabled.
- Docker image builds, because the local environment cannot access `/var/run/docker.sock`.

## Riesgos operativos

- Never set `BOONTORY_DISABLE_AUTH=true` in production.
- Never build production frontend with `VITE_AUTH_MODE=mock`.
- Install the Keycloak theme before setting `login_theme=boontory` on live clients.
