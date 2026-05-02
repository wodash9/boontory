# Boontory MCP Package

This package provides two standalone stdio MCP servers:

- `coolify`: read and lifecycle tools for Coolify applications and services
- `keycloak`: read and controlled mutation tools for Keycloak realms, users, groups, and realm roles

## Requirements

- Node.js 18+
- A Coolify API token for the Coolify server
- A Keycloak confidential client with service-account access for the Keycloak server

## Install

```bash
cd mcp
npm install
```

## Environment files

Copy one of the example files and fill in real values:

- `.env.coolify.example`
- `.env.keycloak.example`

The package intentionally ignores real `.env*` files while keeping the examples tracked.

## Scripts

```bash
npm run dev:coolify
npm run dev:keycloak
npm run typecheck
npm test
npm run build
npm run start:coolify
npm run start:keycloak
```

## Coolify server

Required environment variables:

- `COOLIFY_BASE_URL`: base URL for your Coolify instance, for example `https://coolify.example.com`
- `COOLIFY_TOKEN`: API token used as a Bearer token against `/api/v1`

Available tools:

- `coolify_list_applications`
- `coolify_get_application`
- `coolify_start_application`
- `coolify_stop_application`
- `coolify_restart_application`
- `coolify_list_services`
- `coolify_get_service`
- `coolify_start_service`
- `coolify_stop_service`
- `coolify_restart_service`

## Keycloak server

Required environment variables:

- `KEYCLOAK_BASE_URL`: base URL for Keycloak, for example `https://sso.example.com`
- `KEYCLOAK_AUTH_REALM`: realm used for client-credentials authentication
- `KEYCLOAK_CLIENT_ID`: confidential client ID
- `KEYCLOAK_CLIENT_SECRET`: confidential client secret

Optional environment variables:

- `KEYCLOAK_DEFAULT_REALM`: default target realm for realm-scoped operations

Available tools:

- `keycloak_list_realms`
- `keycloak_list_users`
- `keycloak_list_groups`
- `keycloak_list_realm_roles`
- `keycloak_create_user`
- `keycloak_add_user_to_group`
- `keycloak_assign_realm_roles`

All mutating Keycloak tools require `confirm: true`.

## Notes

- Both servers use stdio transport only for V1.
- All logs go to stderr so stdout remains clean for MCP JSON-RPC traffic.
- Tool responses are text-first and human-readable.
