# Coolify Deploy Plan

## Deployment model

Deploy Boontory through the root `Dockerfile` as a single Coolify application:

1. Vue is built in the `frontend-build` stage.
2. The compiled SPA is copied into Spring Boot static resources.
3. The final app serves the SPA and `/api/**` from the same container on port `8080`.

The live Coolify application is `p7naj0uvggxjs36fifqgsyuh` with FQDN `https://boontory.etharlia.com`.

This is the target rollout once the auth code lands in the Boontory app.

## Auth rollout

- Keep Keycloak at `https://auth.etharlia.com`.
- Import/apply `ops/keycloak/etharlia-boontory-clients.json` in Keycloak before deploying oauth2-proxy.
- Create a new oauth2-proxy app on `https://oauth.etharlia.com` using `quay.io/oauth2-proxy/oauth2-proxy:v7.15.1`.
- Set `OAUTH2_PROXY_CLIENT_ID`, `OAUTH2_PROXY_CLIENT_SECRET`, and `OAUTH2_PROXY_COOKIE_SECRET` in Coolify for the oauth2-proxy app.
- `OAUTH2_PROXY_CLIENT_SECRET` must come from the Keycloak `oauth2-proxy` confidential client credentials.
- `OAUTH2_PROXY_COOKIE_SECRET` must be a 32-byte base64 value.
- Add the labels from `ops/coolify/boontory-forward-auth.labels` to the Boontory app custom labels, replacing `<ROUTER_ID>` with the live router id.
- Set build env on the Boontory app for `VITE_KEYCLOAK_URL`, `VITE_KEYCLOAK_REALM`, `VITE_KEYCLOAK_CLIENT_ID`, and `VITE_SSO_LOGOUT_URL` (`https://oauth.etharlia.com/oauth2/sign_out`).
- Set runtime env on the Boontory app for `KEYCLOAK_ISSUER_URI`, `FRONTEND_ORIGIN_PATTERNS`, and `BOONTORY_DB_PATH`.
- Verify `https://oauth.etharlia.com/ping` returns `200`, unauthenticated Boontory requests redirect or fail closed, and remove the forward-auth labels to roll back if deployment fails.
- Chained logout flow: frontend uses `VITE_SSO_LOGOUT_URL` to call oauth2-proxy `sign_out`, oauth2-proxy redirects to Keycloak logout, and Keycloak redirects back to the configured post-logout URI.
