<template>
  <div class="activity-detail" v-loading="loading">
    <el-page-header @back="$router.back()" content="活动详情" style="margin-bottom: 20px" />

    <el-card v-if="activity">
      <template #header>
        <div class="card-header">
          <span>{{ activity.title }}</span>
          <el-tag :type="statusType(activity.status)">{{ statusText(activity.status) }}</el-tag>
        </div>
      </template>
      <el-descriptions :column="2" border>
        <el-descriptions-item label="活动ID">{{ activity.activityId }}</el-descriptions-item>
        <el-descriptions-item label="所属社团">{{ activity.clubName || activity.clubId }}</el-descriptions-item>
        <el-descriptions-item label="申请人">{{ activity.applicantId }}</el-descriptions-item>
        <el-descriptions-item label="地点">{{ activity.location }}</el-descriptions-item>
        <el-descriptions-item label="活动时间">{{ activity.eventTime }}</el-descriptions-item>
        <el-descriptions-item label="人数上限">{{ activity.maxParticipants }}</el-descriptions-item>
        <el-descriptions-item label="活动详情" :span="2">{{ activity.details }}</el-descriptions-item>
      </el-descriptions>

      <div class="actions" style="margin-top: 20px">
        <el-button v-if="activity.status === 1" type="success" @click="handleSignup">我要报名</el-button>
        <el-button v-if="userStore.isAdmin && activity.status === 0" type="primary" @click="handleApprove">审批通过</el-button>
        <el-button v-if="userStore.isAdmin && activity.status === 0" type="danger" @click="handleReject">驳回</el-button>
        <el-button v-if="activity.status === 1" type="warning" @click="handleFinish">结束活动</el-button>
      </div>
    </el-card>

    <el-card style="margin-top: 20px" v-if="activity && activity.status !== 0">
      <template #header>
        <div class="card-header">
          <span>报名列表</span>
          <el-button v-if="activity.status === 1" size="small" type="primary" @click="showBatchSignin = true">批量签到</el-button>
        </div>
      </template>
      <el-table :data="registrations" stripe>
        <el-table-column prop="studentId" label="学号" />
        <el-table-column prop="regTime" label="报名时间" />
        <el-table-column label="签到状态">
          <template #default="{ row }">
            <el-tag :type="row.signInStatus === 1 ? 'success' : 'info'">{{ row.signInStatus === 1 ? '已签到' : '未签到' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120">
          <template #default="{ row }">
            <el-button v-if="row.signInStatus === 0" size="small" type="success" @click="handleSignin(row)">签到</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="showBatchSignin" title="批量签到" width="400px">
      <el-input v-model="batchStudentIds" type="textarea" :rows="4" placeholder="请输入学号，每行一个" />
      <template #footer>
        <el-button @click="showBatchSignin = false">取消</el-button>
        <el-button type="primary" @click="handleBatchSignin">确认签到</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '../../stores/user'
import { getActivityDetail, approveActivity, rejectActivity, finishActivity } from '../../api/activity'
import { getRegistrationList, signup, signin, batchSignin } from '../../api/registration'

const route = useRoute()
const userStore = useUserStore()
const loading = ref(false)
const activity = ref(null)
const registrations = ref([])
const showBatchSignin = ref(false)
const batchStudentIds = ref('')

const statusText = (s) => ['待审批', '已发布', '已结束', '已驳回'][s]
const statusType = (s) => ['warning', 'success', 'info', 'danger'][s]

const fetchData = async () => {
  loading.value = true
  try {
    const res = await getActivityDetail(route.params.id)
    if (res.code === 200) activity.value = res.data
    if (activity.value && activity.value.status !== 0) {
      const regRes = await getRegistrationList(route.params.id)
      if (regRes.code === 200) registrations.value = regRes.data || []
    }
  } finally {
    loading.value = false
  }
}

const handleSignup = async () => {
  try {
    const res = await signup(activity.value.activityId)
    if (res.code === 200) {
      ElMessage.success('报名成功')
      fetchData()
    } else {
      ElMessage.error(res.message || '报名失败')
    }
  } catch (e) {
    ElMessage.error('报名失败')
  }
}

const handleApprove = async () => {
  await ElMessageBox.confirm('确认审批通过该活动？', '提示')
  const res = await approveActivity(activity.value.activityId)
  if (res.code === 200) {
    ElMessage.success('审批通过')
    fetchData()
  }
}

const handleReject = async () => {
  await ElMessageBox.confirm('确认驳回该活动？', '提示')
  const res = await rejectActivity(activity.value.activityId)
  if (res.code === 200) {
    ElMessage.success('已驳回')
    fetchData()
  }
}

const handleFinish = async () => {
  await ElMessageBox.confirm('确认结束该活动？', '提示')
  const res = await finishActivity(activity.value.activityId)
  if (res.code === 200) {
    ElMessage.success('活动已结束')
    fetchData()
  }
}

const handleSignin = async (row) => {
  const res = await signin(row.id)
  if (res.code === 200) {
    ElMessage.success('签到成功')
    fetchData()
  } else {
    ElMessage.error(res.message || '签到失败')
  }
}

const handleBatchSignin = async () => {
  const ids = batchStudentIds.value.split('\n').map(s => s.trim()).filter(Boolean)
  if (ids.length === 0) return ElMessage.warning('请输入学号')
  const res = await batchSignin(activity.value.activityId, ids)
  if (res.code === 200) {
    ElMessage.success('批量签到成功')
    showBatchSignin.value = false
    batchStudentIds.value = ''
    fetchData()
  } else {
    ElMessage.error(res.message || '签到失败')
  }
}

onMounted(fetchData)
</script>

<style scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
