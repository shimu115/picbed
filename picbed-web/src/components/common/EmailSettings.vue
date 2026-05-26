<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { useTokenStore } from '@/stores/token'
import { sendVerificationCode, verifyEmailCode, updateOwnEmail } from '@/api'
import { ElMessage } from 'element-plus'

const router = useRouter()
const { t } = useI18n()
const tokenStore = useTokenStore()

const showDialog = ref(false)
const emailInput = ref('')
const codeInput = ref('')
const sendingCode = ref(false)
const verifying = ref(false)
const countdown = ref(0)
let countdownTimer = null

// After old-email verification, show inline new-email input
const showNewEmailInput = ref(false)
const newEmailInput = ref('')
const savingNew = ref(false)

onMounted(() => {
  if (tokenStore.pendingEmailChange) {
    showNewEmailInput.value = true
    newEmailInput.value = ''
    tokenStore.setPendingEmailChange(false)
  }
})

function openSetEmail() {
  if (window.innerWidth < 768) {
    router.push('/email/verify?mode=set')
  } else {
    openSetEmailDialog()
  }
}

function openModifyEmail() {
  if (window.innerWidth < 768) {
    router.push('/email/verify')
  } else {
    openSetEmailDialog()
  }
}

function openSetEmailDialog() {
  emailInput.value = ''
  codeInput.value = ''
  countdown.value = 0
  if (countdownTimer) { clearInterval(countdownTimer); countdownTimer = null }
  showDialog.value = true
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

// For "set new email" flow: the user types their email in the dialog
async function handleSendCodeToInput() {
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

// For "modify email" flow: send code to existing email
async function handleSendCodeToExisting() {
  sendingCode.value = true
  try {
    await sendVerificationCode(tokenStore.email)
    ElMessage.success(t('token.codeSent'))
    startCountdown()
  } catch (e) {
    ElMessage.error(e.response?.data?.msg || t('error.serverError'))
  } finally {
    sendingCode.value = false
  }
}

async function handleVerifyNewEmail() {
  if (!emailInput.value.trim() || !codeInput.value.trim()) return
  verifying.value = true
  try {
    await verifyEmailCode(emailInput.value.trim(), codeInput.value.trim())
    tokenStore.setEmail(emailInput.value.trim())
    window.dispatchEvent(new CustomEvent('token-email-updated'))
    ElMessage.success(t('token.emailVerified'))
    showDialog.value = false
  } catch (e) {
    ElMessage.error(e.response?.data?.msg || t('error.serverError'))
  } finally {
    verifying.value = false
  }
}

async function handleVerifyOldEmail() {
  if (!codeInput.value.trim()) return
  verifying.value = true
  try {
    await verifyEmailCode(tokenStore.email, codeInput.value.trim())
    ElMessage.success(t('token.emailVerified'))
    tokenStore.setEmail('')
    showNewEmailInput.value = true
    newEmailInput.value = ''
    showDialog.value = false
  } catch (e) {
    ElMessage.error(e.response?.data?.msg || t('error.serverError'))
  } finally {
    verifying.value = false
  }
}

async function handleSaveNewEmail() {
  if (!newEmailInput.value.trim()) return
  savingNew.value = true
  try {
    await updateOwnEmail(newEmailInput.value.trim())
    tokenStore.setEmail(newEmailInput.value.trim())
    window.dispatchEvent(new CustomEvent('token-email-updated'))
    ElMessage.success(t('token.emailSaved'))
    showNewEmailInput.value = false
    newEmailInput.value = ''
  } catch (e) {
    ElMessage.error(e.response?.data?.msg || t('error.serverError'))
  } finally {
    savingNew.value = false
  }
}
</script>

<template>
  <div id="email-settings" class="email-settings">
    <div class="email-field">
      <span class="email-label">{{ t('token.email') }}</span>
      <template v-if="tokenStore.email">
        <span class="email-value">{{ tokenStore.email }}</span>
        <el-button size="small" type="primary" plain @click="openModifyEmail">
          {{ t('manage.modifyEmail') }}
        </el-button>
      </template>
      <template v-else-if="showNewEmailInput">
        <el-input
          v-model="newEmailInput"
          :placeholder="t('token.emailPlaceholder')"
          class="new-email-input"
          size="small"
        />
        <el-button size="small" type="primary" :disabled="!newEmailInput.trim()" :loading="savingNew" @click="handleSaveNewEmail">
          {{ t('common.save') }}
        </el-button>
      </template>
      <template v-else>
        <span class="email-empty">{{ t('manage.emailNotSet') }}</span>
        <el-button size="small" type="primary" @click="openSetEmail">
          {{ t('token.setEmail') }}
        </el-button>
      </template>
    </div>
    <p class="email-hint">{{ t('manage.emailRealHint') }}</p>

    <!-- Dialog: set new email (no existing email) OR modify email (has existing email) -->
    <el-dialog
      v-model="showDialog"
      :title="tokenStore.email ? t('token.verifyOldEmail') : t('token.verifyEmail')"
      width="420px"
      :close-on-click-modal="false"
    >
      <!-- Verify old email before modification -->
      <template v-if="tokenStore.email">
        <p class="current-email-hint">{{ t('token.currentEmail') }}：{{ tokenStore.email }}</p>
        <div class="verify-row">
          <el-input
            v-model="codeInput"
            :placeholder="t('token.codePlaceholder')"
            @keyup.enter="handleVerifyOldEmail"
          />
          <el-button
            type="primary"
            :loading="sendingCode"
            :disabled="countdown > 0"
            @click="handleSendCodeToExisting"
          >
            {{ countdown > 0 ? t('token.codeResend', { seconds: countdown }) : t('token.sendCode') }}
          </el-button>
        </div>
      </template>

      <!-- Set new email for the first time -->
      <template v-else>
        <div class="verify-row">
          <el-input
            v-model="emailInput"
            :placeholder="t('token.emailPlaceholder')"
          />
          <el-button
            type="primary"
            :loading="sendingCode"
            :disabled="!emailInput.trim() || countdown > 0"
            @click="handleSendCodeToInput"
          >
            {{ countdown > 0 ? t('token.codeResend', { seconds: countdown }) : t('token.sendCode') }}
          </el-button>
        </div>
        <el-input
          v-model="codeInput"
          :placeholder="t('token.codePlaceholder')"
          style="margin-top: 12px"
          @keyup.enter="handleVerifyNewEmail"
        />
      </template>

      <template #footer>
        <el-button @click="showDialog = false">{{ t('common.cancel') }}</el-button>
        <template v-if="tokenStore.email">
          <el-button type="primary" :loading="verifying" :disabled="!codeInput.trim()" @click="handleVerifyOldEmail">
            {{ t('token.verify') }}
          </el-button>
        </template>
        <template v-else>
          <el-button type="primary" :loading="verifying" :disabled="!emailInput.trim() || !codeInput.trim()" @click="handleVerifyNewEmail">
            {{ t('common.save') }}
          </el-button>
        </template>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.email-settings {
  background: #fff;
  border-radius: 8px;
  padding: 16px 20px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.08);
}
.email-field {
  display: flex;
  align-items: center;
  gap: 12px;
}
.email-label {
  font-size: 14px;
  color: #606266;
  flex-shrink: 0;
}
.email-value {
  font-size: 14px;
  color: #303133;
  font-weight: 500;
}
.email-empty {
  font-size: 14px;
  color: #c0c4cc;
}
.new-email-input {
  width: 240px;
}
.email-hint {
  margin: 8px 0 0;
  font-size: 12px;
  color: #909399;
}
.current-email-hint {
  margin: 0 0 12px;
  font-size: 14px;
  color: #606266;
}
.verify-row {
  display: flex;
  gap: 8px;
}
.verify-row .el-input {
  flex: 1;
}
@media (max-width: 767px) {
  .email-field {
    flex-wrap: wrap;
  }
  .new-email-input {
    width: 100%;
  }
  .verify-row {
    flex-direction: column;
  }
}
</style>
