<script setup lang="ts">
import { reactive, watch } from 'vue'
import ShelfPreviewGrid from './ShelfPreviewGrid.vue'

interface ShelfDraft {
  name: string
  columns: number
  rows: number
}

const props = defineProps<{
  initialValue?: Partial<ShelfDraft>
  submitLabel: string
  busy?: boolean
}>()

const emit = defineEmits<{
  (event: 'submit', value: ShelfDraft): void
  (event: 'cancel'): void
}>()

const form = reactive<ShelfDraft>({
  name: '',
  columns: 4,
  rows: 3,
})

watch(
  () => props.initialValue,
  (value) => {
    form.name = value?.name ?? ''
    form.columns = value?.columns ?? 4
    form.rows = value?.rows ?? 3
  },
  { immediate: true },
)

function handleSubmit() {
  emit('submit', {
    name: form.name.trim(),
    columns: Math.min(12, Math.max(1, form.columns)),
    rows: Math.min(12, Math.max(1, form.rows)),
  })
}
</script>

<template>
  <form class="form" @submit.prevent="handleSubmit">
    <div>
      <h3>{{ submitLabel }}</h3>
      <p>Define the visible shelf grid. The book count can grow beyond these slots.</p>
    </div>

    <label>
      <span>Name</span>
      <input v-model="form.name" required maxlength="80" placeholder="Living room tall shelf" />
    </label>

    <div class="grid">
      <label>
        <span>Columns</span>
        <input v-model.number="form.columns" type="number" min="1" max="12" required />
      </label>
      <label>
        <span>Rows</span>
        <input v-model.number="form.rows" type="number" min="1" max="12" required />
      </label>
    </div>

    <div class="preview-wrap">
      <div>
        <strong>{{ form.columns }} × {{ form.rows }} layout</strong>
        <p>{{ form.columns * form.rows }} visible slots before stacking.</p>
      </div>
      <ShelfPreviewGrid :columns="form.columns" :rows="form.rows" />
    </div>

    <div class="actions">
      <button type="button" class="ghost" @click="$emit('cancel')">Cancel</button>
      <button type="submit" class="submit" :disabled="busy || !form.name.trim()">
        {{ busy ? 'Saving…' : submitLabel }}
      </button>
    </div>
  </form>
</template>

<style scoped>
.form,
label,
.grid,
.preview-wrap,
.actions {
  display: grid;
  gap: 14px;
}

.form {
  padding: 20px;
  border-radius: 28px;
  background: var(--surface);
  border: 1px solid var(--border);
}

.grid,
.actions,
.preview-wrap {
  grid-template-columns: repeat(2, minmax(0, 1fr));
}

input {
  width: 100%;
  border: 1px solid rgba(120, 53, 15, 0.18);
  border-radius: 16px;
  padding: 12px 14px;
  background: #fff;
}

span,
strong,
h3,
p {
  margin: 0;
}

span {
  font-weight: 600;
}

p {
  color: var(--text-soft);
}

.preview-wrap {
  align-items: center;
  padding: 16px;
  border-radius: 20px;
  background: rgba(180, 83, 9, 0.06);
}

.ghost,
.submit {
  border: none;
  border-radius: 16px;
  padding: 14px 18px;
  font-weight: 700;
}

.ghost {
  background: rgba(28, 25, 23, 0.08);
}

.submit {
  background: var(--accent);
  color: white;
}

@media (max-width: 720px) {
  .grid,
  .preview-wrap,
  .actions {
    grid-template-columns: 1fr;
  }
}
</style>
