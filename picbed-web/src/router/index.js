import { createRouter, createWebHistory } from 'vue-router'
import { getStatus, getSession } from '@/api'

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
    path: '/login',
    name: 'Login',
    component: () => import('@/views/LoginView.vue'),
    meta: { title: 'Login', requiresToken: false }
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
  // Check system initialization status once
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

  // For setup page, redirect to login if already initialized
  if (to.path === '/setup') {
    try {
      const res = await getStatus()
      if (res.data?.data?.initialized) {
        next('/login')
        return
      }
    } catch {
      // proceed anyway
    }
  }

  // Validate session for protected routes
  if (to.meta.requiresToken) {
    try {
      const res = await getSession()
      if (res.data?.data?.valid !== true) {
        next({ path: '/login', query: { redirect: to.fullPath } })
        return
      }
    } catch {
      next({ path: '/login', query: { redirect: to.fullPath } })
      return
    }
  }

  next()
})

export default router
