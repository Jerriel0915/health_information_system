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

// 流式对话 — 使用浏览器原生 EventSource 接收 SSE
export function sendChatMessageStream(sessionId, question, onMessage, onDone, onError) {
    const url = `/dev-api/algorithm/chat/stream?sessionId=${encodeURIComponent(sessionId)}&question=${encodeURIComponent(question)}`

    const es = new EventSource(url)

    es.addEventListener('message', (event) => {
        if (event.data) {
            onMessage(event.data)
        }
    })

    es.addEventListener('done', () => {
        es.close()
        onDone()
    })

    es.onerror = () => {
        es.close()
        onError('SSE 连接失败')
    }
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

// ==================== 统计报告 TTS ====================
// 一步完成"取数据 → 组装文本 → 合成音频"
export function ttsReport(type) {
    return request({
        url: '/algorithm/tts/report',
        method: 'post',
        data: { type },
        responseType: 'blob'
    })
}

// 仅获取报告文本（用于预览，不合成）
export function getReportText(type) {
    return request({
        url: `/algorithm/tts/report/text?type=${encodeURIComponent(type)}`,
        method: 'get'
    })
}

// ==================== 图像分类 ====================
export function imageClassify(file) {
    const formData = new FormData()
    formData.append('file', file)
    return request({
        url: '/algorithm/image-classify',
        method: 'post',
        data: formData,
        headers: { 'Content-Type': 'multipart/form-data' }
    })
}


// ==================== 骨骼分类 ====================
export function boneClassify(file) {
    const formData = new FormData()
    formData.append('file', file)
    return request({
        url: '/algorithm/predict',
        method: 'post',
        data: formData,
        headers: { 'Content-Type': 'multipart/form-data' }
    })
}

// ==================== 异常检测 ====================
export function runAnomalyDetection() {
    return request({
        url: '/algorithm/object-detect',
        method: 'post'
    })
}
