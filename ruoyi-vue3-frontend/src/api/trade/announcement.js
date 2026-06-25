import request from '@/utils/request'

// 查询交易公告列表
export function listAnnouncement(query) {
  return request({
    url: '/trade/announcement/list',
    method: 'get',
    params: query
  })
}

// 查询交易公告详细
export function getAnnouncement(announcementId) {
  return request({
    url: '/trade/announcement/' + announcementId,
    method: 'get'
  })
}

// 新增交易公告
export function addAnnouncement(data) {
  return request({
    url: '/trade/announcement',
    method: 'post',
    data: data
  })
}

// 修改交易公告
export function updateAnnouncement(data) {
  return request({
    url: '/trade/announcement',
    method: 'put',
    data: data
  })
}

// 删除交易公告
export function delAnnouncement(announcementId) {
  return request({
    url: '/trade/announcement/' + announcementId,
    method: 'delete'
  })
}

// 发布公告
export function publishAnnouncement(announcementId) {
  return request({
    url: '/trade/announcement/publish/' + announcementId,
    method: 'post'
  })
}

// 撤回公告
export function retractAnnouncement(announcementId) {
  return request({
    url: '/trade/announcement/retract/' + announcementId,
    method: 'post'
  })
}
