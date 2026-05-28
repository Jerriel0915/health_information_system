import request from '@/utils/request'

// ==================== 首页看板 API ====================

/**
 * 获取全系统总览数据
 * 返回：机构数/人员数/床位数/服务量/费用总额等
 */
export function getDashboardSummary() {
    return request({
        url: '/dashboard/summary',
        method: 'get'
    })
}