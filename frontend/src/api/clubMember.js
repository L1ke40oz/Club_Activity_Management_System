import request from '@/utils/request'

export function getMemberList(clubId, params) {
  return request.get(`/club-member/list/${clubId}`, { params })
}

export function getAllPendingMembers() {
  return request.get('/club-member/pending-all')
}

export function applyJoin(data) {
  return request.post('/club-member/apply', data)
}

export function approveMember(id) {
  return request.put(`/club-member/approve/${id}`)
}

export function rejectMember(id) {
  return request.put(`/club-member/reject/${id}`)
}

export function quitClub(clubId, transferTo) {
  const params = transferTo ? { transferTo } : {}
  return request.put(`/club-member/quit/${clubId}`, null, { params })
}

export function updatePosition(data) {
  return request.put('/club-member/position', data)
}
