import request from '@/utils/request'

// 查询???????列表
export function listInstitution(query) {
  return request({
    url: '/system/institution/list',
    method: 'get',
    params: query
  })
}

// 查询???????详细
export function getInstitution(id) {
  return request({
    url: '/system/institution/detail/' + id,
    method: 'get'
  })
}

// 新增???????
export function addInstitution(data) {
  return request({
    url: '/system/institution',
    method: 'post',
    data: data
  })
}

// 修改???????
export function updateInstitution(data) {
  return request({
    url: '/system/institution',
    method: 'put',
    data: data
  })
}

// 删除???????
export function delInstitution(id) {
  return request({
    url: '/system/institution/detail/' + id,
    method: 'delete'
  })
}
