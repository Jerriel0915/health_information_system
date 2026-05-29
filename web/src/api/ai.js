import request from '@/utils/request'

// ==================== 智能对话 ====================

// ==================== 会话管理 ====================

// 发送消息（带会话）
export function sendChatMessage(sessionId, question) {
    return request({
        url: '/algorithm/chat',
        method: 'post',
        data: { sessionId, question }
    })
}

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
