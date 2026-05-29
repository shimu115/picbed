<script setup>
import { ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { useTokenStore } from '@/stores/token'
import { getStatus } from '@/api'
import { ElMessage } from 'element-plus'

const router = useRouter()
const route = useRoute()
const { t } = useI18n()
const tokenStore = useTokenStore()

const loading = ref(false)
const inputToken = ref('')
const error = ref('')
const initialized = ref(true)

// Check if system is initialized
getStatus().then(res => {
  initialized.value = res.data?.data?.initialized
}).catch(() => {
  initialized.value = true
})

async function handleLogin() {
  if (!inputToken.value.trim()) return
  loading.value = true
  error.value = ''
  try {
    tokenStore.setToken(inputToken.value.trim())
    const ok = await tokenStore.loginWithToken()
    if (ok) {
      const redirect = route.query.redirect || '/'
      router.push(redirect)
    } else {
      error.value = t('login.loginFailed')
    }
  } catch (e) {
    error.value = e.response?.data?.msg || t('login.loginFailed')
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="login-page">
    <h2>{{ t('login.title') }}</h2>
    <p class="login-desc">{{ t('login.prompt') }}</p>

    <el-card class="login-card">
      <el-form @submit.prevent="handleLogin">
        <el-form-item>
          <el-input
            v-model="inputToken"
            type="password"
            show-password
            :placeholder="t('login.tokenPlaceholder')"
            @keyup.enter="handleLogin"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleLogin" :loading="loading" :disabled="!inputToken.trim()" class="login-btn">
            {{ t('login.loginBtn') }}
          </el-button>
        </el-form-item>
        <p v-if="error" class="error-text">{{ error }}</p>
      </el-form>

      <div v-if="!initialized" class="setup-link">
        <el-button type="primary" text @click="router.push('/setup')">
          {{ t('login.goToSetup') }}
        </el-button>
      </div>
    </el-card>
  </div>
</template>

<style scoped>
.login-page {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding-top: 60px;
}
.login-desc {
  color: #606266;
  margin-bottom: 20px;
  text-align: center;
}
.login-card {
  max-width: 420px;
  width: 100%;
}
.login-btn {
  width: 100%;
}
.error-text {
  color: #f56c6c;
  font-size: 13px;
}
.setup-link {
  text-align: center;
  margin-top: 8px;
}
</style>
