import { describe, expect, it } from 'vitest'
import router from '../../src/router'

describe('router registration route', () => {
  it('keeps /register public so users can reach Keycloak self-signup', () => {
    const route = router.getRoutes().find((candidate) => candidate.path === '/register')

    expect(route).toBeDefined()
    expect(route?.meta.requiresAuth).toBe(false)
  })
})
