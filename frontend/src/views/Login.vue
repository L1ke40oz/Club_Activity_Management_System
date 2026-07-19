<template>
  <div class="auth-page">
    <div class="auth-card">
      <div class="auth-header">
        <div class="auth-logo">
          <svg viewBox="0 0 24 24" width="36" height="36" fill="none" stroke="currentColor" stroke-width="1.5">
            <path d="M12 2L2 7l10 5 10-5-10-5zM2 17l10 5 10-5M2 12l10 5 10-5"/>
          </svg>
        </div>
        <h1>校园社团管理</h1>
        <p>{{ activeTab === 'login' ? '欢迎回来，请登录您的账号' : '创建新账号，加入精彩社团' }}</p>
      </div>

      <div class="auth-tabs">
        <button :class="{ active: activeTab === 'login' }" @click="activeTab = 'login'">登录</button>
        <button :class="{ active: activeTab === 'register' }" @click="activeTab = 'register'">注册</button>
      </div>

      <form v-if="activeTab === 'login'" class="auth-form" @submit.prevent="handleLogin">
        <div class="form-group">
          <label for="login-id">学号</label>
          <input
            id="login-id"
            v-model="loginForm.studentId"
            type="text"
            placeholder="请输入学号"
            autocomplete="username"
            required
          />
        </div>
        <div class="form-group">
          <label for="login-pwd">密码</label>
          <input
            id="login-pwd"
            v-model="loginForm.password"
            type="password"
            placeholder="请输入密码"
            autocomplete="current-password"
            required
            @keyup.enter="handleLogin"
          />
        </div>
        <p v-if="errorMsg" class="auth-error">{{ errorMsg }}</p>
        <button type="submit" class="auth-btn" :disabled="loading">
          {{ loading ? '登录中...' : '登 录' }}
        </button>
      </form>

      <form v-else class="auth-form" @submit.prevent="handleRegister">
        <div class="form-group">
          <label for="reg-id">学号</label>
          <input id="reg-id" v-model="registerForm.studentId" type="text" placeholder="请输入学号" required />
        </div>
        <div class="form-group">
          <label for="reg-name">姓名</label>
          <input id="reg-name" v-model="registerForm.name" type="text" placeholder="请输入姓名" required />
        </div>
        <div class="form-group">
          <label for="reg-pwd">密码</label>
          <input id="reg-pwd" v-model="registerForm.password" type="password" placeholder="请输入密码" required />
        </div>
        <div class="form-row">
          <div class="form-group">
            <label for="reg-class">班级</label>
            <input id="reg-class" v-model="registerForm.className" type="text" placeholder="如：计科2401" required />
          </div>
          <div class="form-group">
            <label for="reg-contact">联系方式</label>
            <input id="reg-contact" v-model="registerForm.contactInfo" type="text" placeholder="手机号" required />
          </div>
        </div>
        <p v-if="errorMsg" class="auth-error">{{ errorMsg }}</p>
        <p v-if="successMsg" class="auth-success">{{ successMsg }}</p>
        <button type="submit" class="auth-btn" :disabled="loading">
          {{ loading ? '注册中...' : '注 册' }}
        </button>
      </form>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { register } from '@/api/user'
import { ElMessage } from 'element-plus'

const router = useRouter()
const userStore = useUserStore()
const activeTab = ref('login')
const loading = ref(false)
const errorMsg = ref('')
const successMsg = ref('')

const loginForm = reactive({ studentId: '', password: '' })
const registerForm = reactive({ studentId: '', name: '', password: '', className: '', contactInfo: '' })

async function handleLogin() {
  errorMsg.value = ''
  if (!loginForm.studentId || !loginForm.password) {
    errorMsg.value = '请填写学号和密码'
    return
  }
  loading.value = true
  try {
    await userStore.login(loginForm)
    ElMessage.success('登录成功')
    router.push('/admin/dashboard')
  } catch (e) {
    errorMsg.value = e.response?.data?.message || '登录失败，请检查学号和密码'
  } finally {
    loading.value = false
  }
}

async function handleRegister() {
  errorMsg.value = ''
  successMsg.value = ''
  const { studentId, name, password, className, contactInfo } = registerForm
  if (!studentId || !name || !password || !className || !contactInfo) {
    errorMsg.value = '请填写所有字段'
    return
  }
  loading.value = true
  try {
    await register(registerForm)
    successMsg.value = '注册成功！正在跳转登录...'
    setTimeout(() => {
      activeTab.value = 'login'
      loginForm.studentId = registerForm.studentId
      successMsg.value = ''
    }, 1200)
  } catch (e) {
    errorMsg.value = e.response?.data?.message || '注册失败，请稍后重试'
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.auth-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24px;
  background:
    radial-gradient(ellipse at top left, rgba(43, 108, 176, 0.08), transparent 50%),
    radial-gradient(ellipse at bottom right, rgba(56, 161, 105, 0.06), transparent 50%),
    linear-gradient(180deg, #f0f7ff 0%, #f7fafc 100%);
}

.auth-card {
  width: 100%;
  max-width: 440px;
  padding: 44px 40px;
  border-radius: 24px;
  background: #fff;
  border: 1px solid rgba(43, 108, 176, 0.08);
  box-shadow: 0 20px 60px rgba(43, 108, 176, 0.08);
}

.auth-header {
  text-align: center;
  margin-bottom: 32px;
}

.auth-logo {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 56px;
  height: 56px;
  border-radius: 16px;
  background: linear-gradient(135deg, var(--color-primary), var(--color-accent));
  color: #fff;
  margin-bottom: 16px;
}

.auth-header h1 {
  margin: 0 0 8px;
  font-size: 26px;
  color: var(--color-primary);
}

.auth-header p {
  margin: 0;
  color: var(--color-text-light);
  font-size: 14px;
}

.auth-tabs {
  display: flex;
  gap: 4px;
  padding: 4px;
  margin-bottom: 28px;
  border-radius: 10px;
  background: #f1f5f9;
}

.auth-tabs button {
  flex: 1;
  height: 38px;
  border-radius: 8px;
  background: transparent;
  color: var(--color-text-light);
  font-size: 14px;
  font-weight: 500;
  transition: all 0.2s;
}

.auth-tabs button.active {
  background: #fff;
  color: var(--color-primary);
  font-weight: 600;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.08);
}

.auth-form {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.form-group label {
  font-size: 13px;
  font-weight: 600;
  color: var(--color-text);
}

.form-group input {
  height: 44px;
  padding: 0 14px;
  border-radius: 10px;
  border: 1px solid #e2e8f0;
  background: #f8fafc;
  color: var(--color-text);
  font-size: 14px;
  transition: border-color 0.2s, box-shadow 0.2s, background 0.2s;
}

.form-group input:focus {
  outline: none;
  border-color: var(--color-primary);
  background: #fff;
  box-shadow: 0 0 0 3px rgba(43, 108, 176, 0.08);
}

.form-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
}

.auth-error {
  margin: 0;
  padding: 10px 14px;
  border-radius: 8px;
  background: rgba(229, 62, 62, 0.06);
  color: var(--color-danger);
  font-size: 13px;
  text-align: center;
}

.auth-success {
  margin: 0;
  padding: 10px 14px;
  border-radius: 8px;
  background: rgba(56, 161, 105, 0.06);
  color: var(--color-accent);
  font-size: 13px;
  text-align: center;
}

.auth-btn {
  height: 44px;
  margin-top: 4px;
  border-radius: 10px;
  background: var(--color-primary);
  color: #fff;
  font-size: 15px;
  font-weight: 600;
  letter-spacing: 0.04em;
  transition: background 0.2s, transform 0.1s;
}

.auth-btn:hover:not(:disabled) {
  background: var(--color-primary-soft);
}

.auth-btn:active:not(:disabled) {
  transform: scale(0.98);
}

.auth-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

@media (max-width: 480px) {
  .auth-card {
    padding: 32px 24px;
  }
  .form-row {
    grid-template-columns: 1fr;
  }
}
</style>
