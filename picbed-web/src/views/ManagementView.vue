<script setup>
import { useI18n } from 'vue-i18n'
import TokenManagePanel from '@/components/admin/TokenManagePanel.vue'
import ImageManageTable from '@/components/admin/ImageManageTable.vue'

const { t } = useI18n()
</script>

<template>
  <div class="management-page">
    <h2>{{ t('manage.title') }}</h2>

    <div class="management-section">
      <h3>{{ t('manage.tokens') }}</h3>
      <TokenManagePanel />
    </div>

    <div class="management-section">
      <h3>{{ t('manage.images') }}</h3>
      <ImageManageTable />
    </div>
  </div>
</template>
