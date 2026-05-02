import { reactive, readonly } from 'vue'
import { readAuthConfig } from './authConfig'
import { createKeycloakClient, type KeycloakLike } from './keycloakClient'

type AuthSnapshot = {
  ready: boolean
  authenticated: boolean
  username: string | null
  error: string | null
}

type AuthRuntimeOptions = {
  ssoLogoutUrl?: string
  redirectToUrl?: (url: string) => void
}


function appendRdParam(baseUrl: string, redirectUrl: string) {
  const separator = baseUrl.includes('?') ? '&' : '?'
  return `${baseUrl}${separator}rd=${encodeURIComponent(redirectUrl)}`
}

export function createAuthRuntime(keycloak: KeycloakLike, options: AuthRuntimeOptions = {}) {
  const state = reactive<AuthSnapshot>({
    ready: false,
    authenticated: false,
    username: null,
    error: null,
  })

  let initPromise: Promise<void> | null = null

  async function initializeAuth() {
    if (initPromise) return initPromise

    state.ready = false
    initPromise = (async () => {
      try {
        const authenticated = await keycloak.init({
          onLoad: 'check-sso',
          pkceMethod: 'S256',
          checkLoginIframe: false,
        })

        state.authenticated = authenticated
        state.username = keycloak.tokenParsed?.preferred_username?.toString() ?? null
        state.error = null
      } catch (error) {
        state.authenticated = false
        state.username = null
        state.error = error instanceof Error ? error.message : 'Authentication initialization failed'
        initPromise = null
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
    await keycloak.login({ redirectUri: window.location.href })
  }

  async function logout() {
    state.ready = false
    state.authenticated = false
    state.username = null

    const keycloakLogoutUrl = keycloak.createLogoutUrl({ redirectUri: window.location.origin })

    if (options.ssoLogoutUrl) {
      const redirectToUrl = options.redirectToUrl ?? ((url: string) => window.location.assign(url))
      redirectToUrl(appendRdParam(options.ssoLogoutUrl, keycloakLogoutUrl))
      return
    }

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

type Runtime = ReturnType<typeof createAuthRuntime>

const state = reactive<AuthSnapshot>({
  ready: false,
  authenticated: false,
  username: null,
  error: null,
})

let runtime: Runtime | null = null

function syncState(next: AuthSnapshot) {
  state.ready = next.ready
  state.authenticated = next.authenticated
  state.username = next.username
  state.error = next.error
}

async function bootstrapAuth() {
  if (!runtime) {
    try {
      const config = readAuthConfig()
      runtime = createAuthRuntime(createKeycloakClient(config), { ssoLogoutUrl: config.ssoLogoutUrl })
    } catch (error) {
      syncState({
        ready: true,
        authenticated: false,
        username: null,
        error: error instanceof Error ? error.message : 'Authentication bootstrap failed',
      })
      return
    }
  }

  try {
    await runtime.initializeAuth()
  } catch {
    // runtime state already has error
  }

  syncState(runtime.state as AuthSnapshot)
}

export const auth = {
  state: readonly(state),
  async bootstrap() {
    await bootstrapAuth()
  },
  isAuthenticated() {
    return state.authenticated
  },
  async getAccessToken(minValidity?: number) {
    if (!runtime) {
      await bootstrapAuth()
    }
    if (!runtime) {
      throw new Error(state.error ?? 'Authentication runtime is unavailable')
    }

    const token = await runtime.getAccessToken(minValidity)
    syncState(runtime.state as AuthSnapshot)
    return token
  },
  async login() {
    if (!runtime) {
      await bootstrapAuth()
    }
    if (!runtime) {
      return
    }

    await runtime.login()
  },
  async logout() {
    if (!runtime) {
      await bootstrapAuth()
    }
    if (!runtime) {
      return
    }

    await runtime.logout()
    syncState(runtime.state as AuthSnapshot)
  },
}
