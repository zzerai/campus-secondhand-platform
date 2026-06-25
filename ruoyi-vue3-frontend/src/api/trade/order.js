import request from '@/utils/request'

// 查询交易订单列表
export function listOrder(query) {
  return request({
    url: '/trade/order/list',
    method: 'get',
    params: query
  })
}

// 查询交易订单详细
export function getOrder(orderId) {
  return request({
    url: '/trade/order/' + orderId,
    method: 'get'
  })
}

// 新增交易订单
export function addOrder(data) {
  return request({
    url: '/trade/order',
    method: 'post',
    data: data
  })
}

// 修改交易订单
export function updateOrder(data) {
  return request({
    url: '/trade/order',
    method: 'put',
    data: data
  })
}

// 删除交易订单
export function delOrder(orderId) {
  return request({
    url: '/trade/order/' + orderId,
    method: 'delete'
  })
}
