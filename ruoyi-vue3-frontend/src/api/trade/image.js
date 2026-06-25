import request from '@/utils/request'

// 查询商品图片列表
export function listImage(query) {
  return request({
    url: '/trade/image/list',
    method: 'get',
    params: query
  })
}

// 查询商品图片详细
export function getImage(imageId) {
  return request({
    url: '/trade/image/' + imageId,
    method: 'get'
  })
}

// 新增商品图片
export function addImage(data) {
  return request({
    url: '/trade/image',
    method: 'post',
    data: data
  })
}

// 修改商品图片
export function updateImage(data) {
  return request({
    url: '/trade/image',
    method: 'put',
    data: data
  })
}

// 删除商品图片
export function delImage(imageId) {
  return request({
    url: '/trade/image/' + imageId,
    method: 'delete'
  })
}
