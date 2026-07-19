<template>
  <div class="page-container">
    <div class="page-header">
      <div>
        <h2 class="page-title">用户管理</h2>
        <p class="page-desc">管理系统中的所有用户</p>
      </div>
      <div class="page-actions">
        <el-input v-model="searchKey" placeholder="搜索学号/姓名..." clearable class="search-input" @clear="loadUsers" @keyup.enter="loadUsers">
          <template #prefix><el-icon><Search /></el-icon></template>
        </el-input>
      </div>
    </div>

    <el-card>
      <el-table :data="filteredUsers" stripe v-loading="loading">
        <el-table-column prop="studentId" label="学号" width="120" />
        <el-table-column prop="name" label="姓名" width="100" />
        <el-table-column prop="className" label="班级" width="120" />
        <el-table-column prop="contactInfo" label="联系方式" width="140" />
        <el-table-column label="角色" width="90">
          <template #default="{ row }">
            <el-tag :type="row.role === 1 ? 'danger' : ''">{{ row.role === 1 ? '管理员' : '学生' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="缺勤" width="70" align="center">
          <template #default="{ row }">
            <span :style="{ color: (row.noShowCount || 0) >= 3 ? 'var(--color-danger)' : 'inherit', fontWeight: (row.noShowCount || 0) >= 3 ? '600' : '400' }">
              {{ row.noShowCount || 0 }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="注册时间" min-width="150">
          <template #default="{ row }">
            {{ formatTime(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button size="small" text type="primary" @click="showEditDialog(row)">编辑</el-button>
            <el-button v-if="row.role !== 1" size="small" text type="warning" @click="handleResetPwd(row)">重置密码</el-button>
            <el-button v-if="(row.noShowCount || 0) >= 3" size="small" text type="success" @click="handleResetNoShow(row)">解除限制</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="editVisible" title="编辑用户" width="450px">
      <el-form :model="editForm" label-width="80px">
        <el-form-item label="学号">
          <el-input :model-value="editForm.studentId" disabled />
        </el-form-item>
        <el-form-item label="姓名">
          <el-input v-model="editForm.name" />
        </el-form-item>
        <el-form-item label="班级">
          <el-input v-model="editForm.className" />
        </el-form-item>
        <el-form-item label="联系方式">
          <el-input v-model="editForm.contactInfo" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editVisible = false">取消</el-button>
        <el-button type="primary" @click="handleEditSubmit">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { getUserList, updateUser } from '@/api/user'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search } from '@element-plus/icons-vue'

const loading = ref(false)
const users = ref([])
const searchKey = ref('')
const editVisible = ref(false)
const editForm = reactive({ studentId: '', name: '', className: '', contactInfo: '' })

const filteredUsers = computed(() => {
  if (!searchKey.value) return users.value
  const key = searchKey.value.toLowerCase()
  return users.value.filter(u =>
    u.studentId?.toLowerCase().includes(key) ||
    u.name?.toLowerCase().includes(key)
  )
})

function formatTime(t) {
  if (!t) return ''
  return t.replace('T', ' ').substring(0, 16)
}

async function loadUsers() {
  loading.value = true
  try {
    const res = await getUserList()
    users.value = res.data || []
  } finally {
    loading.value = false
  }
}

function showEditDialog(row) {
  Object.assign(editForm, {
    studentId: row.studentId,
    name: row.name,
    className: row.className,
    contactInfo: row.contactInfo
  })
  editVisible.value = true
}

async function handleEditSubmit() {
  const res = await updateUser(editForm)
  if (res.code === 200) {
    ElMessage.success('更新成功')
    editVisible.value = false
    loadUsers()
  } else {
    ElMessage.error(res.message || '更新失败')
  }
}

async function handleResetPwd(row) {
  await ElMessageBox.confirm(`确定将 ${row.name}(${row.studentId}) 的密码重置为 123456？`, '重置密码', { type: 'warning' })
  const res = await updateUser({ studentId: row.studentId, password: '123456' })
  if (res.code === 200) {
    ElMessage.success('密码已重置为 123456')
  } else {
    ElMessage.error(res.message || '重置失败')
  }
}

async function handleResetNoShow(row) {
  await ElMessageBox.confirm(`确定解除 ${row.name}(${row.studentId}) 的报名限制？缺勤次数将归零`, '解除限制', { type: 'warning' })
  const res = await updateUser({ studentId: row.studentId, noShowCount: 0 })
  if (res.code === 200) {
    ElMessage.success('已解除报名限制')
    row.noShowCount = 0
  } else {
    ElMessage.error(res.message || '操作失败')
  }
}

onMounted(loadUsers)
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
}

.search-input {
  width: 200px;
}
</style>
