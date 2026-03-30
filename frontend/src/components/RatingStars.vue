<script setup lang="ts">
const props = defineProps<{
  modelValue: number
  readonly?: boolean
}>()

const emit = defineEmits<{
  (event: 'update:modelValue', value: number): void
}>()

function update(value: number) {
  if (!props.readonly) emit('update:modelValue', value)
}
</script>

<template>
  <div class="stars">
    <button
      v-for="star in 5"
      :key="star"
      type="button"
      class="star"
      :class="{ active: star <= modelValue, readonly }"
      @click="update(star)"
    >
      ★
    </button>
  </div>
</template>

<style scoped>
.stars {
  display: inline-flex;
  gap: 4px;
}

.star {
  border: none;
  background: transparent;
  color: #d6d3d1;
  font-size: 1.25rem;
  padding: 0;
}

.star.active {
  color: #f59e0b;
}

.star.readonly {
  cursor: default;
}
</style>
