import axios from 'axios'

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

// Public APIs
export function getPublicImages(page = 0, size = 20) {
  return api.get('/api/public/images', { params: { page, size } })
}

export function getPublicImage(id) {
  return api.get(`/api/public/images/${id}`)
}

// Setup
export function setupToken(masterToken, name) {
  return api.post('/api/setup/token', { name }, {
    headers: { 'X-Setup-Token': masterToken }
  })
}

// Upload
export function getUploadSignature(filename, contentType, md5) {
  return api.post('/api/upload/signature', { filename, contentType, md5 })
}

// Admin - images
export function getAdminImages(page = 0, size = 20) {
  return api.get('/api/admin/images', { params: { page, size } })
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

// Verify
export function verifyToken() {
  return api.get('/api/verify')
}

// Admin - tokens
export function listTokens() {
  return api.get('/api/admin/tokens')
}

export function createToken(name) {
  return api.post('/api/admin/tokens', { name })
}

export function revokeToken(id) {
  return api.delete(`/api/admin/tokens/${id}`)
}
