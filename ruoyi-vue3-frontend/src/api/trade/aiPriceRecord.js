import request from '@/utils/request'

// 查询AI估价记录列表
export function listAiPriceRecord(query) {
  return request({
    url: '/trade/aiPriceRecord/list',
    method: 'get',
    params: query
  })
}

// 查询AI估价记录详细
export function getAiPriceRecord(recordId) {
  return request({
    url: '/trade/aiPriceRecord/' + recordId,
    method: 'get'
  })
}

// 新增AI估价记录
export function addAiPriceRecord(data) {
  return request({
    url: '/trade/aiPriceRecord',
    method: 'post',
    data: data
  })
}

// 修改AI估价记录
export function updateAiPriceRecord(data) {
  return request({
    url: '/trade/aiPriceRecord',
    method: 'put',
    data: data
  })
}

// 删除AI估价记录
export function delAiPriceRecord(recordId) {
  return request({
    url: '/trade/aiPriceRecord/' + recordId,
    method: 'delete'
  })
}
