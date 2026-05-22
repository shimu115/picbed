<script setup>
import TokenManagePanel from '@/components/admin/TokenManagePanel.vue'
import ImageManageTable from '@/components/admin/ImageManageTable.vue'
</script>

<template>
  <div class="management-page">
    <h2>Management</h2>

    <div class="management-section">
      <h3>Tokens</h3>
      <TokenManagePanel />
    </div>

    <div class="management-section">
      <h3>Images</h3>
      <ImageManageTable />
    </div>
  </div>
</template>
