import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import * as echarts from 'echarts'
import store from '@/store'

const app = createApp(App)

// 使用 Element Plus
app.use(ElementPlus)

// 使用路由
app.use(router)

// 使用 Vuex Store
app.use(store)

// 全局挂载 echarts
app.config.globalProperties.$echarts = echarts

// 全局时间格式化
app.config.globalProperties.parseTime = (time, pattern = '{y}-{m}-{d} {h}:{i}:{s}') => {
    if (!time) return ''
    const date = new Date(time)
    if (isNaN(date.getTime())) return time
    const formatObj = {
        y: date.getFullYear(),
        m: date.getMonth() + 1,
        d: date.getDate(),
        h: date.getHours(),
        i: date.getMinutes(),
        s: date.getSeconds()
    }
    return pattern.replace(/{([ymdhisa])+}/g, (result, key) => {
        let value = formatObj[key]
        if (result.length > 0 && value < 10) {
            value = '0' + value
        }
        return value || 0
    })
}

// 全局数字格式化
app.config.globalProperties.formatNumber = (value) => {
    if (!value && value !== 0) return '--'
    if (value >= 10000) {
        return (value / 10000).toFixed(1) + '万'
    }
    return value.toLocaleString()
}

app.mount('#app')
