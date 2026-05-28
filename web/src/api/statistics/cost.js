import request from '@/utils/request'

// 费用概况统计
export function getCostOverview(params) {
    return request({
        url: '/statistics/cost/overview',
        method: 'get',
        params
    })
}

// 费用结构分析
export function getCostStructure(params) {
    return request({
        url: '/statistics/cost/structure',
        method: 'get',
        params
    })
}

// 医保基金分析
export function getInsuranceAnalysis(params) {
    return request({
        url: '/statistics/cost/insurance',
        method: 'get',
        params
    })
}

// 人均费用趋势
export function getPerCapitaCostTrend(params) {
    return request({
        url: '/statistics/cost/perCapitaTrend',
        method: 'get',
        params
    })
}

// 次均费用趋势
export function getAverageCostTrend(params) {
    return request({
        url: '/statistics/cost/averageTrend',
        method: 'get',
        params
    })
}

// 机构费用排行
export function getInstitutionCostRanking(params) {
    return request({
        url: '/statistics/cost/institutionRanking',
        method: 'get',
        params
    })
}

// 费用详情列表
export function getCostList(params) {
    return request({
        url: '/statistics/cost/list',
        method: 'get',
        params
    })
}