export type AuthConfigEnv = Partial<{
  VITE_KEYCLOAK_URL: string
  VITE_KEYCLOAK_REALM: string
  VITE_KEYCLOAK_CLIENT_ID: string
  VITE_SSO_LOGOUT_URL: string
}>

export type AuthConfig = {
  url: string
  realm: string
  clientId: string
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

export function readAuthConfig(env: AuthConfigEnv = import.meta.env): AuthConfig {
  return {
    url: requireValue('VITE_KEYCLOAK_URL', env.VITE_KEYCLOAK_URL),
    realm: requireValue('VITE_KEYCLOAK_REALM', env.VITE_KEYCLOAK_REALM),
    clientId: requireValue('VITE_KEYCLOAK_CLIENT_ID', env.VITE_KEYCLOAK_CLIENT_ID),
    ssoLogoutUrl: optionalValue(env.VITE_SSO_LOGOUT_URL),
  }
}
