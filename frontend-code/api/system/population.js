import request from '@/utils/request'

// 查询?????列表
export function listPopulation(query) {
  return request({
    url: '/system/population/list',
    method: 'get',
    params: query
  })
}

// 查询?????详细
export function getPopulation(id) {
  return request({
    url: '/system/population/detail/' + id,
    method: 'get'
  })
}

// 新增?????
export function addPopulation(data) {
  return request({
    url: '/system/population',
    method: 'post',
    data: data
  })
}

// 修改?????
export function updatePopulation(data) {
  return request({
    url: '/system/population',
    method: 'put',
    data: data
  })
}

// 删除?????
export function delPopulation(id) {
  return request({
    url: '/system/population/detail/' + id,
    method: 'delete'
  })
}
