import request from '@/utils/request'

// ==================== 智能对话（非流式，用于保留场景） ====================

// 发送消息（带会话）
export function sendChatMessage(sessionId, question) {
    return request({
        url: '/algorithm/chat',
        method: 'post',
        data: { sessionId, question }
    })
}

// ==================== 智能对话（流式） ====================

// 流式对话 — 通过 EventSource 读取，回调逐块处理
export function sendChatMessageStream(sessionId, question, onMessage, onDone, onError) {
    const url = `/dev-api/algorithm/chat/stream?sessionId=${encodeURIComponent(sessionId)}&question=${encodeURIComponent(question)}`
    
    fetch(url, {
        method: 'GET',
        headers: { 'Accept': 'text/event-stream' }
    }).then(response => {
        if (!response.ok) {
            onError(`HTTP 错误: ${response.status}`)
            return
        }
        
        const reader = response.body.getReader()
        const decoder = new TextDecoder()
        let buffer = ''
        let streamEnded = false
        
        function processBuffer() {
            const parts = buffer.split(/\r?\n\r?\n/)
            buffer = parts.pop() || ''
            
            for (const part of parts) {
                const lines = part.split('\n')
                let data = null
                for (const line of lines) {
                    if (line.startsWith('data:')) {
                        data = line.substring(5).trim()
                        break
                    }
                }
                if (data === null) continue
                
                if (data === '[DONE]') {
                    streamEnded = true
                    onDone()
                    return
                }
                if (data) {
                    onMessage(data)
                }
            }
        }
        
        function readChunk() {
            reader.read().then(({ done, value }) => {
                if (done) {
                    if (buffer.trim()) processBuffer()
                    if (!streamEnded) onDone()
                    return
                }
                
                buffer += decoder.decode(value, { stream: true })
                processBuffer()
                
                if (!streamEnded) readChunk()
            }).catch(err => {
                onError(err.message || '读取流失败')
            })
        }
        
        readChunk()
    }).catch(err => {
        onError(err.message || '连接失败')
    })
}

// ==================== 会话管理 ====================

// 获取会话列表
export function getSessionList(userId) {
    return request({
        url: '/algorithm/chat/sessions',
        method: 'post',
        data: { userId }
    })
}

// 创建新会话
export function createSession(userId) {
    return request({
        url: '/algorithm/chat/session/create',
        method: 'post',
        data: { userId }
    })
}

// 删除会话
export function deleteSession(sessionId) {
    return request({
        url: '/algorithm/chat/session/delete',
        method: 'post',
        data: { sessionId }
    })
}

// 获取历史消息
export function getChatHistory(sessionId) {
    return request({
        url: '/algorithm/chat/history',
        method: 'post',
        data: { sessionId }
    })
}

// ==================== 语音识别 (ASR) ====================
export function speechToText(audioBlob) {
    const formData = new FormData()
    formData.append('file', audioBlob, 'recording.wav')
    return request({
        url: '/algorithm/asr',
        method: 'post',
        data: formData,
        headers: { 'Content-Type': 'multipart/form-data' }
    })
}

// ==================== 语音合成 (TTS) ====================
export function textToSpeech(text) {
    return request({
        url: '/algorithm/tts',
        method: 'post',
        data: { text },
        responseType: 'blob'
    })
}
