import { beforeEach, describe, expect, it, vi } from 'vitest'

const { getAccessToken } = vi.hoisted(() => ({
  getAccessToken: vi.fn().mockResolvedValue('bearer-token'),
}))

vi.mock('../../src/auth/authState', () => ({
  auth: {
    getAccessToken,
  },
}))

import { booksApi, buildHeaders } from '../../src/services/api'

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
    const requestOptions = (fetch as any).mock.calls[0][1]
    expect(requestOptions.headers.get('Authorization')).toBe('Bearer bearer-token')
  })

  it('merges custom headers and preserves bearer authorization', () => {
    const headers = buildHeaders('bearer-token', {
      Authorization: 'Bearer bad-token',
      'X-Trace-Id': 'trace-123',
    })

    expect(headers.get('Authorization')).toBe('Bearer bearer-token')
    expect(headers.get('X-Trace-Id')).toBe('trace-123')
    expect(headers.get('Content-Type')).toBe('application/json')
  })
})
