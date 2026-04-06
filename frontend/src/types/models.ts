export type ReadingStatus = 'WANT_TO_READ' | 'READING' | 'READ'

export interface Shelf {
  id: number
  name: string
  columns: number
  rows: number
  layoutCapacity: number
  bookCount: number
}

export interface Book {
  id: number
  isbn?: string | null
  title: string
  authors: string[]
  description?: string | null
  coverUrl?: string | null
  status: ReadingStatus
  rating: number
  notes?: string | null
  publishedYear?: number | null
  dateRead?: string | null
  shelf?: Shelf | null
  createdAt?: string | null
  updatedAt?: string | null
}

export interface UpsertBookPayload {
  isbn?: string | null
  title: string
  authors: string[]
  description?: string | null
  coverUrl?: string | null
  status: ReadingStatus
  rating: number
  notes?: string | null
  publishedYear?: number | null
  dateRead?: string | null
  shelfId?: number | null
}

export interface UpsertShelfPayload {
  name: string
  columns: number
  rows: number
}

export interface LibraryStats {
  totalBooks: number
  byStatus: Record<ReadingStatus, number>
  readByYear: Record<string, number>
}

export interface CatalogBook {
  isbn?: string | null
  title: string
  authors: string[]
  description?: string | null
  coverUrl?: string | null
  publishedYear?: number | null
}

export interface CatalogSearchResponse {
  query: string
  results: CatalogBook[]
}

export interface IsbnLookupResponse {
  isbn: string
  book: CatalogBook | null
  alreadyInLibrary: boolean
}
