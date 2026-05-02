<script setup lang="ts">
import { computed } from 'vue'
import { RouterView, useRoute } from 'vue-router'
import AuthGate from './components/auth/AuthGate.vue'

const route = useRoute()
const requiresAuth = computed(() => route.meta.requiresAuth !== false)
</script>

<template>
  <AuthGate v-if="requiresAuth">
    <RouterView />
  </AuthGate>
  <RouterView v-else />
</template>
