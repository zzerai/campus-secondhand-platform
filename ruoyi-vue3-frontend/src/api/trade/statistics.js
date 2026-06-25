import request from '@/utils/request'

export function getOverview() {
  return request({
    url: '/trade/statistics/overview',
    method: 'get'
  })
}

export function getCategoryStatistics() {
  return request({
    url: '/trade/statistics/category',
    method: 'get'
  })
}

export function getOrderTrend(query) {
  return request({
    url: '/trade/statistics/orderTrend',
    method: 'get',
    params: query
  })
}

export function getPaymentTrend(query) {
  return request({
    url: '/trade/statistics/paymentTrend',
    method: 'get',
    params: query
  })
}
