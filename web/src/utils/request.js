import axios from 'axios'
import { Message, MessageBox } from 'element-plus'
import store from '@/store'
import { getToken } from '@/utils/auth'

// 创建 axios 实例
const service = axios.create({
    baseURL: process.env.VUE_APP_BASE_API || '/api',
    timeout: 30000,
    headers: {
        'Content-Type': 'application/json;charset=utf-8'
    }
})

// 请求拦截器
service.interceptors.request.use(
    config => {
        // 在发送请求之前做些什么
        const token = getToken()
        if (token) {
            config.headers['Authorization'] = 'Bearer ' + token
        }
        return config
    },
    error => {
        console.error('请求错误:', error)
        return Promise.reject(error)
    }
)

// 响应拦截器
service.interceptors.response.use(
    response => {
        const res = response.data

        // 如果返回的是文件流，直接返回
        if (response.config.responseType === 'blob') {
            return response
        }

        // 根据后端返回的 code 判断
        if (res.code === 200) {
            return res
        }

        // 其他错误码处理
        if (res.code === 401) {
            // 未授权，清除 token 并跳转到登录页
            ElMessageBox.confirm('登录状态已过期，请重新登录', '系统提示', {
                confirmButtonText: '重新登录',
                cancelButtonText: '取消',
                type: 'warning'
            }).then(() => {
                store.dispatch('LogOut').then(() => {
                    location.href = '/login'
                })
            })
            return Promise.reject(new Error('未授权'))
        } else if (res.code === 500) {
            Message.error(res.msg || '服务器错误')
            return Promise.reject(new Error(res.msg || '服务器错误'))
        } else {
            Message.error(res.msg || '请求失败')
            return Promise.reject(new Error(res.msg || '请求失败'))
        }
    },
    error => {
        console.error('响应错误:', error)
        const { response } = error
        if (response) {
            switch (response.status) {
                case 400:
                    Message.error('请求参数错误')
                    break
                case 401:
                    Message.error('未授权，请重新登录')
                    store.dispatch('LogOut').then(() => {
                        location.href = '/login'
                    })
                    break
                case 403:
                    Message.error('拒绝访问')
                    break
                case 404:
                    Message.error('请求资源不存在')
                    break
                case 500:
                    Message.error('服务器内部错误')
                    break
                default:
                    Message.error(response.data?.msg || '网络错误')
            }
        } else {
            Message.error('网络连接异常，请检查网络')
        }
        return Promise.reject(error)
    }
)

export default service