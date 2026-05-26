import axios from 'axios'
import { ElMessage } from 'element-plus'
import i18n from '@/i18n'

const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '',
  timeout: 30000
})

api.interceptors.request.use(config => {
  const token = localStorage.getItem('auth_token')
  if (token) {
    config.headers['X-Auth-Token'] = token
  }
  return config
})

api.interceptors.response.use(
  response => response,
  error => {
    if (error.response?.status === 401) {
      localStorage.removeItem('auth_token')
      ElMessage.error(i18n.global.t('error.invalidToken'))
      window.dispatchEvent(new CustomEvent('auth-token-expired'))
    }
    return Promise.reject(error)
  }
)

export default api

// Status
export function getStatus() {
  return api.get('/api/public/status')
}

export function getEmailDomains() {
  return api.get('/api/public/email-domains')
}

// Public APIs
export function getPublicImages(page = 0, size = 20) {
  return api.get('/api/public/images', { params: { page, size } })
}

export function getPublicImage(id) {
  return api.get(`/api/public/images/${id}`)
}

export function getAdminImage(id) {
  return api.get(`/api/admin/images/${id}`)
}

// Setup
export function setupToken(masterToken, name, email) {
  return api.post('/api/setup/token', { name, email }, {
    headers: { 'X-Setup-Token': masterToken }
  })
}

// Upload
export function getUploadSignature(filename, contentType, md5) {
  return api.post('/api/upload/signature', { filename, contentType, md5 })
}

// Admin - images
export function getAdminImages(page = 0, size = 20, published) {
  const params = { page, size }
  if (published !== undefined) params.published = published
  return api.get('/api/admin/images', { params })
}

export function batchPublishImages(ids, published) {
  return api.put('/api/admin/images/batch/publish', { ids, published })
}

export function saveImageMetadata(data) {
  return api.post('/api/admin/images', data)
}

export function deleteImage(id) {
  return api.delete(`/api/admin/images/${id}`)
}

export function batchDeleteImages(ids) {
  return api.delete('/api/admin/images/batch', { data: { ids } })
}

// Settings
export function getSettings() {
  return api.get('/api/settings')
}

export function updateSettings(data) {
  return api.put('/api/admin/settings', data)
}

// Verify
export function verifyToken() {
  return api.get('/api/verify')
}

// Admin - tokens
export function listTokens() {
  return api.get('/api/admin/tokens')
}

export function createToken(name, email) {
  return api.post('/api/admin/tokens', { name, email })
}

export function updateTokenEmail(id, email) {
  return api.put(`/api/admin/tokens/${id}/email`, { email })
}

export function updateOwnEmail(email) {
  return api.put('/api/account/email', { email })
}

export function sendVerificationCode(email) {
  return api.post('/api/account/email/send-code', { email })
}

export function verifyEmailCode(email, code) {
  return api.post('/api/account/email/verify', { email, code })
}

export function refreshOwnToken(sendEmail) {
  return api.post('/api/account/refresh', { sendEmail })
}

export function adminRefreshToken(id) {
  return api.post(`/api/admin/tokens/${id}/refresh`)
}

export function warnToken(id) {
  return api.post(`/api/admin/tokens/${id}/warn`)
}

export function adminSendVerificationCode(id) {
  return api.post(`/api/admin/tokens/${id}/email/send-code`)
}

export function adminVerifyEmailCode(id, code) {
  return api.post(`/api/admin/tokens/${id}/email/verify-code`, { code })
}

export function revokeToken(id) {
  return api.delete(`/api/admin/tokens/${id}`)
}
