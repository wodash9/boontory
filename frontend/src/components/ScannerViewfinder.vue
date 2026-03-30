<script setup lang="ts">
defineProps<{
  scanning: boolean
  message?: string
}>()
</script>

<template>
  <div class="frame" :class="{ scanning }">
    <div class="window"></div>
    <p>{{ message || 'Align an ISBN-13 barcode inside the frame.' }}</p>
  </div>
</template>

<style scoped>
.frame {
  position: absolute;
  inset: 0;
  display: grid;
  place-items: center;
  padding: 24px;
  background: linear-gradient(rgba(28, 25, 23, 0.35), rgba(28, 25, 23, 0.55));
}

.window {
  width: min(80vw, 340px);
  height: min(42vw, 180px);
  border: 3px solid rgba(255, 255, 255, 0.92);
  border-radius: 24px;
  box-shadow: 0 0 0 999px rgba(0, 0, 0, 0.28);
}

p {
  position: absolute;
  bottom: 16px;
  left: 16px;
  right: 16px;
  color: white;
  text-align: center;
}

.frame.scanning .window {
  border-color: #4ade80;
}
</style>
