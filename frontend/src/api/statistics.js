import request from '@/utils/request'

export function getOverview() {
  return request.get('/statistics/overview')
}

export function getClubStats(params) {
  return request.get('/statistics/clubs', { params })
}

export function getActivityStats(params) {
  return request.get('/statistics/activities', { params })
}

export function getMemberActivityStats(params) {
  return request.get('/statistics/member-activity', { params })
}
