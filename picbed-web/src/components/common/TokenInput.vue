<script setup>
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { useTokenStore } from '@/stores/token'

const router = useRouter()
const { t } = useI18n()
const tokenStore = useTokenStore()

function goToLogin() {
  router.push('/login')
}

async function handleLogout() {
  await tokenStore.doLogout()
  router.push('/login')
}
</script>

<template>
  <div class="token-indicator">
    <template v-if="tokenStore.isValid">
      <el-tag type="success" size="small">
        <el-icon style="margin-right: 4px"><svg viewBox="0 0 24 24" width="14" height="14" fill="currentColor"><path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm-2 15l-5-5 1.41-1.41L10 14.17l7.59-7.59L19 8l-9 9z"/></svg></el-icon>
        {{ t('token.set') }}
      </el-tag>
      <el-button type="danger" size="small" text @click="handleLogout">{{ t('token.remove') }}</el-button>
    </template>
    <template v-else>
      <el-button type="warning" size="small" @click="goToLogin">
        {{ t('token.setToken') }}
      </el-button>
    </template>
  </div>
</template>

<style scoped>
.token-indicator {
  display: flex;
  align-items: center;
  gap: 6px;
}
</style>
