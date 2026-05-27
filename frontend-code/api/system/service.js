import request from '@/utils/request'

// 查询???????列表
export function listService(query) {
  return request({
    url: '/system/service/list',
    method: 'get',
    params: query
  })
}

// 查询???????详细
export function getService(id) {
  return request({
    url: '/system/service/detail/' + id,
    method: 'get'
  })
}

// 新增???????
export function addService(data) {
  return request({
    url: '/system/service',
    method: 'post',
    data: data
  })
}

// 修改???????
export function updateService(data) {
  return request({
    url: '/system/service',
    method: 'put',
    data: data
  })
}

// 删除???????
export function delService(id) {
  return request({
    url: '/system/service/detail/' + id,
    method: 'delete'
  })
}
