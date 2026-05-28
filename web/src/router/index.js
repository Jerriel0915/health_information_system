import { createRouter, createWebHistory } from 'vue-router'
import Layout from '@/components/layout/AppMain.vue'

const router = createRouter({
    history: createWebHistory(),
    routes: [
        {
            path: '/login',
            name: 'Login',
            component: () => import('@/views/login/index.vue'),
            meta: { title: '登录', requiresAuth: false }
        },
        {
            path: '/',
            component: Layout,
            redirect: '/dashboard',
            meta: { requiresAuth: true },
            children: [
                {
                    path: 'dashboard',
                    name: 'Dashboard',
                    component: () => import('@/views/statistics/dashboard/index.vue'),
                    meta: { title: '首页', requiresAuth: true }
                },
                {
                    path: 'population',
                    name: 'Population',
                    component: () => import('@/views/statistics/population/index.vue'),
                    meta: { title: '人口信息统计分析', requiresAuth: true }
                },
                {
                    path: 'institution',
                    name: 'Institution',
                    component: () => import('@/views/statistics/institution/index.vue'),
                    meta: { title: '医疗卫生机构统计分析', requiresAuth: true }
                },
                {
                    path: 'staff',
                    name: 'Staff',
                    component: () => import('@/views/statistics/staff/index.vue'),
                    meta: { title: '医疗卫生人员统计分析', requiresAuth: true }
                },
                {
                    path: 'bed',
                    name: 'Bed',
                    component: () => import('@/views/statistics/bed/index.vue'),
                    meta: { title: '医疗卫生床位统计分析', requiresAuth: true }
                },
                {
                    path: 'service',
                    name: 'Service',
                    component: () => import('@/views/statistics/service/index.vue'),
                    meta: { title: '医疗服务统计分析', requiresAuth: true }
                },
                {
                    path: 'cost',
                    name: 'Cost',
                    component: () => import('@/views/statistics/cost/index.vue'),
                    meta: { title: '医疗费用统计分析', requiresAuth: true }
                },
                {
                    path: 'ai',
                    name: 'AI',
                    component: () => import('@/views/statistics/ai/index.vue'),
                    meta: { title: '智能算法中心', requiresAuth: true }
                }
            ]
        }
    ]
})

// 路由守卫：检查登录状态
router.beforeEach((to, from, next) => {
    // 检查是否需要登录
    const requiresAuth = to.matched.some(record => record.meta.requiresAuth)
    const token = localStorage.getItem('token')

    // 如果目标页面是登录页
    if (to.path === '/login') {
        if (token) {
            // 已登录，跳转到首页
            next('/')
        } else {
            next()
        }
    }
    // 如果目标页面需要登录
    else if (requiresAuth) {
        if (token) {
            // 已登录，允许访问
            next()
        } else {
            // 未登录，跳转到登录页
            next('/login')
        }
    }
    // 不需要登录的页面
    else {
        next()
    }
})

export default router