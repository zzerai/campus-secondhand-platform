import request from '@/utils/request'

// 查询交易评价列表
export function listEvaluation(query) {
  return request({
    url: '/trade/evaluation/list',
    method: 'get',
    params: query
  })
}

// 查询交易评价详细
export function getEvaluation(evaluationId) {
  return request({
    url: '/trade/evaluation/' + evaluationId,
    method: 'get'
  })
}

// 新增交易评价
export function addEvaluation(data) {
  return request({
    url: '/trade/evaluation',
    method: 'post',
    data: data
  })
}

// 修改交易评价
export function updateEvaluation(data) {
  return request({
    url: '/trade/evaluation',
    method: 'put',
    data: data
  })
}

// 删除交易评价
export function delEvaluation(evaluationId) {
  return request({
    url: '/trade/evaluation/' + evaluationId,
    method: 'delete'
  })
}
