<script setup lang="ts">
import { computed } from 'vue'
import type { ReadingStatus } from '../types/models'
import { getStatusLabel } from '../utils/status'

const props = defineProps<{ status: ReadingStatus }>()

const label = computed(() => getStatusLabel(props.status))
</script>

<template>
  <span class="badge" :data-status="status">{{ label }}</span>
</template>

<style scoped>
.badge {
  display: inline-flex;
  align-items: center;
  padding: 6px 10px;
  border-radius: 999px;
  font-size: 0.78rem;
  font-weight: 700;
  background: var(--surface-muted);
  color: var(--accent-strong);
}

.badge[data-status='READ'] {
  color: var(--success);
  background: rgba(22, 101, 52, 0.12);
}

.badge[data-status='READING'] {
  color: #1d4ed8;
  background: rgba(59, 130, 246, 0.12);
}
</style>
