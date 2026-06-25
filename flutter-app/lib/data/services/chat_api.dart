import 'dart:convert';
import 'package:flutter/foundation.dart';
import 'package:http/http.dart' as http;
import '../models/chat_models.dart';
import '../models/ajax_result.dart';
import 'session.dart';

class ChatApi {
  static final ChatApi instance = ChatApi._();
  ChatApi._();

  final _session = Session.instance;

  Future<AjaxResult> sendMessage({
    required int receiverId,
    required int goodsId,
    int? orderId,
    required String content,
  }) async {
    final body = <String, dynamic>{
      'receiverId': receiverId,
      'goodsId': goodsId,
      'content': content,
    };
    if (orderId != null) body['orderId'] = orderId;

    debugPrint('[ChatApi] 发送消息: POST /app/message/send');
    final response = await http.post(
      Uri.parse('${Session.baseUrl}/app/message/send'),
      headers: _session.headers,
      body: jsonEncode(body),
    );
    debugPrint('[ChatApi] 发送结果: ${response.statusCode}');
    return AjaxResult.fromJson(jsonDecode(response.body));
  }

  Future<ConversationListResult> getConversations({
    int pageNum = 1,
    int pageSize = 20,
  }) async {
    final uri = Uri.parse('${Session.baseUrl}/app/message/conversations')
        .replace(queryParameters: {
      'pageNum': pageNum.toString(),
      'pageSize': pageSize.toString(),
    });

    debugPrint('[ChatApi] 会话列表: GET $uri');
    final response = await http.get(uri, headers: _session.headers);

    if (response.statusCode == 200) {
      final body = jsonDecode(response.body);
      if (body is Map && body['rows'] is List) {
        return ConversationListResult(
          rows: (body['rows'] as List)
              .map((e) => AppConversationVo.fromJson(e))
              .toList(),
          total: body['total'] ?? 0,
        );
      }
    }
    return ConversationListResult(rows: []);
  }

  Future<MessageListResult> getMessages({
    required int peerId,
    required int goodsId,
  }) async {
    final uri = Uri.parse('${Session.baseUrl}/app/message/messages')
        .replace(queryParameters: {
      'peerId': peerId.toString(),
      'goodsId': goodsId.toString(),
    });

    debugPrint('[ChatApi] 消息记录: GET $uri');
    final response = await http.get(uri, headers: _session.headers);

    if (response.statusCode == 200) {
      final body = jsonDecode(response.body);
      if (body is Map && body['rows'] is List) {
        return MessageListResult(
          rows: (body['rows'] as List)
              .map((e) => AppMessageVo.fromJson(e))
              .toList(),
          total: body['total'] ?? 0,
        );
      }
    }
    return MessageListResult(rows: []);
  }

  Future<int> markRead({
    required int peerId,
    required int goodsId,
  }) async {
    final uri = Uri.parse('${Session.baseUrl}/app/message/read')
        .replace(queryParameters: {
      'peerId': peerId.toString(),
      'goodsId': goodsId.toString(),
    });
    debugPrint('[ChatApi] 标记已读: POST $uri');
    final response = await http.post(uri, headers: _session.headers);
    if (response.statusCode == 200) {
      final body = jsonDecode(response.body);
      if (body['data'] is Map) {
        return body['data']['markedCount'] as int? ?? 0;
      }
    }
    return 0;
  }

  Future<void> markAllRead() async {
    final conversations = await getConversations();
    for (final c in conversations.rows) {
      if (c.unreadCount > 0) {
        await markRead(peerId: c.peerId, goodsId: c.goodsId);
      }
    }
  }

  /// 获取管理员信息（用于"联系管理员"功能）。
  Future<AdminInfo> getAdminInfo() async {
    debugPrint('[ChatApi] 获取管理员信息: GET /app/message/admin/info');
    final response = await http.get(
      Uri.parse('${Session.baseUrl}/app/message/admin/info'),
      headers: _session.headers,
    );
    if (response.statusCode == 200) {
      final body = jsonDecode(response.body);
      if (body is Map && body['data'] is Map) {
        return AdminInfo.fromJson(Map<String, dynamic>.from(body['data']));
      }
    }
    throw Exception('获取管理员信息失败');
  }

  Future<int> deleteMessages({required List<int> messageIds}) async {
    if (messageIds.isEmpty) return 0;
    final uri = Uri.parse('${Session.baseUrl}/app/message/delete/${messageIds.join(',')}');
    debugPrint('[ChatApi] 删除消息: POST $uri');
    final response = await http.post(uri, headers: _session.headers);
    if (response.statusCode == 200) {
      final body = jsonDecode(response.body);
      if (body['data'] is Map) {
        return body['data']['deletedCount'] as int? ?? 0;
      }
    }
    return 0;
  }

  Future<int> getUnreadCount() async {
    debugPrint('[ChatApi] 未读数: GET /app/message/unread/count');
    final response = await http.get(
      Uri.parse('${Session.baseUrl}/app/message/unread/count'),
      headers: _session.headers,
    );
    if (response.statusCode == 200) {
      final body = jsonDecode(response.body);
      if (body['data'] is Map) {
        return body['data']['count'] as int? ?? 0;
      }
    }
    return 0;
  }
}
