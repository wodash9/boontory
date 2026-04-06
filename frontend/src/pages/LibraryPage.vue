<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { RouterLink } from 'vue-router'
import BookCard from '../components/BookCard.vue'
import EmptyState from '../components/EmptyState.vue'
import { booksApi, shelvesApi } from '../services/api'
import type { Book, ReadingStatus, Shelf } from '../types/models'
import { statusOptions } from '../utils/status'

const books = ref<Book[]>([])
const shelves = ref<Shelf[]>([])
const loading = ref(false)
const error = ref('')
const query = ref('')
const status = ref<ReadingStatus | ''>('')
const shelfId = ref<number | ''>('')

async function loadBooks() {
  loading.value = true
  error.value = ''
  try {
    books.value = await booksApi.list(query.value, status.value || undefined, shelfId.value || undefined)
  } catch (err) {
    error.value = err instanceof Error ? err.message : 'Unable to load books'
  } finally {
    loading.value = false
  }
}

onMounted(async () => {
  await Promise.allSettled([
    loadBooks(),
    shelvesApi.list().then((value) => {
      shelves.value = value
    }),
  ])
})
watch([query, status, shelfId], loadBooks)

const hasBooks = computed(() => books.value.length > 0)
const isFiltered = computed(() => Boolean(query.value || status.value || shelfId.value))
</script>

<template>
  <section class="toolbar">
    <input v-model="query" placeholder="Search by title or author" />
    <select v-model="status">
      <option value="">All statuses</option>
      <option v-for="option in statusOptions" :key="option.value" :value="option.value">
        {{ option.label }}
      </option>
    </select>
    <select v-model="shelfId">
      <option value="">All shelves</option>
      <option v-for="shelf in shelves" :key="shelf.id" :value="shelf.id">
        {{ shelf.name }}
      </option>
    </select>
    <RouterLink class="add" to="/library/new">New Book</RouterLink>
  </section>

  <p v-if="error" class="error">{{ error }}</p>
  <p v-if="loading" class="muted">Loading books…</p>

  <section v-if="hasBooks" class="grid">
    <RouterLink v-for="book in books" :key="book.id" :to="`/library/${book.id}`">
      <BookCard :book="book" />
    </RouterLink>
  </section>

  <EmptyState
    v-else-if="!loading"
    :title="isFiltered ? 'No books match these filters' : 'Your library is empty'"
    :description="isFiltered ? 'Try a different search, status, or shelf filter.' : 'Scan a barcode, search Open Library, or add a book manually.'"
  />
</template>

<style scoped>
.toolbar,
.grid {
  display: grid;
  gap: 14px;
}

.toolbar {
  grid-template-columns: 1.6fr 1fr 1fr auto;
  margin-bottom: 18px;
}

input,
select,
.add {
  border: 1px solid rgba(120, 53, 15, 0.18);
  border-radius: 16px;
  padding: 12px 14px;
  background: rgba(255, 255, 255, 0.84);
}

.add {
  text-align: center;
  background: var(--accent);
  color: white;
  font-weight: 700;
}

.grid {
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
}

.muted {
  color: var(--text-soft);
}

.error {
  color: var(--danger);
}

@media (max-width: 720px) {
  .toolbar {
    grid-template-columns: 1fr;
  }
}
</style>
