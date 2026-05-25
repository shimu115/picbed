<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useI18n } from 'vue-i18n'
import AppHeader from '@/components/layout/AppHeader.vue'
import { useSettingsStore } from '@/stores/settings'
import { useTokenStore } from '@/stores/token'
import { sendVerificationCode, verifyEmailCode } from '@/api'
import { ElMessage } from 'element-plus'

const { t } = useI18n()
const settingsStore = useSettingsStore()
const tokenStore = useTokenStore()

const showEmailDialog = ref(false)
const emailInput = ref('')
const codeInput = ref('')
const sendingCode = ref(false)
const verifying = ref(false)
const countdown = ref(0)
let countdownTimer = null

onMounted(() => {
  settingsStore.fetchSettings()
})

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
  if (!emailInput.value.trim()) return
  sendingCode.value = true
  try {
    await sendVerificationCode(emailInput.value.trim())
    ElMessage.success(t('token.codeSent'))
    startCountdown()
  } catch (e) {
    ElMessage.error(e.response?.data?.msg || t('error.serverError'))
  } finally {
    sendingCode.value = false
  }
}

async function handleVerify() {
  if (!emailInput.value.trim() || !codeInput.value.trim()) return
  verifying.value = true
  try {
    await verifyEmailCode(emailInput.value.trim(), codeInput.value.trim())
    tokenStore.setEmail(emailInput.value.trim())
    window.dispatchEvent(new CustomEvent('token-email-updated'))
    ElMessage.success(t('token.emailVerified'))
    showEmailDialog.value = false
  } catch (e) {
    ElMessage.error(e.response?.data?.msg || t('error.serverError'))
  } finally {
    verifying.value = false
  }
}

function resetDialog() {
  emailInput.value = ''
  codeInput.value = ''
  countdown.value = 0
  if (countdownTimer) {
    clearInterval(countdownTimer)
    countdownTimer = null
  }
}
</script>

<template>
  <div id="app-container">
    <AppHeader />
    <div v-if="tokenStore.emailMissing" class="email-banner">
      <span>{{ t('token.emailPrompt') }}</span>
      <el-button size="small" type="warning" @click="showEmailDialog = true">
        {{ t('token.setEmail') }}
      </el-button>
    </div>
    <main class="main-content">
      <router-view />
    </main>

    <el-dialog
      v-model="showEmailDialog"
      :title="t('token.verifyEmail')"
      width="420px"
      :close-on-click-modal="false"
      @close="resetDialog"
    >
      <div class="email-verify-form">
        <div class="email-row">
          <el-input
            v-model="emailInput"
            :placeholder="t('token.emailPlaceholder')"
          />
          <el-button
            type="primary"
            :loading="sendingCode"
            :disabled="!emailInput.trim() || countdown > 0"
            @click="handleSendCode"
          >
            {{ countdown > 0 ? t('token.codeResend', { seconds: countdown }) : t('token.sendCode') }}
          </el-button>
        </div>
        <el-input
          v-model="codeInput"
          :placeholder="t('token.codePlaceholder')"
          style="margin-top: 12px"
          @keyup.enter="handleVerify"
        />
      </div>
      <template #footer>
        <el-button @click="showEmailDialog = false">{{ t('common.cancel') }}</el-button>
        <el-button type="primary" :loading="verifying" :disabled="!emailInput.trim() || !codeInput.trim()" @click="handleVerify">
          {{ t('common.save') }}
        </el-button>
      </template>
    </el-dialog>
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
.email-verify-form {
  display: flex;
  flex-direction: column;
}
.email-row {
  display: flex;
  gap: 8px;
}
.email-row .el-input {
  flex: 1;
}
@media (max-width: 767px) {
  .email-banner {
    flex-direction: column;
    gap: 8px;
    text-align: center;
    font-size: 13px;
  }
  .email-row {
    flex-direction: column;
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
