import request from '@/utils/request'

// 查询???????列表
export function listStaff(query) {
  return request({
    url: '/system/staff/list',
    method: 'get',
    params: query
  })
}

// 查询???????详细
export function getStaff(id) {
  return request({
    url: '/system/staff/detail/' + id,
    method: 'get'
  })
}

// 新增???????
export function addStaff(data) {
  return request({
    url: '/system/staff',
    method: 'post',
    data: data
  })
}

// 修改???????
export function updateStaff(data) {
  return request({
    url: '/system/staff',
    method: 'put',
    data: data
  })
}

// 删除???????
export function delStaff(id) {
  return request({
    url: '/system/staff/detail/' + id,
    method: 'delete'
  })
}
