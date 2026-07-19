import request from '@/utils/request'

export function getClubComments(clubId) {
  return request.get(`/club-comment/list/${clubId}`)
}

export function addClubComment(data) {
  return request.post('/club-comment/add', data)
}

export function deleteClubComment(id) {
  return request.delete(`/club-comment/${id}`)
}
