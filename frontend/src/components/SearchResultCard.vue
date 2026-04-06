<script setup lang="ts">
import type { CatalogBook } from '../types/models'

defineProps<{
  book: CatalogBook
  busy?: boolean
  hideAction?: boolean
}>()

defineEmits<{
  (event: 'add'): void
}>()
</script>

<template>
  <article class="card">
    <img
      class="cover"
      :src="book.coverUrl || 'https://placehold.co/160x220/f5efe6/8b5e34?text=Open+Library'"
      :alt="book.title"
    />
    <div class="content">
      <div>
        <h3>{{ book.title }}</h3>
        <p>{{ book.authors.join(', ') || 'Unknown author' }}</p>
      </div>
      <p class="desc">{{ book.description || 'No description available.' }}</p>
      <div class="footer" :class="{ solo: hideAction }">
        <span>{{ book.publishedYear || 'Unknown year' }}</span>
        <button v-if="!hideAction" type="button" class="btn" :disabled="busy" @click="$emit('add')">
          {{ busy ? 'Adding…' : 'Add to Library' }}
        </button>
      </div>
    </div>
  </article>
</template>

<style scoped>
.card {
  display: grid;
  grid-template-columns: 84px 1fr;
  gap: 16px;
  padding: 16px;
  border-radius: 24px;
  border: 1px solid var(--border);
  background: var(--surface);
}

.cover {
  width: 84px;
  height: 120px;
  object-fit: cover;
  border-radius: 14px;
}

.content,
.footer {
  display: grid;
  gap: 10px;
}

h3,
p {
  margin: 0;
}

.desc {
  color: var(--text-soft);
}

.footer {
  grid-template-columns: 1fr auto;
  align-items: center;
}

.footer.solo {
  grid-template-columns: 1fr;
}

.btn {
  border: none;
  border-radius: 14px;
  padding: 10px 14px;
  background: var(--accent);
  color: white;
  font-weight: 700;
}
</style>
