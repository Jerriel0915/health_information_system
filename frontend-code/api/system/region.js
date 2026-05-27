import request from '@/utils/request'

// 查询???????列表
export function listRegion(query) {
  return request({
    url: '/system/region/list',
    method: 'get',
    params: query
  })
}

// 查询???????详细
export function getRegion(id) {
  return request({
    url: '/system/region/detail/' + id,
    method: 'get'
  })
}

// 新增???????
export function addRegion(data) {
  return request({
    url: '/system/region',
    method: 'post',
    data: data
  })
}

// 修改???????
export function updateRegion(data) {
  return request({
    url: '/system/region',
    method: 'put',
    data: data
  })
}

// 删除???????
export function delRegion(id) {
  return request({
    url: '/system/region/detail/' + id,
    method: 'delete'
  })
}
