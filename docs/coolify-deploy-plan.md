# Coolify Deploy Plan

## Deployment model

Deploy Boontory as a Docker Compose application in Coolify with two services:

1. `frontend`: Nginx container serving the built Vue app and reverse-proxying `/api` to the backend
2. `backend`: Spring Boot container exposing the REST API and writing SQLite data to a persistent volume

This keeps the public entrypoint simple and avoids CORS issues in production.

## Files used

- `docker-compose.coolify.yml`
- `frontend/Dockerfile`
- `frontend/docker/nginx.conf`
- `backend/Dockerfile`

## Coolify steps

1. Create a new project in Coolify and choose `Docker Compose`.
2. Point it at this repository and set the compose file to `docker-compose.coolify.yml`.
3. Expose only the `frontend` service publicly.
4. Add a persistent volume for the `backend-data` volume managed by Compose.
5. Set `FRONTEND_ORIGIN` on the backend to your final public domain, for example `https://books.example.com`.
6. Deploy once and verify:
   - `GET /api/books` returns `200`
   - the frontend loads from the public domain
   - adding a book creates or updates `/app/data/boontory.db`

## Environment variables

Backend:

- `SERVER_PORT=8080`
- `BOONTORY_DB_PATH=/app/data/boontory.db`
- `FRONTEND_ORIGIN=https://your-domain.example`

Frontend:

- no extra runtime variables are required in the containerized setup because Nginx proxies `/api`

## Post-deploy checks

1. Open the site on mobile and confirm camera permission prompts appear on the scan page.
2. Scan a valid ISBN-13 barcode and verify Open Library lookup returns a preview.
3. Add, edit, and delete a book to confirm SQLite persistence survives container restarts.
4. Review Coolify health, logs, and storage mapping before promoting the deployment.

## Recommended follow-up

- Add a small `/actuator/health` endpoint if you want first-class health checks in Coolify.
- Add authentication before exposing the app beyond a single trusted user.
- Split the frontend and backend into separate Coolify apps only if you later need independent scaling or deployments.
