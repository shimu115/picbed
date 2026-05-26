<script setup>
import { useI18n } from 'vue-i18n'
import { useTokenStore } from '@/stores/token'
import TokenManagePanel from '@/components/admin/TokenManagePanel.vue'
import ImageManageTable from '@/components/admin/ImageManageTable.vue'
import SettingsPanel from '@/components/admin/SettingsPanel.vue'
import EmailSettings from '@/components/common/EmailSettings.vue'

const { t } = useI18n()
const tokenStore = useTokenStore()
</script>

<template>
  <div class="management-page">
    <h2>{{ t('manage.title') }}</h2>

    <div class="management-section">
      <h3>{{ t('manage.emailSettings') }}</h3>
      <EmailSettings />
    </div>

    <div v-if="tokenStore.isAdmin" class="management-section">
      <h3>{{ t('manage.settings') }}</h3>
      <SettingsPanel />
    </div>

    <div v-if="tokenStore.isAdmin" class="management-section">
      <h3>{{ t('manage.tokens') }}</h3>
      <TokenManagePanel />
    </div>

    <div class="management-section">
      <h3>{{ t('manage.images') }}</h3>
      <ImageManageTable />
    </div>
  </div>
</template>