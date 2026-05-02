import { afterEach, beforeAll, vi } from 'vitest'

beforeAll(() => {
  vi.stubEnv('VITE_KEYCLOAK_URL', 'https://auth.test')
  vi.stubEnv('VITE_KEYCLOAK_REALM', 'etharlia')
  vi.stubEnv('VITE_KEYCLOAK_CLIENT_ID', 'boontory-frontend')
})

afterEach(() => {
  vi.restoreAllMocks()
})
