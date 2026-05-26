<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { useTokenStore } from '@/stores/token'
import { sendVerificationCode, verifyEmailCode, getEmailDomains } from '@/api'
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

// Set-mode: split email input
const emailUsername = ref('')
const emailDomain = ref('')
const customDomain = ref('')
const domainOptions = ref([])
const isOther = ref(false)

// Shared
const codeInput = ref('')
const sendingCode = ref(false)
const verifying = ref(false)
const countdown = ref(0)
let countdownTimer = null

onMounted(async () => {
  if (isSetMode.value) {
    try {
      const res = await getEmailDomains()
      domainOptions.value = res.data.data || []
    } catch {
      domainOptions.value = ['qq.com', 'outlook.com', '163.com']
    }
  }
})

onUnmounted(() => {
  if (countdownTimer) clearInterval(countdownTimer)
})

function onDomainChange(val) {
  isOther.value = val === '__other__'
  if (isOther.value) {
    customDomain.value = ''
  }
}

function fullEmail() {
  const domain = isOther.value ? customDomain.value.trim() : emailDomain.value
  if (!emailUsername.value.trim() || !domain) return ''
  return emailUsername.value.trim() + '@' + domain
}

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
  const targetEmail = isSetMode.value ? fullEmail() : tokenStore.email
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
  if (isSetMode.value && !fullEmail()) return

  verifying.value = true
  try {
    const targetEmail = isSetMode.value ? fullEmail() : tokenStore.email
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
      <!-- Set mode: split email + send code + code + actions -->
      <template v-if="isSetMode">
        <div class="email-split">
          <el-input
            v-model="emailUsername"
            :placeholder="t('setup.emailUsernamePlaceholder')"
            class="email-user-input"
            size="large"
          />
          <span class="email-at">@</span>
          <el-select
            v-model="emailDomain"
            :placeholder="t('setup.emailDomainPlaceholder')"
            class="email-domain-select"
            size="large"
            @change="onDomainChange"
          >
            <el-option
              v-for="d in domainOptions"
              :key="d"
              :label="d"
              :value="d"
            />
            <el-option
              :label="t('setup.emailDomainOther')"
              value="__other__"
            />
          </el-select>
        </div>
        <el-input
          v-if="isOther"
          v-model="customDomain"
          :placeholder="t('setup.emailDomainCustomPlaceholder')"
          size="large"
          class="custom-domain-input"
        />
        <p v-if="isOther" class="other-warn">{{ t('setup.emailDomainOtherWarn') }}</p>

        <el-input
          v-model="codeInput"
          :placeholder="t('token.codePlaceholder')"
          size="large"
          class="code-input"
          @keyup.enter="handleSubmit"
        />

        <div class="action-row">
          <el-button
            type="primary"
            size="large"
            class="action-btn"
            :loading="sendingCode"
            :disabled="!fullEmail() || countdown > 0"
            @click="handleSendCode"
          >
            {{ countdown > 0 ? t('token.codeResend', { seconds: countdown }) : t('token.sendCode') }}
          </el-button>
          <el-button
            type="primary"
            size="large"
            class="action-btn"
            :loading="verifying"
            :disabled="!codeInput.trim() || !fullEmail()"
            @click="handleSubmit"
          >
            {{ t('common.save') }}
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

        <el-button
          type="primary"
          size="large"
          class="submit-btn"
          :loading="verifying"
          :disabled="!codeInput.trim()"
          @click="handleSubmit"
        >
          {{ t('token.verify') }}
        </el-button>
      </template>
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

/* Set mode: email split */
.email-split {
  display: flex;
  align-items: center;
  gap: 4px;
  margin-bottom: 12px;
}
.email-user-input {
  flex: 1;
}
.email-at {
  font-size: 15px;
  color: #606266;
  font-weight: 500;
  flex-shrink: 0;
}
.email-domain-select {
  width: 140px;
  flex-shrink: 0;
}
.custom-domain-input {
  margin-bottom: 8px;
}
.other-warn {
  font-size: 12px;
  color: #e6a23c;
  margin: 0 0 8px;
  line-height: 1.5;
}
.code-input {
  margin-bottom: 12px;
}

/* Action row: two buttons side by side */
.action-row {
  display: flex;
  gap: 10px;
}
.action-btn {
  flex: 1;
}

/* Verify mode */
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
  gap: 10px;
  margin-bottom: 16px;
}
.code-section .el-input {
  flex: 1;
}
.code-section .el-button {
  flex-shrink: 0;
}
.submit-btn {
  width: 100%;
}
</style>
