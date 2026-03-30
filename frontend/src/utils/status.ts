import type { ReadingStatus } from '../types/models'

export const statusOptions: Array<{ label: string; value: ReadingStatus }> = [
  { label: 'Want to Read', value: 'WANT_TO_READ' },
  { label: 'Reading', value: 'READING' },
  { label: 'Read', value: 'READ' },
]

export function getStatusLabel(status: ReadingStatus) {
  return statusOptions.find((option) => option.value === status)?.label ?? status
}
