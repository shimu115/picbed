<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import AppHeader from '@/components/layout/AppHeader.vue'
import { useSettingsStore } from '@/stores/settings'
import { useTokenStore } from '@/stores/token'

const router = useRouter()
const { t } = useI18n()
const settingsStore = useSettingsStore()
const tokenStore = useTokenStore()

const bannerIgnored = ref(localStorage.getItem('email_banner_ignored') === '1')

onMounted(() => {
  settingsStore.fetchSettings()
})

function ignoreBanner() {
  bannerIgnored.value = true
  localStorage.setItem('email_banner_ignored', '1')
}

function goToEmailSettings() {
  router.push('/manage')
  setTimeout(() => {
    document.getElementById('email-settings')?.scrollIntoView({ behavior: 'smooth' })
  }, 100)
}
</script>

<template>
  <div id="app-container">
    <AppHeader />
    <div v-if="tokenStore.emailMissing && !bannerIgnored" class="email-banner">
      <span>{{ t('token.emailPrompt') }}</span>
      <div class="banner-actions">
        <el-button size="small" type="warning" @click="goToEmailSettings">
          {{ t('token.setEmail') }}
        </el-button>
        <el-button size="small" text @click="ignoreBanner">
          {{ t('token.ignore') }}
        </el-button>
      </div>
    </div>
    <main class="main-content">
      <router-view />
    </main>
  </div>
</template>

<style scoped>
.email-banner {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  padding: 10px 16px;
  background: #fef0f0;
  border-bottom: 1px solid #fab6b6;
  font-size: 14px;
  color: #e6a23c;
}
.banner-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}
@media (max-width: 767px) {
  .email-banner {
    flex-direction: column;
    gap: 8px;
    text-align: center;
    font-size: 13px;
  }
}
</style>

<style scoped>
#app-container {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}
.main-content {
  flex: 1;
  padding: 20px;
  max-width: 1400px;
  width: 100%;
  margin: 0 auto;
  box-sizing: border-box;
}

@media (max-width: 767px) {
  .main-content {
    padding: 12px;
  }
}
</style>
