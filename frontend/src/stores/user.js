import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import request from '../utils/request'

export const useUserStore = defineStore('user', () => {
  const userInfo = ref(JSON.parse(localStorage.getItem('userInfo') || 'null'))

  const isLoggedIn = computed(() => !!userInfo.value)
  const isAdmin = computed(() => userInfo.value?.role === 1)

  async function login(data) {
    const res = await request.post('/user/login', data)
    userInfo.value = res.data
    localStorage.setItem('userInfo', JSON.stringify(res.data))
    return res
  }

  async function logout() {
    await request.post('/user/logout')
    userInfo.value = null
    localStorage.removeItem('userInfo')
  }

  async function fetchUserInfo() {
    const res = await request.get('/user/info')
    userInfo.value = res.data
    localStorage.setItem('userInfo', JSON.stringify(res.data))
    return res.data
  }

  function clearUser() {
    userInfo.value = null
    localStorage.removeItem('userInfo')
  }

  return { userInfo, isLoggedIn, isAdmin, login, logout, fetchUserInfo, clearUser }
})
