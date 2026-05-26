<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { setupToken, getEmailDomains } from '@/api'
import { ElMessage } from 'element-plus'

const router = useRouter()
const { t } = useI18n()

const masterKey = ref('')
const tokenName = ref('Admin')
const emailUsername = ref('')
const emailDomain = ref('')
const customDomain = ref('')
const domainOptions = ref([])
const generatedToken = ref('')
const loading = ref(false)
const error = ref('')
const isOther = ref(false)

onMounted(async () => {
  try {
    const res = await getEmailDomains()
    domainOptions.value = res.data.data || []
  } catch {
    domainOptions.value = ['qq.com', 'outlook.com', '163.com']
  }
})

function onDomainChange(val) {
  isOther.value = val === '__other__'
  if (isOther) {
    customDomain.value = ''
  }
}

function fullEmail() {
  const domain = isOther.value ? customDomain.value.trim() : emailDomain.value
  if (!emailUsername.value.trim() || !domain) return ''
  return emailUsername.value.trim() + '@' + domain
}

async function handleSetup() {
  error.value = ''
  const email = fullEmail()
  if (!email) {
    error.value = t('setup.emailRequired')
    return
  }
  loading.value = true
  try {
    const res = await setupToken(masterKey.value, tokenName.value || 'Admin', email)
    generatedToken.value = res.data.data.token
    ElMessage.success(t('setup.success'))
  } catch (e) {
    error.value = e.response?.data?.msg || t('setup.setupFailed')
  } finally {
    loading.value = false
  }
}

const canSubmit = computed(() => {
  return masterKey.value && fullEmail()
})

function copyAndGo() {
  navigator.clipboard.writeText(generatedToken.value)
  ElMessage.success(t('common.copySuccess'))
  setTimeout(() => router.push('/upload'), 500)
}
</script>

<template>
  <div class="setup-page">
    <h2>{{ t('setup.title') }}</h2>
    <p class="setup-desc">{{ t('setup.desc') }}</p>

    <el-card v-if="!generatedToken" class="setup-card">
      <el-form @submit.prevent="handleSetup">
        <el-form-item :label="t('setup.masterKey')">
          <el-input v-model="masterKey" type="password" :placeholder="t('setup.masterKeyPlaceholder')" />
        </el-form-item>
        <el-form-item :label="t('setup.tokenName')">
          <el-input v-model="tokenName" :placeholder="t('setup.tokenNamePlaceholder')" />
        </el-form-item>
        <el-form-item :label="t('token.email')" required>
          <div class="email-split">
            <el-input
              v-model="emailUsername"
              :placeholder="t('setup.emailUsernamePlaceholder')"
              class="email-user-input"
            />
            <span class="email-at">@</span>
            <el-select
              v-model="emailDomain"
              :placeholder="t('setup.emailDomainPlaceholder')"
              class="email-domain-select"
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
            class="custom-domain-input"
          />
          <p v-if="isOther" class="other-warn">{{ t('setup.emailDomainOtherWarn') }}</p>
          <p class="email-hint">{{ t('setup.emailHint') }}</p>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSetup" :loading="loading" :disabled="!canSubmit">
            {{ t('setup.createToken') }}
          </el-button>
        </el-form-item>
        <p v-if="error" class="error-text">{{ error }}</p>
      </el-form>
    </el-card>

    <el-result
      v-else
      icon="success"
      :title="t('setup.success')"
      :sub-title="t('setup.successDesc')"
    >
      <template #extra>
        <el-input :model-value="generatedToken" readonly size="large" style="margin-bottom: 16px" />
        <el-button type="primary" @click="copyAndGo">{{ t('token.copyAndGo') }}</el-button>
      </template>
    </el-result>
  </div>
</template>

<style scoped>
.setup-page {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding-top: 40px;
}
.setup-desc {
  color: #606266;
  margin-bottom: 20px;
  max-width: 500px;
  text-align: center;
}
.setup-card {
  max-width: 500px;
  width: 100%;
}
.error-text {
  color: #f56c6c;
  font-size: 13px;
}
.email-split {
  display: flex;
  align-items: center;
  gap: 4px;
  width: 100%;
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
  width: 160px;
  flex-shrink: 0;
}
.custom-domain-input {
  margin-top: 8px;
}
.other-warn {
  font-size: 12px;
  color: #e6a23c;
  margin-top: 6px;
  line-height: 1.5;
}
.email-hint {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
  line-height: 1.5;
}
</style>
