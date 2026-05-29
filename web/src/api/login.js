import request from '@/utils/request'

// 登录
export function login(data) {
    return request({
        url: '/login',
        method: 'post',
        data: data
    })
}

// 退出登录
export function logout() {
    return request({
        url: '/logout',
        method: 'post'
    })
}

// 获取用户信息
export function getInfo() {
    return request({
        url: '/getInfo',
        method: 'get'
    })
}