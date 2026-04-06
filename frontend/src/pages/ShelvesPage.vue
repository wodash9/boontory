<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import EmptyState from '../components/EmptyState.vue'
import ShelfForm from '../components/ShelfForm.vue'
import ShelfPreviewGrid from '../components/ShelfPreviewGrid.vue'
import { shelvesApi } from '../services/api'
import type { Shelf, UpsertShelfPayload } from '../types/models'

const shelves = ref<Shelf[]>([])
const loading = ref(false)
const saving = ref(false)
const deletingId = ref<number | null>(null)
const editingShelf = ref<Shelf | null>(null)
const showForm = ref(false)
const error = ref('')

const formTitle = computed(() => (editingShelf.value ? 'Edit shelf' : 'Create shelf'))

async function loadShelves() {
  loading.value = true
  error.value = ''
  try {
    shelves.value = await shelvesApi.list()
  } catch (err) {
    error.value = err instanceof Error ? err.message : 'Unable to load shelves'
  } finally {
    loading.value = false
  }
}

function openCreate() {
  editingShelf.value = null
  showForm.value = true
}

function openEdit(shelf: Shelf) {
  editingShelf.value = shelf
  showForm.value = true
}

function closeForm() {
  showForm.value = false
  editingShelf.value = null
}

async function saveShelf(payload: UpsertShelfPayload) {
  saving.value = true
  error.value = ''
  try {
    if (editingShelf.value) {
      await shelvesApi.update(editingShelf.value.id, payload)
    } else {
      await shelvesApi.create(payload)
    }
    closeForm()
    await loadShelves()
  } catch (err) {
    error.value = err instanceof Error ? err.message : 'Unable to save shelf'
  } finally {
    saving.value = false
  }
}

async function deleteShelf(shelf: Shelf) {
  if (deletingId.value !== shelf.id) {
    deletingId.value = shelf.id
    return
  }

  error.value = ''
  try {
    await shelvesApi.remove(shelf.id)
    deletingId.value = null
    if (editingShelf.value?.id === shelf.id) closeForm()
    await loadShelves()
  } catch (err) {
    error.value = err instanceof Error ? err.message : 'Unable to delete shelf'
  }
}

onMounted(loadShelves)
</script>

<template>
  <section class="hero">
    <div>
      <p class="eyebrow">Shelf management</p>
      <h2>Organize books by physical shelf while keeping shelf layouts clear.</h2>
      <p class="subtle">Rows and columns describe the visible shelf face. Books can keep stacking past that layout.</p>
    </div>
    <button type="button" class="primary" @click="openCreate">Create Shelf</button>
  </section>

  <p v-if="error" class="error">{{ error }}</p>
  <p v-if="loading" class="muted">Loading shelves…</p>

  <section class="layout">
    <div class="list-block">
      <section v-if="shelves.length" class="list">
        <article v-for="shelf in shelves" :key="shelf.id" class="card">
          <div class="card-head">
            <div>
              <h3>{{ shelf.name }}</h3>
              <p>{{ shelf.columns }} columns × {{ shelf.rows }} rows · {{ shelf.layoutCapacity }} visible slots</p>
            </div>
            <span class="pill">{{ shelf.bookCount }} books</span>
          </div>

          <ShelfPreviewGrid :columns="shelf.columns" :rows="shelf.rows" />

          <p class="hint">
            Capacity layout shows the shelf face. {{ shelf.bookCount > shelf.layoutCapacity ? 'This shelf is currently stacked beyond the visible grid.' : 'More books can still be assigned beyond this preview.' }}
          </p>

          <div class="actions">
            <button type="button" class="secondary" @click="openEdit(shelf)">Edit</button>
            <button
              type="button"
              class="danger"
              :class="{ confirm: deletingId === shelf.id }"
              @click="deleteShelf(shelf)"
            >
              {{ deletingId === shelf.id ? 'Confirm Delete' : 'Delete' }}
            </button>
          </div>

          <p v-if="deletingId === shelf.id" class="delete-note">
            Deletes the shelf only. Assigned books stay in your library and become unassigned.
          </p>
        </article>
      </section>

      <EmptyState
        v-else-if="!loading"
        title="No shelves yet"
        description="Create your first shelf to organize scanned and saved books by location."
      />
    </div>

    <ShelfForm
      v-if="showForm"
      :initial-value="editingShelf ?? undefined"
      :submit-label="formTitle"
      :busy="saving"
      @submit="saveShelf"
      @cancel="closeForm"
    />
  </section>
</template>

<style scoped>
.hero,
.layout,
.list,
.card,
.card-head,
.actions,
.list-block {
  display: grid;
  gap: 16px;
}

.hero {
  grid-template-columns: 1fr auto;
  align-items: center;
  padding: 24px;
  border-radius: 28px;
  border: 1px solid var(--border);
  background: var(--surface);
  margin-bottom: 18px;
}

.layout {
  grid-template-columns: minmax(0, 1.5fr) minmax(320px, 0.9fr);
  align-items: start;
}

.list {
  grid-template-columns: repeat(auto-fit, minmax(260px, 1fr));
}

.card {
  padding: 18px;
  border-radius: 24px;
  background: var(--surface);
  border: 1px solid var(--border);
  box-shadow: var(--shadow);
}

.card-head,
.actions {
  grid-template-columns: 1fr auto;
  align-items: start;
}

.pill {
  padding: 8px 10px;
  border-radius: 999px;
  background: var(--accent-soft);
  color: var(--accent-strong);
  font-weight: 700;
}

.eyebrow,
h2,
h3,
p {
  margin: 0;
}

.eyebrow {
  color: var(--accent);
  text-transform: uppercase;
  font-size: 0.78rem;
  font-weight: 700;
  letter-spacing: 0.08em;
  margin-bottom: 6px;
}

.subtle,
.hint,
.delete-note,
.muted,
.card p {
  color: var(--text-soft);
}

.primary,
.secondary,
.danger {
  border: none;
  border-radius: 16px;
  padding: 12px 14px;
  font-weight: 700;
}

.primary {
  background: var(--accent);
  color: white;
}

.secondary {
  background: rgba(28, 25, 23, 0.08);
}

.danger {
  background: rgba(185, 28, 28, 0.1);
  color: var(--danger);
}

.danger.confirm {
  background: var(--danger);
  color: white;
}

.error {
  color: var(--danger);
}

@media (max-width: 880px) {
  .layout,
  .hero,
  .card-head,
  .actions {
    grid-template-columns: 1fr;
  }
}
</style>
