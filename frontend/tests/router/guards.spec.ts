import { describe, expect, it, vi } from 'vitest'
import { createAuthGuard } from '../../src/router/guards'

describe('createAuthGuard', () => {
  it('bootstraps auth before protected navigation', async () => {
    const deps = {
      bootstrap: vi.fn().mockResolvedValue(undefined),
      isAuthenticated: vi.fn().mockReturnValue(true),
      login: vi.fn().mockResolvedValue(undefined),
      state: { error: null },
    }

    const guard = createAuthGuard(deps)
    const result = await guard({ meta: { requiresAuth: true } } as any)

    expect(deps.bootstrap).toHaveBeenCalledTimes(1)
    expect(result).toBe(true)
  })

  it('kicks off login when unauthenticated and no bootstrap error', async () => {
    const deps = {
      bootstrap: vi.fn().mockResolvedValue(undefined),
      isAuthenticated: vi.fn().mockReturnValue(false),
      login: vi.fn().mockResolvedValue(undefined),
      state: { error: null },
    }

    const guard = createAuthGuard(deps)
    const result = await guard({ meta: { requiresAuth: true } } as any)

    expect(deps.login).toHaveBeenCalledTimes(1)
    expect(result).toBe(false)
  })

  it('does not login when bootstrap has error', async () => {
    const deps = {
      bootstrap: vi.fn().mockResolvedValue(undefined),
      isAuthenticated: vi.fn().mockReturnValue(false),
      login: vi.fn().mockResolvedValue(undefined),
      state: { error: 'VITE_KEYCLOAK_URL is required' },
    }

    const guard = createAuthGuard(deps)
    const result = await guard({ meta: { requiresAuth: true } } as any)

    expect(deps.login).not.toHaveBeenCalled()
    expect(result).toBe(false)
  })
})
