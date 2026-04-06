<script setup lang="ts">
import type { Book } from '../types/models'
import RatingStars from './RatingStars.vue'
import StatusBadge from './StatusBadge.vue'

defineProps<{ book: Book }>()
</script>

<template>
  <article class="card">
    <img
      class="cover"
      :src="book.coverUrl || 'https://placehold.co/160x220/f5efe6/8b5e34?text=No+Cover'"
      :alt="book.title"
    />
    <div class="details">
      <div class="title-row">
        <div>
          <h3>{{ book.title }}</h3>
          <p>{{ book.authors.join(', ') || 'Unknown author' }}</p>
        </div>
        <StatusBadge :status="book.status" />
      </div>
      <RatingStars :model-value="book.rating" readonly />
      <p class="meta">
        <span v-if="book.isbn">ISBN {{ book.isbn }}</span>
        <span v-if="book.publishedYear">{{ book.publishedYear }}</span>
        <span v-if="book.shelf">Shelf {{ book.shelf.name }}</span>
      </p>
    </div>
  </article>
</template>

<style scoped>
.card {
  display: grid;
  grid-template-columns: 94px 1fr;
  gap: 16px;
  padding: 16px;
  border-radius: 24px;
  background: var(--surface);
  border: 1px solid var(--border);
  box-shadow: var(--shadow);
}

.cover {
  width: 94px;
  height: 132px;
  object-fit: cover;
  border-radius: 16px;
  background: #f5efe6;
}

.details {
  display: grid;
  gap: 10px;
}

.title-row {
  display: flex;
  justify-content: space-between;
  gap: 12px;
}

h3 {
  margin: 0 0 4px;
  font-size: 1.05rem;
}

p {
  margin: 0;
  color: var(--text-soft);
}

.meta {
  display: flex;
  flex-wrap: wrap;
  gap: 8px 12px;
  font-size: 0.88rem;
}

@media (max-width: 560px) {
  .title-row {
    flex-direction: column;
  }
}
</style>
