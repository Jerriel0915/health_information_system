import request from '@/utils/request'

// ==================== 对话大模型 API ====================

/**
 * 发送对话消息（如后端有接口，替换为真实地址）
 * @param {string} message - 用户消息
 * @param {string} context - 上下文（可选）
 */
export function sendChatMessage(message, context) {
    // TODO: 替换为后端真实接口
    return request({
        url: '/ai/chat',
        method: 'post',
        data: { message, context }
    })
}

// ==================== 图像分类 API ====================

/**
 * 上传图像并进行分类
 * @param {File} file - 图像文件
 */
export function classifyImage(file) {
    const formData = new FormData()
    formData.append('image', file)
    // TODO: 替换为后端真实接口
    return request({
        url: '/ai/image/classify',
        method: 'post',
        data: formData,
        headers: { 'Content-Type': 'multipart/form-data' }
    })
}

// ==================== 目标检测 API ====================

/**
 * 检测异常数据（违规收费/异常就诊）
 * @param {Object} params - 检测参数
 */
export function detectAnomaly(params) {
    // TODO: 替换为后端真实接口
    return request({
        url: '/ai/detection/anomaly',
        method: 'post',
        data: params
    })
}

// ==================== ASR 语音识别 API（如需后端） ====================

/**
 * 语音识别（如使用后端 API）
 * @param {Blob} audioBlob - 音频数据
 */
export function speechToText(audioBlob) {
    const formData = new FormData()
    formData.append('audio', audioBlob, 'recording.wav')
    // TODO: 替换为后端真实接口
    return request({
        url: '/ai/asr',
        method: 'post',
        data: formData,
        headers: { 'Content-Type': 'multipart/form-data' }
    })
}

// ==================== TTS 语音合成 API（如需后端） ====================

/**
 * 文字转语音
 * @param {string} text - 要转换的文字
 */
export function textToSpeech(text) {
    // TODO: 替换为后端真实接口
    return request({
        url: '/ai/tts',
        method: 'post',
        data: { text },
        responseType: 'blob'
    })
}