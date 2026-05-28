import request from '@/utils/request'

// 人员概况统计
export function getStaffOverview(params) {
    return request({
        url: '/statistics/staff/overview',
        method: 'get',
        params
    })
}

// 人员职称结构
export function getStaffTitleStructure(params) {
    return request({
        url: '/statistics/staff/title',
        method: 'get',
        params
    })
}

// 人员学历结构
export function getStaffEducationStructure(params) {
    return request({
        url: '/statistics/staff/education',
        method: 'get',
        params
    })
}

// 人员类型分布
export function getStaffTypeDistribution(params) {
    return request({
        url: '/statistics/staff/type',
        method: 'get',
        params
    })
}

// 人员趋势分析
export function getStaffTrend(params) {
    return request({
        url: '/statistics/staff/trend',
        method: 'get',
        params
    })
}

// 医护比分析
export function getDoctorNurseRatio(params) {
    return request({
        url: '/statistics/staff/ratio',
        method: 'get',
        params
    })
}

// 人员区域分布
export function getStaffDistribution(params) {
    return request({
        url: '/statistics/staff/distribution',
        method: 'get',
        params
    })
}

// 人员列表
export function getStaffList(params) {
    return request({
        url: '/statistics/staff/list',
        method: 'get',
        params
    })
}