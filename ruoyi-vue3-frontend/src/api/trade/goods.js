import request from '@/utils/request'

// 查询闲置商品列表
export function listGoods(query) {
  return request({
    url: '/trade/goods/list',
    method: 'get',
    params: query
  })
}

// 查询闲置商品详细
export function getGoods(goodsId) {
  return request({
    url: '/trade/goods/' + goodsId,
    method: 'get'
  })
}

// 新增闲置商品
export function addGoods(data) {
  return request({
    url: '/trade/goods',
    method: 'post',
    data: data
  })
}

// 修改闲置商品
export function updateGoods(data) {
  return request({
    url: '/trade/goods',
    method: 'put',
    data: data
  })
}

// 删除闲置商品
export function delGoods(goodsId) {
  return request({
    url: '/trade/goods/' + goodsId,
    method: 'delete'
  })
}

// 审核商品（通过/拒绝，可单条或批量）
// body: { goodsId? , goodsIds?: number[], goodsStatus: '1'|'2', auditRemark?: string }
export function auditGoods(data) {
  return request({
    url: '/trade/goods/audit',
    method: 'put',
    data: data
  })
}

// 强制下架商品
export function offlineGoods(goodsId) {
  return request({
    url: '/trade/goods/offline/' + goodsId,
    method: 'put'
  })
}

// 恢复上架商品
export function onlineGoods(goodsId) {
  return request({
    url: '/trade/goods/online/' + goodsId,
    method: 'put'
  })
}

// 导出商品
export function exportGoods(query) {
  return request({
    url: '/trade/goods/export',
    method: 'post',
    responseType: 'blob',
    params: query
  })
}

// 下载导入模板
export function importGoodsTemplate() {
  return request({
    url: '/trade/goods/importTemplate',
    method: 'get',
    responseType: 'blob'
  })
}

// AI 商品审核
export function aiAuditGoods(goodsId) {
  return request({
    url: '/trade/ai/audit/goods',
    method: 'post',
    params: { goodsId }
  })
}

// 导入商品
export function importGoods(data) {
  return request({
    url: '/trade/goods/import',
    method: 'post',
    data: data,
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}
