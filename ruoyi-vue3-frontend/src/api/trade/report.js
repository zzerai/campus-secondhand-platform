import request from '@/utils/request'

// 查询举报信息列表
export function listReport(query) {
  return request({
    url: '/trade/report/list',
    method: 'get',
    params: query
  })
}

// 查询举报信息详细
export function getReport(reportId) {
  return request({
    url: '/trade/report/' + reportId,
    method: 'get'
  })
}

// 新增举报信息
export function addReport(data) {
  return request({
    url: '/trade/report',
    method: 'post',
    data: data
  })
}

// 修改举报信息
export function updateReport(data) {
  return request({
    url: '/trade/report',
    method: 'put',
    data: data
  })
}

// 处理举报（通过/驳回）
export function handleReport(data) {
  return request({
    url: '/trade/report/handle',
    method: 'put',
    data: data
  })
}

// 批量处理举报（后端 /handle 接口按 reportIds 数组识别批量）
export function batchHandleReport(data) {
  return request({
    url: '/trade/report/handle',
    method: 'put',
    data: data
  })
}

// 删除举报信息
export function delReport(reportId) {
  return request({
    url: '/trade/report/' + reportId,
    method: 'delete'
  })
}
