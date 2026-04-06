<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { RouterLink, useRoute, useRouter } from 'vue-router'
import RatingStars from '../components/RatingStars.vue'
import ShelfPreviewGrid from '../components/ShelfPreviewGrid.vue'
import ShelfSelectField from '../components/ShelfSelectField.vue'
import StatusBadge from '../components/StatusBadge.vue'
import { booksApi, shelvesApi } from '../services/api'
import type { Book, Shelf, UpsertBookPayload } from '../types/models'

const route = useRoute()
const router = useRouter()
const book = ref<Book | null>(null)
const shelves = ref<Shelf[]>([])
const selectedShelfId = ref<number | null>(null)
const shelfSaving = ref(false)
const error = ref('')

const shelfDescription = computed(() => {
  if (!book.value?.shelf) return 'This book is currently unassigned.'
  return `${book.value.shelf.columns} columns × ${book.value.shelf.rows} rows · ${book.value.shelf.layoutCapacity} visible slots · ${book.value.shelf.bookCount} total books assigned`
})

function toPayload(currentBook: Book, shelfId: number | null): UpsertBookPayload {
  return {
    isbn: currentBook.isbn ?? null,
    title: currentBook.title,
    authors: currentBook.authors,
    description: currentBook.description ?? null,
    coverUrl: currentBook.coverUrl ?? null,
    status: currentBook.status,
    rating: currentBook.rating,
    notes: currentBook.notes ?? null,
    publishedYear: currentBook.publishedYear ?? null,
    dateRead: currentBook.dateRead ?? null,
    shelfId,
  }
}

async function loadBook() {
  try {
    book.value = await booksApi.get(route.params.id as string)
    selectedShelfId.value = book.value.shelf?.id ?? null
  } catch (err) {
    error.value = err instanceof Error ? err.message : 'Unable to load book'
  }
}

async function saveShelfAssignment() {
  if (!book.value) return
  shelfSaving.value = true
  error.value = ''

  try {
    book.value = await booksApi.update(book.value.id, toPayload(book.value, selectedShelfId.value))
    selectedShelfId.value = book.value.shelf?.id ?? null
  } catch (err) {
    error.value = err instanceof Error ? err.message : 'Unable to update shelf'
  } finally {
    shelfSaving.value = false
  }
}

async function deleteBook() {
  if (!book.value || !window.confirm(`Delete "${book.value.title}"?`)) return
  await booksApi.remove(book.value.id)
  await router.push('/library')
}

onMounted(async () => {
  await Promise.allSettled([
    loadBook(),
    shelvesApi.list().then((value) => {
      shelves.value = value
    }),
  ])
})
</script>

<template>
  <p v-if="error" class="error">{{ error }}</p>

  <section v-if="book" class="detail">
    <img
      class="cover"
      :src="book.coverUrl || 'https://placehold.co/240x340/f5efe6/8b5e34?text=No+Cover'"
      :alt="book.title"
    />
    <div class="body">
      <div class="header">
        <div>
          <h2>{{ book.title }}</h2>
          <p>{{ book.authors.join(', ') || 'Unknown author' }}</p>
        </div>
        <StatusBadge :status="book.status" />
      </div>

      <RatingStars :model-value="book.rating" readonly />
      <p>{{ book.description || 'No description available.' }}</p>

      <dl>
        <div><dt>ISBN</dt><dd>{{ book.isbn || 'Not set' }}</dd></div>
        <div><dt>Published</dt><dd>{{ book.publishedYear || 'Unknown' }}</dd></div>
        <div><dt>Date Read</dt><dd>{{ book.dateRead || 'Not set' }}</dd></div>
      </dl>

      <section class="location">
        <div class="section-head">
          <div>
            <h3>Shelf assignment</h3>
            <p>{{ shelfDescription }}</p>
          </div>
          <RouterLink to="/shelves" class="manage-link">Manage shelves</RouterLink>
        </div>

        <div v-if="book.shelf" class="shelf-card">
          <div>
            <strong>{{ book.shelf.name }}</strong>
            <p>{{ book.shelf.columns }} columns × {{ book.shelf.rows }} rows</p>
          </div>
          <ShelfPreviewGrid :columns="book.shelf.columns" :rows="book.shelf.rows" compact />
        </div>

        <div class="assign-row">
          <ShelfSelectField v-model="selectedShelfId" :shelves="shelves" compact />
          <button type="button" class="assign" :disabled="shelfSaving" @click="saveShelfAssignment">
            {{ shelfSaving ? 'Saving…' : 'Save Shelf' }}
          </button>
        </div>
      </section>

      <section class="notes">
        <h3>Notes</h3>
        <p>{{ book.notes || 'No notes added.' }}</p>
      </section>

      <div class="actions">
        <RouterLink :to="`/library/${book.id}/edit`" class="edit">Edit</RouterLink>
        <button type="button" class="delete" @click="deleteBook">Delete</button>
      </div>
    </div>
  </section>
</template>

<style scoped>
.detail {
  display: grid;
  grid-template-columns: 260px 1fr;
  gap: 22px;
  padding: 20px;
  border-radius: 28px;
  background: var(--surface);
  border: 1px solid var(--border);
}

.cover {
  width: 100%;
  max-width: 260px;
  border-radius: 24px;
  object-fit: cover;
}

.body,
dl,
.actions,
.location,
.assign-row,
.section-head,
.shelf-card {
  display: grid;
  gap: 16px;
}

.header {
  display: flex;
  justify-content: space-between;
  gap: 12px;
}

h2,
h3,
p,
dt,
dd {
  margin: 0;
}

dt {
  color: var(--text-soft);
}

.actions {
  grid-template-columns: repeat(2, minmax(0, 1fr));
}

.section-head,
.shelf-card {
  grid-template-columns: 1fr auto;
  align-items: start;
}

.location {
  padding: 18px;
  border-radius: 22px;
  background: rgba(180, 83, 9, 0.06);
}

.assign-row {
  grid-template-columns: 1fr auto;
  align-items: end;
}

.manage-link {
  color: var(--accent);
  font-weight: 700;
}

.assign {
  border: none;
  border-radius: 16px;
  padding: 14px 18px;
  background: var(--accent);
  color: white;
  font-weight: 700;
}

.shelf-card {
  padding: 16px;
  border-radius: 18px;
  background: rgba(255, 255, 255, 0.8);
  border: 1px solid var(--border);
}

.location p,
.shelf-card p {
  color: var(--text-soft);
}

.edit,
.delete {
  border: none;
  border-radius: 16px;
  padding: 14px;
  text-align: center;
  font-weight: 700;
}

.edit {
  background: var(--accent);
  color: white;
}

.delete {
  background: rgba(185, 28, 28, 0.12);
  color: var(--danger);
}

.error {
  color: var(--danger);
}

@media (max-width: 760px) {
  .detail {
    grid-template-columns: 1fr;
  }

  .section-head,
  .shelf-card,
  .assign-row {
    grid-template-columns: 1fr;
  }
}
</style>
