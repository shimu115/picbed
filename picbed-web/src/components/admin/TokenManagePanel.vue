<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { listTokens, createToken, revokeToken, updateTokenEmail, adminRefreshToken, warnToken } from '@/api'
import { ElMessage, ElMessageBox } from 'element-plus'

const { t } = useI18n()
const tokens = ref([])
const loading = ref(false)
const newTokenName = ref('')
const newTokenEmail = ref('')
const generatedToken = ref('')
const editingEmailId = ref(null)
const editingEmailValue = ref('')
const savingEmail = ref(false)

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
    const res = await createToken(newTokenName.value.trim(), newTokenEmail.value.trim())
    generatedToken.value = res.data.data.token
    newTokenName.value = ''
    newTokenEmail.value = ''
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

function startEditEmail(token) {
  editingEmailId.value = token.id
  editingEmailValue.value = token.email || ''
}

async function saveEmail(token) {
  savingEmail.value = true
  try {
    await updateTokenEmail(token.id, editingEmailValue.value.trim())
    await loadTokens()
    editingEmailId.value = null
  } catch (e) {
    ElMessage.error(e.response?.data?.msg || t('error.serverError'))
  } finally {
    savingEmail.value = false
  }
}

function cancelEditEmail() {
  editingEmailId.value = null
}

const refreshingId = ref(null)
const adminGeneratedToken = ref('')

async function handleAdminRefresh(token) {
  try {
    await ElMessageBox.confirm(
      t('token.adminRefreshConfirm', { name: token.name }),
      t('common.confirm'),
      { type: 'warning', confirmButtonText: t('token.adminRefresh'), cancelButtonText: t('common.cancel') }
    )
    refreshingId.value = token.id
    const res = await adminRefreshToken(token.id)
    adminGeneratedToken.value = res.data.data.token
    ElMessage.success(t('token.refreshSuccess'))
  } catch (e) {
    if (e !== 'cancel' && e?.response?.data?.msg) {
      ElMessage.error(e.response.data.msg)
    }
  } finally {
    refreshingId.value = null
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

function copyAdminGeneratedToken() {
  copyToClipboard(adminGeneratedToken.value)
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
      <el-input
        v-model="newTokenEmail"
        :placeholder="t('token.emailPlaceholder')"
        class="email-input"
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

    <div v-if="adminGeneratedToken" class="generated-token-box">
      <p class="warning-text">{{ t('token.newTokenShown') }}</p>
      <el-input :model-value="adminGeneratedToken" readonly>
        <template #append>
          <el-button @click="copyAdminGeneratedToken">{{ t('common.copy') }}</el-button>
        </template>
      </el-input>
    </div>

    <div class="token-table-wrap">
      <el-table :data="tokens" v-loading="loading">
      <el-table-column prop="id" :label="t('token.id')" width="70" />
      <el-table-column prop="name" :label="t('token.name')" />
      <el-table-column :label="t('token.email')" width="260">
        <template #default="{ row }">
          <template v-if="editingEmailId === row.id">
            <el-input
              v-model="editingEmailValue"
              size="small"
              :placeholder="t('token.emailPlaceholder')"
              style="width: 130px"
              @keyup.enter="saveEmail(row)"
              @keyup.escape="cancelEditEmail"
            />
            <el-button size="small" type="primary" text :loading="savingEmail" @click="saveEmail(row)">
              {{ t('common.save') }}
            </el-button>
            <el-button size="small" text @click="cancelEditEmail">{{ t('common.cancel') }}</el-button>
          </template>
          <template v-else>
            <span class="email-cell" @click="startEditEmail(row)">
              {{ row.email || '-' }}
            </span>
          </template>
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
          <el-tag :type="row.isActive ? 'success' : 'danger'" size="small">
            {{ row.isActive ? t('common.yes') : t('common.no') }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column :label="t('token.created')" width="170">
        <template #default="{ row }">{{ row.createdAt?.replace('T', ' ')?.substring(0, 19) }}</template>
      </el-table-column>
      <el-table-column :label="t('token.actions')" width="220">
        <template #default="{ row }">
          <template v-if="row.isActive">
            <el-button
              v-if="row.email"
              size="small"
              type="primary"
              text
              :loading="refreshingId === row.id"
              @click="handleAdminRefresh(row)"
            >
              {{ t('token.adminRefresh') }}
            </el-button>
            <el-button
              v-if="row.email"
              size="small"
              type="warning"
              text
              @click="handleWarn(row)"
            >
              {{ t('token.warnUser') }}
            </el-button>
            <el-button size="small" type="danger" text @click="handleRevoke(row)">
              {{ t('token.revoke') }}
            </el-button>
          </template>
        </template>
      </el-table-column>
    </el-table>
    </div>

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
.email-input {
  max-width: 260px;
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
