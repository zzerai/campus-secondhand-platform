import 'dart:convert';
import 'package:flutter/foundation.dart';
import 'package:http/http.dart' as http;
import '../models/trade_goods.dart';
import 'session.dart';

class FavoriteApi {
  static final FavoriteApi instance = FavoriteApi._();
  FavoriteApi._();

  final _session = Session.instance;

  /// 返回 null 表示成功，否则返回错误消息。
  Future<String?> addFavorite(int goodsId) async {
    debugPrint('[FavoriteApi] 收藏: POST /app/favorite/$goodsId');
    final response = await http.post(
      Uri.parse('${Session.baseUrl}/app/favorite/$goodsId'),
      headers: _session.headers,
    );
    final body = jsonDecode(response.body);
    if (body is Map && body['code'] == 200) return null;
    if (body is Map && body['msg'] is String) return body['msg'];
    return '操作失败';
  }

  Future<String?> removeFavorite(int goodsId) async {
    debugPrint('[FavoriteApi] 取消收藏: DELETE /app/favorite/$goodsId');
    final response = await http.delete(
      Uri.parse('${Session.baseUrl}/app/favorite/$goodsId'),
      headers: _session.headers,
    );
    final body = jsonDecode(response.body);
    if (body is Map && body['code'] == 200) return null;
    if (body is Map && body['msg'] is String) return body['msg'];
    return '操作失败';
  }

  Future<bool> checkFavorite(int goodsId) async {
    debugPrint('[FavoriteApi] 检查收藏: GET /app/favorite/check/$goodsId');
    try {
      final response = await http.get(
        Uri.parse('${Session.baseUrl}/app/favorite/check/$goodsId'),
        headers: _session.headers,
      );
      if (response.statusCode == 200) {
        final body = jsonDecode(response.body);
        if (body is Map && body['data'] is bool) return body['data'];
      }
    } catch (e) {
      debugPrint('[FavoriteApi] 检查收藏异常: $e');
    }
    return false;
  }

  Future<GoodsListResult> getMyFavorites({int pageNum = 1, int pageSize = 20}) async {
    final uri = Uri.parse('${Session.baseUrl}/app/favorite/myList').replace(
      queryParameters: {'pageNum': pageNum.toString(), 'pageSize': pageSize.toString()},
    );
    debugPrint('[FavoriteApi] 收藏列表: GET $uri');
    final response = await http.get(uri, headers: _session.headers);
    if (response.statusCode == 200) {
      final body = jsonDecode(response.body);
      if (body is Map && body['rows'] is List) {
        return GoodsListResult(
          rows: (body['rows'] as List)
              .map((e) => TradeGoods.fromJson(Map<String, dynamic>.from(e)))
              .toList(),
          total: body['total'] ?? 0,
        );
      }
    }
    return GoodsListResult(rows: []);
  }
}
