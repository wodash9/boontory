import { afterEach, describe, expect, it, vi } from 'vitest'
import { createAuthRuntime, createMockAuthRuntime } from '../../src/auth/authState'

function createKeycloakStub(overrides: Partial<any> = {}) {
  return {
    authenticated: true,
    token: 'token-123',
    tokenParsed: { preferred_username: 'ventura' },
    init: vi.fn().mockResolvedValue(true),
    updateToken: vi.fn().mockResolvedValue(true),
    login: vi.fn().mockResolvedValue(undefined),
    register: vi.fn().mockResolvedValue(undefined),
    logout: vi.fn().mockResolvedValue(undefined),
    createLogoutUrl: vi.fn().mockReturnValue('https://auth.etharlia.com/realms/Boontory/protocol/openid-connect/logout?post_logout_redirect_uri=https%3A%2F%2Fboontory.etharlia.com'),
    ...overrides,
  }
}

describe('createAuthRuntime', () => {
  afterEach(() => {
    vi.restoreAllMocks()
  })

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

  it('retries initializeAuth after transient failure', async () => {
    const keycloak = createKeycloakStub({
      init: vi.fn().mockRejectedValueOnce(new Error('temporary outage')).mockResolvedValueOnce(true),
    })
    const runtime = createAuthRuntime(keycloak)

    await expect(runtime.initializeAuth()).rejects.toThrow('temporary outage')
    await expect(runtime.initializeAuth()).resolves.toBeUndefined()

    expect(keycloak.init).toHaveBeenCalledTimes(2)
    expect(runtime.state.error).toBeNull()
    expect(runtime.state.authenticated).toBe(true)
  })

  it('opens the Keycloak registration flow using the app root as redirect URI', async () => {
    const keycloak = createKeycloakStub()
    const runtime = createAuthRuntime(keycloak)

    await runtime.register()

    expect(keycloak.register).toHaveBeenCalledWith({ redirectUri: window.location.origin })
    expect(keycloak.login).not.toHaveBeenCalled()
  })

  it('uses SSO logout redirect with rd keycloak logout url when configured', async () => {
    const keycloak = createKeycloakStub()
    const redirectToUrl = vi.fn()
    const runtime = createAuthRuntime(keycloak, {
      ssoLogoutUrl: 'https://oauth.etharlia.com/oauth2/sign_out',
      redirectToUrl,
    })

    await runtime.logout()

    expect(redirectToUrl).toHaveBeenCalledWith('https://oauth.etharlia.com/oauth2/sign_out?rd=https%3A%2F%2Fauth.etharlia.com%2Frealms%2FBoontory%2Fprotocol%2Fopenid-connect%2Flogout%3Fpost_logout_redirect_uri%3Dhttps%253A%252F%252Fboontory.etharlia.com')
    expect(keycloak.logout).not.toHaveBeenCalled()
    expect(runtime.state.authenticated).toBe(false)
  })



  it('uses ampersand when SSO logout URL already has query params', async () => {
    const keycloak = createKeycloakStub()
    const redirectToUrl = vi.fn()
    const runtime = createAuthRuntime(keycloak, {
      ssoLogoutUrl: 'https://oauth.etharlia.com/oauth2/sign_out?foo=bar',
      redirectToUrl,
    })

    await runtime.logout()

    expect(redirectToUrl).toHaveBeenCalledWith('https://oauth.etharlia.com/oauth2/sign_out?foo=bar&rd=https%3A%2F%2Fauth.etharlia.com%2Frealms%2FBoontory%2Fprotocol%2Fopenid-connect%2Flogout%3Fpost_logout_redirect_uri%3Dhttps%253A%252F%252Fboontory.etharlia.com')
  })

  it('falls back to keycloak logout when SSO logout url not set', async () => {
    const keycloak = createKeycloakStub()
    const runtime = createAuthRuntime(keycloak)

    await runtime.logout()

    expect(keycloak.logout).toHaveBeenCalledTimes(1)
    expect(keycloak.logout).toHaveBeenCalledWith({ redirectUri: window.location.origin })
  })

  it('provides a local mock runtime for browser QA without Keycloak', async () => {
    const runtime = createMockAuthRuntime('qa-reader')

    await runtime.initializeAuth()

    expect(runtime.state.ready).toBe(true)
    expect(runtime.state.authenticated).toBe(true)
    expect(runtime.state.username).toBe('qa-reader')
    await expect(runtime.getAccessToken()).resolves.toBe('boontory-local-qa-token')
  })
})
