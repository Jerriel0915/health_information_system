import request from '@/utils/request'

// 查询床位列表
export function listBed(query) {
    return request({
        url: '/system/bed/list',
        method: 'get',
        params: query
    })
}

// 查询床位详细
export function getBed(id) {
    return request({
        url: '/system/bed/detail/' + id,
        method: 'get'
    })
}

// 新增床位
export function addBed(data) {
    return request({
        url: '/system/bed',
        method: 'post',
        data: data
    })
}

// 修改床位
export function updateBed(data) {
    return request({
        url: '/system/bed',
        method: 'put',
        data: data
    })
}

// 删除床位
export function delBed(id) {
    return request({
        url: '/system/bed/' + id,
        method: 'delete'
    })
}

// ==================== 床位统计接口 ====================

// 床位总览统计
export function getBedSummary(params) {
    return request({
        url: '/system/bed/summary',
        method: 'get',
        params: params
    })
}

// 按科室分布
export function getBedDeptDistribution(params) {
    return request({
        url: '/system/bed/dept-distribution',
        method: 'get',
        params: params
    })
}

// 按区域分布
export function getBedRegionDistribution() {
    return request({
        url: '/system/bed/region-distribution',
        method: 'get'
    })
}

// 床位使用率分析
export function getBedUsageRate(params) {
    return request({
        url: '/system/bed/usage-rate',
        method: 'get',
        params: params
    })
}

// 床位数趋势
export function getBedTrend() {
    return request({
        url: '/system/bed/trend',
        method: 'get'
    })
}