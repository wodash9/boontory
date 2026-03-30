<script setup lang="ts">
import { computed } from 'vue'
import { RouterLink, useRoute } from 'vue-router'

const route = useRoute()

const items = [
  { label: 'Home', to: '/' },
  { label: 'Library', to: '/library' },
  { label: 'Scan', to: '/scan' },
  { label: 'Search', to: '/search' },
]

const activePath = computed(() => route.path)
</script>

<template>
  <nav class="nav">
    <RouterLink
      v-for="item in items"
      :key="item.to"
      :to="item.to"
      class="nav-item"
      :class="{ active: activePath === item.to || activePath.startsWith(`${item.to}/`) }"
    >
      {{ item.label }}
    </RouterLink>
  </nav>
</template>

<style scoped>
.nav {
  position: fixed;
  left: 50%;
  bottom: 16px;
  transform: translateX(-50%);
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 8px;
  width: min(94vw, 560px);
  padding: 10px;
  background: rgba(41, 37, 36, 0.92);
  border-radius: 22px;
  box-shadow: var(--shadow);
  backdrop-filter: blur(16px);
}

.nav-item {
  color: rgba(255, 255, 255, 0.72);
  text-align: center;
  padding: 12px 10px;
  border-radius: 16px;
  font-weight: 600;
  transition: 160ms ease;
}

.nav-item.active {
  background: #fff7ed;
  color: #9a3412;
}
</style>
