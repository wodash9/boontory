import { auth } from '../auth/authState'
import type {
  Book,
  CatalogSearchResponse,
  IsbnLookupResponse,
  LibraryStats,
  Shelf,
  UpsertBookPayload,
  UpsertShelfPayload,
} from '../types/models'

const API_BASE = import.meta.env.VITE_API_BASE_URL ?? ''

export function buildHeaders(token: string, initHeaders?: HeadersInit): Headers {
  const headers = new Headers(initHeaders)

  if (!headers.has('Content-Type')) {
    headers.set('Content-Type', 'application/json')
  }

  headers.set('Authorization', `Bearer ${token}`)
  return headers
}

async function request<T>(path: string, init?: RequestInit): Promise<T> {
  const token = await auth.getAccessToken()
  const response = await fetch(`${API_BASE}${path}`, {
    ...init,
    headers: buildHeaders(token, init?.headers),
  })

  if (!response.ok) {
    const rawBody = await response.text()
    const contentType = response.headers.get('content-type') ?? ''
    let message = `${response.status} ${response.statusText}`

    if (contentType.includes('application/json')) {
      const payload = JSON.parse(rawBody) as { message?: string }
      message = payload.message ?? message
    } else if (rawBody.trim()) {
      message = rawBody.trim()
    }

    throw new Error(message)
  }

  if (response.status === 204) {
    return undefined as T
  }

  return response.json() as Promise<T>
}

export const booksApi = {
  list(query = '', status?: string, shelfId?: number | string) {
    const params = new URLSearchParams()
    if (query.trim()) params.set('query', query.trim())
    if (status) params.set('status', status)
    if (shelfId !== undefined && shelfId !== '') params.set('shelfId', String(shelfId))
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

export const shelvesApi = {
  list() {
    return request<Shelf[]>('/api/shelves')
  },
  create(payload: UpsertShelfPayload) {
    return request<Shelf>('/api/shelves', { method: 'POST', body: JSON.stringify(payload) })
  },
  update(id: number | string, payload: UpsertShelfPayload) {
    return request<Shelf>(`/api/shelves/${id}`, { method: 'PUT', body: JSON.stringify(payload) })
  },
  remove(id: number | string) {
    return request<void>(`/api/shelves/${id}`, { method: 'DELETE' })
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
