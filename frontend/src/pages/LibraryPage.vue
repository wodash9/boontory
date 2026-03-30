<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { RouterLink } from 'vue-router'
import BookCard from '../components/BookCard.vue'
import EmptyState from '../components/EmptyState.vue'
import { booksApi } from '../services/api'
import type { Book, ReadingStatus } from '../types/models'
import { statusOptions } from '../utils/status'

const books = ref<Book[]>([])
const loading = ref(false)
const error = ref('')
const query = ref('')
const status = ref<ReadingStatus | ''>('')

async function loadBooks() {
  loading.value = true
  error.value = ''
  try {
    books.value = await booksApi.list(query.value, status.value || undefined)
  } catch (err) {
    error.value = err instanceof Error ? err.message : 'Unable to load books'
  } finally {
    loading.value = false
  }
}

onMounted(loadBooks)
watch([query, status], loadBooks)

const hasBooks = computed(() => books.value.length > 0)
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
    title="Your library is empty"
    description="Scan a barcode, search Open Library, or add a book manually."
  />
</template>

<style scoped>
.toolbar,
.grid {
  display: grid;
  gap: 14px;
}

.toolbar {
  grid-template-columns: 1.6fr 1fr auto;
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
