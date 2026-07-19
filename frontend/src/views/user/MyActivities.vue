<template>
  <div class="page-container">
    <div class="page-header">
      <div>
        <h2 class="page-title">我的活动</h2>
        <p class="page-desc">查看我报名参加的活动，或为社团申请新活动</p>
      </div>
      <el-button v-if="myClubs.length > 0" type="primary" class="btn-apply" @click="showApplyDialog">
        <el-icon><Plus /></el-icon>申请活动
      </el-button>
    </div>

    <!-- 我申请的活动 -->
    <div v-if="myAppliedActivities.length > 0" class="section">
      <h3 class="section-title">我申请的活动</h3>
      <div class="activity-list">
        <div v-for="act in myAppliedActivities" :key="act.activityId" class="activity-card">
          <div class="activity-card__left">
            <div class="activity-date-badge">
              <span class="date-day">{{ getDay(act.eventTime) }}</span>
              <span class="date-month">{{ getMonth(act.eventTime) }}</span>
            </div>
          </div>
          <div class="activity-card__body">
            <div class="activity-card__top">
              <h3>{{ act.title }}</h3>
              <el-tag :type="statusType(act.status)" size="small" effect="light">
                {{ statusText(act.status) }}
              </el-tag>
            </div>
            <div class="activity-card__info">
              <span>📍 {{ act.location || '待定' }}</span>
              <span>📅 {{ formatTime(act.eventTime) }}</span>
              <span>👥 上限 {{ act.maxParticipants }} 人</span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 我报名的活动 -->
    <div class="section">
      <h3 class="section-title">我报名的活动</h3>
      <div class="activity-list">
        <div v-for="item in myActivities" :key="item.registration.id" class="activity-card">
          <div class="activity-card__left">
            <div class="activity-date-badge">
              <span class="date-day">{{ getDay(item.activity.eventTime) }}</span>
              <span class="date-month">{{ getMonth(item.activity.eventTime) }}</span>
            </div>
          </div>
          <div class="activity-card__body">
            <div class="activity-card__top">
              <h3>{{ item.activity.title }}</h3>
              <el-tag :type="statusType(item.activity.status)" size="small" effect="light">
                {{ statusText(item.activity.status) }}
              </el-tag>
            </div>
            <div class="activity-card__info">
              <span>📍 {{ item.activity.location || '待定' }}</span>
              <span>📅 {{ formatTime(item.activity.eventTime) }}</span>
            </div>
            <div class="activity-card__status">
              <el-tag :type="item.registration.signInStatus === 1 ? 'success' : (item.activity.status === 2 ? 'danger' : 'warning')" size="small" effect="light">
                {{ item.registration.signInStatus === 1 ? '已签到' : (item.activity.status === 2 ? '未签到' : '待签到') }}
              </el-tag>
              <span class="reg-time">报名时间：{{ formatTime(item.registration.regTime) }}</span>
              <el-button
                v-if="item.activity.status === 1 && item.registration.signInStatus === 0"
                size="small" type="danger" text
                @click="handleCancelReg(item.registration)"
              >取消报名</el-button>
            </div>
          </div>
        </div>
      </div>
      <el-empty v-if="!loading && myActivities.length === 0" description="你还没有报名任何活动" />
    </div>

    <!-- 申请活动弹窗 -->
    <el-dialog v-model="dialogVisible" title="申请活动" width="550px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="所属社团" prop="clubId">
          <el-select v-model="form.clubId" placeholder="选择你所在的社团" style="width:100%">
            <el-option v-for="c in myClubs" :key="c.clubId" :label="c.name" :value="c.clubId" />
          </el-select>
        </el-form-item>
        <el-form-item label="活动名称" prop="title">
          <el-input v-model="form.title" placeholder="请输入活动名称" />
        </el-form-item>
        <el-form-item label="活动详情" prop="details">
          <el-input v-model="form.details" type="textarea" :rows="3" placeholder="活动内容描述..." />
        </el-form-item>
        <el-form-item label="活动时间" prop="eventTime">
          <el-date-picker v-model="form.eventTime" type="datetime" value-format="YYYY-MM-DDTHH:mm:ss" placeholder="选择时间" style="width:100%" />
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
        <el-button type="primary" :loading="submitting" @click="handleSubmit">提交申请</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useUserStore } from '@/stores/user'
import { getMyRegistrations, cancelRegistration } from '@/api/registration'
import { getActivityList, getActivityDetail, addActivity } from '@/api/activity'
import { getClubList } from '@/api/club'
import { getMemberList } from '@/api/clubMember'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'

const userStore = useUserStore()
const myActivities = ref([])
const myAppliedActivities = ref([])
const myClubs = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const submitting = ref(false)
const formRef = ref()

const form = reactive({ clubId: null, title: '', details: '', eventTime: '', location: '', maxParticipants: 50 })
const rules = {
  clubId: [{ required: true, message: '请选择社团', trigger: 'change' }],
  title: [{ required: true, message: '请输入活动名称', trigger: 'blur' }],
  details: [{ required: true, message: '请输入活动详情', trigger: 'blur' }],
  eventTime: [{ required: true, message: '请选择时间', trigger: 'change' }],
  location: [{ required: true, message: '请输入地点', trigger: 'blur' }]
}

function statusType(s) {
  return { 0: 'warning', 1: 'success', 2: 'info', 3: 'danger' }[s] || 'info'
}
function statusText(s) {
  return { 0: '待审批', 1: '进行中', 2: '已结束', 3: '已驳回' }[s] || '未知'
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

async function loadMyClubs() {
  // 只获取我是社长/副社长/队长的社团（才能申请活动）
  try {
    const clubRes = await getClubList()
    const allClubs = clubRes.data || []
    const studentId = userStore.userInfo?.studentId
    const leaderClubs = []
    for (const club of allClubs) {
      try {
        const memberRes = await getMemberList(club.clubId, { status: 1 })
        const members = memberRes.data || []
        const me = members.find(m => m.studentId === studentId)
        if (me && (me.position === '社长' || me.position === '副社长' || me.position === '队长')) {
          leaderClubs.push(club)
        }
      } catch (e) { /* ignore */ }
    }
    myClubs.value = leaderClubs
  } catch (e) { /* ignore */ }
}

async function loadMyAppliedActivities() {
  // 获取我申请的活动（applicantId === 我的学号）
  try {
    const res = await getActivityList()
    const all = res.data || []
    const studentId = userStore.userInfo?.studentId
    myAppliedActivities.value = all.filter(a => a.applicantId === studentId)
  } catch (e) { /* ignore */ }
}

async function loadMyRegistrations() {
  try {
    const res = await getMyRegistrations()
    const registrations = res.data || []
    const results = []
    for (const reg of registrations) {
      try {
        const actRes = await getActivityDetail(reg.activityId)
        results.push({ registration: reg, activity: actRes.data || {} })
      } catch (e) {
        results.push({ registration: reg, activity: { title: `活动 #${reg.activityId}`, eventTime: '', location: '', status: -1 } })
      }
    }
    myActivities.value = results
  } catch (e) { /* ignore */ }
}

function showApplyDialog() {
  Object.assign(form, { clubId: null, title: '', details: '', eventTime: '', location: '', maxParticipants: 50 })
  dialogVisible.value = true
}

async function handleSubmit() {
  await formRef.value.validate()
  submitting.value = true
  try {
    await addActivity(form)
    ElMessage.success('活动申请已提交，等待管理员审批')
    dialogVisible.value = false
    loadMyAppliedActivities()
  } catch (e) {
    ElMessage.error(e.response?.data?.message || e.message || '申请失败')
  } finally {
    submitting.value = false
  }
}

async function handleCancelReg(reg) {
  await ElMessageBox.confirm('确定取消报名该活动？', '取消报名', { type: 'warning' })
  try {
    await cancelRegistration(reg.id)
    ElMessage.success('已取消报名')
    loadMyRegistrations()
  } catch (e) {
    ElMessage.error(e.response?.data?.message || '取消失败')
  }
}

onMounted(async () => {
  loading.value = true
  try {
    await Promise.all([loadMyClubs(), loadMyAppliedActivities(), loadMyRegistrations()])
  } finally {
    loading.value = false
  }
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

.btn-apply {
  border-radius: 10px;
  padding: 10px 20px;
}

.section {
  margin-bottom: 32px;
}

.section-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--color-text);
  margin: 0 0 16px;
  padding-left: 10px;
  border-left: 3px solid var(--color-primary);
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
}

.activity-card__info {
  display: flex;
  gap: 20px;
  margin-bottom: 10px;
  font-size: 13px;
  color: var(--color-text-light);
}

.activity-card__status {
  display: flex;
  align-items: center;
  gap: 12px;
}

.reg-time {
  font-size: 12px;
  color: var(--color-text-light);
}
</style>
