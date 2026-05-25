<script setup>
import { ref, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import AppHeader from '@/components/layout/AppHeader.vue'
import { useSettingsStore } from '@/stores/settings'
import { useTokenStore } from '@/stores/token'
import { updateOwnEmail } from '@/api'
import { ElMessage, ElMessageBox } from 'element-plus'

const { t } = useI18n()
const settingsStore = useSettingsStore()
const tokenStore = useTokenStore()

const showEmailDialog = ref(false)
const emailInput = ref('')
const savingEmail = ref(false)

onMounted(() => {
  settingsStore.fetchSettings()
})

async function handleSetEmail() {
  if (!emailInput.value.trim()) return
  savingEmail.value = true
  try {
    await updateOwnEmail(emailInput.value.trim())
    tokenStore.setEmail(emailInput.value.trim())
    window.dispatchEvent(new CustomEvent('token-email-updated'))
    ElMessage.success(t('token.emailSaved'))
    showEmailDialog.value = false
  } catch (e) {
    ElMessage.error(e.response?.data?.msg || t('error.serverError'))
  } finally {
    savingEmail.value = false
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
      :title="t('token.setEmail')"
      width="400px"
      :close-on-click-modal="false"
    >
      <el-input
        v-model="emailInput"
        :placeholder="t('token.emailPlaceholder')"
        @keyup.enter="handleSetEmail"
      />
      <template #footer>
        <el-button @click="showEmailDialog = false">{{ t('common.cancel') }}</el-button>
        <el-button type="primary" :loading="savingEmail" :disabled="!emailInput.trim()" @click="handleSetEmail">
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
@media (max-width: 767px) {
  .email-banner {
    flex-direction: column;
    gap: 8px;
    text-align: center;
    font-size: 13px;
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
