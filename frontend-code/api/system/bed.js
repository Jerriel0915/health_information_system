import request from '@/utils/request'

// 查询???????列表
export function listBed(query) {
  return request({
    url: '/system/bed/list',
    method: 'get',
    params: query
  })
}

// 查询???????详细
export function getBed(id) {
  return request({
    url: '/system/bed/detail/' + id,
    method: 'get'
  })
}

// 新增???????
export function addBed(data) {
  return request({
    url: '/system/bed',
    method: 'post',
    data: data
  })
}

// 修改???????
export function updateBed(data) {
  return request({
    url: '/system/bed',
    method: 'put',
    data: data
  })
}

// 删除???????
export function delBed(id) {
  return request({
    url: '/system/bed/detail/' + id,
    method: 'delete'
  })
}
