<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { RouterLink, useRoute, useRouter } from 'vue-router'
import RatingStars from '../components/RatingStars.vue'
import StatusBadge from '../components/StatusBadge.vue'
import { booksApi } from '../services/api'
import type { Book } from '../types/models'

const route = useRoute()
const router = useRouter()
const book = ref<Book | null>(null)
const error = ref('')

async function loadBook() {
  try {
    book.value = await booksApi.get(route.params.id as string)
  } catch (err) {
    error.value = err instanceof Error ? err.message : 'Unable to load book'
  }
}

async function deleteBook() {
  if (!book.value || !window.confirm(`Delete "${book.value.title}"?`)) return
  await booksApi.remove(book.value.id)
  await router.push('/library')
}

onMounted(loadBook)
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
.actions {
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
}
</style>
