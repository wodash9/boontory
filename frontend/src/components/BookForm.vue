<script setup lang="ts">
import { reactive, watch } from 'vue'
import type { Book, ReadingStatus, Shelf, UpsertBookPayload } from '../types/models'
import { statusOptions } from '../utils/status'
import RatingStars from './RatingStars.vue'
import ShelfSelectField from './ShelfSelectField.vue'

const props = defineProps<{
  initialValue?: Partial<Book>
  submitLabel?: string
  shelves?: Shelf[]
}>()

const emit = defineEmits<{
  (event: 'submit', value: UpsertBookPayload): void
}>()

interface BookFormState extends Omit<UpsertBookPayload, 'publishedYear' | 'dateRead' | 'shelfId'> {
  publishedYear?: number
  dateRead: string
  shelfId: number | null
}

const form = reactive<BookFormState>({
  isbn: '',
  title: '',
  authors: [],
  description: '',
  coverUrl: '',
  status: 'WANT_TO_READ',
  rating: 0,
  notes: '',
  publishedYear: undefined,
  dateRead: '',
  shelfId: null,
})

watch(
  () => props.initialValue,
  (value) => {
    form.isbn = value?.isbn ?? ''
    form.title = value?.title ?? ''
    form.authors = value?.authors ?? []
    form.description = value?.description ?? ''
    form.coverUrl = value?.coverUrl ?? ''
    form.status = (value?.status as ReadingStatus | undefined) ?? 'WANT_TO_READ'
    form.rating = value?.rating ?? 0
    form.notes = value?.notes ?? ''
    form.publishedYear = value?.publishedYear ?? undefined
    form.dateRead = value?.dateRead ?? ''
    form.shelfId = value?.shelf?.id ?? null
  },
  { immediate: true },
)

function handleSubmit() {
  emit('submit', {
    ...form,
    isbn: form.isbn?.trim() || null,
    coverUrl: form.coverUrl?.trim() || null,
    description: form.description?.trim() || null,
    notes: form.notes?.trim() || null,
    dateRead: form.dateRead || null,
    shelfId: form.shelfId || null,
    authors: Array.from(
      new Set(
        form.authors
          .join(',')
          .split(',')
          .map((item) => item.trim())
          .filter(Boolean),
      ),
    ),
  })
}
</script>

<template>
  <form class="form" @submit.prevent="handleSubmit">
    <label>
      <span>Title</span>
      <input v-model="form.title" required placeholder="The Hobbit" />
    </label>
    <label>
      <span>Authors</span>
      <input
        :value="form.authors.join(', ')"
        placeholder="J.R.R. Tolkien"
        @input="form.authors = ($event.target as HTMLInputElement).value.split(',')"
      />
    </label>
    <label>
      <span>ISBN</span>
      <input v-model="form.isbn" placeholder="9780261103344" />
    </label>
    <ShelfSelectField v-model="form.shelfId" :shelves="props.shelves ?? []" />
    <div class="grid">
      <label>
        <span>Status</span>
        <select v-model="form.status">
          <option v-for="option in statusOptions" :key="option.value" :value="option.value">
            {{ option.label }}
          </option>
        </select>
      </label>
      <label>
        <span>Published Year</span>
        <input
          :value="form.publishedYear ?? ''"
          type="number"
          placeholder="1937"
          @input="form.publishedYear = Number(($event.target as HTMLInputElement).value) || undefined"
        />
      </label>
    </div>
    <label>
      <span>Cover URL</span>
      <input v-model="form.coverUrl" placeholder="https://..." />
    </label>
    <label>
      <span>Description</span>
      <textarea v-model="form.description" rows="4" placeholder="Short summary" />
    </label>
    <label>
      <span>Notes</span>
      <textarea v-model="form.notes" rows="4" placeholder="Personal notes" />
    </label>
    <div class="grid">
      <label>
        <span>Date Read</span>
        <input v-model="form.dateRead" type="date" />
      </label>
      <label>
        <span>Rating</span>
        <RatingStars v-model="form.rating" />
      </label>
    </div>
    <button type="submit" class="submit">{{ submitLabel ?? 'Save Book' }}</button>
  </form>
</template>

<style scoped>
.form,
label {
  display: grid;
  gap: 10px;
}

.form {
  padding: 20px;
  border-radius: 28px;
  background: var(--surface);
  border: 1px solid var(--border);
}

.grid {
  display: grid;
  gap: 16px;
  grid-template-columns: repeat(2, minmax(0, 1fr));
}

span {
  font-weight: 600;
}

input,
textarea,
select {
  width: 100%;
  border: 1px solid rgba(120, 53, 15, 0.18);
  border-radius: 16px;
  padding: 12px 14px;
  background: #fff;
}

.submit {
  border: none;
  border-radius: 16px;
  padding: 14px 18px;
  background: var(--accent);
  color: white;
  font-weight: 700;
}

@media (max-width: 720px) {
  .grid {
    grid-template-columns: 1fr;
  }
}
</style>
