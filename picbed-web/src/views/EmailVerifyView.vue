<script setup>
import { ref, computed, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { useTokenStore } from '@/stores/token'
import { sendVerificationCode, verifyEmailCode } from '@/api'
import { ElMessage } from 'element-plus'

const route = useRoute()
const router = useRouter()
const { t } = useI18n()
const tokenStore = useTokenStore()

const isSetMode = computed(() => route.query.mode === 'set')
const isDesktop = window.innerWidth >= 768

if (isDesktop || (!isSetMode.value && !tokenStore.email)) {
  router.replace('/manage')
}

const emailInput = ref('')
const codeInput = ref('')
const sendingCode = ref(false)
const verifying = ref(false)
const countdown = ref(0)
let countdownTimer = null

onUnmounted(() => {
  if (countdownTimer) clearInterval(countdownTimer)
})

function startCountdown() {
  countdown.value = 60
  countdownTimer = setInterval(() => {
    countdown.value--
    if (countdown.value <= 0) {
      clearInterval(countdownTimer)
      countdownTimer = null
    }
  }, 1000)
}

async function handleSendCode() {
  const targetEmail = isSetMode.value ? emailInput.value.trim() : tokenStore.email
  if (!targetEmail) return
  sendingCode.value = true
  try {
    await sendVerificationCode(targetEmail)
    ElMessage.success(t('token.codeSent'))
    startCountdown()
  } catch (e) {
    ElMessage.error(e.response?.data?.msg || t('error.serverError'))
  } finally {
    sendingCode.value = false
  }
}

async function handleSubmit() {
  if (!codeInput.value.trim()) return
  if (isSetMode.value && !emailInput.value.trim()) return

  verifying.value = true
  try {
    const targetEmail = isSetMode.value ? emailInput.value.trim() : tokenStore.email
    await verifyEmailCode(targetEmail, codeInput.value.trim())
    ElMessage.success(t('token.emailVerified'))

    if (isSetMode.value) {
      tokenStore.setEmail(targetEmail)
      window.dispatchEvent(new CustomEvent('token-email-updated'))
    } else {
      tokenStore.setEmail('')
      tokenStore.setPendingEmailChange(true)
    }
    router.replace('/manage')
  } catch (e) {
    ElMessage.error(e.response?.data?.msg || t('error.serverError'))
  } finally {
    verifying.value = false
  }
}
</script>

<template>
  <div class="email-verify-page">
    <div class="page-header">
      <el-button size="small" text @click="router.replace('/manage')">
        <el-icon><svg viewBox="0 0 24 24" width="18" height="18" fill="currentColor"><path d="M20 11H7.83l5.59-5.59L12 4l-8 8 8 8 1.41-1.41L7.83 13H20v-2z"/></svg></el-icon>
      </el-button>
      <h2>{{ isSetMode ? t('token.verifyEmail') : t('token.verifyOldEmail') }}</h2>
    </div>

    <div class="verify-card">
      <!-- Set mode: email input + send code -->
      <template v-if="isSetMode">
        <div class="code-section">
          <el-input
            v-model="emailInput"
            :placeholder="t('token.emailPlaceholder')"
            size="large"
          />
          <el-button
            type="primary"
            size="large"
            :loading="sendingCode"
            :disabled="!emailInput.trim() || countdown > 0"
            @click="handleSendCode"
          >
            {{ countdown > 0 ? t('token.codeResend', { seconds: countdown }) : t('token.sendCode') }}
          </el-button>
        </div>
      </template>

      <!-- Verify mode: show current email + send code -->
      <template v-else>
        <p class="current-email-label">{{ t('token.currentEmail') }}</p>
        <p class="current-email-value">{{ tokenStore.email }}</p>
        <div class="code-section">
          <el-input
            v-model="codeInput"
            :placeholder="t('token.codePlaceholder')"
            size="large"
            @keyup.enter="handleSubmit"
          />
          <el-button
            type="primary"
            size="large"
            :loading="sendingCode"
            :disabled="countdown > 0"
            @click="handleSendCode"
          >
            {{ countdown > 0 ? t('token.codeResend', { seconds: countdown }) : t('token.sendCode') }}
          </el-button>
        </div>
      </template>

      <!-- Code input for set mode (after email input) -->
      <template v-if="isSetMode">
        <el-input
          v-model="codeInput"
          :placeholder="t('token.codePlaceholder')"
          size="large"
          style="margin-bottom: 16px"
          @keyup.enter="handleSubmit"
        />
      </template>

      <el-button
        type="primary"
        size="large"
        class="submit-btn"
        :loading="verifying"
        :disabled="!codeInput.trim() || (isSetMode && !emailInput.trim())"
        @click="handleSubmit"
      >
        {{ isSetMode ? t('common.save') : t('token.verify') }}
      </el-button>
    </div>
  </div>
</template>

<style scoped>
.email-verify-page {
  padding: 16px;
  max-width: 480px;
  margin: 0 auto;
}
.page-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 24px;
}
.page-header h2 {
  margin: 0;
  font-size: 18px;
}
.verify-card {
  background: #fff;
  border-radius: 12px;
  padding: 24px 20px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.08);
}
.current-email-label {
  margin: 0 0 4px;
  font-size: 13px;
  color: #909399;
}
.current-email-value {
  margin: 0 0 20px;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  word-break: break-all;
}
.code-section {
  display: flex;
  flex-direction: column;
  gap: 10px;
  margin-bottom: 16px;
}
.submit-btn {
  width: 100%;
}
</style>
