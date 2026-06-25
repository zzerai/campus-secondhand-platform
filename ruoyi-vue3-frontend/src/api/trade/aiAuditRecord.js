import request from '@/utils/request'

// 查询AI审核记录列表
export function listAiAuditRecord(query) {
    return request({
        url: '/trade/aiAuditRecord/list',
        method: 'get',
        params: query
    })
}

// 查询AI审核记录详细
export function getAiAuditRecord(recordId) {
    return request({
        url: '/trade/aiAuditRecord/' + recordId,
        method: 'get'
    })
}

// 新增AI审核记录
export function addAiAuditRecord(data) {
    return request({
        url: '/trade/aiAuditRecord',
        method: 'post',
        data: data
    })
}

// 修改AI审核记录
export function updateAiAuditRecord(data) {
    return request({
        url: '/trade/aiAuditRecord',
        method: 'put',
        data: data
    })
}

// 删除AI审核记录
export function delAiAuditRecord(recordId) {
    return request({
        url: '/trade/aiAuditRecord/' + recordId,
        method: 'delete'
    })
}
