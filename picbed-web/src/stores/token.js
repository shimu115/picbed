import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { login, logout, getSession } from '@/api'

export const useTokenStore = defineStore('token', () => {
  const router = useRouter()
  const token = ref(localStorage.getItem('auth_token') || '')
  const role = ref('')
  const tokenId = ref(null)
  const email = ref('')
  const isValid = ref(false)
  const pendingEmailChange = ref(false)

  const hasToken = computed(() => token.value.length > 0)
  const isAdmin = computed(() => role.value === 'ADMIN')
  const emailMissing = computed(() => hasToken.value && isValid.value && !email.value)

  async function fetchSessionInfo() {
    try {
      const res = await getSession()
      const data = res.data?.data
      if (data?.valid) {
        isValid.value = true
        role.value = data.role || ''
        tokenId.value = data.id || null
        email.value = data.email || ''
        return true
      }
    } catch {
      // no valid session
    }
    isValid.value = false
    role.value = ''
    tokenId.value = null
    email.value = ''
    return false
  }

  async function loginWithToken() {
    if (!hasToken.value) return false
    try {
      const res = await login(token.value)
      const data = res.data?.data
      isValid.value = true
      role.value = data.role || ''
      tokenId.value = data.id || null
      email.value = data.email || ''
      return true
    } catch {
      isValid.value = false
      role.value = ''
      return false
    }
  }

  function setToken(rawToken) {
    token.value = rawToken
    localStorage.setItem('auth_token', rawToken)
  }

  function setEmail(val) {
    email.value = val
  }

  function setPendingEmailChange(val) {
    pendingEmailChange.value = val
  }

  async function doLogout() {
    try {
      await logout()
    } catch {
      // ignore
    }
    clearToken()
  }

  function clearToken() {
    token.value = ''
    isValid.value = false
    role.value = ''
    tokenId.value = null
    email.value = ''
    localStorage.removeItem('auth_token')
  }

  // Check for existing session on init
  fetchSessionInfo()

  window.addEventListener('session-expired', () => {
    clearToken()
    router.push('/login')
  })

  return {
    token, role, tokenId, email, isValid, pendingEmailChange,
    hasToken, isAdmin, emailMissing,
    setToken, clearToken, fetchSessionInfo, loginWithToken, doLogout,
    setEmail, setPendingEmailChange
  }
})
