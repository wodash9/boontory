<script setup lang="ts">
import type { Shelf } from '../types/models'

const props = withDefaults(
  defineProps<{
    shelves: Shelf[]
    modelValue: number | null
    label?: string
    helper?: string
    compact?: boolean
  }>(),
  {
    label: 'Shelf',
    helper: 'Rows and columns describe the visible shelf layout. Books can still keep stacking beyond those slots.',
    compact: false,
  },
)

const emit = defineEmits<{
  (event: 'update:modelValue', value: number | null): void
}>()

function handleChange(event: Event) {
  const value = Number((event.target as HTMLSelectElement).value)
  emit('update:modelValue', Number.isNaN(value) || value === 0 ? null : value)
}
</script>

<template>
  <label class="field" :class="{ compact }">
    <div class="label-row">
      <span>{{ props.label }}</span>
      <RouterLink to="/shelves" class="manage-link">Manage</RouterLink>
    </div>
    <select :value="modelValue ?? 0" @change="handleChange">
      <option :value="0">No shelf</option>
      <option v-for="shelf in shelves" :key="shelf.id" :value="shelf.id">
        {{ shelf.name }} · {{ shelf.columns }}×{{ shelf.rows }} · {{ shelf.bookCount }} books
      </option>
    </select>
    <small v-if="!compact" class="helper">{{ props.helper }}</small>
  </label>
</template>

<style scoped>
.field,
.label-row {
  display: grid;
  gap: 8px;
}

.label-row {
  grid-template-columns: 1fr auto;
  align-items: center;
}

span {
  font-weight: 600;
}

select {
  width: 100%;
  border: 1px solid rgba(120, 53, 15, 0.18);
  border-radius: 16px;
  padding: 12px 14px;
  background: #fff;
}

.helper {
  color: var(--text-soft);
}

.manage-link {
  color: var(--accent);
  font-weight: 700;
}

.compact .helper {
  display: none;
}
</style>
