<script setup lang="ts">
import { RouterView, useRoute } from 'vue-router'
import BottomNav from '../components/BottomNav.vue'

const route = useRoute()
</script>

<template>
  <div class="shell">
    <header class="topbar">
      <div>
        <p class="eyebrow">Personal Book Library</p>
        <h1>Boontory</h1>
      </div>
      <p class="route-label">{{ route.name }}</p>
    </header>

    <main class="content">
      <RouterView />
    </main>

    <BottomNav />
  </div>
</template>

<style scoped>
.shell {
  min-height: 100vh;
  max-width: 1100px;
  margin: 0 auto;
  padding: 24px 20px 92px;
}

.topbar {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  align-items: end;
  margin-bottom: 24px;
}

.eyebrow {
  margin: 0 0 4px;
  color: var(--accent);
  text-transform: uppercase;
  letter-spacing: 0.08em;
  font-size: 0.78rem;
  font-weight: 700;
}

h1 {
  margin: 0;
  font-size: clamp(2rem, 5vw, 3rem);
  line-height: 0.95;
}

.route-label {
  margin: 0;
  color: var(--text-soft);
  text-transform: capitalize;
}

.content {
  display: grid;
}

@media (max-width: 720px) {
  .shell {
    padding-inline: 14px;
  }

  .topbar {
    align-items: start;
    flex-direction: column;
  }
}
</style>
