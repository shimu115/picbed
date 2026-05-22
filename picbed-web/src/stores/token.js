import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { listTokens } from '@/api'

export const useTokenStore = defineStore('token', () => {
  const token = ref(localStorage.getItem('auth_token') || '')
  const isValid = ref(false)

  const hasToken = computed(() => token.value.length > 0)

  async function validateToken() {
    if (!hasToken.value) {
      isValid.value = false
      return
    }
    try {
      await listTokens()
      isValid.value = true
    } catch {
      isValid.value = false
      setToken('')
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
    localStorage.removeItem('auth_token')
  }

  return { token, isValid, hasToken, setToken, clearToken, validateToken }
})
