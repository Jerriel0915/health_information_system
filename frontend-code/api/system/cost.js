import request from '@/utils/request'

// 查询?????列表
export function listCost(query) {
  return request({
    url: '/system/cost/list',
    method: 'get',
    params: query
  })
}

// 查询?????详细
export function getCost(id) {
  return request({
    url: '/system/cost/detail/' + id,
    method: 'get'
  })
}

// 新增?????
export function addCost(data) {
  return request({
    url: '/system/cost',
    method: 'post',
    data: data
  })
}

// 修改?????
export function updateCost(data) {
  return request({
    url: '/system/cost',
    method: 'put',
    data: data
  })
}

// 删除?????
export function delCost(id) {
  return request({
    url: '/system/cost/detail/' + id,
    method: 'delete'
  })
}
