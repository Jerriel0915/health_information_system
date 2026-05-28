import request from '@/utils/request'

// 医疗机构概况统计
export function getInstitutionOverview(params) {
    return request({
        url: '/statistics/institution/overview',
        method: 'get',
        params
    })
}

// 医疗机构类型分布
export function getInstitutionType(params) {
    return request({
        url: '/statistics/institution/type',
        method: 'get',
        params
    })
}

// 医疗机构等级分布
export function getInstitutionLevel(params) {
    return request({
        url: '/statistics/institution/level',
        method: 'get',
        params
    })
}

// 医疗机构区域分布
export function getInstitutionDistribution(params) {
    return request({
        url: '/statistics/institution/distribution',
        method: 'get',
        params
    })
}

// 医疗机构趋势分析
export function getInstitutionTrend(params) {
    return request({
        url: '/statistics/institution/trend',
        method: 'get',
        params
    })
}

// 医疗机构服务能力分析
export function getInstitutionCapacity(params) {
    return request({
        url: '/statistics/institution/capacity',
        method: 'get',
        params
    })
}