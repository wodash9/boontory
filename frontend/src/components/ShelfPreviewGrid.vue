<script setup lang="ts">
import { computed } from 'vue'

const props = defineProps<{
  columns: number
  rows: number
  compact?: boolean
}>()

const cells = computed(() => Array.from({ length: props.columns * props.rows }, (_, index) => index))
</script>

<template>
  <div class="preview" :class="{ compact }" :style="{ gridTemplateColumns: `repeat(${columns}, minmax(0, 1fr))` }">
    <span v-for="cell in cells" :key="cell" class="cell" />
  </div>
</template>

<style scoped>
.preview {
  display: grid;
  gap: 6px;
  min-width: 132px;
}

.cell {
  aspect-ratio: 1;
  border-radius: 8px;
  background: linear-gradient(180deg, rgba(180, 83, 9, 0.18), rgba(180, 83, 9, 0.08));
  border: 1px solid rgba(180, 83, 9, 0.12);
}

.compact {
  min-width: 96px;
  gap: 4px;
}

.compact .cell {
  border-radius: 6px;
}
</style>
