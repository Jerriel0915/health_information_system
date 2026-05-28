import request from '@/utils/request'

// 人口概况统计
export function getPopulationOverview(params) {
    return request({
        url: '/statistics/population/overview',
        method: 'get',
        params
    })
}

// 人口结构分析（年龄/性别）
export function getPopulationStructure(params) {
    return request({
        url: '/statistics/population/structure',
        method: 'get',
        params
    })
}

// 人口趋势分析
export function getPopulationTrend(params) {
    return request({
        url: '/statistics/population/trend',
        method: 'get',
        params
    })
}

// 人口地域分布
export function getPopulationDistribution(params) {
    return request({
        url: '/statistics/population/distribution',
        method: 'get',
        params
    })
}

// 人口预测数据
export function getPopulationForecast(params) {
    return request({
        url: '/statistics/population/forecast',
        method: 'get',
        params
    })
}