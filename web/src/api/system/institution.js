import request from '@/utils/request'

// 查询机构列表
export function listInstitution(query) {
    return request({
        url: '/system/institution/list',
        method: 'get',
        params: query
    })
}

// 查询机构详细
export function getInstitution(id) {
    return request({
        url: '/system/institution/detail/' + id,
        method: 'get'
    })
}

// 新增机构
export function addInstitution(data) {
    return request({
        url: '/system/institution',
        method: 'post',
        data: data
    })
}

// 修改机构
export function updateInstitution(data) {
    return request({
        url: '/system/institution',
        method: 'put',
        data: data
    })
}

// 删除机构
export function delInstitution(id) {
    return request({
        url: '/system/institution/' + id,
        method: 'delete'
    })
}

// ==================== 机构统计接口 ====================

// 机构总览统计
export function getInstitutionSummary(params) {
    return request({
        url: '/system/institution/summary',
        method: 'get',
        params: params
    })
}

// 按类型分布
export function getInstitutionTypeDistribution(params) {
    return request({
        url: '/system/institution/type-distribution',
        method: 'get',
        params: params
    })
}

// 按等级分布
export function getInstitutionLevelDistribution(params) {
    return request({
        url: '/system/institution/level-distribution',
        method: 'get',
        params: params
    })
}

// 按区域分布
export function getInstitutionRegionDistribution() {
    return request({
        url: '/system/institution/region-distribution',
        method: 'get'
    })
}

// 机构数量趋势
export function getInstitutionTrend() {
    return request({
        url: '/system/institution/trend',
        method: 'get'
    })
}