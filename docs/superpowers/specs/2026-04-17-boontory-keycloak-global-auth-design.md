# Boontory Keycloak + Global Auth Design

## Context

Boontory lives at `/home/ventura/Documents/boontory` as a small monorepo:

- `frontend/` — Vue 3 + TypeScript SPA
- `backend/` — Kotlin + Spring Boot REST API with SQLite persistence
- `Dockerfile` — current live Coolify deployment path that builds the SPA into Spring Boot static assets
- `docker-compose.coolify.yml` — older split-service deploy artifact still present in the repo

Current deployment exposes `boontory.etharlia.com` through Coolify. Current repo docs already note authentication is the missing step before broader exposure.

User goal for this slice:

1. Add centralized user management through Keycloak
2. Implement Boontory-side auth in code
3. Configure required pieces in Keycloak and Coolify
4. Make login through Keycloak mandatory for all current and future `*.etharlia.com` subdomains
5. Execute work in parallel lanes where independent

## Goals

- Enforce login for all public `*.etharlia.com` subdomains before app access
- Integrate real OIDC login inside Boontory frontend
- Protect Boontory backend APIs with JWT validation
- Centralize identity, redirect rules, logout flow, and role mapping in Keycloak
- Leave a reusable pattern for future Etharlia apps

## Non-Goals

- Building fine-grained multi-tenant authorization
- Replacing Coolify's own dashboard auth
- Migrating every app to deep in-app OIDC on day one
- Designing per-user sharing, invitations, or self-service account management

## Chosen Architecture

Use a hybrid model:

1. **Global edge auth** at the proxy layer with `Traefik -> oauth2-proxy -> Keycloak`
2. **Native app auth** inside Boontory with Vue OIDC login plus Spring Security JWT validation

Why this model:

- Proxy auth solves the global requirement: every public subdomain must pass Keycloak login first
- Native Boontory auth solves user awareness inside the app and API, not just edge gating
- One realm plus separate clients per app keeps redirect rules tight and scalable

## Domain Topology

- `auth.etharlia.com` — existing Keycloak public host
- `oauth.etharlia.com` — oauth2-proxy callback and auth entrypoint
- `boontory.etharlia.com` — Boontory frontend public domain
- `*.etharlia.com` — all other public apps protected through the same forward-auth middleware

Requirements:

- Wildcard DNS `*.etharlia.com` points to the Coolify server
- If wildcard TLS is needed for catch-all routing, manage wildcard certs explicitly
- Public apps must go through Traefik, not direct host-port exposure

## Keycloak Design

Create one realm:

- Realm: `etharlia`

Clients:

1. `oauth2-proxy`
   - Type: confidential
   - Redirect URI: `https://oauth.etharlia.com/oauth2/callback`
   - Purpose: browser SSO session at the proxy layer

2. `boontory-frontend`
   - Type: public
   - Flow: Authorization Code + PKCE
   - Redirect URIs:
     - `https://boontory.etharlia.com/*`
     - `http://localhost:5173/*`
     - `http://127.0.0.1:5173/*`
   - Web origins:
     - `https://boontory.etharlia.com`
     - `http://localhost:5173`
     - `http://127.0.0.1:5173`
   - Post logout redirect URIs:
     - `https://boontory.etharlia.com/*`

3. Future apps
   - One client per app/subdomain
   - Same realm, explicit redirect URIs, explicit origins

Roles:

- `user`
- `admin` (optional now, ready for future admin-only pages)

Role mapping target for Boontory backend:

- realm roles from `realm_access.roles`
- client roles from `resource_access.<client>.roles`

## Global Auth Gateway Design

Deploy `oauth2-proxy` in Coolify as a dedicated service.

Recommended settings:

- provider: OIDC / Keycloak
- auth host: `oauth.etharlia.com`
- cookie domain: `.etharlia.com`
- cookie secure: true
- reverse proxy: true
- whitelist domain: `.etharlia.com`
- upstream: `static://202` so oauth2-proxy can serve forward-auth checks without app upstream coupling

Traefik pattern:

- Use `forwardAuth` middleware against oauth2-proxy
- Attach middleware to each public router/app in Coolify
- Treat this middleware as mandatory for all public `*.etharlia.com` apps

Operational rule:

- No public subdomain is considered production-ready unless its router has the shared auth middleware attached

## Boontory Frontend Design

Boontory frontend keeps real app-side identity handling.

New responsibilities:

- bootstrap Keycloak on app startup
- use `login-required` so unauthenticated sessions redirect immediately
- hold tokens in memory, not localStorage
- refresh access token before expiry
- expose login/logout/auth-ready state to the app shell
- add bearer token to API requests

Target files:

- `frontend/src/main.ts`
- `frontend/src/router/index.ts`
- `frontend/src/services/api.ts`
- new auth support module(s) under `frontend/src/`

Behavior:

- App does not render protected content until auth init completes
- All existing routes remain protected
- Logout triggers Boontory logout flow and then Keycloak logout redirect

## Boontory Backend Design

Boontory backend becomes a stateless resource server.

Add Spring Security with:

- `spring-boot-starter-oauth2-resource-server`
- JWT validation via Keycloak issuer URI
- authenticated access required for `/api/**`
- custom role converter for Keycloak claims

Target files:

- `backend/build.gradle.kts`
- `backend/src/main/resources/application.yml`
- new Spring Security config under `backend/src/main/kotlin/com/boontory/backend/config/`
- existing controllers remain protected by global security rules

Health:

- Add `/actuator/health` or equivalent unauthenticated health endpoint only if Coolify needs it
- Keep business APIs authenticated

## CORS and API Boundary

Current backend already uses origin-pattern CORS for `/api/**`.

Desired posture:

- Allow only explicit trusted origins for local dev and real public frontend origins
- Keep API relative behind the same public origin for normal Boontory browser usage
- Support bearer-token API access from the authenticated SPA

## Coolify Design

Boontory:

- keep the current live deployment as one Coolify dockerfile app on `https://boontory.etharlia.com`
- pass Keycloak values into the frontend build stage through docker build args
- keep backend runtime env for issuer URI, allowed origins, and SQLite path
- keep SQLite persistent storage on the Boontory app

Global platform:

- deploy oauth2-proxy as a separate Coolify application on `https://oauth.etharlia.com`
- attach shared forward-auth middleware to Boontory and other public apps
- avoid direct host-port exposure for apps meant to be protected at the edge

## Environment and Secrets

Frontend env:

- Keycloak base URL
- realm
- client ID

Backend env:

- issuer URI: `https://auth.etharlia.com/realms/etharlia`
- allowed frontend origin patterns for local dev plus `https://boontory.etharlia.com`

oauth2-proxy env:

- OIDC issuer URL
- client ID
- client secret
- cookie secret
- redirect URL
- cookie domain

Secrets must live in Coolify / Keycloak, not in tracked source files.

## Error Handling

- Unauthenticated browser hit -> redirect to Keycloak through oauth2-proxy
- Expired frontend token -> refresh attempt first, then re-login if refresh fails
- Missing or invalid API token -> backend returns `401`
- Valid token without role -> backend returns `403`
- oauth2-proxy unavailable -> protected routes fail closed
- Keycloak unavailable -> new login fails; existing sessions continue only until their session/token limits expire

## Testing Strategy

Follow TDD during implementation.

Frontend tests:

- route guard requires authenticated session
- auth bootstrap blocks render until ready
- API client attaches bearer token
- logout clears local auth state

Backend tests:

- `/api/**` returns `401` without token
- valid JWT reaches protected controllers
- invalid JWT returns `401`
- Keycloak role claims map into Spring authorities
- allowed and denied CORS origins behave correctly

Manual platform checks:

- login once on `boontory.etharlia.com`
- open another protected `*.etharlia.com` app without re-login
- verify logout clears access across protected hosts
- verify container restart does not break auth config or SQLite persistence

## Parallel Execution Plan

After spec approval, split work into independent lanes:

1. **Keycloak lane**
   - realm, clients, redirects, roles

2. **Global proxy lane**
   - oauth2-proxy, Coolify config, shared forward-auth middleware

3. **Boontory frontend lane**
   - OIDC bootstrap, router guards, token-aware API client

4. **Boontory backend lane**
   - Spring Security, JWT validation, role mapping, tests

5. **Docs and ops lane**
   - README, deploy docs, environment variables, rollout checklist

These lanes can run mostly in parallel once the three hostnames are fixed as `auth.etharlia.com`, `oauth.etharlia.com`, and `boontory.etharlia.com`.

## Risks and Mitigations

### Logout complexity
Risk: browser session, oauth2-proxy session, and Keycloak session can drift.

Mitigation:

- define one canonical logout path
- test logout across multiple subdomains before rollout complete

### Middleware bypass
Risk: an app exposed by host port or router without middleware bypasses auth.

Mitigation:

- enforce a platform checklist for every public app
- avoid direct public host-port exposure

### Wildcard TLS / routing edge cases
Risk: catch-all routing or new subdomains fail due to cert or router config.

Mitigation:

- prefer explicit app domains first
- use wildcard DNS and wildcard TLS deliberately, not implicitly

### Token/session mismatch
Risk: edge session valid while app token expired, causing confusing behavior.

Mitigation:

- frontend keeps its own refresh logic
- handle 401 centrally and force clean re-login

## Rollout Order

1. Configure Keycloak realm and clients
2. Deploy oauth2-proxy and shared Traefik forward-auth middleware in Coolify
3. Integrate Boontory frontend auth
4. Integrate Boontory backend JWT security
5. Verify Boontory end-to-end
6. Attach shared middleware to remaining public `*.etharlia.com` apps
7. Document the onboarding checklist for future subdomains

## Final Decision Summary

- **Realm model:** one realm for Etharlia
- **Edge auth:** oauth2-proxy + Traefik forwardAuth + Keycloak
- **Boontory frontend:** public OIDC client with PKCE and mandatory login
- **Boontory backend:** stateless JWT resource server
- **Platform rule:** all public `*.etharlia.com` hosts must sit behind the shared auth gateway
