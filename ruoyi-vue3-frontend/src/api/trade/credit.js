import request from '@/utils/request'

// 查询信用流水列表
export function listCreditLog(query) {
  return request({
    url: '/trade/credit/log/list',
    method: 'get',
    params: query
  })
}

// 管理员手动调整信用分
export function adjustCredit(data) {
  return request({
    url: '/trade/credit/adjust',
    method: 'post',
    data: data
  })
}
