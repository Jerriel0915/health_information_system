import request from '@/utils/request'

// 查询费用列表
export function listCost(query) {
    return request({
        url: '/system/cost/list',
        method: 'get',
        params: query
    })
}

// 查询费用详细
export function getCost(id) {
    return request({
        url: '/system/cost/detail/' + id,
        method: 'get'
    })
}

// 新增费用
export function addCost(data) {
    return request({
        url: '/system/cost',
        method: 'post',
        data: data
    })
}

// 修改费用
export function updateCost(data) {
    return request({
        url: '/system/cost',
        method: 'put',
        data: data
    })
}

// 删除费用
export function delCost(id) {
    return request({
        url: '/system/cost/' + id,
        method: 'delete'
    })
}

// ==================== 费用统计接口 ====================

// 费用总览
export function getCostSummary(params) {
    return request({
        url: '/system/cost/summary',
        method: 'get',
        params: params
    })
}

// 费用构成分析
export function getCostComposition(params) {
    return request({
        url: '/system/cost/composition',
        method: 'get',
        params: params
    })
}

// 医保费用分析
export function getInsuranceAnalysis(params) {
    return request({
        url: '/system/cost/insurance-analysis',
        method: 'get',
        params: params
    })
}

// 次均费用趋势
export function getAvgCostTrend() {
    return request({
        url: '/system/cost/avg-cost-trend',
        method: 'get'
    })
}

// 总费用趋势
export function getCostTrend() {
    return request({
        url: '/system/cost/trend',
        method: 'get'
    })
}

// 按费用类别分析
export function getCostCategoryAnalysis(params) {
    return request({
        url: '/system/cost/category-analysis',
        method: 'get',
        params: params
    })
}