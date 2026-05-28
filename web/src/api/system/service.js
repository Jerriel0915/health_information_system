import request from '@/utils/request'

// 查询服务列表
export function listService(query) {
    return request({
        url: '/system/service/list',
        method: 'get',
        params: query
    })
}

// 查询服务详细
export function getService(id) {
    return request({
        url: '/system/service/detail/' + id,
        method: 'get'
    })
}

// 新增服务
export function addService(data) {
    return request({
        url: '/system/service',
        method: 'post',
        data: data
    })
}

// 修改服务
export function updateService(data) {
    return request({
        url: '/system/service',
        method: 'put',
        data: data
    })
}

// 删除服务
export function delService(id) {
    return request({
        url: '/system/service/' + id,
        method: 'delete'
    })
}

// ==================== 服务统计接口 ====================

// 服务总览
export function getServiceSummary(params) {
    return request({
        url: '/system/service/summary',
        method: 'get',
        params: params
    })
}

// 按服务类型分布
export function getServiceTypeDistribution(params) {
    return request({
        url: '/system/service/type-distribution',
        method: 'get',
        params: params
    })
}

// 按科室分布
export function getServiceDeptDistribution(params) {
    return request({
        url: '/system/service/dept-distribution',
        method: 'get',
        params: params
    })
}

// 疾病排行
export function getDiagnosisRanking(params) {
    return request({
        url: '/system/service/diagnosis-ranking',
        method: 'get',
        params: params
    })
}

// 服务量趋势
export function getServiceTrend() {
    return request({
        url: '/system/service/trend',
        method: 'get'
    })
}

// 平均住院天数
export function getAvgHospitalDays(params) {
    return request({
        url: '/system/service/avg-days',
        method: 'get',
        params: params
    })
}