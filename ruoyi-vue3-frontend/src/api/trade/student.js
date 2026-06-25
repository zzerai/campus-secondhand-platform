import request from '@/utils/request'

// 查询学生用户列表
export function listStudent(query) {
  return request({
    url: '/trade/student/list',
    method: 'get',
    params: query
  })
}

// 查询学生用户详细
export function getStudent(userId) {
  return request({
    url: '/trade/student/' + userId,
    method: 'get'
  })
}

// 新增学生用户
export function addStudent(data) {
  return request({
    url: '/trade/student',
    method: 'post',
    data: data
  })
}

// 修改学生用户
export function updateStudent(data) {
  return request({
    url: '/trade/student',
    method: 'put',
    data: data
  })
}

// 删除学生用户
export function delStudent(userId) {
  return request({
    url: '/trade/student/' + userId,
    method: 'delete'
  })
}

// 导出学生用户
export function exportStudent(query) {
  return request({
    url: '/trade/student/export',
    method: 'post',
    responseType: 'blob',
    params: query
  })
}

// 下载导入模板
export function importStudentTemplate() {
  return request({
    url: '/trade/student/importTemplate',
    method: 'get',
    responseType: 'blob'
  })
}

// 导入学生用户
export function importStudent(data) {
  return request({
    url: '/trade/student/import',
    method: 'post',
    data: data,
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}
