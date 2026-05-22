<script setup>
import { ref, onMounted } from 'vue'
import { listTokens, createToken, revokeToken } from '@/api'
import { ElMessage, ElMessageBox } from 'element-plus'

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
    ElMessage.error(e.response?.data?.message || 'Failed to create token')
  }
}

async function handleRevoke(token) {
  try {
    await ElMessageBox.confirm(`Revoke token "${token.name}"? This cannot be undone.`, 'Warning', {
      type: 'warning',
      confirmButtonText: 'Revoke',
      cancelButtonText: 'Cancel'
    })
    await revokeToken(token.id)
    ElMessage.success('Token revoked')
    await loadTokens()
  } catch { /* cancelled */ }
}

function copyGeneratedToken() {
  navigator.clipboard.writeText(generatedToken.value)
  ElMessage.success('Token copied to clipboard')
}

onMounted(loadTokens)
</script>

<template>
  <div class="token-panel">
    <div class="create-token">
      <el-input
        v-model="newTokenName"
        placeholder="Token name (e.g. My Laptop)"
        class="name-input"
        @keyup.enter="handleCreate"
      />
      <el-button type="primary" @click="handleCreate" :disabled="!newTokenName.trim()">
        Generate Token
      </el-button>
    </div>

    <div v-if="generatedToken" class="generated-token-box">
      <p class="warning-text">Copy this token now - it won't be shown again!</p>
      <el-input :model-value="generatedToken" readonly>
        <template #append>
          <el-button @click="copyGeneratedToken">Copy</el-button>
        </template>
      </el-input>
    </div>

    <el-table :data="tokens" v-loading="loading" style="margin-top: 16px">
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column prop="name" label="Name" />
      <el-table-column label="Active" width="80">
        <template #default="{ row }">
          <el-tag :type="row.isActive ? 'success' : 'danger'" size="small">
            {{ row.isActive ? 'Yes' : 'No' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="Created" width="170">
        <template #default="{ row }">{{ row.createdAt?.replace('T', ' ')?.substring(0, 19) }}</template>
      </el-table-column>
      <el-table-column label="Actions" width="100">
        <template #default="{ row }">
          <el-button v-if="row.isActive" size="small" type="danger" text @click="handleRevoke(row)">
            Revoke
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
