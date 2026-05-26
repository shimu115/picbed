import { createRouter, createWebHistory } from 'vue-router'
import { getStatus } from '@/api'

const routes = [
  {
    path: '/',
    name: 'Gallery',
    component: () => import('@/views/GalleryView.vue'),
    meta: { title: 'Gallery', requiresToken: false }
  },
  {
    path: '/upload',
    name: 'Upload',
    component: () => import('@/views/UploadView.vue'),
    meta: { title: 'Upload', requiresToken: true }
  },
  {
    path: '/manage',
    name: 'Management',
    component: () => import('@/views/ManagementView.vue'),
    meta: { title: 'Management', requiresToken: true }
  },
  {
    path: '/setup',
    name: 'Setup',
    component: () => import('@/views/SetupView.vue'),
    meta: { title: 'Setup', requiresToken: false }
  },
  {
    path: '/image/:id',
    name: 'ImageView',
    component: () => import('@/views/ImageView.vue'),
    meta: { title: 'Image Detail', requiresToken: false }
  },
  {
    path: '/email/verify',
    name: 'EmailVerify',
    component: () => import('@/views/EmailVerifyView.vue'),
    meta: { title: 'Email Verify', requiresToken: true }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

let statusChecked = false

router.beforeEach(async (to, from, next) => {
  const token = localStorage.getItem('auth_token')

  if (to.meta.requiresToken && !token) {
    next('/')
    return
  }

  if (to.path === '/setup') {
    try {
      const res = await getStatus()
      if (res.data?.data?.initialized) {
        next('/')
        return
      }
    } catch {
      // backend unreachable, proceed anyway
    }
  }

  if (!statusChecked) {
    statusChecked = true
    try {
      const res = await getStatus()
      const initialized = res.data?.data?.initialized
      if (!initialized && to.path !== '/setup') {
        next('/setup')
        return
      }
    } catch {
      // backend unreachable, proceed anyway
    }
  }

  next()
})

export default router
