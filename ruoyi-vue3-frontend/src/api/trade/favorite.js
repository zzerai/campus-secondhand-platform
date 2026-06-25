import request from '@/utils/request'

// 查询商品收藏列表
export function listFavorite(query) {
  return request({
    url: '/trade/favorite/list',
    method: 'get',
    params: query
  })
}

// 查询商品收藏详细
export function getFavorite(favoriteId) {
  return request({
    url: '/trade/favorite/' + favoriteId,
    method: 'get'
  })
}

// 新增商品收藏
export function addFavorite(data) {
  return request({
    url: '/trade/favorite',
    method: 'post',
    data: data
  })
}

// 修改商品收藏
export function updateFavorite(data) {
  return request({
    url: '/trade/favorite',
    method: 'put',
    data: data
  })
}

// 删除商品收藏
export function delFavorite(favoriteId) {
  return request({
    url: '/trade/favorite/' + favoriteId,
    method: 'delete'
  })
}
