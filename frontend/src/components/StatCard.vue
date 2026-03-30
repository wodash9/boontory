<script setup lang="ts">
defineProps<{
  label: string
  value: string | number
}>()
</script>

<template>
  <article class="stat">
    <p>{{ label }}</p>
    <strong>{{ value }}</strong>
  </article>
</template>

<style scoped>
.stat {
  padding: 18px;
  border-radius: 24px;
  background: var(--surface);
  border: 1px solid var(--border);
}

p,
strong {
  margin: 0;
  display: block;
}

p {
  color: var(--text-soft);
  margin-bottom: 8px;
}

strong {
  font-size: 1.8rem;
}
</style>
