export type AuthGuardDeps = {
  bootstrap: () => Promise<void>
  isAuthenticated: () => boolean
  login: () => Promise<void>
  state: {
    error: string | null
  }
}

export function createAuthGuard(deps: AuthGuardDeps) {
  return async (to: { meta: { requiresAuth?: boolean } }) => {
    if (to.meta.requiresAuth === false) {
      return true
    }

    await deps.bootstrap()

    if (deps.isAuthenticated()) {
      return true
    }

    if (deps.state.error) {
      return false
    }

    await deps.login()
    return false
  }
}
