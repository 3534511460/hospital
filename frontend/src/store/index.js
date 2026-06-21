import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export const useUserStore = defineStore('user', () => {
  const user = ref(JSON.parse(localStorage.getItem('user') || 'null'))
  const token = ref(localStorage.getItem('token') || '')

  const isLoggedIn = computed(() => !!token.value)
  const role = computed(() => user.value?.role || '')
  const userName = computed(() => user.value?.realName || user.value?.username || '')

  function setLogin(data) {
    token.value = data.accessToken
    user.value = { id: data.userId, username: data.username, realName: data.realName, role: data.role, avatar: data.avatar }
    localStorage.setItem('token', data.accessToken)
    localStorage.setItem('refreshToken', data.refreshToken)
    localStorage.setItem('user', JSON.stringify(user.value))
  }

  function setUser(u) {
    user.value = { ...user.value, ...u }
    localStorage.setItem('user', JSON.stringify(user.value))
  }

  function logout() {
    token.value = ''
    user.value = null
    localStorage.removeItem('token')
    localStorage.removeItem('refreshToken')
    localStorage.removeItem('user')
  }

  return { user, token, isLoggedIn, role, userName, setLogin, setUser, logout }
})
