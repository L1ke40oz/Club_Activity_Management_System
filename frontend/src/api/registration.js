import request from '@/utils/request'

export function signup(activityId) {
  return request.post(`/registration/signup/${activityId}`)
}

export function cancelRegistration(id) {
  return request.put(`/registration/cancel/${id}`)
}

export function signin(id) {
  return request.put(`/registration/signin/${id}`)
}

export function batchSignin(activityId, studentIds) {
  return request.put(`/registration/signin/batch/${activityId}`, studentIds)
}

export function getRegistrationList(activityId) {
  return request.get(`/registration/list/${activityId}`)
}

export function getAllRegistrations() {
  return request.get('/registration/all')
}

export function getMyRegistrations() {
  return request.get('/registration/my')
}
