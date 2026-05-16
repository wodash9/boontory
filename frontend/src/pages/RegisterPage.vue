<script setup lang="ts">
import { onMounted } from 'vue'
import { auth } from '../auth/authState'

onMounted(() => {
  void auth.register()
})
</script>

<template>
  <main class="register-page">
    <section class="register-card">
      <p class="eyebrow">Boontory account</p>
      <h1>Create your account</h1>
      <p v-if="auth.state.error" class="error">{{ auth.state.error }}</p>
      <p v-else>Opening the secure Keycloak registration screen…</p>
      <button type="button" class="button" @click="auth.register()">Open registration</button>
      <RouterLink class="secondary-link" to="/landing">Back to landing</RouterLink>
    </section>
  </main>
</template>

<style scoped>
.register-page {
  min-height: 100vh;
  display: grid;
  place-items: center;
  padding: 24px;
}

.register-card {
  width: min(100%, 520px);
  padding: 32px;
  border: 1px solid var(--border);
  border-radius: 28px;
  background: var(--surface);
  box-shadow: var(--shadow);
}

.eyebrow {
  margin: 0 0 12px;
  color: var(--accent);
  text-transform: uppercase;
  letter-spacing: 0.12em;
  font-size: 0.78rem;
  font-weight: 800;
}

h1 {
  margin: 0 0 16px;
  font-size: clamp(2.4rem, 8vw, 4.4rem);
  line-height: 0.92;
  letter-spacing: -0.06em;
}

p {
  color: var(--text-soft);
}

.error {
  color: #c0392b;
}

.button,
.secondary-link {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-height: 48px;
  margin-top: 16px;
  border-radius: 999px;
  font-weight: 800;
}

.button {
  width: 100%;
  border: 0;
  background: var(--text);
  color: #fff7ed;
  cursor: pointer;
}

.secondary-link {
  width: 100%;
  color: var(--accent-strong);
}
</style>
