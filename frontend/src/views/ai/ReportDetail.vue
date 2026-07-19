<template>
  <div class="report-detail" v-loading="loading">
    <el-page-header @back="$router.back()" content="报告详情" style="margin-bottom: 20px" />

    <el-card v-if="report">
      <template #header>
        <div class="card-header">
          <span>AI分析报告 #{{ report.reportId }}</span>
          <el-tag>{{ report.timeRange }}</el-tag>
        </div>
      </template>
      <el-descriptions :column="2" border style="margin-bottom: 20px">
        <el-descriptions-item label="报告ID">{{ report.reportId }}</el-descriptions-item>
        <el-descriptions-item label="社团ID">{{ report.clubId || '全校汇总' }}</el-descriptions-item>
        <el-descriptions-item label="时间范围">{{ report.timeRange }}</el-descriptions-item>
        <el-descriptions-item label="生成时间">{{ report.createTime }}</el-descriptions-item>
      </el-descriptions>
      <div class="report-content">
        <h4>分析与建议</h4>
        <div class="content-text markdown-body" v-html="renderedContent"></div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { getReportDetail } from '../../api/aiReport'
import { marked } from 'marked'

const route = useRoute()
const loading = ref(false)
const report = ref(null)

marked.setOptions({
  breaks: true,
  gfm: true
})

const renderedContent = computed(() => {
  if (!report.value?.evaluationAndSuggestion) return ''
  return marked(report.value.evaluationAndSuggestion)
})

const fetchReport = async () => {
  loading.value = true
  try {
    const res = await getReportDetail(route.params.id)
    if (res.code === 200) report.value = res.data
  } finally {
    loading.value = false
  }
}

onMounted(fetchReport)
</script>

<style scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.report-content {
  padding: 20px;
  background: #f9fafb;
  border-radius: 12px;
}
.content-text {
  line-height: 1.8;
}
.markdown-body :deep(h1),
.markdown-body :deep(h2),
.markdown-body :deep(h3) {
  margin-top: 16px;
  margin-bottom: 8px;
  font-weight: 600;
  color: #303133;
}
.markdown-body :deep(h1) { font-size: 20px; }
.markdown-body :deep(h2) { font-size: 18px; }
.markdown-body :deep(h3) { font-size: 16px; }
.markdown-body :deep(p) {
  margin-bottom: 12px;
}
.markdown-body :deep(ul),
.markdown-body :deep(ol) {
  padding-left: 20px;
  margin-bottom: 12px;
}
.markdown-body :deep(li) {
  margin-bottom: 4px;
}
.markdown-body :deep(strong) {
  color: #303133;
}
.markdown-body :deep(code) {
  background: #e8eaed;
  padding: 2px 6px;
  border-radius: 4px;
  font-size: 13px;
}
.markdown-body :deep(blockquote) {
  border-left: 4px solid var(--color-primary, #409eff);
  padding-left: 12px;
  color: #606266;
  margin: 12px 0;
}
</style>
