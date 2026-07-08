import { ref } from 'vue'

export const unreadChatCount = ref(0)

// call this whenever sessions list is refreshed
export function updateUnread(sessions, role) {
  let total = 0
  for (const s of sessions) {
    total += role === 'DOCTOR' ? (s.unreadCountDoctor || 0) : (s.unreadCountPatient || 0)
  }
  unreadChatCount.value = total
}
