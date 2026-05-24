import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { verifyToken } from '@/api'

export const useTokenStore = defineStore('token', () => {
  const token = ref(localStorage.getItem('auth_token') || '')
  const role = ref('')
  const isValid = ref(false)

  const hasToken = computed(() => token.value.length > 0)
  const isAdmin = computed(() => role.value === 'ADMIN')

  async function validateToken() {
    if (!hasToken.value) {
      isValid.value = false
      role.value = ''
      return
    }
    try {
      const res = await verifyToken()
      isValid.value = res.data?.data?.valid === true
      role.value = res.data?.data?.role || ''
    } catch {
      isValid.value = false
      role.value = ''
      clearToken()
    }
  }

  function setToken(rawToken) {
    token.value = rawToken
    localStorage.setItem('auth_token', rawToken)
    validateToken()
  }

  function clearToken() {
    token.value = ''
    isValid.value = false
    role.value = ''
    localStorage.removeItem('auth_token')
  }

  if (hasToken.value) {
    validateToken()
  }

  return { token, role, isValid, hasToken, isAdmin, setToken, clearToken, validateToken }
})
