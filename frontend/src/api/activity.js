import request from '@/utils/request'

export function getActivityList(params) {
  return request.get('/activity/list', { params })
}

export function getActivityDetail(id) {
  return request.get(`/activity/${id}`)
}

export function addActivity(data) {
  return request.post('/activity/add', data)
}

export function updateActivity(data) {
  return request.put('/activity/update', data)
}

export function approveActivity(id) {
  return request.put(`/activity/approve/${id}`)
}

export function rejectActivity(id) {
  return request.put(`/activity/reject/${id}`)
}

export function finishActivity(id) {
  return request.put(`/activity/finish/${id}`)
}

export function deleteActivity(id) {
  return request.delete(`/activity/${id}`)
}
