<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import EmptyState from '../components/EmptyState.vue'
import SearchResultCard from '../components/SearchResultCard.vue'
import ShelfSelectField from '../components/ShelfSelectField.vue'
import { booksApi, catalogApi, shelvesApi } from '../services/api'
import type { CatalogBook, CatalogSearchResponse, Shelf } from '../types/models'

const router = useRouter()
const query = ref('')
const results = ref<CatalogSearchResponse | null>(null)
const loading = ref(false)
const error = ref('')
const addingIsbn = ref('')
const shelves = ref<Shelf[]>([])
const selectedShelfId = ref<number | null>(null)

async function runSearch() {
  loading.value = true
  error.value = ''
  try {
    results.value = await catalogApi.search(query.value)
  } catch (err) {
    error.value = err instanceof Error ? err.message : 'Search failed'
  } finally {
    loading.value = false
  }
}

async function addBook(book: CatalogBook) {
  addingIsbn.value = book.isbn ?? book.title
  try {
    const created = await booksApi.create({
      title: book.title,
      authors: book.authors,
      isbn: book.isbn,
      description: book.description,
      coverUrl: book.coverUrl,
      status: 'WANT_TO_READ',
      rating: 0,
      notes: '',
      publishedYear: book.publishedYear,
      dateRead: null,
      shelfId: selectedShelfId.value,
    })
    await router.push(`/library/${created.id}`)
  } catch (err) {
    error.value = err instanceof Error ? err.message : 'Unable to add book'
  } finally {
    addingIsbn.value = ''
  }
}

onMounted(async () => {
  try {
    shelves.value = await shelvesApi.list()
  } catch {
    // optional helper data
  }
})
</script>

<template>
  <form class="search" @submit.prevent="runSearch">
    <input v-model="query" placeholder="Search Open Library by title or author" />
    <button type="submit">Search</button>
  </form>

  <ShelfSelectField
    v-model="selectedShelfId"
    :shelves="shelves"
    label="Default shelf for search results"
    helper="Applies when you add a result to your library."
  />

  <p v-if="error" class="error">{{ error }}</p>
  <p v-if="loading" class="muted">Searching catalog…</p>

  <section v-if="results?.results.length" class="results">
    <SearchResultCard
      v-for="book in results.results"
      :key="`${book.isbn || book.title}`"
      :book="book"
      :busy="addingIsbn === (book.isbn ?? book.title)"
      @add="addBook(book)"
    />
  </section>

  <EmptyState
    v-else-if="results && !results.results.length && !loading"
    title="No books found"
    :description="`No books found for '${results.query}'. Try another keyword.`"
  />
</template>

<style scoped>
.search,
.results {
  display: grid;
  gap: 14px;
}

.search {
  grid-template-columns: 1fr auto;
  margin-bottom: 18px;
}

input,
button {
  border-radius: 16px;
  padding: 13px 14px;
  border: 1px solid rgba(120, 53, 15, 0.18);
}

input {
  background: rgba(255, 255, 255, 0.84);
}

button {
  border: none;
  background: var(--accent);
  color: white;
  font-weight: 700;
}

.error {
  color: var(--danger);
}

.muted {
  color: var(--text-soft);
}

@media (max-width: 640px) {
  .search {
    grid-template-columns: 1fr;
  }
}
</style>
