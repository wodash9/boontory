<script setup lang="ts">
import { nextTick, onBeforeUnmount, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import SearchResultCard from '../components/SearchResultCard.vue'
import ShelfSelectField from '../components/ShelfSelectField.vue'
import ScannerViewfinder from '../components/ScannerViewfinder.vue'
import { booksApi, catalogApi, shelvesApi } from '../services/api'
import { scanIsbnFromVideo } from '../services/barcodeScanner'
import type { CatalogBook, Shelf } from '../types/models'

const router = useRouter()
const videoRef = ref<HTMLVideoElement | null>(null)
const manualIsbn = ref('')
const lookupBusy = ref(false)
const scanning = ref(false)
const error = ref('')
const foundBook = ref<CatalogBook | null>(null)
const detectedIsbn = ref('')
const shelves = ref<Shelf[]>([])
const selectedShelfId = ref<number | null>(null)

let stopScanner: (() => void) | null = null

async function startScanner() {
  error.value = ''
  scanning.value = true
  foundBook.value = null
  detectedIsbn.value = ''

  await nextTick()
  if (!videoRef.value) return

  try {
    stopScanner = await scanIsbnFromVideo(videoRef.value, async (value) => {
      detectedIsbn.value = value
      scanning.value = false
      await lookupIsbn(value)
    })
  } catch (err) {
    scanning.value = false
    error.value = err instanceof Error ? err.message : 'Camera access failed'
  }
}

async function lookupIsbn(isbn = manualIsbn.value) {
  lookupBusy.value = true
  error.value = ''
  foundBook.value = null
  detectedIsbn.value = isbn
  try {
    const response = await catalogApi.lookupByIsbn(isbn)
    foundBook.value = response.book
    if (!response.book) {
      error.value = 'Book not found in Open Library.'
    }
  } catch (err) {
    error.value = err instanceof Error ? err.message : 'Lookup failed'
  } finally {
    lookupBusy.value = false
  }
}

async function addDetectedBook() {
  if (!foundBook.value) return
  const created = await booksApi.create({
    title: foundBook.value.title,
    authors: foundBook.value.authors,
    isbn: foundBook.value.isbn ?? detectedIsbn.value,
    description: foundBook.value.description,
    coverUrl: foundBook.value.coverUrl,
    status: 'WANT_TO_READ',
    rating: 0,
    notes: '',
    publishedYear: foundBook.value.publishedYear,
    dateRead: null,
    shelfId: selectedShelfId.value,
  })
  await router.push(`/library/${created.id}`)
}

function resetScan() {
  stopScanner?.()
  void startScanner()
}

onBeforeUnmount(() => stopScanner?.())
onMounted(async () => {
  try {
    shelves.value = await shelvesApi.list()
  } catch {
    // non-blocking helper data
  }
})
</script>

<template>
  <section class="scan-page">
    <div class="viewer">
      <video ref="videoRef" class="video" autoplay playsinline muted />
      <ScannerViewfinder :scanning="scanning" />
    </div>

    <div class="controls">
      <button type="button" class="primary" @click="startScanner">Start Camera Scan</button>
      <div class="manual">
        <input v-model="manualIsbn" placeholder="Enter ISBN-10 or ISBN-13" />
        <button type="button" @click="lookupIsbn()" :disabled="lookupBusy || manualIsbn.length < 10">
          {{ lookupBusy ? 'Looking up…' : 'Look Up ISBN' }}
        </button>
      </div>
      <p v-if="detectedIsbn" class="muted">Detected ISBN: {{ detectedIsbn }}</p>
      <p v-if="error" class="error">{{ error }}</p>
    </div>

    <section v-if="foundBook" class="result">
      <SearchResultCard :book="foundBook" hide-action />
      <div class="post-scan">
        <ShelfSelectField
          v-model="selectedShelfId"
          :shelves="shelves"
          label="Assign after scan"
          helper="Optional now, easy to change later."
        />
        <div class="result-actions">
          <button type="button" class="primary" @click="addDetectedBook">Add to Library</button>
          <button type="button" class="secondary" @click="resetScan">Scan Again</button>
        </div>
      </div>
    </section>
  </section>
</template>

<style scoped>
.scan-page,
.controls,
.manual,
.result {
  display: grid;
  gap: 16px;
}

.post-scan,
.result-actions {
  display: grid;
  gap: 12px;
}

.result-actions {
  grid-template-columns: repeat(2, minmax(0, 1fr));
}

.viewer {
  position: relative;
  min-height: 320px;
  border-radius: 32px;
  overflow: hidden;
  background: #1c1917;
}

.video {
  width: 100%;
  height: 100%;
  min-height: 320px;
  object-fit: cover;
}

.controls {
  margin-top: 16px;
}

.manual {
  grid-template-columns: 1fr auto;
}

input,
button {
  border-radius: 16px;
  padding: 13px 14px;
}

input {
  border: 1px solid rgba(120, 53, 15, 0.18);
}

button {
  border: none;
  font-weight: 700;
}

.primary {
  background: var(--accent);
  color: white;
}

.secondary {
  background: rgba(28, 25, 23, 0.08);
  color: var(--text);
}

.muted {
  color: var(--text-soft);
}

.error {
  color: var(--danger);
}

@media (max-width: 640px) {
  .manual,
  .result-actions {
    grid-template-columns: 1fr;
  }
}
</style>
