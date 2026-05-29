import { createRouter, createWebHashHistory } from 'vue-router'
import Layout from '@/layout/index.vue'

const routes = [
    {
        path: '/login',
        name: 'Login',
        component: () => import('@/views/login/index.vue'),
        meta: { title: '登录' }
    },
    {
        path: '/',
        component: Layout,
        redirect: '/dashboard',
        children: [
            {
                path: 'dashboard',
                name: 'Dashboard',
                component: () => import('@/views/dashboard/index.vue'),
                meta: { title: '首页看板' }
            }
        ]
    },
    {
        path: '/system/institution',
        component: Layout,
        children: [
            {
                path: '',
                name: 'Institution',
                component: () => import('@/views/system/institution/index.vue'),
                meta: { title: '医疗机构' }
            }
        ]
    },
    {
        path: '/system/staff',
        component: Layout,
        children: [
            {
                path: '',
                name: 'Staff',
                component: () => import('@/views/system/staff/index.vue'),
                meta: { title: '人员管理' }
            }
        ]
    },
    {
        path: '/system/bed',
        component: Layout,
        children: [
            {
                path: '',
                name: 'Bed',
                component: () => import('@/views/system/bed/index.vue'),
                meta: { title: '床位管理' }
            }
        ]
    },
    {
        path: '/system/service',
        component: Layout,
        children: [
            {
                path: '',
                name: 'Service',
                component: () => import('@/views/system/service/index.vue'),
                meta: { title: '服务管理' }
            }
        ]
    },
    {
        path: '/system/cost',
        component: Layout,
        children: [
            {
                path: '',
                name: 'Cost',
                component: () => import('@/views/system/cost/index.vue'),
                meta: { title: '费用管理' }
            }
        ]
    },
    {
        path: '/system/population',
        component: Layout,
        children: [
            {
                path: '',
                name: 'Population',
                component: () => import('@/views/system/population/index.vue'),
                meta: { title: '人口统计' }
            }
        ]
    },
    {
        path: '/system/region',
        component: Layout,
        children: [
            {
                path: '',
                name: 'Region',
                component: () => import('@/views/system/region/index.vue'),
                meta: { title: '区域管理' }
            }
        ]
    },
    {
        path: '/ai',
        component: Layout,
        children: [
            {
                path: '',
                name: 'AIAnalysis',
                component: () => import('@/views/ai/index.vue'),
                meta: { title: '智能分析' }
            }
        ]
    }
]

const router = createRouter({
    history: createWebHashHistory(),
    routes
})

// 路由守卫：如果没有登录，跳转到登录页
router.beforeEach((to, from, next) => {
    const m = document.cookie.match(/(?:^| )Admin-Token=([^;]*)/)
    const token = m ? m[1] : null

    if (to.path === '/login') {
        next()
    } else {
        if (!token) {
            next('/login')
        } else {
            next()
        }
    }
})

export default router

