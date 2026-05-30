<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { listTokens, createToken, revokeToken, toggleTokenActive, updateTokenEmail, warnToken, adminSendVerificationCode, adminVerifyEmailCode, adminRefreshToken } from '@/api'
import { ElMessage, ElMessageBox } from 'element-plus'

const { t } = useI18n()
const tokens = ref([])
const loading = ref(false)
const newTokenName = ref('')
const generatedToken = ref('')
const showEmailDialog = ref(false)
const editingToken = ref(null)
const emailDialogStep = ref(1)
const isNewEmail = ref(false)
const editNewEmail = ref('')
const savingEmail = ref(false)
const codeInput = ref('')
const verifyingOldEmail = ref(false)
const sendingCode = ref(false)
const codeCountdown = ref(0)
let countdownTimer = null

async function loadTokens() {
  loading.value = true
  try {
    const res = await listTokens()
    tokens.value = res.data.data
  } finally {
    loading.value = false
  }
}

async function handleCreate() {
  if (!newTokenName.value.trim()) return
  try {
    const res = await createToken(newTokenName.value.trim(), '')
    generatedToken.value = res.data.data.token
    newTokenName.value = ''
    await loadTokens()
  } catch (e) {
    ElMessage.error(e.response?.data?.msg || t('token.createFailed'))
  }
}

async function handleRevoke(token) {
  try {
    await ElMessageBox.confirm(
      t('token.revokeConfirm', { name: token.name }),
      t('common.confirm'),
      { type: 'warning', confirmButtonText: t('token.revoke'), cancelButtonText: t('common.cancel') }
    )
    await revokeToken(token.id)
    ElMessage.success(t('token.revokeSuccess'))
    await loadTokens()
  } catch (e) {
    if (e.response?.data?.msg) {
      ElMessage.error(e.response.data.msg)
    }
  }
}

async function handleToggleActive(token) {
  const newActive = !token.isActive
  try {
    await toggleTokenActive(token.id, newActive)
    ElMessage.success(t('token.operationSuccess'))
    await loadTokens()
  } catch (e) {
    ElMessage.error(e.response?.data?.msg || t('error.serverError'))
  }
}

function openEmailDialog(token) {
  editingToken.value = token
  editNewEmail.value = ''
  codeInput.value = ''
  codeCountdown.value = 0
  if (countdownTimer) {
    clearInterval(countdownTimer)
    countdownTimer = null
  }
  if (token.email) {
    isNewEmail.value = false
    emailDialogStep.value = 1
  } else {
    isNewEmail.value = true
    emailDialogStep.value = 1
  }
  showEmailDialog.value = true
}

function startCountdown() {
  codeCountdown.value = 60
  countdownTimer = setInterval(() => {
    codeCountdown.value--
    if (codeCountdown.value <= 0) {
      clearInterval(countdownTimer)
      countdownTimer = null
    }
  }, 1000)
}

async function handleAdminSendCode() {
  if (!editingToken.value) return
  sendingCode.value = true
  try {
    const email = isNewEmail.value ? editNewEmail.value.trim() : null
    if (isNewEmail.value && !email) {
      ElMessage.warning(t('token.emailRequired'))
      sendingCode.value = false
      return
    }
    await adminSendVerificationCode(editingToken.value.id, email)
    ElMessage.success(t('token.codeSent'))
    startCountdown()
  } catch (e) {
    ElMessage.error(e.response?.data?.msg || t('error.serverError'))
  } finally {
    sendingCode.value = false
  }
}

async function handleVerifyOldEmail() {
  if (!codeInput.value.trim() || !editingToken.value) return
  verifyingOldEmail.value = true
  try {
    await adminVerifyEmailCode(editingToken.value.id, codeInput.value.trim())
    ElMessage.success(t('token.emailVerified'))
    emailDialogStep.value = 2
  } catch (e) {
    ElMessage.error(e.response?.data?.msg || t('error.serverError'))
  } finally {
    verifyingOldEmail.value = false
  }
}

async function handleNewEmailVerifyAndSave() {
  if (!codeInput.value.trim() || !editNewEmail.value.trim() || !editingToken.value) return
  savingEmail.value = true
  try {
    await adminVerifyEmailCode(editingToken.value.id, codeInput.value.trim())
    await updateTokenEmail(editingToken.value.id, editNewEmail.value.trim())
    await loadTokens()
    showEmailDialog.value = false
    ElMessage.success(t('token.emailSaved'))
  } catch (e) {
    ElMessage.error(e.response?.data?.msg || t('error.serverError'))
  } finally {
    savingEmail.value = false
  }
}

async function saveEmailDialog() {
  savingEmail.value = true
  try {
    await updateTokenEmail(editingToken.value.id, editNewEmail.value.trim())
    await loadTokens()
    showEmailDialog.value = false
    ElMessage.success(t('token.emailSaved'))
  } catch (e) {
    ElMessage.error(e.response?.data?.msg || t('error.serverError'))
  } finally {
    savingEmail.value = false
  }
}

async function handleRefresh(token) {
  try {
    const res = await adminRefreshToken(token.id)
    generatedToken.value = res.data.data.token
    ElMessage.success(t('token.refreshSuccess'))
    await loadTokens()
  } catch (e) {
    ElMessage.error(e.response?.data?.msg || t('error.serverError'))
  }
}

async function handleWarn(token) {
  try {
    await ElMessageBox.confirm(
      t('token.warnConfirm', { name: token.name }),
      t('common.confirm'),
      { type: 'warning', confirmButtonText: t('token.warnUser'), cancelButtonText: t('common.cancel') }
    )
    await warnToken(token.id)
    ElMessage.success(t('token.warnSent'))
  } catch (e) {
    // cancelled
  }
}

function copyGeneratedToken() {
  copyToClipboard(generatedToken.value)
}

function copyToClipboard(text) {
  if (navigator.clipboard && window.isSecureContext) {
    navigator.clipboard.writeText(text).then(() => {
      ElMessage.success(t('common.copySuccess'))
    })
  } else {
    const ta = document.createElement('textarea')
    ta.value = text
    ta.style.position = 'fixed'
    ta.style.left = '-9999px'
    document.body.appendChild(ta)
    ta.select()
    document.execCommand('copy')
    document.body.removeChild(ta)
    ElMessage.success(t('common.copySuccess'))
  }
}

onMounted(() => {
  loadTokens()
  window.addEventListener('token-email-updated', loadTokens)
})

onUnmounted(() => {
  window.removeEventListener('token-email-updated', loadTokens)
})
</script>

<template>
  <div class="token-panel">
    <div class="create-token">
      <el-input
        v-model="newTokenName"
        :placeholder="t('token.namePlaceholder')"
        class="name-input"
        @keyup.enter="handleCreate"
      />
      <el-button type="primary" @click="handleCreate" :disabled="!newTokenName.trim()">
        {{ t('token.generate') }}
      </el-button>
    </div>

    <div v-if="generatedToken" class="generated-token-box">
      <p class="warning-text">{{ t('token.generatedWarning') }}</p>
      <el-input :model-value="generatedToken" readonly>
        <template #append>
          <el-button @click="copyGeneratedToken">{{ t('common.copy') }}</el-button>
        </template>
      </el-input>
    </div>

    <div class="token-table-wrap">
      <el-table :data="tokens" v-loading="loading">
      <el-table-column prop="id" :label="t('token.id')" width="70" />
      <el-table-column prop="name" :label="t('token.name')" />
      <el-table-column :label="t('token.email')" width="260">
        <template #default="{ row }">
          <span class="email-cell" @click="openEmailDialog(row)">
            {{ row.email || '-' }}
          </span>
        </template>
      </el-table-column>
      <el-table-column :label="t('token.role')" width="80">
        <template #default="{ row }">
          <el-tag :type="row.role === 'ADMIN' ? 'warning' : 'info'" size="small">
            {{ row.role === 'ADMIN' ? t('token.admin') : t('token.user') }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column :label="t('token.active')" width="80">
        <template #default="{ row }">
          <el-switch
            :model-value="row.isActive"
            :disabled="row.role === 'ADMIN'"
            size="small"
            @change="handleToggleActive(row)"
          />
        </template>
      </el-table-column>
      <el-table-column :label="t('token.created')" width="170">
        <template #default="{ row }">{{ row.createdAt?.replace('T', ' ')?.substring(0, 19) }}</template>
      </el-table-column>
      <el-table-column :label="t('token.actions')" width="230">
        <template #default="{ row }">
          <el-button
            v-if="row.role === 'ADMIN' && (row.email !== null)"
            size="small"
            type="primary"
            text
            @click="handleRefresh(row)"
          >
            {{ t('token.refresh') }}
          </el-button>
          <el-button
              v-if="row.role !== 'ADMIN' && (row.email !== null)"
              size="small"
              type="primary"
              text
              @click="handleRefresh(row)"
          >
            {{ t('token.refresh') }}
          </el-button>
          <el-button
              v-if="row.role !== 'ADMIN' && (row.email !== null)"
              size="small"
              type="warning"
              text
              @click="handleWarn(row)"
          >
            {{ t('token.warnUser') }}
          </el-button>
          <el-button
            v-if="row.role !== 'ADMIN'"
            size="small"
            type="danger"
            text
            @click="handleRevoke(row)"
          >
            {{ t('token.revoke') }}
          </el-button>
        </template>
      </el-table-column>
    </el-table>
    </div>

    <el-dialog
      v-model="showEmailDialog"
      :title="isNewEmail ? t('token.setEmail') : (emailDialogStep === 1 ? t('token.verifyOldEmail') : t('token.setEmail'))"
      width="420px"
      :close-on-click-modal="false"
      @close="editingToken = null"
    >
      <template v-if="editingToken">
        <!-- No existing email: single-step set email with verification -->
        <template v-if="isNewEmail">
          <el-input
            v-model="editNewEmail"
            :placeholder="t('token.emailPlaceholder')"
            style="margin-bottom: 10px"
          />
          <div class="code-row">
            <el-input
              v-model="codeInput"
              :placeholder="t('token.codePlaceholder')"
              @keyup.enter="handleNewEmailVerifyAndSave"
            />
            <el-button
              type="primary"
              :loading="sendingCode"
              :disabled="codeCountdown > 0 || !editNewEmail.trim()"
              @click="handleAdminSendCode"
            >
              {{ codeCountdown > 0 ? t('token.codeResend', { seconds: codeCountdown }) : t('token.sendCode') }}
            </el-button>
          </div>
        </template>
        <!-- Has existing email: two-step flow -->
        <template v-else>
          <template v-if="emailDialogStep === 1">
            <p class="current-email-hint">
              {{ t('token.currentEmail') }}：{{ editingToken.email }}
            </p>
            <div class="code-row">
              <el-input
                v-model="codeInput"
                :placeholder="t('token.codePlaceholder')"
                @keyup.enter="handleVerifyOldEmail"
              />
              <el-button
                type="primary"
                :loading="sendingCode"
                :disabled="codeCountdown > 0"
                @click="handleAdminSendCode"
              >
                {{ codeCountdown > 0 ? t('token.codeResend', { seconds: codeCountdown }) : t('token.sendCode') }}
              </el-button>
            </div>
          </template>
          <template v-else>
            <el-input
              v-model="editNewEmail"
              :placeholder="t('token.emailPlaceholder')"
              @keyup.enter="saveEmailDialog"
            />
          </template>
        </template>
      </template>
      <template #footer>
        <el-button @click="showEmailDialog = false">{{ t('common.cancel') }}</el-button>
        <template v-if="isNewEmail">
          <el-button type="primary" :loading="savingEmail" :disabled="!codeInput.trim() || !editNewEmail.trim()" @click="handleNewEmailVerifyAndSave">
            {{ t('token.verifyAndSave') }}
          </el-button>
        </template>
        <template v-else-if="emailDialogStep === 1">
          <el-button type="primary" :loading="verifyingOldEmail" :disabled="!codeInput.trim()" @click="handleVerifyOldEmail">
            {{ t('token.verify') }}
          </el-button>
        </template>
        <template v-else>
          <el-button type="primary" :loading="savingEmail" :disabled="!editNewEmail.trim()" @click="saveEmailDialog">
            {{ t('common.save') }}
          </el-button>
        </template>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.create-token {
  display: flex;
  gap: 10px;
}
.name-input {
  max-width: 240px;
  flex: 1;
}
.generated-token-box {
  margin-top: 16px;
  padding: 12px;
  background: #fef0f0;
  border-radius: 6px;
  border: 1px solid #fab6b6;
}
.warning-text {
  font-size: 13px;
  color: #f56c6c;
  margin-bottom: 8px;
  font-weight: 500;
}
.email-cell {
  cursor: pointer;
  color: #409eff;
  max-width: 180px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  display: inline-block;
}
.current-email-hint {
  font-size: 13px;
  color: #909399;
  margin-bottom: 10px;
}
.code-row {
  display: flex;
  gap: 8px;
}
.code-row .el-input {
  flex: 1;
}
.email-cell:hover {
  text-decoration: underline;
}
.token-table-wrap {
  margin-top: 16px;
  overflow-x: auto;
  -webkit-overflow-scrolling: touch;
}

@media (max-width: 767px) {
  .create-token {
    flex-direction: column;
    gap: 8px;
  }
  .name-input {
    max-width: none;
    width: 100%;
  }
  .create-token .el-button {
    width: 100%;
  }
}
</style>
