import request from '@/utils/request'

// 查询区域列表
export function listRegion(query) {
    return request({
        url: '/system/region/list',
        method: 'get',
        params: query
    })
}

// 查询区域详细
export function getRegion(id) {
    return request({
        url: '/system/region/detail/' + id,
        method: 'get'
    })
}

// 新增区域
export function addRegion(data) {
    return request({
        url: '/system/region',
        method: 'post',
        data: data
    })
}

// 修改区域
export function updateRegion(data) {
    return request({
        url: '/system/region',
        method: 'put',
        data: data
    })
}

// 删除区域
export function delRegion(id) {
    return request({
        url: '/system/region/' + id,
        method: 'delete'
    })
}

// ==================== 区域统计接口 ====================

// 区域树形数据
export function getRegionTree() {
    return request({
        url: '/system/region/tree',
        method: 'get'
    })
}

// 区域总览
export function getRegionSummary() {
    return request({
        url: '/system/region/summary',
        method: 'get'
    })
}