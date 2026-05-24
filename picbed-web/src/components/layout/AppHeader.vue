<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { useTokenStore } from '@/stores/token'
import { switchLang } from '@/i18n'
import TokenInput from '@/components/common/TokenInput.vue'

const router = useRouter()
const tokenStore = useTokenStore()
const { t, locale } = useI18n()

const drawerVisible = ref(false)

const navItems = [
  { path: '/', label: 'nav.gallery', requiresToken: false, isActive: false },
  { path: '/upload', label: 'nav.upload', requiresToken: true, isActive: false },
  { path: '/manage', label: 'nav.manage', requiresToken: true, isActive: false }
]

function goTo(path) {
  navItems.forEach(item => item.isActive = item.path === path)
  drawerVisible.value = false
  router.push(path)
}

function toggleLang() {
  switchLang(locale.value === 'zh-CN' ? 'en-US' : 'zh-CN')
}
</script>

<template>
  <el-header class="app-header">
    <div class="header-left">
      <router-link to="/" class="logo">
        <img src="/favicon.svg" alt="PicBed" class="logo-icon" />
        <span class="logo-text">{{ t('common.appName') }}</span>
      </router-link>
      <nav class="nav-links desktop-nav">
        <el-button
          v-for="item in navItems"
          :key="item.path"
          text
          size="small"
          :class="{ 'nav-active': $route.path === item.path }"
          @click="goTo(item.path)"
          :disabled="item.requiresToken && !tokenStore.hasToken && $route.path !== item.path"
        >
          {{ t(item.label) }}
        </el-button>
      </nav>
    </div>
    <div class="header-right desktop-right">
      <el-button size="small" text @click="toggleLang" class="lang-btn">
        {{ t('lang.switch') }}
      </el-button>
      <TokenInput />
    </div>
    <div class="header-right mobile-right">
      <el-button size="small" text @click="toggleLang" class="lang-btn">
        {{ t('lang.switch') }}
      </el-button>
      <el-button size="small" @click="drawerVisible = true" class="menu-btn">
        <el-icon :size="20"><svg viewBox="0 0 24 24" width="20" height="20" fill="currentColor"><path d="M3 18h18v-2H3v2zm0-5h18v-2H3v2zm0-7v2h18V6H3z"/></svg></el-icon>
      </el-button>
    </div>
  </el-header>

  <el-drawer
    v-model="drawerVisible"
    direction="ltr"
    :size="260"
    :with-header="false"
  >
    <div class="drawer-content">
      <div class="drawer-logo">
        <img src="/favicon.svg" alt="PicBed" class="logo-icon" />
        <span class="logo-text">{{ t('common.appName') }}</span>
      </div>
      <div class="drawer-nav" v-for="item in navItems">
        <el-button
          text
          v-if="!item.isActive"
          size="default"
          :class="{ 'nav-active': $route.path === item.path }"
          @click="goTo(item.path)"
          :disabled="item.requiresToken && !tokenStore.hasToken && $route.path !== item.path"
        >
          {{ t(item.label) }}
        </el-button>
        <el-button
          v-else
          text bg
          color=""
          size="default"
          :class="{ 'nav-active': $route.path === item.path }"
          class="drawer-nav-btn"
          @click="goTo(item.path)"
          :disabled="item.requiresToken && !tokenStore.hasToken && $route.path !== item.path"
        >
          {{ t(item.label) }}
        </el-button>
<!--        <el-button-->
<!--          v-for="item in navItems"-->
<!--          :key="item.path"-->
<!--          text-->
<!--          size="default"-->
<!--          :class="{ 'nav-active': $route.path === item.path }"-->
<!--          class="drawer-nav-btn"-->
<!--          @click="goTo(item.path)"-->
<!--          :disabled="item.requiresToken && !tokenStore.hasToken && $route.path !== item.path"-->
<!--        >-->
<!--          {{ t(item.label) }}-->
<!--        </el-button>-->
      </div>
      <div class="drawer-token">
        <TokenInput />
      </div>
    </div>
  </el-drawer>
</template>

<style scoped>
.app-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: #fff;
  border-bottom: 1px solid #e4e7ed;
  padding: 0 16px;
  height: 56px;
}
.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
}
.logo {
  display: flex;
  align-items: center;
  gap: 8px;
  text-decoration: none;
  color: #303133;
  flex-shrink: 0;
}
.logo-icon {
  width: 26px;
  height: 26px;
}
.logo-text {
  font-size: 17px;
  font-weight: 600;
}
.nav-links {
  display: flex;
  align-items: center;
  gap: 4px;
}
.nav-links .el-button {
  font-size: 14px;
  height: 32px;
  padding: 0 12px;
  border-radius: 6px;
}
.desktop-nav .nav-active {
  color: #409eff !important;
  background: #ecf5ff !important;
  font-weight: 600;
}
.header-right {
  display: flex;
  align-items: center;
  gap: 6px;
}
.lang-btn {
  font-size: 12px;
}
.menu-btn {
  padding: 4px;
}

/* Mobile: hide desktop nav, show hamburger */
.desktop-nav { display: flex; }
.desktop-right { display: flex; }
.mobile-right { display: none; }

@media (max-width: 767px) {
  .app-header {
    padding: 0 12px;
    height: 52px;
  }
  .header-left {
    gap: 8px;
  }
  .desktop-nav {
    display: none;
  }
  .desktop-right {
    display: none;
  }
  .mobile-right {
    display: flex;
  }
  .logo-text {
    font-size: 16px;
  }
  .logo-icon {
    width: 24px;
    height: 24px;
  }
}

/* Drawer */
.drawer-content {
  padding: 20px;
  display: flex;
  flex-direction: column;
  gap: 20px;
}
.drawer-logo {
  display: flex;
  align-items: center;
  gap: 10px;
  padding-bottom: 16px;
  border-bottom: 1px solid #e4e7ed;
}
.drawer-nav {
  display: flex;
  flex-direction: column;
  gap: 4px;
}
.drawer-nav-btn {
  width: 100%;
  //justify-content: flex-start;
  justify-content: center;
  height: 40px;
  font-size: 15px;
  border-radius: 8px;
  padding: 0 16px;
}
.drawer-nav-btn.nav-active {
  color: #409eff !important;
  background: #ecf5ff !important;
  font-weight: 600;

}
.drawer-token {
  padding-top: 12px;
  border-top: 1px solid #e4e7ed;
}
</style>
