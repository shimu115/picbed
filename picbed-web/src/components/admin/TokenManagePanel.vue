<script setup>
import { ref, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { listTokens, createToken, revokeToken } from '@/api'
import { ElMessage, ElMessageBox } from 'element-plus'

const { t } = useI18n()
const tokens = ref([])
const loading = ref(false)
const newTokenName = ref('')
const generatedToken = ref('')

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
    const res = await createToken(newTokenName.value.trim())
    generatedToken.value = res.data.data.token
    newTokenName.value = ''
    await loadTokens()
  } catch (e) {
    ElMessage.error(e.response?.data?.message || t('token.createFailed'))
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
  } catch { /* cancelled */ }
}

function copyGeneratedToken() {
  navigator.clipboard.writeText(generatedToken.value)
  ElMessage.success(t('common.copySuccess'))
}

onMounted(loadTokens)
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

    <el-table :data="tokens" v-loading="loading" style="margin-top: 16px">
      <el-table-column prop="id" :label="t('token.id')" width="70" />
      <el-table-column prop="name" :label="t('token.name')" />
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
      <el-table-column :label="t('token.actions')" width="100">
        <template #default="{ row }">
          <el-button v-if="row.isActive" size="small" type="danger" text @click="handleRevoke(row)">
            {{ t('token.revoke') }}
          </el-button>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<style scoped>
.create-token {
  display: flex;
  gap: 10px;
}
.name-input {
  max-width: 300px;
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
</style>
