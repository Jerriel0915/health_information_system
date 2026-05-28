import request from '@/utils/request'

// ========== 图像分类相关 ==========
// 获取图像分类统计
export function getImageClassifyStats(params) {
    return request({
        url: '/statistics/ai/imageClassify/stats',
        method: 'get',
        params
    })
}

// 获取图像分类趋势
export function getImageClassifyTrend(params) {
    return request({
        url: '/statistics/ai/imageClassify/trend',
        method: 'get',
        params
    })
}

// 上传图像进行分类
export function uploadAndClassify(file) {
    const formData = new FormData()
    formData.append('file', file)
    return request({
        url: '/statistics/ai/imageClassify/upload',
        method: 'post',
        data: formData,
        headers: { 'Content-Type': 'multipart/form-data' }
    })
}

// ========== 异常检测相关 ==========
// 获取异常检测统计
export function getAnomalyStats(params) {
    return request({
        url: '/statistics/ai/anomaly/stats',
        method: 'get',
        params
    })
}

// 获取异常记录列表
export function getAnomalyList(params) {
    return request({
        url: '/statistics/ai/anomaly/list',
        method: 'get',
        params
    })
}

// 处理异常记录
export function handleAnomaly(data) {
    return request({
        url: '/statistics/ai/anomaly/handle',
        method: 'post',
        data
    })
}

// ========== 智能对话相关 ==========
// 发送对话消息
export function sendChatMessage(params) {
    return request({
        url: '/statistics/ai/chat/send',
        method: 'post',
        data: params
    })
}

// 获取对话历史
export function getChatHistory(params) {
    return request({
        url: '/statistics/ai/chat/history',
        method: 'get',
        params
    })
}

// 清除对话历史
export function clearChatHistory() {
    return request({
        url: '/statistics/ai/chat/clear',
        method: 'delete'
    })
}

// 获取对话建议问题
export function getSuggestQuestions() {
    return request({
        url: '/statistics/ai/chat/suggest',
        method: 'get'
    })
}

// ========== 语音功能相关 ==========
// 语音识别（ASR）
export function speechRecognition(file) {
    const formData = new FormData()
    formData.append('file', file)
    return request({
        url: '/statistics/ai/voice/asr',
        method: 'post',
        data: formData,
        headers: { 'Content-Type': 'multipart/form-data' }
    })
}

// 语音合成（TTS）
export function textToSpeech(params) {
    return request({
        url: '/statistics/ai/voice/tts',
        method: 'post',
        data: params,
        responseType: 'blob'
    })
}

// 获取语音查询结果
export function voiceQuery(params) {
    return request({
        url: '/statistics/ai/voice/query',
        method: 'post',
        data: params
    })
}