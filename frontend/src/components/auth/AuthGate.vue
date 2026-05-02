<script setup lang="ts">
import { auth } from '../../auth/authState'
</script>

<template>
  <div v-if="!auth.state.ready" class="auth-status">Signing you in…</div>
  <div v-else-if="auth.state.error" class="auth-status auth-status--error">{{ auth.state.error }}</div>
  <slot v-else />
</template>

<style scoped>
.auth-status {
  min-height: 100vh;
  display: grid;
  place-items: center;
  color: var(--text-soft);
  font-size: 1rem;
}

.auth-status--error {
  color: #c0392b;
}
</style>
