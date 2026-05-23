<script setup>
import { ref } from 'vue'
import { useI18n } from 'vue-i18n'
import { useTokenStore } from '@/stores/token'

const { t } = useI18n()
const tokenStore = useTokenStore()
const showDialog = ref(false)
const inputToken = ref('')

function saveToken() {
  if (inputToken.value.trim()) {
    tokenStore.setToken(inputToken.value.trim())
    showDialog.value = false
    inputToken.value = ''
  }
}

function removeToken() {
  tokenStore.clearToken()
}
</script>

<template>
  <div class="token-indicator">
    <template v-if="tokenStore.hasToken">
      <el-tag type="success" size="small">
        <el-icon style="margin-right: 4px"><svg viewBox="0 0 24 24" width="14" height="14" fill="currentColor"><path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm-2 15l-5-5 1.41-1.41L10 14.17l7.59-7.59L19 8l-9 9z"/></svg></el-icon>
        {{ t('token.set') }}
      </el-tag>
      <el-button type="danger" size="small" text @click="removeToken">{{ t('token.remove') }}</el-button>
    </template>
    <template v-else>
      <el-button type="warning" size="small" @click="showDialog = true">
        {{ t('token.setToken') }}
      </el-button>
    </template>

    <el-dialog v-model="showDialog" :title="t('token.setAuthToken')" width="420px">
      <el-input
        v-model="inputToken"
        :placeholder="t('token.pasteToken')"
        clearable
        @keyup.enter="saveToken"
      />
      <template #footer>
        <el-button @click="showDialog = false">{{ t('common.cancel') }}</el-button>
        <el-button type="primary" @click="saveToken">{{ t('common.save') }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.token-indicator {
  display: flex;
  align-items: center;
  gap: 6px;
}
</style>
