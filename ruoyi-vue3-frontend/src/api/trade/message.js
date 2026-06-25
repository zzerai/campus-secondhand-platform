import request from '@/utils/request'

// 查询私信消息列表
export function listMessage(query) {
  return request({
    url: '/trade/message/list',
    method: 'get',
    params: query
  })
}

// 查询私信消息详细
export function getMessage(messageId) {
  return request({
    url: '/trade/message/' + messageId,
    method: 'get'
  })
}

// 新增私信消息
export function addMessage(data) {
  return request({
    url: '/trade/message',
    method: 'post',
    data: data
  })
}

// 修改私信消息
export function updateMessage(data) {
  return request({
    url: '/trade/message',
    method: 'put',
    data: data
  })
}

// 删除私信消息
export function delMessage(messageId) {
  return request({
    url: '/trade/message/' + messageId,
    method: 'delete'
  })
}

// 查询管理员咨询会话列表（goods_id=0）
export function listAdminConversations(query) {
  return request({
    url: '/trade/message/admin/conversations',
    method: 'get',
    params: query
  })
}

// 查询管理员咨询消息历史
export function listAdminMessages(query) {
  return request({
    url: '/trade/message/admin/messages',
    method: 'get',
    params: query
  })
}

// 标记管理员咨询已读
export function adminMarkRead(userId) {
  return request({
    url: '/trade/message/admin/read',
    method: 'post',
    params: { userId }
  })
}

// 获取管理员学生用户ID
export function getAdminInfo() {
  return request({
    url: '/trade/message/admin/info',
    method: 'get'
  })
}

// 管理员回复用户咨询
export function adminReply(data) {
  return request({
    url: '/trade/message/admin/reply',
    method: 'post',
    data: data
  })
}
