import axios from 'axios'
import { ElMessage } from 'element-plus'

// 创建axios实例
const service = axios.create({
    baseURL: import.meta.env.VITE_APP_BASE_API || '/dev-api',
    timeout: 30000
})

// 请求拦截器
service.interceptors.request.use(
    config => {
        // 添加 Token
        const token = localStorage.getItem('token')
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
        // 若依框架返回格式：code=200成功
        if (res.code === 200 || res.code === 0) {
            return res
        } else if (res.code === 401) {
            ElMessage.error('登录已过期，请重新登录')
            localStorage.removeItem('token')
            window.location.href = '/login'
            return Promise.reject(new Error('登录已过期'))
        } else {
            ElMessage.error(res.msg || '请求失败')
            return Promise.reject(new Error(res.msg || '请求失败'))
        }
    },
    error => {
        console.error('响应错误:', error)
        ElMessage.error(error.message || '网络错误')
        return Promise.reject(error)
    }
)

export default service