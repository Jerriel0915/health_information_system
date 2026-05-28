import request from '@/utils/request'

// 查询人员列表
export function listStaff(query) {
    return request({
        url: '/system/staff/list',
        method: 'get',
        params: query
    })
}

// 查询人员详细
export function getStaff(id) {
    return request({
        url: '/system/staff/detail/' + id,
        method: 'get'
    })
}

// 新增人员
export function addStaff(data) {
    return request({
        url: '/system/staff',
        method: 'post',
        data: data
    })
}

// 修改人员
export function updateStaff(data) {
    return request({
        url: '/system/staff',
        method: 'put',
        data: data
    })
}

// 删除人员
export function delStaff(id) {
    return request({
        url: '/system/staff/' + id,
        method: 'delete'
    })
}

// ==================== 人员统计接口 ====================

// 人员总览
export function getStaffSummary(params) {
    return request({
        url: '/system/staff/summary',
        method: 'get',
        params: params
    })
}

// 按职称分布
export function getStaffJobTitleDistribution(params) {
    return request({
        url: '/system/staff/job-title-distribution',
        method: 'get',
        params: params
    })
}

// 按职业类别分布
export function getStaffCategoryDistribution(params) {
    return request({
        url: '/system/staff/category-distribution',
        method: 'get',
        params: params
    })
}

// 按学历分布
export function getStaffEducationDistribution(params) {
    return request({
        url: '/system/staff/education-distribution',
        method: 'get',
        params: params
    })
}

// 性别比例
export function getStaffGenderRatio(params) {
    return request({
        url: '/system/staff/gender-ratio',
        method: 'get',
        params: params
    })
}

// 人员数量趋势
export function getStaffTrend() {
    return request({
        url: '/system/staff/trend',
        method: 'get'
    })
}