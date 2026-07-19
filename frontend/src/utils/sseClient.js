const BASE_URL = '/api'

export function fetchSSE({ url, method = 'GET', body = null, onMessage, onDone, onError }) {
  const controller = new AbortController()

  const options = {
    method,
    headers: { 'Content-Type': 'application/json' },
    credentials: 'include',
    signal: controller.signal
  }
  if (body) {
    options.body = JSON.stringify(body)
  }

  fetch(BASE_URL + url, options)
    .then(response => {
      if (!response.ok) {
        throw new Error(`HTTP ${response.status}`)
      }
      const reader = response.body.getReader()
      const decoder = new TextDecoder()
      let buffer = ''
      let currentEvent = ''
      let dataLines = []

      function processLine(line) {
        if (line.startsWith('event:')) {
          currentEvent = line.slice(6).trim()
        } else if (line.startsWith('data:')) {
          dataLines.push(line.slice(5))
        } else if (line === '') {
          if (currentEvent === 'done') {
            onDone && onDone()
            controller.abort()
            return false
          }
          if (currentEvent !== 'error' && dataLines.length > 0) {
            const data = dataLines.join('\n')
            onMessage && onMessage(data)
          }
          currentEvent = ''
          dataLines = []
        }
        return true
      }

      function read() {
        reader.read().then(({ done, value }) => {
          if (done) {
            onDone && onDone()
            return
          }
          buffer += decoder.decode(value, { stream: true })
          const lines = buffer.split('\n')
          buffer = lines.pop()

          for (const line of lines) {
            if (!processLine(line)) return
          }

          read()
        }).catch(err => {
          if (err.name !== 'AbortError') {
            onError && onError(err)
          }
        })
      }
      read()
    })
    .catch(err => {
      if (err.name !== 'AbortError') {
        onError && onError(err)
      }
    })

  return controller
}
