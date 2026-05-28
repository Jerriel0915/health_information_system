import request from '@/utils/request'

// 登录
export function login(data) {
    return request({
        url: '/login',
        method: 'post',
        data
    })
}

// 获取验证码
export function getCaptcha() {
    return request({
        url: '/captchaImage',
        method: 'get'
    })
}

// 退出登录
export function logout() {
    return request({
        url: '/logout',
        method: 'delete'
    })
}