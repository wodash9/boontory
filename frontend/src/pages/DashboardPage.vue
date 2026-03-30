<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { RouterLink } from 'vue-router'
import StatCard from '../components/StatCard.vue'
import { booksApi } from '../services/api'
import type { LibraryStats } from '../types/models'

const stats = ref<LibraryStats | null>(null)
const error = ref('')

onMounted(async () => {
  try {
    stats.value = await booksApi.stats()
  } catch (err) {
    error.value = err instanceof Error ? err.message : 'Unable to load stats'
  }
})
</script>

<template>
  <section class="hero">
    <div>
      <p class="kicker">Built for quick scanning</p>
      <h2>Scan, enrich, and manage your personal library from one mobile-first workflow.</h2>
    </div>
    <div class="actions">
      <RouterLink to="/scan" class="primary">Scan ISBN</RouterLink>
      <RouterLink to="/library/new" class="secondary">Add Manually</RouterLink>
    </div>
  </section>

  <section v-if="stats" class="stats">
    <StatCard label="Total Books" :value="stats.totalBooks" />
    <StatCard label="Want to Read" :value="stats.byStatus.WANT_TO_READ ?? 0" />
    <StatCard label="Reading" :value="stats.byStatus.READING ?? 0" />
    <StatCard label="Read" :value="stats.byStatus.READ ?? 0" />
  </section>

  <p v-if="error" class="error">{{ error }}</p>
</template>

<style scoped>
.hero {
  display: grid;
  gap: 18px;
  padding: 24px;
  border-radius: 32px;
  background:
    linear-gradient(135deg, rgba(180, 83, 9, 0.96), rgba(146, 64, 14, 0.88)),
    linear-gradient(45deg, rgba(255, 255, 255, 0.1), transparent);
  color: white;
}

.kicker {
  margin: 0 0 8px;
  text-transform: uppercase;
  letter-spacing: 0.08em;
  font-size: 0.78rem;
}

h2 {
  margin: 0;
  font-size: clamp(1.7rem, 4vw, 2.4rem);
}

.actions,
.stats {
  display: grid;
  gap: 12px;
}

.actions {
  grid-template-columns: repeat(2, minmax(0, 1fr));
}

.stats {
  grid-template-columns: repeat(4, minmax(0, 1fr));
  margin-top: 20px;
}

.primary,
.secondary {
  text-align: center;
  border-radius: 16px;
  padding: 14px;
  font-weight: 700;
}

.primary {
  background: #fff;
  color: var(--accent-strong);
}

.secondary {
  background: rgba(255, 255, 255, 0.14);
  color: white;
}

.error {
  color: var(--danger);
}

@media (max-width: 720px) {
  .actions,
  .stats {
    grid-template-columns: 1fr;
  }
}
</style>
