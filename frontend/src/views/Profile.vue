<template>
  <div class="page-container">
    <div class="page-header">
      <div>
        <h2 class="page-title">个人中心</h2>
        <p class="page-desc">管理您的个人信息和查看活动记录</p>
      </div>
    </div>

    <div class="profile-grid">
      <div class="profile-card">
        <div class="profile-card__avatar">
          <div class="avatar-circle">{{ form.name?.charAt(0) || '?' }}</div>
          <h3>{{ form.name }}</h3>
          <p>{{ form.studentId }}</p>
        </div>
        <el-form ref="formRef" :model="form" :rules="rules" label-width="80px" class="profile-form">
          <el-form-item label="学号">
            <el-input :model-value="form.studentId" disabled />
          </el-form-item>
          <el-form-item label="姓名" prop="name">
            <el-input v-model="form.name" />
          </el-form-item>
          <el-form-item label="班级">
            <el-input v-model="form.className" placeholder="请输入班级" />
          </el-form-item>
          <el-form-item label="联系方式">
            <el-input v-model="form.contactInfo" placeholder="请输入联系方式" />
          </el-form-item>
          <el-form-item label="新密码">
            <el-input v-model="form.password" type="password" placeholder="留空则不修改" show-password />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" :loading="saving" class="save-btn" @click="handleSave">保存修改</el-button>
          </el-form-item>
        </el-form>
      </div>

      <div class="records-card">
        <h3 class="records-title">我的报名记录</h3>
        <div v-if="myRegistrations.length === 0 && !regLoading" class="records-empty">
          <p>暂无报名记录</p>
        </div>
        <div v-else class="records-list">
          <div v-for="reg in myRegistrations" :key="reg.id" class="record-item">
            <div class="record-item__info">
              <span class="record-activity">{{ reg.activityTitle || '加载中...' }}</span>
              <span class="record-time">{{ formatTime(reg.regTime) }}</span>
            </div>
            <el-tag :type="reg.signInStatus === 1 ? 'success' : 'info'" size="small" effect="light">
              {{ reg.signInStatus === 1 ? '已签到' : '未签到' }}
            </el-tag>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useUserStore } from '@/stores/user'
import { updateUser } from '@/api/user'
import { getMyRegistrations } from '@/api/registration'
import { getActivityDetail } from '@/api/activity'
import { ElMessage } from 'element-plus'

const userStore = useUserStore()
const formRef = ref()
const saving = ref(false)
const regLoading = ref(false)
const myRegistrations = ref([])

const form = reactive({
  studentId: '',
  name: '',
  className: '',
  contactInfo: '',
  password: ''
})

const rules = {
  name: [{ required: true, message: '请输入姓名', trigger: 'blur' }]
}

async function loadProfile() {
  const info = userStore.userInfo
  if (info) {
    Object.assign(form, {
      studentId: info.studentId,
      name: info.name,
      className: info.className || '',
      contactInfo: info.contactInfo || '',
      password: ''
    })
  }
}

async function loadRegistrations() {
  regLoading.value = true
  try {
    const res = await getMyRegistrations()
    const list = res.data || []
    // 补充活动标题
    for (const reg of list) {
      if (!reg.activityTitle) {
        try {
          const actRes = await getActivityDetail(reg.activityId)
          reg.activityTitle = actRes.data?.title || `活动 #${reg.activityId}`
        } catch (e) {
          reg.activityTitle = `活动 #${reg.activityId}`
        }
      }
    }
    myRegistrations.value = list
  } finally {
    regLoading.value = false
  }
}

function formatTime(t) {
  if (!t) return ''
  return t.replace('T', ' ').substring(0, 16)
}

async function handleSave() {
  await formRef.value.validate()
  saving.value = true
  try {
    const data = { ...form }
    if (!data.password) delete data.password
    await updateUser(data)
    ElMessage.success('保存成功')
    await userStore.fetchUserInfo()
  } finally {
    saving.value = false
  }
}

onMounted(() => {
  loadProfile()
  loadRegistrations()
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

.profile-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 24px;
}

@media (max-width: 900px) {
  .profile-grid {
    grid-template-columns: 1fr;
  }
}

.profile-card {
  padding: 32px;
  border-radius: 16px;
  background: var(--color-surface);
  border: 1px solid var(--color-border);
}

.profile-card__avatar {
  text-align: center;
  margin-bottom: 28px;
}

.avatar-circle {
  width: 72px;
  height: 72px;
  margin: 0 auto 12px;
  border-radius: 50%;
  background: linear-gradient(135deg, var(--color-primary), var(--color-accent));
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28px;
  font-weight: 700;
}

.profile-card__avatar h3 {
  margin: 0 0 4px;
  font-size: 18px;
  color: var(--color-text);
}

.profile-card__avatar p {
  margin: 0;
  font-size: 14px;
  color: var(--color-text-light);
}

.save-btn {
  width: 100%;
  border-radius: 10px;
  height: 40px;
}

.records-card {
  padding: 32px;
  border-radius: 16px;
  background: var(--color-surface);
  border: 1px solid var(--color-border);
}

.records-title {
  margin: 0 0 20px;
  font-size: 16px;
  color: var(--color-primary);
}

.records-empty {
  text-align: center;
  padding: 40px 0;
  color: var(--color-text-light);
}

.records-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.record-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 14px 16px;
  border-radius: 10px;
  background: var(--color-bg);
  border: 1px solid var(--color-border);
}

.record-item__info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.record-activity {
  font-size: 14px;
  font-weight: 500;
  color: var(--color-text);
}

.record-time {
  font-size: 12px;
  color: var(--color-text-light);
}
</style>
