import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { verifyToken } from '@/api'

export const useTokenStore = defineStore('token', () => {
  const token = ref(localStorage.getItem('auth_token') || '')
  const role = ref('')
  const tokenId = ref(null)
  const isValid = ref(false)
  const router = useRouter()

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
      tokenId.value = res.data?.data?.id || null
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
    tokenId.value = null
    localStorage.removeItem('auth_token')
    if (router.currentRoute?.value?.meta?.requiresToken) {
      router.push('/')
    }
  }

  if (hasToken.value) {
    validateToken()
  }

  window.addEventListener('auth-token-expired', () => {
    clearToken()
  })

  return { token, role, tokenId, isValid, hasToken, isAdmin, setToken, clearToken, validateToken }
})
