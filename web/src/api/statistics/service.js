import request from '@/utils/request'

// 医疗服务概况
export function getServiceOverview(params) {
    return request({
        url: '/statistics/service/overview',
        method: 'get',
        params
    })
}

// 门诊服务统计
export function getOutpatientStatistics(params) {
    return request({
        url: '/statistics/service/outpatient',
        method: 'get',
        params
    })
}

// 住院服务统计
export function getInpatientStatistics(params) {
    return request({
        url: '/statistics/service/inpatient',
        method: 'get',
        params
    })
}

// 手术服务统计
export function getSurgeryStatistics(params) {
    return request({
        url: '/statistics/service/surgery',
        method: 'get',
        params
    })
}

// 服务效率统计
export function getServiceEfficiency(params) {
    return request({
        url: '/statistics/service/efficiency',
        method: 'get',
        params
    })
}

// 科室服务量排行
export function getDepartmentRanking(params) {
    return request({
        url: '/statistics/service/departmentRanking',
        method: 'get',
        params
    })
}

// 服务趋势分析
export function getServiceTrend(params) {
    return request({
        url: '/statistics/service/trend',
        method: 'get',
        params
    })
}

// 服务详情列表
export function getServiceList(params) {
    return request({
        url: '/statistics/service/list',
        method: 'get',
        params
    })
}