# Boontory Keycloak + Global Auth Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add mandatory Keycloak-backed authentication to Boontory, protect `*.etharlia.com` at the proxy layer, and leave repeatable Coolify/Keycloak rollout artifacts in the repo.

**Architecture:** Keep Boontory on the live single-container deployment path (`/home/ventura/Documents/boontory/Dockerfile`) while adding app-level OIDC in the Vue SPA and JWT resource-server protection for `/api/**` in Spring Boot. Protect public subdomains at the edge with a new `oauth2-proxy` app on `oauth.etharlia.com` plus Traefik `forwardAuth` attached to the live Boontory Coolify app.

**Tech Stack:** Vue 3, TypeScript, Vue Router, Vitest, keycloak-js, Kotlin, Spring Boot 2.7, Spring Security OAuth2 Resource Server, Keycloak 26.1, oauth2-proxy 7.15.1, Coolify, Traefik

---

## File Structure

### Frontend auth and tests
- Create: `frontend/vitest.config.ts` — Vitest + jsdom config for Vue unit tests
- Create: `frontend/tests/setup.ts` — shared test bootstrap
- Create: `frontend/tests/auth/authConfig.spec.ts` — env parsing regression test
- Create: `frontend/tests/auth/authState.spec.ts` — Keycloak runtime state and token refresh tests
- Create: `frontend/tests/router/guards.spec.ts` — route guard tests
- Create: `frontend/tests/services/api.spec.ts` — bearer header injection tests
- Create: `frontend/src/auth/authConfig.ts` — parse required Keycloak env
- Create: `frontend/src/auth/keycloakClient.ts` — Keycloak client factory
- Create: `frontend/src/auth/authState.ts` — auth runtime used by app and API client
- Create: `frontend/src/router/guards.ts` — navigation guard logic
- Create: `frontend/src/components/auth/AuthGate.vue` — blocks render until auth init completes
- Create: `frontend/src/env.d.ts` — type declarations for Vite auth env
- Modify: `frontend/package.json` — add `keycloak-js`, Vitest, jsdom, Vue test utils, test scripts
- Modify: `frontend/tsconfig.app.json` — include Vitest globals
- Modify: `frontend/src/App.vue` — wrap the router in `AuthGate`
- Modify: `frontend/src/layouts/AppShell.vue` — add signed-in user label and logout button
- Modify: `frontend/src/router/index.ts` — attach `requiresAuth` meta and register guard
- Modify: `frontend/src/services/api.ts` — attach bearer token to every API request

### Backend security
- Create: `backend/src/main/kotlin/com/boontory/backend/config/KeycloakJwtAuthoritiesConverter.kt` — maps Keycloak claims to Spring roles
- Create: `backend/src/main/kotlin/com/boontory/backend/config/SecurityConfig.kt` — stateless `/api/**` protection with JWT validation
- Create: `backend/src/test/kotlin/com/boontory/backend/ApiSecurityTest.kt` — 401/200/public-route security tests
- Create: `backend/src/test/kotlin/com/boontory/backend/KeycloakJwtAuthoritiesConverterTest.kt` — role conversion tests
- Modify: `backend/build.gradle.kts` — add security, resource-server, actuator, and security test deps
- Modify: `backend/src/main/resources/application.yml` — issuer URI, origin patterns, actuator exposure
- Modify: `backend/src/test/kotlin/com/boontory/backend/ApiEndpointsTest.kt` — keep business endpoint tests authenticated under the new filter chain

### Deployment and ops artifacts
- Create: `frontend/.env.example` — concrete local/prod frontend auth env values
- Create: `backend/.env.example` — concrete backend runtime env values
- Create: `ops/keycloak/etharlia-boontory-clients.json` — repo-managed realm/client config artifact
- Create: `ops/oauth2-proxy/oauth2-proxy.env.example` — repo-managed oauth2-proxy config example with placeholder secret keys clearly marked for Coolify-only storage
- Create: `ops/coolify/boontory-forward-auth.labels` — reusable forward-auth label template plus live router example note
- Modify: `Dockerfile` — pass Vite Keycloak env into the frontend build stage used by the live Coolify app
- Modify: `README.md` — local dev + auth workflow for the real deployment path
- Modify: `docs/coolify-deploy-plan.md` — document current dockerfile deployment plus oauth2-proxy rollout

### Live resources to touch during rollout
- Coolify project: `xtmri3iccgvn26tz7g3nprxh` (`Etharlia`)
- Coolify environment: `os515y0qkzdrj37aw0of3141` (`production`)
- Live Boontory application: `p7naj0uvggxjs36fifqgsyuh` (`https://boontory.etharlia.com`)
- Live Keycloak service: `xqweldaiomrhufnqpepjaip7` (`https://auth.etharlia.com`)
- Live Keycloak app in the service: `hl4okh9j102guqnf4oa5ob58`
- New app to create: `oauth2-proxy` on `https://oauth.etharlia.com`

## Task 1: Frontend test harness and auth config

**Files:**
- Create: `frontend/vitest.config.ts`
- Create: `frontend/tests/setup.ts`
- Create: `frontend/tests/auth/authConfig.spec.ts`
- Create: `frontend/src/auth/authConfig.ts`
- Create: `frontend/src/env.d.ts`
- Modify: `frontend/package.json`
- Modify: `frontend/tsconfig.app.json`

- [ ] **Step 1: Add the frontend test harness**

```json
{
  "name": "frontend",
  "private": true,
  "version": "0.0.0",
  "type": "module",
  "scripts": {
    "dev": "vite",
    "build": "vue-tsc -b && vite build",
    "preview": "vite preview",
    "test": "vitest run",
    "test:watch": "vitest"
  },
  "dependencies": {
    "@zxing/browser": "^0.1.5",
    "@zxing/library": "^0.21.3",
    "keycloak-js": "^26.2.3",
    "vue": "^3.5.30",
    "vue-router": "^4.5.1"
  },
  "devDependencies": {
    "@types/node": "^24.12.0",
    "@vitejs/plugin-vue": "^6.0.5",
    "@vue/test-utils": "^2.4.6",
    "@vue/tsconfig": "^0.9.0",
    "jsdom": "^29.0.2",
    "typescript": "~5.9.3",
    "vite": "^8.0.1",
    "vitest": "^4.1.4",
    "vue-tsc": "^3.2.5"
  }
}
```

```ts
// frontend/vitest.config.ts
import { defineConfig } from 'vitest/config'
import vue from '@vitejs/plugin-vue'

export default defineConfig({
  plugins: [vue()],
  test: {
    environment: 'jsdom',
    setupFiles: ['./tests/setup.ts'],
    globals: true,
    css: true,
  },
})
```

```ts
// frontend/tests/setup.ts
import { afterEach, vi } from 'vitest'

afterEach(() => {
  vi.restoreAllMocks()
})
```

```json
// frontend/tsconfig.app.json
{
  "extends": "@vue/tsconfig/tsconfig.dom.json",
  "compilerOptions": {
    "tsBuildInfoFile": "./node_modules/.tmp/tsconfig.app.tsbuildinfo",
    "types": ["vite/client", "vitest/globals"],
    "strict": true,
    "noUnusedLocals": true,
    "noUnusedParameters": true,
    "erasableSyntaxOnly": true,
    "noFallthroughCasesInSwitch": true,
    "noUncheckedSideEffectImports": true
  },
  "include": ["src/**/*.ts", "src/**/*.tsx", "src/**/*.vue", "tests/**/*.ts"]
}
```

- [ ] **Step 2: Write the failing auth config test**

```ts
// frontend/tests/auth/authConfig.spec.ts
import { describe, expect, it } from 'vitest'
import { readAuthConfig } from '../../src/auth/authConfig'

describe('readAuthConfig', () => {
  it('returns trimmed Keycloak env values', () => {
    expect(
      readAuthConfig({
        VITE_KEYCLOAK_URL: ' https://auth.etharlia.com ',
        VITE_KEYCLOAK_REALM: ' etharlia ',
        VITE_KEYCLOAK_CLIENT_ID: ' boontory-frontend ',
      }),
    ).toEqual({
      url: 'https://auth.etharlia.com',
      realm: 'etharlia',
      clientId: 'boontory-frontend',
    })
  })

  it('throws when a required Keycloak env value is missing', () => {
    expect(() =>
      readAuthConfig({
        VITE_KEYCLOAK_URL: 'https://auth.etharlia.com',
        VITE_KEYCLOAK_REALM: '',
        VITE_KEYCLOAK_CLIENT_ID: 'boontory-frontend',
      }),
    ).toThrow('VITE_KEYCLOAK_REALM is required')
  })
})
```

- [ ] **Step 3: Run the test to verify it fails**

Run: `cd frontend && npm install && npm run test -- authConfig.spec.ts`

Expected: FAIL with `Cannot find module '../../src/auth/authConfig'`.

- [ ] **Step 4: Write the minimal auth config implementation**

```ts
// frontend/src/auth/authConfig.ts
export type AuthConfigEnv = Partial<{
  VITE_KEYCLOAK_URL: string
  VITE_KEYCLOAK_REALM: string
  VITE_KEYCLOAK_CLIENT_ID: string
}>

export type AuthConfig = {
  url: string
  realm: string
  clientId: string
}

function requireValue(name: keyof AuthConfigEnv, value?: string): string {
  const trimmed = value?.trim()
  if (!trimmed) {
    throw new Error(`${name} is required`)
  }
  return trimmed
}

export function readAuthConfig(env: AuthConfigEnv = import.meta.env): AuthConfig {
  return {
    url: requireValue('VITE_KEYCLOAK_URL', env.VITE_KEYCLOAK_URL),
    realm: requireValue('VITE_KEYCLOAK_REALM', env.VITE_KEYCLOAK_REALM),
    clientId: requireValue('VITE_KEYCLOAK_CLIENT_ID', env.VITE_KEYCLOAK_CLIENT_ID),
  }
}
```

```ts
// frontend/src/env.d.ts
interface ImportMetaEnv {
  readonly VITE_API_BASE_URL?: string
  readonly VITE_KEYCLOAK_URL: string
  readonly VITE_KEYCLOAK_REALM: string
  readonly VITE_KEYCLOAK_CLIENT_ID: string
}

interface ImportMeta {
  readonly env: ImportMetaEnv
}
```

- [ ] **Step 5: Run the test to verify it passes**

Run: `cd frontend && npm run test -- authConfig.spec.ts`

Expected: PASS.

- [ ] **Step 6: Commit**

```bash
git add frontend/package.json frontend/tsconfig.app.json frontend/vitest.config.ts frontend/tests/setup.ts frontend/tests/auth/authConfig.spec.ts frontend/src/auth/authConfig.ts frontend/src/env.d.ts

git commit -m "test(frontend): add auth config harness"
```

### Task 2: Frontend Keycloak runtime, route guard, and bearer API client

**Files:**
- Create: `frontend/src/auth/keycloakClient.ts`
- Create: `frontend/src/auth/authState.ts`
- Create: `frontend/src/router/guards.ts`
- Create: `frontend/src/components/auth/AuthGate.vue`
- Create: `frontend/tests/auth/authState.spec.ts`
- Create: `frontend/tests/router/guards.spec.ts`
- Create: `frontend/tests/services/api.spec.ts`
- Modify: `frontend/src/App.vue`
- Modify: `frontend/src/layouts/AppShell.vue`
- Modify: `frontend/src/router/index.ts`
- Modify: `frontend/src/services/api.ts`

- [ ] **Step 1: Write the failing frontend auth tests**

```ts
// frontend/tests/auth/authState.spec.ts
import { describe, expect, it, vi } from 'vitest'
import { createAuthRuntime } from '../../src/auth/authState'

function createKeycloakStub(overrides: Partial<any> = {}) {
  return {
    authenticated: true,
    token: 'token-123',
    tokenParsed: { preferred_username: 'ventura' },
    init: vi.fn().mockResolvedValue(true),
    updateToken: vi.fn().mockResolvedValue(true),
    login: vi.fn().mockResolvedValue(undefined),
    logout: vi.fn().mockResolvedValue(undefined),
    ...overrides,
  }
}

describe('createAuthRuntime', () => {
  it('initializes auth state from the Keycloak client', async () => {
    const runtime = createAuthRuntime(createKeycloakStub())

    await runtime.initializeAuth()

    expect(runtime.state.ready).toBe(true)
    expect(runtime.state.authenticated).toBe(true)
    expect(runtime.state.username).toBe('ventura')
  })

  it('refreshes the token before returning it', async () => {
    const keycloak = createKeycloakStub()
    const runtime = createAuthRuntime(keycloak)

    await runtime.initializeAuth()
    const token = await runtime.getAccessToken()

    expect(keycloak.updateToken).toHaveBeenCalledWith(30)
    expect(token).toBe('token-123')
  })
})
```

```ts
// frontend/tests/router/guards.spec.ts
import { describe, expect, it, vi } from 'vitest'
import { createAuthGuard } from '../../src/router/guards'

describe('createAuthGuard', () => {
  it('initializes auth before protected navigation', async () => {
    const deps = {
      initializeAuth: vi.fn().mockResolvedValue(undefined),
      isAuthenticated: vi.fn().mockReturnValue(true),
      login: vi.fn().mockResolvedValue(undefined),
    }

    const guard = createAuthGuard(deps)
    const result = await guard({ meta: { requiresAuth: true } } as any)

    expect(deps.initializeAuth).toHaveBeenCalledTimes(1)
    expect(result).toBe(true)
  })

  it('kicks off login when auth init finishes unauthenticated', async () => {
    const deps = {
      initializeAuth: vi.fn().mockResolvedValue(undefined),
      isAuthenticated: vi.fn().mockReturnValue(false),
      login: vi.fn().mockResolvedValue(undefined),
    }

    const guard = createAuthGuard(deps)
    const result = await guard({ meta: { requiresAuth: true } } as any)

    expect(deps.login).toHaveBeenCalledTimes(1)
    expect(result).toBe(false)
  })
})
```

```ts
// frontend/tests/services/api.spec.ts
import { beforeEach, describe, expect, it, vi } from 'vitest'

const getAccessToken = vi.fn().mockResolvedValue('bearer-token')
vi.mock('../../src/auth/authState', () => ({
  auth: {
    getAccessToken,
  },
}))

import { booksApi } from '../../src/services/api'

describe('booksApi', () => {
  beforeEach(() => {
    vi.stubGlobal(
      'fetch',
      vi.fn().mockResolvedValue({
        ok: true,
        status: 200,
        json: async () => [],
      }),
    )
  })

  it('sends the bearer token on API requests', async () => {
    await booksApi.list()

    expect(getAccessToken).toHaveBeenCalledTimes(1)
    expect(fetch).toHaveBeenCalledWith(
      '/api/books',
      expect.objectContaining({
        headers: expect.objectContaining({
          Authorization: 'Bearer bearer-token',
        }),
      }),
    )
  })
})
```

- [ ] **Step 2: Run the frontend auth tests to verify they fail**

Run: `cd frontend && npm run test -- authState.spec.ts guards.spec.ts api.spec.ts`

Expected: FAIL with missing modules under `src/auth` and `src/router/guards`.

- [ ] **Step 3: Write the frontend auth runtime and guard implementation**

```ts
// frontend/src/auth/keycloakClient.ts
import Keycloak from 'keycloak-js'
import type { AuthConfig } from './authConfig'

export type KeycloakLike = Pick<
  Keycloak,
  'authenticated' | 'token' | 'tokenParsed' | 'init' | 'updateToken' | 'login' | 'logout'
>

export function createKeycloakClient(config: AuthConfig): KeycloakLike {
  return new Keycloak({
    url: config.url,
    realm: config.realm,
    clientId: config.clientId,
  })
}
```

```ts
// frontend/src/auth/authState.ts
import { reactive, readonly } from 'vue'
import { readAuthConfig } from './authConfig'
import { createKeycloakClient, type KeycloakLike } from './keycloakClient'

type AuthSnapshot = {
  ready: boolean
  authenticated: boolean
  username: string | null
  error: string | null
}

export function createAuthRuntime(keycloak: KeycloakLike) {
  const state = reactive<AuthSnapshot>({
    ready: false,
    authenticated: false,
    username: null,
    error: null,
  })

  let initPromise: Promise<void> | null = null

  async function initializeAuth() {
    if (initPromise) return initPromise

    initPromise = (async () => {
      try {
        const authenticated = await keycloak.init({
          onLoad: 'login-required',
          pkceMethod: 'S256',
          checkLoginIframe: false,
        })

        state.authenticated = authenticated
        state.username = keycloak.tokenParsed?.preferred_username?.toString() ?? null
        state.error = null
      } catch (error) {
        state.error = error instanceof Error ? error.message : 'Authentication initialization failed'
        throw error
      } finally {
        state.ready = true
      }
    })()

    return initPromise
  }

  function isAuthenticated() {
    return state.authenticated
  }

  async function getAccessToken(minValidity = 30) {
    await initializeAuth()
    await keycloak.updateToken(minValidity)
    if (!keycloak.token) {
      throw new Error('Access token is not available')
    }
    return keycloak.token
  }

  async function login() {
    await keycloak.login()
  }

  async function logout() {
    state.ready = false
    state.authenticated = false
    state.username = null
    await keycloak.logout({ redirectUri: window.location.origin })
  }

  return {
    state: readonly(state),
    initializeAuth,
    isAuthenticated,
    getAccessToken,
    login,
    logout,
  }
}

export const auth = createAuthRuntime(createKeycloakClient(readAuthConfig()))
```

```ts
// frontend/src/router/guards.ts
export type AuthGuardDeps = {
  initializeAuth: () => Promise<void>
  isAuthenticated: () => boolean
  login: () => Promise<void>
}

export function createAuthGuard(deps: AuthGuardDeps) {
  return async (to: { meta: { requiresAuth?: boolean } }) => {
    if (to.meta.requiresAuth === false) {
      return true
    }

    await deps.initializeAuth()

    if (deps.isAuthenticated()) {
      return true
    }

    await deps.login()
    return false
  }
}
```

```vue
<!-- frontend/src/components/auth/AuthGate.vue -->
<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { auth } from '../../auth/authState'

const error = ref<string | null>(null)

onMounted(async () => {
  try {
    await auth.initializeAuth()
  } catch (value) {
    error.value = value instanceof Error ? value.message : 'Authentication failed'
  }
})
</script>

<template>
  <div v-if="!auth.state.ready" class="auth-status">Signing you in…</div>
  <div v-else-if="error" class="auth-status auth-status--error">{{ error }}</div>
  <slot v-else />
</template>

<style scoped>
.auth-status {
  min-height: 100vh;
  display: grid;
  place-items: center;
  color: var(--text-soft);
  font-size: 1rem;
}

.auth-status--error {
  color: #c0392b;
}
</style>
```

```vue
<!-- frontend/src/App.vue -->
<script setup lang="ts">
import { RouterView } from 'vue-router'
import AuthGate from './components/auth/AuthGate.vue'
</script>

<template>
  <AuthGate>
    <RouterView />
  </AuthGate>
</template>
```

```ts
// frontend/src/router/index.ts
import { createRouter, createWebHistory } from 'vue-router'
import { auth } from '../auth/authState'
import AppShell from '../layouts/AppShell.vue'
import BookDetailPage from '../pages/BookDetailPage.vue'
import BookEditorPage from '../pages/BookEditorPage.vue'
import DashboardPage from '../pages/DashboardPage.vue'
import LibraryPage from '../pages/LibraryPage.vue'
import ScanPage from '../pages/ScanPage.vue'
import SearchPage from '../pages/SearchPage.vue'
import ShelvesPage from '../pages/ShelvesPage.vue'
import { createAuthGuard } from './guards'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/',
      component: AppShell,
      meta: { requiresAuth: true },
      children: [
        { path: '', name: 'dashboard', component: DashboardPage },
        { path: 'library', name: 'library', component: LibraryPage },
        { path: 'library/new', name: 'book-create', component: BookEditorPage },
        { path: 'library/:id', name: 'book-detail', component: BookDetailPage, props: true },
        { path: 'library/:id/edit', name: 'book-edit', component: BookEditorPage, props: true },
        { path: 'scan', name: 'scan', component: ScanPage },
        { path: 'search', name: 'search', component: SearchPage },
        { path: 'shelves', name: 'shelves', component: ShelvesPage },
      ],
    },
  ],
})

router.beforeEach(createAuthGuard(auth))

export default router
```

```ts
// frontend/src/services/api.ts
import { auth } from '../auth/authState'
import type {
  Book,
  CatalogSearchResponse,
  IsbnLookupResponse,
  LibraryStats,
  Shelf,
  UpsertBookPayload,
  UpsertShelfPayload,
} from '../types/models'

const API_BASE = import.meta.env.VITE_API_BASE_URL ?? ''

async function request<T>(path: string, init?: RequestInit): Promise<T> {
  const token = await auth.getAccessToken()
  const response = await fetch(`${API_BASE}${path}`, {
    headers: {
      'Content-Type': 'application/json',
      Authorization: `Bearer ${token}`,
      ...(init?.headers ?? {}),
    },
    ...init,
  })

  if (!response.ok) {
    const rawBody = await response.text()
    const contentType = response.headers.get('content-type') ?? ''
    let message = `${response.status} ${response.statusText}`

    if (contentType.includes('application/json')) {
      const payload = JSON.parse(rawBody) as { message?: string }
      message = payload.message ?? message
    } else if (rawBody.trim()) {
      message = rawBody.trim()
    }

    throw new Error(message)
  }

  if (response.status === 204) {
    return undefined as T
  }

  return response.json() as Promise<T>
}

export const booksApi = {
  list(query = '', status?: string, shelfId?: number | string) {
    const params = new URLSearchParams()
    if (query.trim()) params.set('query', query.trim())
    if (status) params.set('status', status)
    if (shelfId !== undefined && shelfId !== '') params.set('shelfId', String(shelfId))
    const suffix = params.toString()
    return request<Book[]>(`/api/books${suffix ? `?${suffix}` : ''}`)
  },
  get(id: number | string) {
    return request<Book>(`/api/books/${id}`)
  },
  create(payload: UpsertBookPayload) {
    return request<Book>('/api/books', { method: 'POST', body: JSON.stringify(payload) })
  },
  update(id: number | string, payload: UpsertBookPayload) {
    return request<Book>(`/api/books/${id}`, { method: 'PUT', body: JSON.stringify(payload) })
  },
  remove(id: number | string) {
    return request<void>(`/api/books/${id}`, { method: 'DELETE' })
  },
  stats() {
    return request<LibraryStats>('/api/books/stats')
  },
}

export const shelvesApi = {
  list() {
    return request<Shelf[]>('/api/shelves')
  },
  create(payload: UpsertShelfPayload) {
    return request<Shelf>('/api/shelves', { method: 'POST', body: JSON.stringify(payload) })
  },
  update(id: number | string, payload: UpsertShelfPayload) {
    return request<Shelf>(`/api/shelves/${id}`, { method: 'PUT', body: JSON.stringify(payload) })
  },
  remove(id: number | string) {
    return request<void>(`/api/shelves/${id}`, { method: 'DELETE' })
  },
}

export const catalogApi = {
  search(query: string) {
    const params = new URLSearchParams({ query })
    return request<CatalogSearchResponse>(`/api/catalog/search?${params.toString()}`)
  },
  lookupByIsbn(isbn: string) {
    return request<IsbnLookupResponse>(`/api/catalog/isbn/${isbn}`)
  },
}
```

```vue
<!-- frontend/src/layouts/AppShell.vue -->
<script setup lang="ts">
import { RouterView, useRoute } from 'vue-router'
import { auth } from '../auth/authState'
import BottomNav from '../components/BottomNav.vue'

const route = useRoute()

async function handleLogout() {
  await auth.logout()
}
</script>

<template>
  <div class="shell">
    <header class="topbar">
      <div>
        <p class="eyebrow">Personal Book Library</p>
        <h1>Boontory</h1>
      </div>
      <div class="topbar-actions">
        <p class="route-label">{{ route.name }}</p>
        <button class="logout-button" type="button" @click="handleLogout">
          Sign out {{ auth.state.username ?? '' }}
        </button>
      </div>
    </header>

    <main class="content">
      <RouterView />
    </main>

    <BottomNav />
  </div>
</template>

<style scoped>
.shell {
  min-height: 100vh;
  max-width: 1100px;
  margin: 0 auto;
  padding: 24px 20px 92px;
}

.topbar {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  align-items: end;
  margin-bottom: 24px;
}

.topbar-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.eyebrow {
  margin: 0 0 4px;
  color: var(--accent);
  text-transform: uppercase;
  letter-spacing: 0.08em;
  font-size: 0.78rem;
  font-weight: 700;
}

h1 {
  margin: 0;
  font-size: clamp(2rem, 5vw, 3rem);
  line-height: 0.95;
}

.route-label {
  margin: 0;
  color: var(--text-soft);
  text-transform: capitalize;
}

.logout-button {
  border: 0;
  border-radius: 999px;
  padding: 10px 14px;
  background: var(--accent);
  color: white;
  font-weight: 700;
  cursor: pointer;
}

.content {
  display: grid;
}

@media (max-width: 720px) {
  .shell {
    padding-inline: 14px;
  }

  .topbar {
    align-items: start;
    flex-direction: column;
  }

  .topbar-actions {
    flex-direction: column;
    align-items: start;
  }
}
</style>
```

- [ ] **Step 4: Run the targeted frontend tests to verify they pass**

Run: `cd frontend && npm run test -- authState.spec.ts guards.spec.ts api.spec.ts`

Expected: PASS.

- [ ] **Step 5: Run the frontend production build**

Run: `cd frontend && npm run build`

Expected: PASS with a generated `frontend/dist/` bundle.

- [ ] **Step 6: Commit**

```bash
git add frontend/src/auth frontend/src/router/guards.ts frontend/src/components/auth/AuthGate.vue frontend/src/App.vue frontend/src/layouts/AppShell.vue frontend/src/router/index.ts frontend/src/services/api.ts frontend/tests/auth/authState.spec.ts frontend/tests/router/guards.spec.ts frontend/tests/services/api.spec.ts

git commit -m "feat(frontend): add keycloak auth flow"
```

### Task 3: Backend JWT resource server for `/api/**`

**Files:**
- Create: `backend/src/main/kotlin/com/boontory/backend/config/KeycloakJwtAuthoritiesConverter.kt`
- Create: `backend/src/main/kotlin/com/boontory/backend/config/SecurityConfig.kt`
- Create: `backend/src/test/kotlin/com/boontory/backend/ApiSecurityTest.kt`
- Create: `backend/src/test/kotlin/com/boontory/backend/KeycloakJwtAuthoritiesConverterTest.kt`
- Modify: `backend/build.gradle.kts`
- Modify: `backend/src/main/resources/application.yml`
- Modify: `backend/src/test/kotlin/com/boontory/backend/ApiEndpointsTest.kt`

- [ ] **Step 1: Add the security dependencies**

```kotlin
// backend/build.gradle.kts
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.springframework.boot") version "2.7.18"
    id("io.spring.dependency-management") version "1.1.7"
    kotlin("jvm") version "1.9.25"
    kotlin("plugin.spring") version "1.9.25"
    kotlin("plugin.jpa") version "1.9.25"
}

group = "com.boontory"
version = "0.0.1-SNAPSHOT"

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.xerial:sqlite-jdbc:3.46.1.3")
    implementation("com.github.gwenn:sqlite-dialect:0.2.0")
    developmentOnly("org.springframework.boot:spring-boot-devtools")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.security:spring-security-test")
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "11"
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}
```

- [ ] **Step 2: Write the failing security tests**

```kotlin
// backend/src/test/kotlin/com/boontory/backend/ApiSecurityTest.kt
package com.boontory.backend

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@SpringBootTest
@AutoConfigureMockMvc
class ApiSecurityTest {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @Test
    fun `api endpoints reject unauthenticated requests`() {
        mockMvc.perform(get("/api/books"))
            .andExpect(status().isUnauthorized)
    }

    @Test
    fun `jwt authenticated requests can reach api endpoints`() {
        mockMvc.perform(
            get("/api/books").with(
                jwt().jwt {
                    it.claim("realm_access", mapOf("roles" to listOf("user")))
                },
            ),
        ).andExpect(status().isOk)
    }

    @Test
    fun `spa route stays public because edge auth protects the host`() {
        mockMvc.perform(get("/"))
            .andExpect(status().isOk)
    }
}
```

```kotlin
// backend/src/test/kotlin/com/boontory/backend/KeycloakJwtAuthoritiesConverterTest.kt
package com.boontory.backend

import com.boontory.backend.config.KeycloakJwtAuthoritiesConverter
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.security.oauth2.jwt.Jwt

class KeycloakJwtAuthoritiesConverterTest {
    private val converter = KeycloakJwtAuthoritiesConverter()

    @Test
    fun `maps realm and client roles to spring authorities`() {
        val jwt = Jwt.withTokenValue("token")
            .header("alg", "none")
            .claim("sub", "user-1")
            .claim("realm_access", mapOf("roles" to listOf("user")))
            .claim(
                "resource_access",
                mapOf(
                    "boontory-frontend" to mapOf("roles" to listOf("admin")),
                ),
            )
            .build()

        val authorities = converter.convert(jwt).map { it.authority }

        assertThat(authorities).containsExactlyInAnyOrder("ROLE_USER", "ROLE_ADMIN")
    }
}
```

```kotlin
// backend/src/test/kotlin/com/boontory/backend/ApiEndpointsTest.kt
package com.boontory.backend

import org.springframework.security.test.context.support.WithMockUser

@SpringBootTest
@AutoConfigureMockMvc
@WithMockUser(username = "ventura", roles = ["USER"])
class ApiEndpointsTest {
    // keep the rest of the file unchanged
}
```

- [ ] **Step 3: Run the security tests to verify they fail**

Run: `cd backend && ./gradlew test --tests com.boontory.backend.ApiSecurityTest --tests com.boontory.backend.KeycloakJwtAuthoritiesConverterTest`

Expected: FAIL because `/api/books` still returns `200`, and `KeycloakJwtAuthoritiesConverter` does not exist.

- [ ] **Step 4: Implement the JWT resource server configuration**

```yaml
# backend/src/main/resources/application.yml
spring:
  datasource:
    url: jdbc:sqlite:${BOONTORY_DB_PATH:./data/boontory.db}
    driver-class-name: org.sqlite.JDBC
  jpa:
    database-platform: org.sqlite.hibernate.dialect.SQLiteDialect
    hibernate:
      ddl-auto: update
    show-sql: false
    properties:
      hibernate:
        format_sql: false
  jackson:
    default-property-inclusion: non_null
  security:
    oauth2:
      resourceserver:
        jwt:
          issuer-uri: ${KEYCLOAK_ISSUER_URI:https://auth.etharlia.com/realms/etharlia}

server:
  port: ${SERVER_PORT:8080}

management:
  endpoints:
    web:
      exposure:
        include: health,info

boontory:
  open-library-base-url: https://openlibrary.org
  frontend-origin-patterns: ${FRONTEND_ORIGIN_PATTERNS:http://localhost:5173,http://127.0.0.1:5173,https://boontory.etharlia.com}
```

```kotlin
// backend/src/main/kotlin/com/boontory/backend/config/KeycloakJwtAuthoritiesConverter.kt
package com.boontory.backend.config

import org.springframework.core.convert.converter.Converter
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.jwt.Jwt

class KeycloakJwtAuthoritiesConverter : Converter<Jwt, Collection<GrantedAuthority>> {
    override fun convert(jwt: Jwt): Collection<GrantedAuthority> {
        val realmRoles = ((jwt.getClaim<Map<String, Any>>("realm_access")?.get("roles") as? Collection<*>) ?: emptyList<Any>())
            .filterIsInstance<String>()

        val resourceAccess = jwt.getClaim<Map<String, Any>>("resource_access") ?: emptyMap()
        val clientRoles = resourceAccess.values
            .filterIsInstance<Map<*, *>>()
            .flatMap { value ->
                (value["roles"] as? Collection<*>)
                    .orEmpty()
                    .filterIsInstance<String>()
            }

        return (realmRoles + clientRoles)
            .distinct()
            .map { role -> SimpleGrantedAuthority("ROLE_${role.uppercase()}") }
    }
}
```

```kotlin
// backend/src/main/kotlin/com/boontory/backend/config/SecurityConfig.kt
package com.boontory.backend.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.Customizer
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter
import org.springframework.security.web.SecurityFilterChain

@Configuration
class SecurityConfig {
    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .csrf().disable()
            .cors(Customizer.withDefaults())
            .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and()
            .authorizeRequests()
            .antMatchers("/api/**").authenticated()
            .anyRequest().permitAll()
            .and()
            .oauth2ResourceServer()
            .jwt()
            .jwtAuthenticationConverter(jwtAuthenticationConverter())

        return http.build()
    }

    @Bean
    fun jwtAuthenticationConverter(): JwtAuthenticationConverter {
        val converter = JwtAuthenticationConverter()
        converter.setJwtGrantedAuthoritiesConverter(KeycloakJwtAuthoritiesConverter())
        return converter
    }
}
```

- [ ] **Step 5: Run the security tests to verify they pass**

Run: `cd backend && ./gradlew test --tests com.boontory.backend.ApiSecurityTest --tests com.boontory.backend.KeycloakJwtAuthoritiesConverterTest`

Expected: PASS.

- [ ] **Step 6: Run the full backend test suite**

Run: `cd backend && ./gradlew test`

Expected: PASS, including the existing `ApiEndpointsTest` under `@WithMockUser`.

- [ ] **Step 7: Commit**

```bash
git add backend/build.gradle.kts backend/src/main/resources/application.yml backend/src/main/kotlin/com/boontory/backend/config/SecurityConfig.kt backend/src/main/kotlin/com/boontory/backend/config/KeycloakJwtAuthoritiesConverter.kt backend/src/test/kotlin/com/boontory/backend/ApiSecurityTest.kt backend/src/test/kotlin/com/boontory/backend/KeycloakJwtAuthoritiesConverterTest.kt backend/src/test/kotlin/com/boontory/backend/ApiEndpointsTest.kt

git commit -m "feat(backend): secure api with keycloak jwt"
```

### Task 4: Production build args, repo-managed ops artifacts, and docs alignment

**Files:**
- Create: `frontend/.env.example`
- Create: `backend/.env.example`
- Create: `ops/keycloak/etharlia-boontory-clients.json`
- Create: `ops/oauth2-proxy/oauth2-proxy.env.example`
- Create: `ops/coolify/boontory-forward-auth.labels`
- Modify: `Dockerfile`
- Modify: `README.md`
- Modify: `docs/coolify-deploy-plan.md`

- [ ] **Step 1: Confirm the ops artifacts do not exist yet**

Run: `cd /home/ventura/Documents/boontory && test -f ops/keycloak/etharlia-boontory-clients.json`

Expected: exit code `1` because the ops artifact tree does not exist yet.

- [ ] **Step 2: Create concrete env examples and the Keycloak / oauth2-proxy artifacts**

```env
# frontend/.env.example
VITE_API_BASE_URL=
VITE_KEYCLOAK_URL=https://auth.etharlia.com
VITE_KEYCLOAK_REALM=etharlia
VITE_KEYCLOAK_CLIENT_ID=boontory-frontend
```

```env
# backend/.env.example
SERVER_PORT=8080
BOONTORY_DB_PATH=./data/boontory.db
FRONTEND_ORIGIN_PATTERNS=http://localhost:5173,http://127.0.0.1:5173,https://boontory.etharlia.com
KEYCLOAK_ISSUER_URI=https://auth.etharlia.com/realms/etharlia
```

```json
// ops/keycloak/etharlia-boontory-clients.json
{
  "realm": "etharlia",
  "enabled": true,
  "roles": {
    "realm": [
      { "name": "user" },
      { "name": "admin" }
    ]
  },
  "clients": [
    {
      "clientId": "oauth2-proxy",
      "name": "oauth2-proxy",
      "enabled": true,
      "protocol": "openid-connect",
      "publicClient": false,
      "standardFlowEnabled": true,
      "directAccessGrantsEnabled": false,
      "serviceAccountsEnabled": false,
      "redirectUris": [
        "https://oauth.etharlia.com/oauth2/callback"
      ],
      "webOrigins": [
        "https://oauth.etharlia.com"
      ]
    },
    {
      "clientId": "boontory-frontend",
      "name": "boontory-frontend",
      "enabled": true,
      "protocol": "openid-connect",
      "publicClient": true,
      "standardFlowEnabled": true,
      "directAccessGrantsEnabled": false,
      "redirectUris": [
        "https://boontory.etharlia.com/*",
        "http://localhost:5173/*",
        "http://127.0.0.1:5173/*"
      ],
      "webOrigins": [
        "https://boontory.etharlia.com",
        "http://localhost:5173",
        "http://127.0.0.1:5173"
      ],
      "attributes": {
        "pkce.code.challenge.method": "S256"
      }
    }
  ]
}
```

```env
# ops/oauth2-proxy/oauth2-proxy.env.example
OAUTH2_PROXY_PROVIDER=oidc
OAUTH2_PROXY_OIDC_ISSUER_URL=https://auth.etharlia.com/realms/etharlia
OAUTH2_PROXY_REDIRECT_URL=https://oauth.etharlia.com/oauth2/callback
OAUTH2_PROXY_CLIENT_ID=oauth2-proxy
OAUTH2_PROXY_CLIENT_SECRET=SET_IN_COOLIFY_ONLY
OAUTH2_PROXY_COOKIE_SECRET=SET_IN_COOLIFY_ONLY
OAUTH2_PROXY_EMAIL_DOMAINS=*
OAUTH2_PROXY_COOKIE_DOMAIN=.etharlia.com
OAUTH2_PROXY_COOKIE_SECURE=true
OAUTH2_PROXY_COOKIE_SAMESITE=lax
OAUTH2_PROXY_REVERSE_PROXY=true
OAUTH2_PROXY_HTTP_ADDRESS=0.0.0.0:4180
OAUTH2_PROXY_UPSTREAMS=static://202
OAUTH2_PROXY_WHITELIST_DOMAINS=.etharlia.com
OAUTH2_PROXY_SET_XAUTHREQUEST=true
OAUTH2_PROXY_PASS_ACCESS_TOKEN=true
OAUTH2_PROXY_PASS_AUTHORIZATION_HEADER=true
OAUTH2_PROXY_SKIP_PROVIDER_BUTTON=true
OAUTH2_PROXY_SCOPE=openid profile email
OAUTH2_PROXY_PING_PATH=/ping
```

```text
# ops/coolify/boontory-forward-auth.labels
# Replace <ROUTER_ID> with the live HTTPS router id from the Boontory Coolify app.
# Current live example: https-0-p7naj0uvggxjs36fifqgsyuh
traefik.http.middlewares.boontory-auth.forwardauth.address=https://oauth.etharlia.com/oauth2/auth
traefik.http.middlewares.boontory-auth.forwardauth.trustForwardHeader=true
traefik.http.middlewares.boontory-auth.forwardauth.authResponseHeaders=X-Auth-Request-User,X-Auth-Request-Email,X-Auth-Request-Preferred-Username,Authorization,Set-Cookie
traefik.http.routers.<ROUTER_ID>.middlewares=gzip,boontory-auth@docker
```

- [ ] **Step 3: Pass Keycloak env through the live production Docker build**

```dockerfile
# Dockerfile
FROM node:25-alpine AS frontend-build
WORKDIR /app
ARG VITE_API_BASE_URL=
ARG VITE_KEYCLOAK_URL=https://auth.etharlia.com
ARG VITE_KEYCLOAK_REALM=etharlia
ARG VITE_KEYCLOAK_CLIENT_ID=boontory-frontend
ENV VITE_API_BASE_URL=$VITE_API_BASE_URL
ENV VITE_KEYCLOAK_URL=$VITE_KEYCLOAK_URL
ENV VITE_KEYCLOAK_REALM=$VITE_KEYCLOAK_REALM
ENV VITE_KEYCLOAK_CLIENT_ID=$VITE_KEYCLOAK_CLIENT_ID
COPY frontend/package*.json ./
RUN npm install
COPY frontend/ .
RUN npm run build

FROM gradle:7.6.4-jdk11 AS backend-build
WORKDIR /workspace
COPY backend/ .
COPY --from=frontend-build /app/dist ./src/main/resources/static
RUN ./gradlew bootJar --no-daemon

FROM eclipse-temurin:11-jre
WORKDIR /app
RUN mkdir -p /app/data
COPY --from=backend-build /workspace/build/libs/boontory-backend-0.0.1-SNAPSHOT.jar app.jar
ENV BOONTORY_DB_PATH=/app/data/boontory.db
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
```

- [ ] **Step 4: Validate the new artifacts and the production build path**

Run: `cd /home/ventura/Documents/boontory && python3 -c "import json, pathlib; json.loads(pathlib.Path('ops/keycloak/etharlia-boontory-clients.json').read_text())" && rg "oauth.etharlia.com" ops/coolify/boontory-forward-auth.labels && docker build --build-arg VITE_KEYCLOAK_URL=https://auth.etharlia.com --build-arg VITE_KEYCLOAK_REALM=etharlia --build-arg VITE_KEYCLOAK_CLIENT_ID=boontory-frontend -t boontory-auth .`

Expected:
- the Python command exits `0`
- `rg` prints the `oauth.etharlia.com` line
- `docker build` succeeds

- [ ] **Step 5: Align the README and deploy docs with the live single-container path**

```md
<!-- README.md: add a new Authentication section after Production build -->
## Authentication

This is the target auth rollout for Boontory:

1. `oauth2-proxy` on `https://oauth.etharlia.com` protects public `*.etharlia.com` hosts at the Traefik edge.
2. The Vue app uses `keycloak-js` with the `boontory-frontend` public client so API calls carry a bearer token once the app auth slice is deployed.

### Frontend env

Use `frontend/.env.example` for local development. The production Docker build reads:

- `VITE_KEYCLOAK_URL`
- `VITE_KEYCLOAK_REALM`
- `VITE_KEYCLOAK_CLIENT_ID`

### Backend env

Use `backend/.env.example` for local development. The backend runtime reads:

- `KEYCLOAK_ISSUER_URI`
- `FRONTEND_ORIGIN_PATTERNS`
- `BOONTORY_DB_PATH`
```

```md
<!-- docs/coolify-deploy-plan.md: replace the old model section with the live one -->
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
- Import or apply `ops/keycloak/etharlia-boontory-clients.json` so the `oauth2-proxy` and `boontory-frontend` clients exist before rollout.
- Create a new oauth2-proxy app on `https://oauth.etharlia.com` using `quay.io/oauth2-proxy/oauth2-proxy:v7.15.1`.
- Set `OAUTH2_PROXY_CLIENT_ID`, `OAUTH2_PROXY_CLIENT_SECRET`, and `OAUTH2_PROXY_COOKIE_SECRET` in Coolify for the oauth2-proxy app. Use the Keycloak confidential client secret for `oauth2-proxy`, and generate `OAUTH2_PROXY_COOKIE_SECRET` as a 32-byte base64 value.
- Add the labels from `ops/coolify/boontory-forward-auth.labels` to the Boontory app custom labels, replacing `<ROUTER_ID>` with the live router id.
- Set build env on the Boontory app for `VITE_KEYCLOAK_URL`, `VITE_KEYCLOAK_REALM`, and `VITE_KEYCLOAK_CLIENT_ID`.
- Set runtime env on the Boontory app for `KEYCLOAK_ISSUER_URI`, `FRONTEND_ORIGIN_PATTERNS`, and `BOONTORY_DB_PATH`.
- Verify `https://oauth.etharlia.com/ping` returns `200`, unauthenticated Boontory requests redirect or fail closed, and remove the forward-auth labels to roll back if deployment fails.
```

- [ ] **Step 6: Commit**

```bash
git add Dockerfile README.md docs/coolify-deploy-plan.md frontend/.env.example backend/.env.example ops/keycloak/etharlia-boontory-clients.json ops/oauth2-proxy/oauth2-proxy.env.example ops/coolify/boontory-forward-auth.labels

git commit -m "build(docs): add auth deployment config"
```

### Task 5: Live Keycloak and Coolify rollout

**Files:**
- External: Keycloak admin console at `https://auth.etharlia.com/admin/`
- External: Coolify project `xtmri3iccgvn26tz7g3nprxh`
- External: Coolify environment `os515y0qkzdrj37aw0of3141`
- External: Coolify app `p7naj0uvggxjs36fifqgsyuh`
- External: Coolify service `xqweldaiomrhufnqpepjaip7`

- [ ] **Step 1: Create or update the Keycloak realm and roles**

Use the Keycloak admin console on `https://auth.etharlia.com/admin/`.

Apply these exact values:

- Realm: `etharlia`
- Realm role `user`
- Realm role `admin`

Then import the client settings from `ops/keycloak/etharlia-boontory-clients.json`.

- [ ] **Step 2: Create the `oauth2-proxy` confidential client secret and copy it into Coolify**

In Keycloak:
- open client `oauth2-proxy`
- go to **Credentials**
- copy the generated secret

Store that value only in Coolify as `OAUTH2_PROXY_CLIENT_SECRET`.

- [ ] **Step 3: Create the new `oauth2-proxy` Coolify app**

Create a Docker image application with these exact values:

- Project UUID: `xtmri3iccgvn26tz7g3nprxh`
- Environment UUID: `os515y0qkzdrj37aw0of3141`
- Server UUID: `cqx75kjn3hg9vll54g41c0er`
- Name: `oauth2-proxy`
- Image: `quay.io/oauth2-proxy/oauth2-proxy:v7.15.1`
- FQDN: `https://oauth.etharlia.com`
- Exposed port: `4180`
- Health check path: `/ping`

Set these env vars from `ops/oauth2-proxy/oauth2-proxy.env.example`, plus three secrets kept only in Coolify:

- `OAUTH2_PROXY_CLIENT_ID=oauth2-proxy`
- the generated Keycloak client secret for `oauth2-proxy`
- a freshly generated 32-byte base64 cookie secret

- [ ] **Step 4: Add the Boontory build-time and runtime env vars in Coolify**

On app `p7naj0uvggxjs36fifqgsyuh`, add build-time env:

- `VITE_KEYCLOAK_URL=https://auth.etharlia.com`
- `VITE_KEYCLOAK_REALM=etharlia`
- `VITE_KEYCLOAK_CLIENT_ID=boontory-frontend`

Add runtime env:

- `KEYCLOAK_ISSUER_URI=https://auth.etharlia.com/realms/etharlia`
- `FRONTEND_ORIGIN_PATTERNS=http://localhost:5173,http://127.0.0.1:5173,https://boontory.etharlia.com`
- `BOONTORY_DB_PATH=/app/data/boontory.db`

- [ ] **Step 5: Append the forward-auth labels to the live Boontory app**

Copy the exact contents of `ops/coolify/boontory-forward-auth.labels` into the Boontory app custom labels so the live HTTPS router `https-0-p7naj0uvggxjs36fifqgsyuh` gains the middleware `boontory-auth@docker`.

- [ ] **Step 6: Redeploy `oauth2-proxy` and Boontory**

Redeploy both resources after the env vars and labels are set.

Expected:
- `https://oauth.etharlia.com/ping` returns `200`
- `https://boontory.etharlia.com` redirects to oauth2-proxy / Keycloak when the browser has no session cookie

- [ ] **Step 7: Verify the end-to-end flow**

Run: `curl -I https://oauth.etharlia.com/ping && curl -I https://boontory.etharlia.com`

Expected:
- the first command returns `HTTP/2 200`
- the second command returns `HTTP/2 302` with a `location:` header pointing at `oauth.etharlia.com` or `auth.etharlia.com`

Then complete these browser checks:
- login once on `https://boontory.etharlia.com`
- open `https://hub.etharlia.com` in the same browser session and confirm no second credential prompt is needed
- use Boontory normally and confirm `/api/books` still works after page refresh
- click **Sign out** in Boontory and confirm the next visit returns to the Keycloak login flow

- [ ] **Step 8: Record the rollout results in the PR / task summary**

Document these exact facts in the implementation summary:
- the Keycloak realm name used
- the oauth2-proxy Coolify app UUID created by Coolify
- the Boontory app UUID `p7naj0uvggxjs36fifqgsyuh`
- whether `curl -I https://boontory.etharlia.com` returned `302`
- whether the cross-subdomain SSO check on `hub.etharlia.com` worked

## Spec Coverage Check

- Global mandatory auth for `*.etharlia.com` — covered by Task 4 repo artifacts plus Task 5 live `oauth2-proxy` and Traefik `forwardAuth`
- Boontory frontend OIDC — covered by Tasks 1 and 2
- Boontory backend JWT resource server — covered by Task 3
- Keycloak setup — covered by Task 4 repo artifact plus Task 5 live console changes
- Coolify setup — covered by Task 4 docs/artifacts plus Task 5 live app/env/label changes
- Docs and rollout repeatability — covered by Task 4

## Verification Checklist

- `cd frontend && npm run test`
- `cd frontend && npm run build`
- `cd backend && ./gradlew test`
- `cd /home/ventura/Documents/boontory && docker build --build-arg VITE_KEYCLOAK_URL=https://auth.etharlia.com --build-arg VITE_KEYCLOAK_REALM=etharlia --build-arg VITE_KEYCLOAK_CLIENT_ID=boontory-frontend -t boontory-auth .`
- `curl -I https://oauth.etharlia.com/ping`
- `curl -I https://boontory.etharlia.com`
- Browser check: login on Boontory, then open `hub.etharlia.com`, then sign out and verify a new login is required
