<script setup lang="ts">
defineProps<{
  title: string
  description: string
}>()
</script>

<template>
  <section class="empty">
    <div class="icon">📚</div>
    <h2>{{ title }}</h2>
    <p>{{ description }}</p>
  </section>
</template>

<style scoped>
.empty {
  padding: 48px 20px;
  border: 1px dashed var(--border);
  border-radius: 28px;
  text-align: center;
  color: var(--text-soft);
  background: rgba(255, 255, 255, 0.55);
}

.icon {
  font-size: 2rem;
  margin-bottom: 12px;
}

h2 {
  margin: 0 0 8px;
  color: var(--text);
}

p {
  margin: 0;
}
</style>
