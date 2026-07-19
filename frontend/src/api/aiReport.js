import request from '@/utils/request'
import { fetchSSE } from '@/utils/sseClient'

export function generateReport(data) {
  return request.post('/ai-report/generate', data)
}

export function getReportList(params) {
  return request.get('/ai-report/list', { params })
}

export function getReportDetail(id) {
  return request.get(`/ai-report/${id}`)
}

export function deleteReport(id) {
  return request.delete(`/ai-report/${id}`)
}

export function generateReportStream({ clubId, timeRange, onMessage, onDone, onError }) {
  const params = new URLSearchParams({ timeRange })
  if (clubId) params.append('clubId', clubId)
  return fetchSSE({
    url: `/ai-report/generate/stream?${params}`,
    method: 'GET',
    onMessage,
    onDone,
    onError
  })
}

export function chatStream({ messages, clubId, onMessage, onDone, onError }) {
  return fetchSSE({
    url: '/ai-report/chat/stream',
    method: 'POST',
    body: { messages, clubId },
    onMessage,
    onDone,
    onError
  })
}
