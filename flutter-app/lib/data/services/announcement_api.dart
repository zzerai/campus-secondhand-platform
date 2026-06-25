import 'dart:convert';
import 'package:flutter/foundation.dart';
import 'package:http/http.dart' as http;
import '../models/trade_announcement.dart';
import 'session.dart';

class AnnouncementApi {
  static final AnnouncementApi instance = AnnouncementApi._();
  AnnouncementApi._();

  final _session = Session.instance;

  /// 获取已发布公告列表（置顶优先、发布时间倒序，后端匿名放行）
  Future<List<TradeAnnouncement>> getNoticeList() async {
    debugPrint('[AnnouncementApi] 公告列表: GET /app/notice/list');
    final response = await http.get(
      Uri.parse('${Session.baseUrl}/app/notice/list'),
      headers: _session.headers,
    );
    debugPrint('[AnnouncementApi] 响应: ${response.statusCode} ${response.body}');
    if (response.statusCode == 200) {
      final body = jsonDecode(response.body);
      if (body is Map && body['data'] is List) {
        return (body['data'] as List)
            .map((e) => TradeAnnouncement.fromJson(Map<String, dynamic>.from(e)))
            .toList();
      }
    }
    throw Exception('获取公告列表失败');
  }

  /// 获取公告详情
  Future<TradeAnnouncement> getNoticeDetail(int announcementId) async {
    debugPrint('[AnnouncementApi] 公告详情: GET /app/notice/detail/$announcementId');
    final response = await http.get(
      Uri.parse('${Session.baseUrl}/app/notice/detail/$announcementId'),
      headers: _session.headers,
    );
    debugPrint('[AnnouncementApi] 响应: ${response.statusCode} ${response.body}');
    if (response.statusCode == 200) {
      final body = jsonDecode(response.body);
      if (body is Map && body['data'] is Map) {
        return TradeAnnouncement.fromJson(Map<String, dynamic>.from(body['data']));
      }
    }
    throw Exception('获取公告详情失败');
  }
}
