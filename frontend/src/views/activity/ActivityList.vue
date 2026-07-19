<template>
  <div class="page-container">
    <div class="page-header">
      <div>
        <h2 class="page-title">活动管理</h2>
        <p class="page-desc">查看和管理社团活动</p>
      </div>
      <div class="page-actions">
        <el-input v-model="searchTitle" placeholder="搜索活动..." clearable class="search-input" @clear="loadActivities" @keyup.enter="loadActivities">
          <template #prefix><el-icon><Search /></el-icon></template>
        </el-input>
        <el-select v-model="searchStatus" placeholder="全部状态" clearable style="width:120px" @change="loadActivities">
          <el-option label="待审批" :value="0" />
          <el-option label="已发布" :value="1" />
          <el-option label="已结束" :value="2" />
          <el-option label="已驳回" :value="3" />
        </el-select>
        <el-button type="primary" class="btn-create" @click="showAddDialog">
          <el-icon><Plus /></el-icon>申请活动
        </el-button>
      </div>
    </div>

    <div class="activity-list">
      <div v-for="act in activities" :key="act.activityId" class="activity-card">
        <div class="activity-card__left">
          <div class="activity-date-badge">
            <span class="date-day">{{ getDay(act.eventTime) }}</span>
            <span class="date-month">{{ getMonth(act.eventTime) }}</span>
          </div>
        </div>
        <div class="activity-card__body">
          <div class="activity-card__top">
            <h3 @click="viewDetail(act)">{{ act.title }}</h3>
            <el-tag :type="statusType(act.status)" size="small" effect="light" class="status-tag">
              {{ statusText(act.status) }}
            </el-tag>
          </div>
          <div class="activity-card__info">
            <span><el-icon><CollectionTag /></el-icon>{{ act.clubName || '未知社团' }}</span>
            <span><el-icon><Location /></el-icon>{{ act.location || '待定' }}</span>
            <span><el-icon><Clock /></el-icon>{{ formatTime(act.eventTime) }}</span>
            <span><el-icon><User /></el-icon>{{ act.maxParticipants }}人上限</span>
          </div>
          <div class="activity-card__actions">
            <el-button size="small" text type="primary" @click="viewDetail(act)">详情</el-button>
            <el-button v-if="act.status === 1" size="small" text type="success" @click="handleSignup(act)">报名</el-button>
            <el-button v-if="act.status === 1" size="small" text @click="viewRegistrations(act)">签到管理</el-button>
            <el-button v-if="userStore.isAdmin && act.status === 0" size="small" text type="success" @click="handleApprove(act)">通过</el-button>
            <el-button v-if="userStore.isAdmin && act.status === 0" size="small" text type="danger" @click="handleReject(act)">驳回</el-button>
            <el-button v-if="act.status === 1" size="small" text type="warning" @click="handleFinish(act)">结束</el-button>
            <el-button v-if="userStore.isAdmin || act.status === 3" size="small" text type="danger" @click="handleDelete(act)">删除</el-button>
          </div>
        </div>
      </div>
    </div>

    <el-empty v-if="!loading && activities.length === 0" description="暂无活动数据" />

    <!-- 申请/编辑活动 -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑活动' : '申请活动'" width="550px" class="custom-dialog">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="所属社团" prop="clubId">
          <el-select v-model="form.clubId" placeholder="选择社团" style="width:100%">
            <el-option v-for="c in clubs" :key="c.clubId" :label="c.name" :value="c.clubId" />
          </el-select>
        </el-form-item>
        <el-form-item label="活动名称" prop="title">
          <el-input v-model="form.title" placeholder="请输入活动名称" />
        </el-form-item>
        <el-form-item label="活动详情" prop="details">
          <el-input v-model="form.details" type="textarea" :rows="3" placeholder="活动内容描述..." />
        </el-form-item>
        <el-form-item label="活动时间" prop="eventTime">
          <el-date-picker v-model="form.eventTime" type="datetime" value-format="YYYY-MM-DDTHH:mm:ss" style="width:100%" />
        </el-form-item>
        <el-form-item label="地点" prop="location">
          <el-input v-model="form.location" placeholder="请输入活动地点" />
        </el-form-item>
        <el-form-item label="人数上限" prop="maxParticipants">
          <el-input-number v-model="form.maxParticipants" :min="1" :max="500" style="width:100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <!-- 活动详情 -->
    <el-dialog v-model="detailVisible" title="活动详情" width="550px" class="custom-dialog">
      <el-descriptions :column="1" border>
        <el-descriptions-item label="名称">{{ detail.title }}</el-descriptions-item>
        <el-descriptions-item label="所属社团">{{ detail.clubName || '未知社团' }}</el-descriptions-item>
        <el-descriptions-item label="地点">{{ detail.location }}</el-descriptions-item>
        <el-descriptions-item label="时间">{{ formatTime(detail.eventTime) }}</el-descriptions-item>
        <el-descriptions-item label="人数上限">{{ detail.maxParticipants }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="statusType(detail.status)">{{ statusText(detail.status) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="详情">{{ detail.details }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>

    <!-- 报名签到 -->
    <el-dialog v-model="regVisible" :title="`${currentActivity.title} - 报名签到`" width="650px" class="custom-dialog">
      <div class="member-toolbar">
        <span class="reg-count">共 {{ registrations.length }} 人报名</span>
        <el-button type="primary" size="small" @click="handleBatchSignin">批量签到（全部）</el-button>
      </div>
      <el-table :data="registrations" stripe v-loading="regLoading">
        <el-table-column prop="studentId" label="学号" width="120" />
        <el-table-column prop="regTime" label="报名时间" />
        <el-table-column label="签到状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.signInStatus === 1 ? 'success' : 'info'" size="small" effect="light">
              {{ row.signInStatus === 1 ? '已签到' : '未签到' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100">
          <template #default="{ row }">
            <el-button v-if="row.signInStatus === 0" size="small" type="success" @click="handleSignin(row)">签到</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useUserStore } from '@/stores/user'
import { getActivityList, addActivity, updateActivity, approveActivity, rejectActivity, finishActivity, deleteActivity, getActivityDetail } from '@/api/activity'
import { signup, signin, batchSignin, getRegistrationList } from '@/api/registration'
import { getClubList } from '@/api/club'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Plus, Location, Clock, User, CollectionTag } from '@element-plus/icons-vue'

const userStore = useUserStore()
const activities = ref([])
const clubs = ref([])
const loading = ref(false)
const searchTitle = ref('')
const searchStatus = ref(null)
const dialogVisible = ref(false)
const detailVisible = ref(false)
const regVisible = ref(false)
const isEdit = ref(false)
const submitting = ref(false)
const formRef = ref()
const detail = ref({})
const currentActivity = ref({})
const registrations = ref([])
const regLoading = ref(false)

const form = reactive({ activityId: null, clubId: null, title: '', details: '', eventTime: '', location: '', maxParticipants: 50 })
const rules = {
  clubId: [{ required: true, message: '请选择社团', trigger: 'change' }],
  title: [{ required: true, message: '请输入活动名称', trigger: 'blur' }],
  eventTime: [{ required: true, message: '请选择时间', trigger: 'change' }],
  location: [{ required: true, message: '请输入地点', trigger: 'blur' }]
}

function statusType(s) {
  return { 0: 'warning', 1: 'success', 2: 'info', 3: 'danger' }[s] || 'info'
}
function statusText(s) {
  return { 0: '待审批', 1: '已发布', 2: '已结束', 3: '已驳回' }[s] || '未知'
}
function formatTime(t) {
  if (!t) return ''
  return t.replace('T', ' ').substring(0, 16)
}
function getDay(t) {
  if (!t) return '--'
  return t.substring(8, 10)
}
function getMonth(t) {
  if (!t) return ''
  const m = parseInt(t.substring(5, 7))
  return m + '月'
}

async function loadActivities() {
  loading.value = true
  try {
    const params = {}
    if (searchTitle.value) params.title = searchTitle.value
    if (searchStatus.value !== null && searchStatus.value !== '') params.status = searchStatus.value
    const res = await getActivityList(params)
    activities.value = res.data || []
  } finally {
    loading.value = false
  }
}

async function loadClubs() {
  const res = await getClubList()
  clubs.value = res.data || []
}

function showAddDialog() {
  isEdit.value = false
  Object.assign(form, { activityId: null, clubId: null, title: '', details: '', eventTime: '', location: '', maxParticipants: 50 })
  dialogVisible.value = true
}

async function handleSubmit() {
  await formRef.value.validate()
  submitting.value = true
  try {
    if (isEdit.value) {
      await updateActivity(form)
      ElMessage.success('更新成功')
    } else {
      await addActivity(form)
      ElMessage.success('活动申请已提交')
    }
    dialogVisible.value = false
    loadActivities()
  } finally {
    submitting.value = false
  }
}

async function handleApprove(row) {
  await approveActivity(row.activityId)
  ElMessage.success('已审批通过')
  loadActivities()
}

async function handleReject(row) {
  await ElMessageBox.confirm('确定驳回该活动？', '提示', { type: 'warning' })
  await rejectActivity(row.activityId)
  ElMessage.success('已驳回')
  loadActivities()
}

async function handleFinish(row) {
  await ElMessageBox.confirm('确定结束该活动？', '提示')
  await finishActivity(row.activityId)
  ElMessage.success('活动已结束')
  loadActivities()
}

async function handleDelete(row) {
  await ElMessageBox.confirm('确定删除该活动？此操作不可恢复', '警告', { type: 'warning' })
  await deleteActivity(row.activityId)
  ElMessage.success('活动已删除')
  loadActivities()
}

async function handleSignup(row) {
  await signup(row.activityId)
  ElMessage.success('报名成功')
}

async function viewDetail(row) {
  const res = await getActivityDetail(row.activityId)
  detail.value = res.data
  detailVisible.value = true
}

async function viewRegistrations(row) {
  currentActivity.value = row
  regVisible.value = true
  regLoading.value = true
  try {
    const res = await getRegistrationList(row.activityId)
    registrations.value = res.data || []
  } finally {
    regLoading.value = false
  }
}

async function handleSignin(row) {
  await signin(row.id)
  ElMessage.success('签到成功')
  viewRegistrations(currentActivity.value)
}

async function handleBatchSignin() {
  const ids = registrations.value.filter(r => r.signInStatus === 0).map(r => r.studentId)
  if (ids.length === 0) {
    ElMessage.info('没有需要签到的人')
    return
  }
  await batchSignin(currentActivity.value.activityId, ids)
  ElMessage.success('批量签到完成')
  viewRegistrations(currentActivity.value)
}

onMounted(() => {
  loadActivities()
  loadClubs()
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
  flex-wrap: wrap;
  gap: 16px;
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

.page-actions {
  display: flex;
  gap: 12px;
  align-items: center;
  flex-wrap: wrap;
}

.search-input {
  width: 180px;
}

.btn-create {
  border-radius: 10px;
  padding: 10px 20px;
}

.activity-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.activity-card {
  display: flex;
  gap: 20px;
  padding: 24px;
  border-radius: 16px;
  background: var(--color-surface);
  border: 1px solid var(--color-border);
  transition: transform 0.2s, box-shadow 0.2s;
}

.activity-card:hover {
  transform: translateX(4px);
  box-shadow: 0 8px 24px rgba(64, 158, 255, 0.08);
}

.activity-date-badge {
  width: 60px;
  height: 68px;
  border-radius: 12px;
  background: linear-gradient(135deg, var(--color-primary), var(--color-accent));
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #fff;
  flex-shrink: 0;
}

.date-day {
  font-size: 24px;
  font-weight: 700;
  line-height: 1.2;
}

.date-month {
  font-size: 12px;
  opacity: 0.85;
}

.activity-card__body {
  flex: 1;
  min-width: 0;
}

.activity-card__top {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 10px;
}

.activity-card__top h3 {
  margin: 0;
  font-size: 16px;
  color: var(--color-text);
  cursor: pointer;
  transition: color 0.2s;
}

.activity-card__top h3:hover {
  color: var(--color-primary);
}

.status-tag {
  border-radius: 6px;
}

.activity-card__info {
  display: flex;
  gap: 20px;
  margin-bottom: 12px;
  font-size: 13px;
  color: var(--color-text-light);
}

.activity-card__info span {
  display: flex;
  align-items: center;
  gap: 4px;
}

.activity-card__actions {
  display: flex;
  gap: 4px;
}

.member-toolbar {
  margin-bottom: 16px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.reg-count {
  font-size: 14px;
  color: var(--color-text-light);
}
</style>
