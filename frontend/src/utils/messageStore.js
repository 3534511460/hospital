import { ref } from 'vue'

export const unreadMsgCount = ref(0)

export async function fetchUnreadMsg(request) {
  const token = localStorage.getItem('token')
  if (!token) return
  try {
    const r = await request.get('/message/unread-count')
    unreadMsgCount.value = r.data?.count || 0
  } catch {}
}
