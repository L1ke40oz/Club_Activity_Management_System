<template>
  <div class="page-container">
    <div class="page-header">
      <div>
        <h2 class="page-title">AI 智能分析报告</h2>
        <p class="page-desc">基于大语言模型的社团运营数据分析与建议</p>
      </div>
      <el-button v-if="userStore.isAdmin" type="primary" class="btn-create" @click="showGenDialog">
        <el-icon><MagicStick /></el-icon>生成报告
      </el-button>
    </div>

    <div class="report-grid" v-loading="loading">
      <div v-for="report in reports" :key="report.reportId" class="report-card">
        <div class="report-card__main" @click="viewReport(report)">
          <div class="report-card__icon">
            <el-icon :size="28"><DataAnalysis /></el-icon>
          </div>
          <div class="report-card__body">
            <h4>{{ getReportTitle(report) }}</h4>
            <p class="report-card__range">{{ report.timeRange }}</p>
            <p class="report-card__time">{{ formatTime(report.createTime) }}</p>
          </div>
          <el-icon class="report-card__arrow"><ArrowRight /></el-icon>
        </div>
        <button class="report-card__delete" title="删除" @click.stop="handleDelete(report)">
          <el-icon><Delete /></el-icon>
        </button>
      </div>
    </div>

    <el-empty v-if="!loading && reports.length === 0" description="暂无分析报告，点击上方按钮生成" />

    <!-- 生成报告弹窗 -->
    <el-dialog v-model="genVisible" title="生成AI分析报告" width="560px" :close-on-click-modal="!generating">
      <el-form label-width="90px" v-if="!generating">
        <el-form-item label="分析类型">
          <el-select v-model="genForm.clubId" placeholder="全校综合分析" clearable style="width:100%">
            <el-option :value="null" label="📊 全校综合分析" />
            <el-option v-for="c in clubs" :key="c.clubId" :label="c.name" :value="c.clubId" />
          </el-select>
        </el-form-item>
        <el-form-item label="统计周期">
          <el-select v-model="genForm.timeRange" placeholder="选择统计周期" style="width:100%">
            <el-option label="2026年春季学期（2月-7月）" value="2026年春季学期" />
            <el-option label="2025年秋季学期（9月-1月）" value="2025年秋季学期" />
            <el-option label="近一个月" value="近一个月" />
            <el-option label="近三个月" value="近三个月" />
            <el-option label="本学年（2025-2026）" value="2025-2026学年" />
          </el-select>
        </el-form-item>
      </el-form>

      <!-- 流式输出预览 -->
      <div v-if="generating" class="stream-preview">
        <div class="stream-header">
          <span class="stream-indicator"></span>
          <span>AI 正在生成报告...</span>
          <el-button size="small" text type="danger" @click="cancelGenerate">取消</el-button>
        </div>
        <div class="stream-content markdown-body" v-html="streamRendered"></div>
      </div>

      <template #footer>
        <template v-if="!generating">
          <el-button @click="genVisible = false">取消</el-button>
          <el-button type="primary" @click="handleGenerate">开始生成</el-button>
        </template>
      </template>
    </el-dialog>

    <!-- 报告详情弹窗 -->
    <el-dialog v-model="reportVisible" :title="getReportTitle(reportDetail)" width="750px" top="5vh">
      <div class="report-detail-header">
        <el-tag effect="light" size="small">{{ reportDetail.timeRange }}</el-tag>
        <span class="report-detail-time">生成于 {{ formatTime(reportDetail.createTime) }}</span>
      </div>
      <div class="report-content markdown-body" v-html="renderedContent"></div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, onBeforeUnmount } from 'vue'
import { useUserStore } from '@/stores/user'
import { getReportList, generateReportStream, getReportDetail, deleteReport } from '@/api/aiReport'
import { getClubList } from '@/api/club'
import { ElMessage, ElMessageBox } from 'element-plus'
import { MagicStick, DataAnalysis, ArrowRight, Delete } from '@element-plus/icons-vue'
import { marked } from 'marked'

marked.setOptions({ breaks: true, gfm: true })

const userStore = useUserStore()
const reports = ref([])
const clubs = ref([])
const loading = ref(false)
const genVisible = ref(false)
const reportVisible = ref(false)
const generating = ref(false)
const reportDetail = ref({})
const genForm = reactive({ clubId: null, timeRange: '' })
const streamingContent = ref('')
let streamController = null

const streamRendered = computed(() => {
  if (!streamingContent.value) return '<p style="color:#999">等待AI响应...</p>'
  return marked(streamingContent.value + ' ▌')
})

const renderedContent = computed(() => {
  if (!reportDetail.value?.evaluationAndSuggestion) return ''
  return marked(reportDetail.value.evaluationAndSuggestion)
})

function formatTime(t) {
  if (!t) return ''
  return t.replace('T', ' ').substring(0, 16)
}

function getReportTitle(report) {
  if (!report || !report.clubId) return '📊 全校综合分析'
  const club = clubs.value.find(c => c.clubId === report.clubId)
  return club ? `📋 ${club.name} 运营分析` : '📋 社团分析'
}

async function loadReports() {
  loading.value = true
  try {
    const res = await getReportList()
    reports.value = res.data || []
  } finally {
    loading.value = false
  }
}

async function loadClubs() {
  const res = await getClubList()
  clubs.value = res.data || []
}

function showGenDialog() {
  genForm.clubId = null
  genForm.timeRange = ''
  streamingContent.value = ''
  genVisible.value = true
}

function handleGenerate() {
  if (!genForm.timeRange) {
    ElMessage.warning('请选择统计周期')
    return
  }
  generating.value = true
  streamingContent.value = ''

  streamController = generateReportStream({
    clubId: genForm.clubId,
    timeRange: genForm.timeRange,
    onMessage(token) {
      streamingContent.value += token
    },
    onDone() {
      generating.value = false
      ElMessage.success('报告生成完成')
      loadReports()
    },
    onError(err) {
      generating.value = false
      ElMessage.error('生成失败：' + (err.message || '网络错误'))
    }
  })
}

function cancelGenerate() {
  if (streamController) {
    streamController.abort()
    streamController = null
  }
  generating.value = false
  streamingContent.value = ''
}

async function viewReport(row) {
  const res = await getReportDetail(row.reportId)
  reportDetail.value = res.data
  reportVisible.value = true
}

async function handleDelete(row) {
  await ElMessageBox.confirm('确定删除该报告？', '提示', { type: 'warning' })
  try {
    await deleteReport(row.reportId)
    ElMessage.success('已删除')
    loadReports()
  } catch (e) {
    ElMessage.error('删除失败')
  }
}

onMounted(() => {
  loadReports()
  loadClubs()
})

onBeforeUnmount(() => {
  if (streamController) streamController.abort()
})
</script>

<style scoped>
.page-container {
  animation: fadeIn 0.3s ease;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(8px); }
  to { opacity: 1; transform: translateY(0); }
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 28px;
}

.page-title {
  margin: 0 0 4px;
  font-size: 22px;
  font-weight: 700;
  color: var(--color-primary);
}

.page-desc {
  margin: 0;
  font-size: 14px;
  color: var(--color-text-light);
}

.btn-create {
  border-radius: 10px;
  padding: 10px 20px;
}

.report-grid {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.report-card {
  display: flex;
  align-items: center;
  border-radius: 14px;
  background: var(--color-surface);
  border: 1px solid var(--color-border);
  transition: transform 0.2s, box-shadow 0.2s, border-color 0.2s;
  overflow: hidden;
}

.report-card:hover {
  box-shadow: 0 8px 24px rgba(64, 158, 255, 0.08);
  border-color: var(--color-primary);
}

.report-card__main {
  display: flex;
  align-items: center;
  gap: 18px;
  padding: 22px 24px;
  flex: 1;
  cursor: pointer;
}

.report-card__icon {
  width: 52px;
  height: 52px;
  border-radius: 12px;
  background: linear-gradient(135deg, rgba(64, 158, 255, 0.1), rgba(103, 194, 58, 0.1));
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--color-primary);
  flex-shrink: 0;
}

.report-card__body {
  flex: 1;
}

.report-card__body h4 {
  margin: 0 0 4px;
  font-size: 15px;
  color: var(--color-text);
}

.report-card__range {
  margin: 0 0 2px;
  font-size: 13px;
  color: var(--color-accent);
}

.report-card__time {
  margin: 0;
  font-size: 12px;
  color: var(--color-text-light);
}

.report-card__arrow {
  color: var(--color-text-light);
  transition: transform 0.2s;
}

.report-card:hover .report-card__arrow {
  transform: translateX(4px);
  color: var(--color-primary);
}

.report-card__delete {
  width: 40px;
  height: 100%;
  min-height: 80px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: transparent;
  border: none;
  border-left: 1px solid var(--color-border);
  color: var(--color-text-light);
  cursor: pointer;
  transition: background 0.2s, color 0.2s;
}

.report-card__delete:hover {
  background: rgba(229, 62, 62, 0.04);
  color: var(--color-danger);
}

/* 流式输出预览 */
.stream-preview {
  border-radius: 12px;
  border: 1px solid var(--color-border);
  overflow: hidden;
}

.stream-header {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px 16px;
  background: rgba(43, 108, 176, 0.04);
  border-bottom: 1px solid var(--color-border);
  font-size: 13px;
  color: var(--color-primary);
}

.stream-indicator {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: var(--color-primary);
  animation: pulse 1.2s infinite;
}

@keyframes pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.3; }
}

.stream-content {
  padding: 20px;
  max-height: 400px;
  overflow-y: auto;
  line-height: 1.8;
  font-size: 14px;
  background: #f9fafb;
}

/* 报告详情 */
.report-detail-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 20px;
}

.report-detail-time {
  margin-left: auto;
  font-size: 12px;
  color: var(--color-text-light);
}

.report-content {
  padding: 24px;
  border-radius: 12px;
  background: #f9fafb;
  border: 1px solid var(--color-border);
  max-height: 60vh;
  overflow-y: auto;
  line-height: 1.8;
  font-size: 14px;
}

.markdown-body :deep(h1),
.markdown-body :deep(h2),
.markdown-body :deep(h3),
.markdown-body :deep(h4) {
  margin-top: 16px;
  margin-bottom: 8px;
  font-weight: 600;
  color: var(--color-primary);
}

.markdown-body :deep(h1) { font-size: 20px; }
.markdown-body :deep(h2) { font-size: 18px; }
.markdown-body :deep(h3) { font-size: 16px; }
.markdown-body :deep(h4) { font-size: 15px; }

.markdown-body :deep(p) {
  margin-bottom: 10px;
  color: var(--color-text);
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
  color: var(--color-text);
}

.markdown-body :deep(table) {
  width: 100%;
  border-collapse: collapse;
  margin: 12px 0;
}

.markdown-body :deep(th),
.markdown-body :deep(td) {
  border: 1px solid var(--color-border);
  padding: 8px 12px;
  text-align: left;
  font-size: 13px;
}

.markdown-body :deep(th) {
  background: rgba(43, 108, 176, 0.04);
  font-weight: 600;
}

.markdown-body :deep(blockquote) {
  border-left: 4px solid var(--color-primary);
  padding-left: 12px;
  color: var(--color-text-light);
  margin: 12px 0;
}
</style>
