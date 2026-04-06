<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import BookForm from '../components/BookForm.vue'
import { booksApi, shelvesApi } from '../services/api'
import type { Book, Shelf, UpsertBookPayload } from '../types/models'

const route = useRoute()
const router = useRouter()
const book = ref<Book | null>(null)
const shelves = ref<Shelf[]>([])
const error = ref('')

const isEdit = computed(() => Boolean(route.params.id))

onMounted(async () => {
  try {
    shelves.value = await shelvesApi.list()
  } catch (err) {
    error.value = err instanceof Error ? err.message : 'Unable to load shelves'
  }

  if (!isEdit.value) return
  try {
    book.value = await booksApi.get(route.params.id as string)
  } catch (err) {
    error.value = err instanceof Error ? err.message : 'Unable to load book'
  }
})

async function saveBook(payload: UpsertBookPayload) {
  try {
    const saved = isEdit.value
      ? await booksApi.update(route.params.id as string, payload)
      : await booksApi.create(payload)
    await router.push(`/library/${saved.id}`)
  } catch (err) {
    error.value = err instanceof Error ? err.message : 'Unable to save book'
  }
}
</script>

<template>
  <section class="page">
    <div>
      <p class="eyebrow">{{ isEdit ? 'Update' : 'Create' }}</p>
      <h2>{{ isEdit ? 'Edit Book' : 'Add a Book Manually' }}</h2>
    </div>
    <p v-if="error" class="error">{{ error }}</p>
    <BookForm
      :initial-value="book ?? undefined"
      :submit-label="isEdit ? 'Save Changes' : 'Create Book'"
      :shelves="shelves"
      @submit="saveBook"
    />
  </section>
</template>

<style scoped>
.page {
  display: grid;
  gap: 18px;
}

.eyebrow {
  margin: 0 0 4px;
  color: var(--accent);
  text-transform: uppercase;
  font-size: 0.78rem;
  font-weight: 700;
}

h2,
.error {
  margin: 0;
}

.error {
  color: var(--danger);
}
</style>
