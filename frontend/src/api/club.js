import request from '@/utils/request'

export function getClubList(params) {
  return request.get('/club/list', { params })
}

export function getClubDetail(id) {
  return request.get(`/club/${id}`)
}

export function getClubPublicDetail(id) {
  return request.get(`/club/detail/${id}`)
}

export function getPendingClubs() {
  return request.get('/club/pending')
}

export function getMyAppliedClubs() {
  return request.get('/club/my-applied')
}

export function applyClub(data) {
  return request.post('/club/apply', data)
}

export function approveClub(id) {
  return request.put(`/club/approve/${id}`)
}

export function rejectClub(id) {
  return request.put(`/club/reject/${id}`)
}

export function addClub(data) {
  return request.post('/club/add', data)
}

export function updateClub(data) {
  return request.put('/club/update', data)
}

export function deleteClub(id) {
  return request.delete(`/club/${id}`)
}
