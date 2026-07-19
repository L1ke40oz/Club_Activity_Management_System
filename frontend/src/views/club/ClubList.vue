<template>
  <div class="page-container">
    <div class="page-header">
      <div>
        <h2 class="page-title">社团管理</h2>
        <p class="page-desc">浏览和管理校园社团组织</p>
      </div>
      <div class="page-actions">
        <el-input v-model="searchName" placeholder="搜索社团名称..." clearable class="search-input" @clear="loadClubs" @keyup.enter="loadClubs">
          <template #prefix><el-icon><Search /></el-icon></template>
        </el-input>
        <el-button type="primary" class="btn-create" @click="showAddDialog">
          <el-icon><Plus /></el-icon>创建社团
        </el-button>
      </div>
    </div>

    <!-- 待审批社团 -->
    <div v-if="pendingClubs.length > 0" class="pending-section">
      <h3 class="section-label">待审批社团申请 ({{ pendingClubs.length }})</h3>
      <div class="pending-list">
        <div v-for="club in pendingClubs" :key="club.clubId" class="pending-card">
          <div class="pending-card__info">
            <strong>{{ club.name }}</strong>
            <span>{{ club.category }} · 指导老师: {{ club.advisor || '未设置' }} · 申请人: {{ club.creatorId }}</span>
            <p>{{ club.description || '暂无简介' }}</p>
          </div>
          <div class="pending-card__actions">
            <el-button type="success" size="small" @click="handleApproveClub(club)">通过</el-button>
            <el-button type="danger" size="small" plain @click="handleRejectClub(club)">驳回</el-button>
          </div>
        </div>
      </div>
    </div>

    <!-- 待审批成员加入 -->
    <div v-if="pendingMembers.length > 0" class="pending-section">
      <h3 class="section-label">待审批成员申请 ({{ pendingMembers.length }})</h3>
      <div class="pending-list">
        <div v-for="pm in pendingMembers" :key="pm.id" class="pending-card">
          <div class="pending-card__info">
            <strong>{{ pm.studentId }} 申请加入「{{ pm.clubName || '社团#' + pm.clubId }}」</strong>
            <span>申请时间：{{ pm.joinTime?.replace('T', ' ').substring(0, 16) }}</span>
          </div>
          <div class="pending-card__actions">
            <el-button type="success" size="small" @click="handleApprovePendingMember(pm)">通过</el-button>
            <el-button type="danger" size="small" plain @click="handleRejectPendingMember(pm)">拒绝</el-button>
          </div>
        </div>
      </div>
    </div>

    <div class="club-grid">
      <div v-for="club in clubs" :key="club.clubId" class="club-card" @click="viewDetail(club)">
        <div class="club-card__header">
          <div class="club-avatar">{{ club.name?.charAt(0) }}</div>
          <div class="club-card__meta">
            <h3>{{ club.name }}</h3>
            <el-tag size="small" effect="plain" class="club-tag">{{ club.category || '未分类' }}</el-tag>
          </div>
        </div>
        <p class="club-card__desc">{{ club.description || '暂无简介' }}</p>
        <div class="club-card__footer">
          <span class="club-info-item">
            <el-icon><User /></el-icon>{{ club.advisor || '未指定' }}
          </span>
          <span class="club-info-item">
            <el-icon><Calendar /></el-icon>{{ club.establishedDate || '未知' }}
          </span>
        </div>
        <div class="club-card__actions" @click.stop>
          <el-button size="small" text type="primary" @click="viewMembers(club)">成员</el-button>
          <el-button size="small" text type="primary" @click="showEditDialog(club)">编辑</el-button>
          <el-button v-if="userStore.isAdmin" size="small" text type="danger" @click="handleDelete(club)">解散</el-button>
        </div>
      </div>
    </div>

    <el-empty v-if="!loading && clubs.length === 0" description="暂无社团数据" />

    <!-- 创建/编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑社团' : '创建社团'" width="500px" class="custom-dialog">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入社团名称" />
        </el-form-item>
        <el-form-item label="类别" prop="category">
          <el-select v-model="form.category" placeholder="选择类别" style="width:100%">
            <el-option label="学术科技" value="学术科技" />
            <el-option label="文艺" value="文艺" />
            <el-option label="体育" value="体育" />
            <el-option label="公益" value="公益" />
            <el-option label="其他" value="其他" />
          </el-select>
        </el-form-item>
        <el-form-item label="成立日期" prop="establishedDate">
          <el-date-picker v-model="form.establishedDate" type="date" value-format="YYYY-MM-DD" style="width:100%" />
        </el-form-item>
        <el-form-item label="指导老师" prop="advisor">
          <el-input v-model="form.advisor" placeholder="请输入指导老师姓名" />
        </el-form-item>
        <el-form-item label="简介">
          <el-input v-model="form.description" type="textarea" :rows="3" placeholder="社团简介..." />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <!-- 详情弹窗 -->
    <el-dialog v-model="detailVisible" title="社团详情" width="500px" class="custom-dialog">
      <el-descriptions :column="1" border>
        <el-descriptions-item label="名称">{{ detail.name }}</el-descriptions-item>
        <el-descriptions-item label="类别">{{ detail.category }}</el-descriptions-item>
        <el-descriptions-item label="指导老师">{{ detail.advisor }}</el-descriptions-item>
        <el-descriptions-item label="成立日期">{{ detail.establishedDate }}</el-descriptions-item>
        <el-descriptions-item label="创建者">{{ detail.creatorId }}</el-descriptions-item>
        <el-descriptions-item label="简介">{{ detail.description }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>

    <!-- 成员管理弹窗 -->
    <el-dialog v-model="memberVisible" :title="`${currentClub.name} - 成员管理`" width="700px" class="custom-dialog">
      <div class="member-toolbar">
        <el-radio-group v-model="memberStatus" @change="loadMembers">
          <el-radio-button :value="1">已通过</el-radio-button>
          <el-radio-button :value="0">待审批</el-radio-button>
        </el-radio-group>
        <el-button type="primary" size="small" @click="handleApplyJoin">申请加入</el-button>
      </div>
      <el-table :data="members" stripe v-loading="memberLoading" class="custom-table">
        <el-table-column prop="studentId" label="学号" width="120" />
        <el-table-column prop="name" label="姓名" width="100" />
        <el-table-column prop="className" label="班级" width="120" />
        <el-table-column prop="position" label="职务" width="100" />
        <el-table-column prop="joinTime" label="加入时间" />
        <el-table-column label="操作" width="200">
          <template #default="{ row }">
            <template v-if="memberStatus === 0">
              <el-button size="small" type="success" @click="handleApprove(row)">通过</el-button>
              <el-button size="small" type="danger" @click="handleReject(row)">拒绝</el-button>
            </template>
            <template v-else>
              <el-input v-model="row.newPosition" size="small" style="width:80px" placeholder="职务" />
              <el-button size="small" type="primary" @click="handleUpdatePosition(row)">更新</el-button>
            </template>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useUserStore } from '@/stores/user'
import { getClubList, addClub, updateClub, deleteClub, getClubDetail, getPendingClubs, approveClub, rejectClub } from '@/api/club'
import { getMemberList, applyJoin, approveMember, rejectMember, updatePosition, getAllPendingMembers } from '@/api/clubMember'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Plus, User, Calendar } from '@element-plus/icons-vue'

const userStore = useUserStore()
const clubs = ref([])
const pendingClubs = ref([])
const pendingMembers = ref([])
const loading = ref(false)
const searchName = ref('')
const dialogVisible = ref(false)
const detailVisible = ref(false)
const memberVisible = ref(false)
const isEdit = ref(false)
const submitting = ref(false)
const formRef = ref()
const detail = ref({})
const currentClub = ref({})
const members = ref([])
const memberLoading = ref(false)
const memberStatus = ref(1)

const form = reactive({ clubId: null, name: '', category: '', establishedDate: '', advisor: '', description: '' })
const rules = {
  name: [{ required: true, message: '请输入名称', trigger: 'blur' }],
  category: [{ required: true, message: '请选择类别', trigger: 'change' }]
}

async function loadClubs() {
  loading.value = true
  try {
    const res = await getClubList({ name: searchName.value || undefined })
    clubs.value = res.data || []
  } finally {
    loading.value = false
  }
}

function showAddDialog() {
  isEdit.value = false
  Object.assign(form, { clubId: null, name: '', category: '', establishedDate: '', advisor: '', description: '' })
  dialogVisible.value = true
}

function showEditDialog(row) {
  isEdit.value = true
  Object.assign(form, row)
  dialogVisible.value = true
}

async function handleSubmit() {
  await formRef.value.validate()
  submitting.value = true
  try {
    if (isEdit.value) {
      await updateClub(form)
      ElMessage.success('更新成功')
    } else {
      await addClub(form)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    loadClubs()
  } finally {
    submitting.value = false
  }
}

async function handleDelete(row) {
  await ElMessageBox.confirm(`确定要解散社团"${row.name}"吗？`, '提示', { type: 'warning' })
  await deleteClub(row.clubId)
  ElMessage.success('已解散')
  loadClubs()
}

async function viewDetail(row) {
  const res = await getClubDetail(row.clubId)
  detail.value = res.data
  detailVisible.value = true
}

function viewMembers(row) {
  currentClub.value = row
  memberStatus.value = 1
  memberVisible.value = true
  loadMembers()
}

async function loadMembers() {
  memberLoading.value = true
  try {
    const res = await getMemberList(currentClub.value.clubId, { status: memberStatus.value })
    members.value = (res.data || []).map(m => ({ ...m, newPosition: m.position }))
  } finally {
    memberLoading.value = false
  }
}

async function handleApplyJoin() {
  await applyJoin({ clubId: currentClub.value.clubId })
  ElMessage.success('申请已提交')
  loadMembers()
}

async function handleApprove(row) {
  await approveMember(row.id)
  ElMessage.success('已通过')
  loadMembers()
}

async function handleReject(row) {
  await rejectMember(row.id)
  ElMessage.success('已拒绝')
  loadMembers()
}

async function handleUpdatePosition(row) {
  await updatePosition({ id: row.id, position: row.newPosition })
  ElMessage.success('职务已更新')
  loadMembers()
}

async function loadPendingClubs() {
  try {
    const res = await getPendingClubs()
    pendingClubs.value = res.data || []
  } catch (e) {
    pendingClubs.value = []
  }
}

async function loadPendingMembers() {
  try {
    const res = await getAllPendingMembers()
    const list = res.data || []
    // 补充社团名称
    const clubRes = await getClubList()
    const clubMap = {}
    for (const c of (clubRes.data || [])) {
      clubMap[c.clubId] = c.name
    }
    pendingMembers.value = list.map(m => ({ ...m, clubName: clubMap[m.clubId] || '' }))
  } catch (e) {
    pendingMembers.value = []
  }
}

async function handleApproveClub(club) {
  await approveClub(club.clubId)
  ElMessage.success(`社团「${club.name}」已通过审批`)
  loadPendingClubs()
  loadClubs()
}

async function handleRejectClub(club) {
  await ElMessageBox.confirm(`确定驳回社团「${club.name}」的申请？`, '提示', { type: 'warning' })
  await rejectClub(club.clubId)
  ElMessage.success('已驳回')
  loadPendingClubs()
}

async function handleApprovePendingMember(pm) {
  await approveMember(pm.id)
  ElMessage.success('成员审批通过')
  loadPendingMembers()
}

async function handleRejectPendingMember(pm) {
  await rejectMember(pm.id)
  ElMessage.success('已拒绝')
  loadPendingMembers()
}

loadClubs()
loadPendingClubs()
loadPendingMembers()
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

.page-actions {
  display: flex;
  gap: 12px;
  align-items: center;
}

.search-input {
  width: 220px;
}

.btn-create {
  border-radius: 10px;
  padding: 10px 20px;
}

.club-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 20px;
}

.club-card {
  padding: 24px;
  border-radius: 16px;
  background: var(--color-surface);
  border: 1px solid var(--color-border);
  cursor: pointer;
  transition: transform 0.2s, box-shadow 0.2s;
}

.club-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 12px 32px rgba(64, 158, 255, 0.1);
}

.club-card__header {
  display: flex;
  align-items: center;
  gap: 14px;
  margin-bottom: 14px;
}

.club-avatar {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  background: linear-gradient(135deg, var(--color-primary), var(--color-accent));
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  font-weight: 700;
  flex-shrink: 0;
}

.club-card__meta h3 {
  margin: 0 0 4px;
  font-size: 16px;
  color: var(--color-text);
}

.club-tag {
  border-radius: 6px;
}

.club-card__desc {
  margin: 0 0 14px;
  font-size: 13px;
  color: var(--color-text-light);
  line-height: 1.6;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.club-card__footer {
  display: flex;
  gap: 16px;
  margin-bottom: 12px;
  font-size: 13px;
  color: var(--color-text-light);
}

.club-info-item {
  display: flex;
  align-items: center;
  gap: 4px;
}

.club-card__actions {
  padding-top: 12px;
  border-top: 1px solid var(--color-border);
  display: flex;
  gap: 4px;
}

.member-toolbar {
  margin-bottom: 16px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.pending-section {
  margin-bottom: 28px;
}

.section-label {
  font-size: 15px;
  font-weight: 600;
  color: var(--color-warning);
  margin: 0 0 14px;
}

.pending-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.pending-card {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 18px 20px;
  border-radius: 12px;
  background: rgba(221, 107, 32, 0.03);
  border: 1px solid rgba(221, 107, 32, 0.15);
}

.pending-card__info {
  flex: 1;
}

.pending-card__info strong {
  display: block;
  font-size: 15px;
  color: var(--color-text);
  margin-bottom: 4px;
}

.pending-card__info span {
  font-size: 13px;
  color: var(--color-text-light);
}

.pending-card__info p {
  margin: 6px 0 0;
  font-size: 13px;
  color: var(--color-text-light);
}

.pending-card__actions {
  display: flex;
  gap: 8px;
  flex-shrink: 0;
  margin-left: 16px;
}
</style>
