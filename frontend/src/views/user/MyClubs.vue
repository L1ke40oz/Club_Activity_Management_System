<template>
  <div class="page-container">
    <div class="page-header">
      <div>
        <h2 class="page-title">我的社团</h2>
        <p class="page-desc">查看已加入的社团，或申请创建新社团</p>
      </div>
      <el-button type="primary" class="btn-apply" @click="showApplyDialog">
        <el-icon><Plus /></el-icon>申请创建社团
      </el-button>
    </div>

    <!-- 我申请创建的社团（待审批/已驳回） -->
    <div v-if="myAppliedClubs.length > 0" class="section">
      <h3 class="section-title">我申请创建的社团</h3>
      <div class="club-grid">
        <div v-for="club in myAppliedClubs" :key="club.clubId" class="club-card">
          <div class="club-card__header">
            <div class="club-avatar">{{ club.name?.charAt(0) }}</div>
            <div class="club-card__meta">
              <h3>{{ club.name }}</h3>
              <el-tag :type="club.status === 0 ? 'warning' : 'danger'" size="small" effect="light">
                {{ club.status === 0 ? '待审批' : '已驳回' }}
              </el-tag>
            </div>
          </div>
          <p class="club-card__desc">{{ club.description || '暂无简介' }}</p>
        </div>
      </div>
    </div>

    <!-- 已加入的社团 -->
    <div class="section">
      <h3 class="section-title">已加入的社团</h3>
      <div class="club-grid">
        <div v-for="club in myClubs" :key="club.clubId" class="club-card" @click="viewDetail(club)">
          <div class="club-card__header">
            <div class="club-avatar">{{ club.name?.charAt(0) }}</div>
            <div class="club-card__meta">
              <h3>{{ club.name }}</h3>
              <el-tag size="small" effect="plain">{{ club.category || '未分类' }}</el-tag>
            </div>
          </div>
          <p class="club-card__desc">{{ club.description || '暂无简介' }}</p>
          <div class="club-card__footer">
            <span class="club-info-item">
              <el-icon><User /></el-icon>{{ club.advisor || '未指定' }}
            </span>
            <div class="club-card__actions">
              <el-tag size="small" type="success" effect="light">{{ club.myPosition || '成员' }}</el-tag>
              <el-button size="small" type="danger" text @click.stop="handleQuit(club)">退出</el-button>
            </div>
          </div>
        </div>
      </div>
      <el-empty v-if="!loading && myClubs.length === 0" description="你还没有加入任何社团，去主页浏览社团吧！" />
    </div>

    <!-- 申请创建社团弹窗 -->
    <el-dialog v-model="dialogVisible" title="申请创建社团" width="500px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="社团名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入社团名称" />
        </el-form-item>
        <el-form-item label="类别" prop="category">
          <el-select v-model="form.category" placeholder="选择类别" style="width:100%">
            <el-option label="学术科技" value="学术科技" />
            <el-option label="文化艺术" value="文化艺术" />
            <el-option label="体育运动" value="体育运动" />
            <el-option label="公益实践" value="公益实践" />
            <el-option label="人文社科" value="人文社科" />
            <el-option label="生活休闲" value="生活休闲" />
            <el-option label="其他" value="其他" />
          </el-select>
        </el-form-item>
        <el-form-item label="成立日期" prop="establishedDate">
          <el-date-picker v-model="form.establishedDate" type="date" value-format="YYYY-MM-DD" placeholder="选择日期" style="width:100%" />
        </el-form-item>
        <el-form-item label="指导老师" prop="advisor">
          <el-input v-model="form.advisor" placeholder="请输入指导老师姓名" />
        </el-form-item>
        <el-form-item label="简介" prop="description">
          <el-input v-model="form.description" type="textarea" :rows="3" placeholder="社团简介..." />
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
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { getClubList, getMyAppliedClubs, applyClub } from '@/api/club'
import { getMemberList, quitClub } from '@/api/clubMember'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, User } from '@element-plus/icons-vue'

const router = useRouter()
const userStore = useUserStore()
const myClubs = ref([])
const myAppliedClubs = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const submitting = ref(false)
const formRef = ref()

const form = reactive({ name: '', category: '', establishedDate: '', advisor: '', description: '' })
const rules = {
  name: [{ required: true, message: '请输入社团名称', trigger: 'blur' }],
  category: [{ required: true, message: '请选择类别', trigger: 'change' }],
  advisor: [{ required: true, message: '请输入指导老师', trigger: 'blur' }]
}

onMounted(async () => {
  loading.value = true
  try {
    await Promise.all([loadMyClubs(), loadApplied()])
  } finally {
    loading.value = false
  }
})

async function loadMyClubs() {
  try {
    const clubRes = await getClubList()
    const allClubs = clubRes.data || []
    const studentId = userStore.userInfo?.studentId
    const joined = []
    for (const club of allClubs) {
      try {
        const memberRes = await getMemberList(club.clubId, { status: 1 })
        const members = memberRes.data || []
        const me = members.find(m => m.studentId === studentId)
        if (me) {
          joined.push({ ...club, myPosition: me.position })
        }
      } catch (e) { /* ignore */ }
    }
    myClubs.value = joined
  } catch (e) { /* ignore */ }
}

async function loadApplied() {
  try {
    const res = await getMyAppliedClubs()
    myAppliedClubs.value = res.data || []
  } catch (e) { /* ignore */ }
}

function viewDetail(club) {
  // 社长/副社长进入管理页面，普通成员进入浏览页面
  const isLeader = club.myPosition === '社长' || club.myPosition === '副社长' || club.myPosition === '队长'
  if (isLeader) {
    router.push(`/admin/clubs/${club.clubId}`)
  } else {
    router.push(`/admin/club-view/${club.clubId}`)
  }
}

async function handleQuit(club) {
  const isLeader = club.myPosition === '社长' || club.myPosition === '副社长' || club.myPosition === '队长'
  if (isLeader) {
    ElMessage.warning('社长/副社长请进入社团管理页面操作退出或转让')
    return
  }
  await ElMessageBox.confirm(`确定退出「${club.name}」吗？`, '退出社团', { type: 'warning' })
  try {
    await quitClub(club.clubId)
    ElMessage.success('已退出社团')
    loadMyClubs()
  } catch (e) {
    ElMessage.error(e.response?.data?.message || '退出失败')
  }
}

function showApplyDialog() {
  Object.assign(form, { name: '', category: '', establishedDate: '', advisor: '', description: '' })
  dialogVisible.value = true
}

async function handleSubmit() {
  await formRef.value.validate()
  submitting.value = true
  try {
    await applyClub(form)
    ElMessage.success('社团创建申请已提交，等待管理员审批')
    dialogVisible.value = false
    loadApplied()
  } catch (e) {
    ElMessage.error(e.response?.data?.message || e.message || '申请失败')
  } finally {
    submitting.value = false
  }
}
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
  justify-content: space-between;
  align-items: center;
  padding-top: 12px;
  border-top: 1px solid var(--color-border);
}

.club-card__actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

.club-info-item {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 13px;
  color: var(--color-text-light);
}
</style>
