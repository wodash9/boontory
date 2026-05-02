import { describe, expect, it } from 'vitest'
import { readAuthConfig } from '../../src/auth/authConfig'

describe('readAuthConfig', () => {
  it('returns trimmed Keycloak env values', () => {
    expect(
      readAuthConfig({
        VITE_KEYCLOAK_URL: ' https://auth.etharlia.com ',
        VITE_KEYCLOAK_REALM: ' etharlia ',
        VITE_KEYCLOAK_CLIENT_ID: ' boontory-frontend ',
        VITE_SSO_LOGOUT_URL: ' https://oauth.etharlia.com/oauth2/sign_out ',
      }),
    ).toEqual({
      authMode: 'keycloak',
      mockUsername: 'boontory-test',
      url: 'https://auth.etharlia.com',
      realm: 'etharlia',
      clientId: 'boontory-frontend',
      ssoLogoutUrl: 'https://oauth.etharlia.com/oauth2/sign_out',
    })
  })

  it('omits optional SSO logout url when empty', () => {
    expect(
      readAuthConfig({
        VITE_KEYCLOAK_URL: 'https://auth.etharlia.com',
        VITE_KEYCLOAK_REALM: 'etharlia',
        VITE_KEYCLOAK_CLIENT_ID: 'boontory-frontend',
        VITE_SSO_LOGOUT_URL: '   ',
      }),
    ).toEqual({
      authMode: 'keycloak',
      mockUsername: 'boontory-test',
      url: 'https://auth.etharlia.com',
      realm: 'etharlia',
      clientId: 'boontory-frontend',
      ssoLogoutUrl: undefined,
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

  it('returns mock auth config without requiring Keycloak values', () => {
    expect(
      readAuthConfig({
        VITE_AUTH_MODE: 'mock',
        VITE_AUTH_MOCK_USERNAME: ' qa-reader ',
      }),
    ).toEqual({
      authMode: 'mock',
      mockUsername: 'qa-reader',
      ssoLogoutUrl: undefined,
    })
  })

  it('rejects unknown auth modes', () => {
    expect(() => readAuthConfig({ VITE_AUTH_MODE: 'disabled' })).toThrow('VITE_AUTH_MODE must be keycloak or mock')
  })
})
