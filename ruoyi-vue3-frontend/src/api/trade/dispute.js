import request from '@/utils/request'

// 查询交易争议处理列表
export function listDispute(query) {
  return request({
    url: '/trade/dispute/list',
    method: 'get',
    params: query
  })
}

// 查询交易争议处理详细
export function getDispute(disputeId) {
  return request({
    url: '/trade/dispute/' + disputeId,
    method: 'get'
  })
}

// 新增交易争议处理
export function addDispute(data) {
  return request({
    url: '/trade/dispute',
    method: 'post',
    data: data
  })
}

// 修改交易争议处理
export function updateDispute(data) {
  return request({
    url: '/trade/dispute',
    method: 'put',
    data: data
  })
}

export function handleDispute(data) {
  return request({
    url: '/trade/dispute/handle',
    method: 'put',
    data: data
  })
}

export function reArbitrateDispute(disputeId) {
  return request({
    url: '/trade/ai/audit/dispute',
    method: 'post',
    params: { disputeId }
  })
}

// 删除交易争议处理
export function delDispute(disputeId) {
  return request({
    url: '/trade/dispute/' + disputeId,
    method: 'delete'
  })
}
