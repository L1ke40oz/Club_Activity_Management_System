import request from '@/utils/request'

export function getMyMessages() {
  return request.get('/message/my')
}

export function getUnreadCount() {
  return request.get('/message/unread-count')
}

export function markRead(id) {
  return request.put(`/message/read/${id}`)
}

export function markAllRead() {
  return request.put('/message/read-all')
}
