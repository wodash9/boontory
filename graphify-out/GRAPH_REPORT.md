# Graph Report - boontory  (2026-05-16)

## Corpus Check
- 101 files · ~107,082 words
- Verdict: corpus is large enough that graph structure adds value.

## Summary
- 512 nodes · 491 edges · 81 communities (49 shown, 32 thin omitted)
- Extraction: 100% EXTRACTED · 0% INFERRED · 0% AMBIGUOUS
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `7192d865`
- Run `git rev-parse HEAD` and compare to check if the graph is stale.
- Run `graphify update .` after code changes (no API cost).

## Community Hubs (Navigation)
- [[_COMMUNITY_Community 0|Community 0]]
- [[_COMMUNITY_Community 1|Community 1]]
- [[_COMMUNITY_Community 2|Community 2]]
- [[_COMMUNITY_Community 3|Community 3]]
- [[_COMMUNITY_Community 4|Community 4]]
- [[_COMMUNITY_Community 5|Community 5]]
- [[_COMMUNITY_Community 6|Community 6]]
- [[_COMMUNITY_Community 7|Community 7]]
- [[_COMMUNITY_Community 8|Community 8]]
- [[_COMMUNITY_Community 9|Community 9]]
- [[_COMMUNITY_Community 10|Community 10]]
- [[_COMMUNITY_Community 11|Community 11]]
- [[_COMMUNITY_Community 12|Community 12]]
- [[_COMMUNITY_Community 13|Community 13]]
- [[_COMMUNITY_Community 14|Community 14]]
- [[_COMMUNITY_Community 15|Community 15]]
- [[_COMMUNITY_Community 16|Community 16]]
- [[_COMMUNITY_Community 17|Community 17]]
- [[_COMMUNITY_Community 18|Community 18]]
- [[_COMMUNITY_Community 19|Community 19]]
- [[_COMMUNITY_Community 20|Community 20]]
- [[_COMMUNITY_Community 21|Community 21]]
- [[_COMMUNITY_Community 22|Community 22]]
- [[_COMMUNITY_Community 23|Community 23]]
- [[_COMMUNITY_Community 24|Community 24]]
- [[_COMMUNITY_Community 25|Community 25]]
- [[_COMMUNITY_Community 26|Community 26]]
- [[_COMMUNITY_Community 27|Community 27]]
- [[_COMMUNITY_Community 28|Community 28]]
- [[_COMMUNITY_Community 29|Community 29]]
- [[_COMMUNITY_Community 30|Community 30]]
- [[_COMMUNITY_Community 31|Community 31]]
- [[_COMMUNITY_Community 32|Community 32]]
- [[_COMMUNITY_Community 34|Community 34]]
- [[_COMMUNITY_Community 35|Community 35]]
- [[_COMMUNITY_Community 36|Community 36]]
- [[_COMMUNITY_Community 37|Community 37]]
- [[_COMMUNITY_Community 38|Community 38]]
- [[_COMMUNITY_Community 39|Community 39]]
- [[_COMMUNITY_Community 40|Community 40]]
- [[_COMMUNITY_Community 41|Community 41]]
- [[_COMMUNITY_Community 42|Community 42]]
- [[_COMMUNITY_Community 44|Community 44]]
- [[_COMMUNITY_Community 45|Community 45]]
- [[_COMMUNITY_Community 46|Community 46]]
- [[_COMMUNITY_Community 47|Community 47]]
- [[_COMMUNITY_Community 48|Community 48]]
- [[_COMMUNITY_Community 54|Community 54]]
- [[_COMMUNITY_Community 55|Community 55]]

## God Nodes (most connected - your core abstractions)
1. `BneSruCatalogProvider` - 20 edges
2. `Boontory Keycloak + Global Auth Design` - 19 edges
3. `KeycloakService` - 14 edges
4. `Development Plan: Boontory — Personal Book Library Web App` - 14 edges
5. `CoolifyClient` - 13 edges
6. `Task 1: Frontend test harness and auth config` - 13 edges
7. `Task 2: Frontend Keycloak runtime, route guard, and bearer API client` - 13 edges
8. `ApiEndpointsTest` - 11 edges
9. `BookService` - 11 edges
10. `Task 4: Production build args, repo-managed ops artifacts, and docs alignment` - 10 edges

## Surprising Connections (you probably didn't know these)
- `bootstrapAuth()` --calls--> `readAuthConfig()`  [EXTRACTED]
  frontend/src/auth/authState.ts → frontend/src/auth/authConfig.ts
- `bootstrapAuth()` --calls--> `createKeycloakClient()`  [EXTRACTED]
  frontend/src/auth/authState.ts → frontend/src/auth/keycloakClient.ts

## Communities (81 total, 32 thin omitted)

### Community 0 - "Community 0"
Cohesion: 0.09
Nodes (24): AuthRuntimeOptions, AuthSnapshot, bootstrapAuth(), createAuthRuntime(), createMockAuthRuntime(), requireConfigValue(), Runtime, keycloak (+16 more)

### Community 1 - "Community 1"
Cohesion: 0.06
Nodes (32): code:json ({), code:ts (// frontend/tests/router/guards.spec.ts), code:ts (// frontend/tests/services/api.spec.ts), code:ts (// frontend/src/auth/keycloakClient.ts), code:ts (// frontend/src/auth/authState.ts), code:ts (// frontend/src/router/guards.ts), code:vue (<!-- frontend/src/components/auth/AuthGate.vue -->), code:vue (<!-- frontend/src/App.vue -->) (+24 more)

### Community 2 - "Community 2"
Cohesion: 0.07
Nodes (29): Acceptance Criteria, Acceptance Criteria, Acceptance Criteria, Acceptance Criteria, Acceptance Criteria, Changelog, Completion Status Summary, Dependencies (+21 more)

### Community 3 - "Community 3"
Cohesion: 0.12
Nodes (7): KeycloakService, main(), createCoolifyServer(), createCoolifyToolDefinitions(), createKeycloakServer(), createKeycloakToolDefinitions(), runTool()

### Community 4 - "Community 4"
Cohesion: 0.09
Nodes (9): auth, registerLink, wrapper, router, route, closeForm(), deleteShelf(), loadShelves() (+1 more)

### Community 5 - "Community 5"
Cohesion: 0.08
Nodes (23): Boontory Backend Design, Boontory Frontend Design, Boontory Keycloak + Global Auth Design, Chosen Architecture, Context, Coolify Design, CORS and API Boundary, Domain Topology (+15 more)

### Community 8 - "Community 8"
Cohesion: 0.21
Nodes (3): CoolifyClient, normalizeBaseUrl(), parseJsonOrText()

### Community 9 - "Community 9"
Cohesion: 0.13
Nodes (14): Authentication, Backend, Backend env, Boontory, code:bash (cd backend), code:bash (cd frontend), code:bash (docker build \), Frontend (+6 more)

### Community 11 - "Community 11"
Cohesion: 0.18
Nodes (10): Boontory MCP Package, code:bash (cd mcp), code:bash (npm run dev:coolify), Coolify server, Environment files, Install, Keycloak server, Notes (+2 more)

### Community 12 - "Community 12"
Cohesion: 0.2
Nodes (9): Boontory Keycloak Login Theme, Client setup, code:text (ops/keycloak/themes/boontory/login/), code:json ({), Do not change, Rollback, Ventura action required, Visual direction (+1 more)

### Community 13 - "Community 13"
Cohesion: 0.2
Nodes (10): code:env (# frontend/.env.example), code:env (# backend/.env.example), code:json (// ops/keycloak/boontory-clients.json), code:env (# ops/oauth2-proxy/oauth2-proxy.env.example), code:text (# ops/coolify/boontory-forward-auth.labels), code:dockerfile (# Dockerfile), code:md (<!-- README.md: add a new Authentication section after Produ), code:md (<!-- docs/coolify-deploy-plan.md: replace the old model sect) (+2 more)

### Community 15 - "Community 15"
Cohesion: 0.22
Nodes (8): Backend security, Boontory Keycloak + Global Auth Implementation Plan, Deployment and ops artifacts, File Structure, Frontend auth and tests, Live resources to touch during rollout, Spec Coverage Check, Verification Checklist

### Community 20 - "Community 20"
Cohesion: 0.25
Nodes (7): Boontory browser QA — 2026-05-02, Bug encontrado y corregido, Checks ejecutados, Entorno, No cubierto, Riesgos operativos, Veredicto

### Community 25 - "Community 25"
Cohesion: 0.33
Nodes (3): BookDto, LibraryStatsDto, UpsertBookRequest

### Community 27 - "Community 27"
Cohesion: 0.33
Nodes (5): Auth rollout, code:bash (# backend), Coolify Deploy Plan, Deployment model, Local browser QA without Keycloak

### Community 28 - "Community 28"
Cohesion: 0.5
Nodes (3): HttpError, mapError(), stringifyUnknown()

### Community 39 - "Community 39"
Cohesion: 0.5
Nodes (3): CatalogBookDto, CatalogSearchResponse, IsbnLookupResponse

## Knowledge Gaps
- **141 isolated node(s):** `route`, `wrapper`, `registerLink`, `runtime`, `keycloak` (+136 more)
  These have ≤1 connection - possible missing edges or undocumented components.
- **32 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `Task 1: Frontend test harness and auth config` connect `Community 1` to `Community 13`, `Community 15`?**
  _High betweenness centrality (0.008) - this node is a cross-community bridge._
- **What connects `route`, `wrapper`, `registerLink` to the rest of the system?**
  _141 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `Community 0` be split into smaller, more focused modules?**
  _Cohesion score 0.09 - nodes in this community are weakly interconnected._
- **Should `Community 1` be split into smaller, more focused modules?**
  _Cohesion score 0.06 - nodes in this community are weakly interconnected._
- **Should `Community 2` be split into smaller, more focused modules?**
  _Cohesion score 0.07 - nodes in this community are weakly interconnected._
- **Should `Community 3` be split into smaller, more focused modules?**
  _Cohesion score 0.12 - nodes in this community are weakly interconnected._
- **Should `Community 4` be split into smaller, more focused modules?**
  _Cohesion score 0.09 - nodes in this community are weakly interconnected._