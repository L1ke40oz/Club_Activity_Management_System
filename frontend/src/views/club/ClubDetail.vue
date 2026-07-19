<template>
  <div>
    <el-page-header @back="goBack" content="社团详情" style="margin-bottom:20px" />
    <el-row :gutter="20">
      <el-col :span="10">
        <el-card>
          <template #header>社团信息</template>
          <el-descriptions :column="1" border>
            <el-descriptions-item label="名称">{{ club.name }}</el-descriptions-item>
            <el-descriptions-item label="分类">{{ club.category }}</el-descriptions-item>
            <el-descriptions-item label="成立日期">{{ club.establishedDate }}</el-descriptions-item>
            <el-descriptions-item label="指导老师">{{ club.advisor }}</el-descriptions-item>
            <el-descriptions-item label="创建者">{{ club.creatorId }}</el-descriptions-item>
            <el-descriptions-item label="简介">{{ club.description }}</el-descriptions-item>
          </el-descriptions>
          <div style="margin-top:16px">
            <el-button v-if="userStore.isAdmin || isLeader" type="primary" @click="showEditDialog">编辑</el-button>
            <el-button v-if="!isMember" @click="handleApply">申请加入</el-button>
            <el-button v-if="isMember && !isLeader" type="warning" @click="handleQuit">退出社团</el-button>
            <el-button v-if="userStore.isAdmin" type="danger" @click="handleDissolve">解散社团</el-button>
          </div>
        </el-card>
      </el-col>
      <el-col :span="14">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>成员列表</span>
              <el-select v-model="memberStatus" style="width:120px" clearable placeholder="全部" @change="loadMembers">
                <el-option label="待审批" :value="0" />
                <el-option label="已通过" :value="1" />
                <el-option label="已退出" :value="2" />
              </el-select>
            </div>
          </template>
          <el-table :data="members" stripe v-loading="memberLoading" size="small">
            <el-table-column prop="studentId" label="学号" width="120" />
            <el-table-column prop="name" label="姓名" width="100" />
            <el-table-column prop="className" label="班级" width="120" />
            <el-table-column prop="position" label="职务" width="100" />
            <el-table-column label="状态" width="80">
              <template #default="{ row }">
                <el-tag :type="row.status === 1 ? 'success' : row.status === 0 ? 'warning' : 'info'" size="small">
                  {{ { 0: '待审批', 1: '已通过', 2: '已退出' }[row.status] }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column v-if="userStore.isAdmin || isLeader" label="操作" width="240">
              <template #default="{ row }">
                <el-button v-if="row.status === 0" size="small" type="success" @click="handleApproveMember(row)">通过</el-button>
                <el-button v-if="row.status === 0" size="small" type="danger" @click="handleRejectMember(row)">拒绝</el-button>
                <el-button v-if="row.status === 1" size="small" @click="showPositionDialog(row)">设置职务</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>

    <el-card style="margin-top:20px">
      <template #header>
        <div class="card-header">
          <span>社团活动</span>
          <el-tag type="info" size="small">共 {{ activities.length }} 个</el-tag>
        </div>
      </template>
      <el-table :data="activities" stripe v-loading="activityLoading" size="small">
        <el-table-column prop="title" label="活动名称" min-width="160" />
        <el-table-column prop="location" label="地点" width="140" />
        <el-table-column label="活动时间" width="160">
          <template #default="{ row }">{{ formatTime(row.eventTime) }}</template>
        </el-table-column>
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="activityStatusType(row.status)" size="small">
              {{ { 0: '待审批', 1: '已发布', 2: '已结束', 3: '已驳回' }[row.status] }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="80">
          <template #default="{ row }">
            <el-button size="small" text type="primary" @click="goActivity(row)">详情</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-empty v-if="!activityLoading && activities.length === 0" description="该社团暂无活动" :image-size="80" />
    </el-card>

    <el-dialog v-model="editVisible" title="编辑社团" width="500px">
      <el-form ref="editFormRef" :model="editForm" label-width="80px">
        <el-form-item label="名称">
          <el-input v-model="editForm.name" />
        </el-form-item>
        <el-form-item label="分类">
          <el-input v-model="editForm.category" />
        </el-form-item>
        <el-form-item label="指导老师">
          <el-input v-model="editForm.advisor" />
        </el-form-item>
        <el-form-item label="简介">
          <el-input v-model="editForm.description" type="textarea" :rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editVisible = false">取消</el-button>
        <el-button type="primary" @click="handleEditSubmit">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="positionVisible" title="设置职务" width="350px">
      <el-input v-model="positionValue" placeholder="如：社长、副社长、干事" />
      <template #footer>
        <el-button @click="positionVisible = false">取消</el-button>
        <el-button type="primary" @click="handlePositionSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { getClubDetail, updateClub, deleteClub } from '@/api/club'
import { getMemberList, approveMember, rejectMember, applyJoin, updatePosition, quitClub } from '@/api/clubMember'
import { getActivityList } from '@/api/activity'
import { ElMessage, ElMessageBox } from 'element-plus'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const clubId = Number(route.params.id)

const club = ref({})
const members = ref([])
const memberLoading = ref(false)
const activities = ref([])
const activityLoading = ref(false)
const memberStatus = ref(null)
const editVisible = ref(false)
const positionVisible = ref(false)
const positionValue = ref('')
const currentMember = ref(null)
const editForm = reactive({ clubId: null, name: '', category: '', advisor: '', description: '' })

const isLeader = computed(() => {
  const studentId = userStore.userInfo?.studentId
  if (!studentId) return false
  const me = members.value.find(m => m.studentId === studentId && m.status === 1)
  return me && (me.position === '社长' || me.position === '副社长' || me.position === '队长')
})

const isMember = computed(() => {
  const studentId = userStore.userInfo?.studentId
  if (!studentId) return false
  return members.value.some(m => m.studentId === studentId && m.status === 1)
})

function goBack() {
  if (userStore.isAdmin) {
    router.push('/admin/clubs')
  } else {
    router.push('/admin/my-clubs')
  }
}

async function loadClub() {
  const res = await getClubDetail(clubId)
  club.value = res.data
}

async function loadMembers() {
  memberLoading.value = true
  try {
    const params = {}
    if (memberStatus.value !== null && memberStatus.value !== '') params.status = memberStatus.value
    const res = await getMemberList(clubId, params)
    members.value = res.data || []
  } finally {
    memberLoading.value = false
  }
}

async function loadActivities() {
  activityLoading.value = true
  try {
    const res = await getActivityList({ clubId })
    activities.value = res.data || []
  } finally {
    activityLoading.value = false
  }
}

function formatTime(t) {
  if (!t) return ''
  return t.replace('T', ' ').substring(0, 16)
}

function activityStatusType(s) {
  return { 0: 'warning', 1: 'success', 2: 'info', 3: 'danger' }[s] || 'info'
}

function goActivity(row) {
  router.push(`/admin/activities/${row.activityId}`)
}

function showEditDialog() {
  Object.assign(editForm, { clubId, name: club.value.name, category: club.value.category, advisor: club.value.advisor, description: club.value.description })
  editVisible.value = true
}

async function handleEditSubmit() {
  await updateClub(editForm)
  ElMessage.success('更新成功')
  editVisible.value = false
  loadClub()
}

async function handleApply() {
  await applyJoin({ clubId })
  ElMessage.success('申请已提交')
  loadMembers()
}

async function handleQuit() {
  await ElMessageBox.confirm('确定退出该社团？', '提示', { type: 'warning' })
  try {
    const res = await quitClub(clubId)
    ElMessage.success('退出成功')
    loadMembers()
  } catch (e) {
    ElMessage.error('退出失败')
  }
}

async function handleDissolve() {
  await ElMessageBox.confirm('确定解散该社团？此操作不可恢复', '警告', { type: 'warning' })
  await deleteClub(clubId)
  ElMessage.success('社团已解散')
  router.push('/admin/clubs')
}

async function handleApproveMember(row) {
  await approveMember(row.id)
  ElMessage.success('已通过')
  loadMembers()
}

async function handleRejectMember(row) {
  await rejectMember(row.id)
  ElMessage.success('已拒绝')
  loadMembers()
}

function showPositionDialog(row) {
  currentMember.value = row
  positionValue.value = row.position || ''
  positionVisible.value = true
}

async function handlePositionSubmit() {
  await updatePosition({ id: currentMember.value.id, position: positionValue.value })
  ElMessage.success('职务已更新')
  positionVisible.value = false
  loadMembers()
}

onMounted(() => {
  loadClub()
  loadMembers()
  loadActivities()
})
</script>

<style scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
