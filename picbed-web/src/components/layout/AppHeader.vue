<script setup>
import { useRouter } from 'vue-router'
import { useTokenStore } from '@/stores/token'
import TokenInput from '@/components/common/TokenInput.vue'

const router = useRouter()
const tokenStore = useTokenStore()

const navItems = [
  { path: '/', label: 'Gallery', requiresToken: false },
  { path: '/upload', label: 'Upload', requiresToken: true },
  { path: '/manage', label: 'Manage', requiresToken: true }
]

function goTo(path) {
  router.push(path)
}
</script>

<template>
  <el-header class="app-header">
    <div class="header-left">
      <router-link to="/" class="logo">
        <img src="/favicon.svg" alt="PicBed" class="logo-icon" />
        <span class="logo-text">PicBed</span>
      </router-link>
      <nav class="nav-links">
        <el-button
          v-for="item in navItems"
          :key="item.path"
          :type="$route.path === item.path ? 'primary' : 'default'"
          size="small"
          @click="goTo(item.path)"
          :disabled="item.requiresToken && !tokenStore.hasToken && $route.path !== item.path"
        >
          {{ item.label }}
        </el-button>
      </nav>
    </div>
    <div class="header-right">
      <TokenInput />
    </div>
  </el-header>
</template>

<style scoped>
.app-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: #fff;
  border-bottom: 1px solid #e4e7ed;
  padding: 0 20px;
  height: 60px;
}
.header-left {
  display: flex;
  align-items: center;
  gap: 24px;
}
.logo {
  display: flex;
  align-items: center;
  gap: 8px;
  text-decoration: none;
  color: #303133;
}
.logo-icon {
  width: 28px;
  height: 28px;
}
.logo-text {
  font-size: 18px;
  font-weight: 600;
}
.nav-links {
  display: flex;
  gap: 8px;
}
.header-right {
  display: flex;
  align-items: center;
}
</style>
