import request from '@/utils/request'

// 查询移动端APK版本列表
export function listAppVersion(query) {
  return request({
    url: '/trade/appVersion/list',
    method: 'get',
    params: query
  })
}

// 查询移动端APK版本详细
export function getAppVersion(versionId) {
  return request({
    url: '/trade/appVersion/' + versionId,
    method: 'get'
  })
}

// 新增移动端APK版本
export function addAppVersion(data) {
  return request({
    url: '/trade/appVersion',
    method: 'post',
    data: data
  })
}

// 修改移动端APK版本
export function updateAppVersion(data) {
  return request({
    url: '/trade/appVersion',
    method: 'put',
    data: data
  })
}

// 删除移动端APK版本
export function delAppVersion(versionId) {
  return request({
    url: '/trade/appVersion/' + versionId,
    method: 'delete'
  })
}
