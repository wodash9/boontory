export type AuthMode = 'keycloak' | 'mock'

export type AuthConfigEnv = Partial<{
  VITE_AUTH_MODE: string
  VITE_AUTH_MOCK_USERNAME: string
  VITE_KEYCLOAK_URL: string
  VITE_KEYCLOAK_REALM: string
  VITE_KEYCLOAK_CLIENT_ID: string
  VITE_SSO_LOGOUT_URL: string
}>

export type AuthConfig = {
  authMode: AuthMode
  mockUsername: string
  url?: string
  realm?: string
  clientId?: string
  ssoLogoutUrl?: string
}

function requireValue(name: keyof AuthConfigEnv, value?: string): string {
  const trimmed = value?.trim()
  if (!trimmed) {
    throw new Error(`${name} is required`)
  }
  return trimmed
}

function optionalValue(value?: string): string | undefined {
  const trimmed = value?.trim()
  return trimmed ? trimmed : undefined
}

function readAuthMode(value?: string): AuthMode {
  const mode = value?.trim() || 'keycloak'
  if (mode === 'keycloak' || mode === 'mock') return mode
  throw new Error('VITE_AUTH_MODE must be keycloak or mock')
}

export function readAuthConfig(env: AuthConfigEnv = import.meta.env): AuthConfig {
  const authMode = readAuthMode(env.VITE_AUTH_MODE)
  const mockUsername = optionalValue(env.VITE_AUTH_MOCK_USERNAME) ?? 'boontory-test'

  if (authMode === 'mock') {
    return {
      authMode,
      mockUsername,
      ssoLogoutUrl: optionalValue(env.VITE_SSO_LOGOUT_URL),
    }
  }

  return {
    authMode,
    mockUsername,
    url: requireValue('VITE_KEYCLOAK_URL', env.VITE_KEYCLOAK_URL),
    realm: requireValue('VITE_KEYCLOAK_REALM', env.VITE_KEYCLOAK_REALM),
    clientId: requireValue('VITE_KEYCLOAK_CLIENT_ID', env.VITE_KEYCLOAK_CLIENT_ID),
    ssoLogoutUrl: optionalValue(env.VITE_SSO_LOGOUT_URL),
  }
}
