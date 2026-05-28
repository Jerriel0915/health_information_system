import request from '@/utils/request'

// 查询人口列表
export function listPopulation(query) {
    return request({
        url: '/system/population/list',
        method: 'get',
        params: query
    })
}

// 查询人口详细
export function getPopulation(id) {
    return request({
        url: '/system/population/detail/' + id,
        method: 'get'
    })
}

// 新增人口
export function addPopulation(data) {
    return request({
        url: '/system/population',
        method: 'post',
        data: data
    })
}

// 修改人口
export function updatePopulation(data) {
    return request({
        url: '/system/population',
        method: 'put',
        data: data
    })
}

// 删除人口
export function delPopulation(id) {
    return request({
        url: '/system/population/' + id,
        method: 'delete'
    })
}

// ==================== 人口统计接口 ====================

// 人口总览
export function getPopulationSummary(params) {
    return request({
        url: '/system/population/summary',
        method: 'get',
        params: params
    })
}

// 年龄分布
export function getPopulationAgeDistribution(params) {
    return request({
        url: '/system/population/age-distribution',
        method: 'get',
        params: params
    })
}

// 性别比例
export function getPopulationGenderRatio(params) {
    return request({
        url: '/system/population/gender-ratio',
        method: 'get',
        params: params
    })
}

// 区域分布
export function getPopulationRegionDistribution(params) {
    return request({
        url: '/system/population/region-distribution',
        method: 'get',
        params: params
    })
}

// 人口趋势
export function getPopulationTrend() {
    return request({
        url: '/system/population/trend',
        method: 'get'
    })
}