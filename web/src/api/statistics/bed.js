import request from '@/utils/request'

// 床位概况统计
export function getBedOverview(params) {
    return request({
        url: '/statistics/bed/overview',
        method: 'get',
        params
    })
}

// 床位类型分布
export function getBedTypeDistribution(params) {
    return request({
        url: '/statistics/bed/type',
        method: 'get',
        params
    })
}

// 床位利用率分析
export function getBedUtilization(params) {
    return request({
        url: '/statistics/bed/utilization',
        method: 'get',
        params
    })
}

// 床位趋势分析
export function getBedTrend(params) {
    return request({
        url: '/statistics/bed/trend',
        method: 'get',
        params
    })
}

// 床位区域分布
export function getBedDistribution(params) {
    return request({
        url: '/statistics/bed/distribution',
        method: 'get',
        params
    })
}

// 机构床位排行
export function getInstitutionBedRanking(params) {
    return request({
        url: '/statistics/bed/institutionRanking',
        method: 'get',
        params
    })
}

// 床位列表
export function getBedList(params) {
    return request({
        url: '/statistics/bed/list',
        method: 'get',
        params
    })
}