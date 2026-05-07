# Boontory Keycloak Login Theme

Date: 2026-05-07
Status: prepared in repo, pending live Keycloak installation/application

## What is included

The repo contains a native Keycloak login theme at:

```text
ops/keycloak/themes/boontory/login/
├── template.ftl
├── login.ftl
├── theme.properties
├── messages/messages_en.properties
├── messages/messages_es.properties
└── resources/
    ├── css/login.css
    └── img/
        ├── favicon.svg
        └── logo.svg
```

This is not a fake SPA login. It keeps the Keycloak/OIDC flow intact and customizes the Keycloak presentation layer only.

## Visual direction

The screen mirrors the Boontory app/landing theme:

- warm cream background and amber highlights;
- black rounded `B` mark;
- large editorial headline style;
- mobile-first ISBN scanner product preview;
- private personal-library copy;
- rounded glass card and pill primary button;
- responsive mobile layout;
- `prefers-reduced-motion` support.

## Client setup

A new client is not required if Boontory continues using the existing client:

- Public SPA client: `boontory-frontend`
- Optional edge-auth client: `oauth2-proxy`

Both should have the client attribute:

```json
{
  "attributes": {
    "login_theme": "boontory"
  }
}
```

The repo file `ops/keycloak/etharlia-boontory-clients.json` already includes that attribute. In production, prefer setting only this attribute manually or via targeted admin automation instead of blindly importing the full JSON over an existing realm.

## Ventura action required

1. Install the theme in the live Keycloak container/service.
   - Option A: build/use the theme image from `ops/keycloak/Dockerfile.theme`.
   - Option B: mount/copy `ops/keycloak/themes/boontory` to `/opt/keycloak/themes/boontory`.
2. Restart Keycloak or clear the theme cache so the theme is loaded.
3. In Keycloak Admin Console, set client attribute `login_theme=boontory` on:
   - `boontory-frontend`
   - `oauth2-proxy`, if Boontory is protected through oauth2-proxy before the SPA loads.
4. Verify while logged out:
   - open `https://boontory.etharlia.com`;
   - confirm redirect to Keycloak;
   - confirm Boontory-branded login appears;
   - log in with a safe test user;
   - confirm callback returns to Boontory;
   - log out and confirm re-login still works.

## Do not change

Do not change these just for visual customization:

- authentication flows;
- redirect URIs;
- client secrets;
- token mappers;
- roles;
- production auth-disable or mock-auth flags.

## Rollback

If the theme does not load or breaks rendering:

1. Remove `login_theme=boontory` from the client attribute or set it back to the previous theme.
2. Restart/clear cache if Keycloak still serves cached theme resources.
3. Keep the theme files in the repo; the rollback is a Keycloak admin setting, not a code revert.
