import request from '@/utils/request'

// 查询订单操作日志列表
export function listLog(query) {
  return request({
    url: '/trade/log/list',
    method: 'get',
    params: query
  })
}

// 查询订单操作日志详细
export function getLog(logId) {
  return request({
    url: '/trade/log/' + logId,
    method: 'get'
  })
}

// 新增订单操作日志
export function addLog(data) {
  return request({
    url: '/trade/log',
    method: 'post',
    data: data
  })
}

// 修改订单操作日志
export function updateLog(data) {
  return request({
    url: '/trade/log',
    method: 'put',
    data: data
  })
}

// 删除订单操作日志
export function delLog(logId) {
  return request({
    url: '/trade/log/' + logId,
    method: 'delete'
  })
}
