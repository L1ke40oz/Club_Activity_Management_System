import request from '@/utils/request'

export function login(data) {
  return request.post('/user/login', data)
}

export function register(data) {
  return request.post('/user/register', data)
}

export function logout() {
  return request.post('/user/logout')
}

export function getUserInfo() {
  return request.get('/user/info')
}

export function getUserList() {
  return request.get('/user/list')
}

export function updateUser(data) {
  return request.put('/user/update', data)
}
