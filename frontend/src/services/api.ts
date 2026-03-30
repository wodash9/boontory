import type { Book, CatalogSearchResponse, IsbnLookupResponse, LibraryStats, UpsertBookPayload } from '../types/models'

const API_BASE = import.meta.env.VITE_API_BASE_URL ?? ''

async function request<T>(path: string, init?: RequestInit): Promise<T> {
  const response = await fetch(`${API_BASE}${path}`, {
    headers: {
      'Content-Type': 'application/json',
      ...(init?.headers ?? {}),
    },
    ...init,
  })

  if (!response.ok) {
    const payload = await response.json().catch(() => ({ message: 'Request failed' }))
    throw new Error(payload.message ?? 'Request failed')
  }

  if (response.status === 204) {
    return undefined as T
  }

  return response.json() as Promise<T>
}

export const booksApi = {
  list(query = '', status?: string) {
    const params = new URLSearchParams()
    if (query.trim()) params.set('query', query.trim())
    if (status) params.set('status', status)
    const suffix = params.toString()
    return request<Book[]>(`/api/books${suffix ? `?${suffix}` : ''}`)
  },
  get(id: number | string) {
    return request<Book>(`/api/books/${id}`)
  },
  create(payload: UpsertBookPayload) {
    return request<Book>('/api/books', { method: 'POST', body: JSON.stringify(payload) })
  },
  update(id: number | string, payload: UpsertBookPayload) {
    return request<Book>(`/api/books/${id}`, { method: 'PUT', body: JSON.stringify(payload) })
  },
  remove(id: number | string) {
    return request<void>(`/api/books/${id}`, { method: 'DELETE' })
  },
  stats() {
    return request<LibraryStats>('/api/books/stats')
  },
}

export const catalogApi = {
  search(query: string) {
    const params = new URLSearchParams({ query })
    return request<CatalogSearchResponse>(`/api/catalog/search?${params.toString()}`)
  },
  lookupByIsbn(isbn: string) {
    return request<IsbnLookupResponse>(`/api/catalog/isbn/${isbn}`)
  },
}
