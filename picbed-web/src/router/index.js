import { createRouter, createWebHistory } from 'vue-router'

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
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  if (to.meta.requiresToken) {
    const token = localStorage.getItem('auth_token')
    if (!token) {
      next('/')
      return
    }
  }
  next()
})

export default router
