import 'dart:convert';
import 'package:flutter/foundation.dart';
import 'package:http/http.dart' as http;
import '../models/trade_goods.dart';
import '../models/ajax_result.dart';
import 'session.dart';

class GoodsNotFoundException implements Exception {
  final String message;
  GoodsNotFoundException(this.message);
  @override
  String toString() => message;
}

class GoodsApi {
  static final GoodsApi instance = GoodsApi._();
  GoodsApi._();

  final _session = Session.instance;

  Future<AjaxResult> publishGoods(TradeGoods goods) async {
    debugPrint('[GoodsApi] 发布: POST /app/goods/publish');
    debugPrint('[GoodsApi] body: ${jsonEncode(goods.toJson())}');

    try {
      final response = await http.post(
        Uri.parse('${Session.baseUrl}/app/goods/publish'),
        headers: _session.headers,
        body: jsonEncode(goods.toJson()),
      );
      debugPrint('[GoodsApi] 响应: ${response.statusCode} ${response.body}');
      return AjaxResult.fromJson(jsonDecode(response.body));
    } catch (e) {
      debugPrint('[GoodsApi] 异常: $e');
      rethrow;
    }
  }

  Future<GoodsListResult> getGoodsList({
    String? keyword,
    int? categoryId,
    int? sellerId,
    int pageNum = 1,
    int pageSize = 10,
    String? sort,
    num? minPrice,
    num? maxPrice,
  }) async {
    final params = <String, String>{
      'pageNum': pageNum.toString(),
      'pageSize': pageSize.toString(),
    };
    if (keyword != null && keyword.isNotEmpty) params['keyword'] = keyword;
    if (categoryId != null) params['categoryId'] = categoryId.toString();
    if (sellerId != null) params['sellerId'] = sellerId.toString();
    if (sort != null) params['sort'] = sort;
    if (minPrice != null) params['minPrice'] = minPrice.toString();
    if (maxPrice != null) params['maxPrice'] = maxPrice.toString();

    final uri = Uri.parse('${Session.baseUrl}/app/goods/list')
        .replace(queryParameters: params);
    final response = await http.get(uri, headers: _session.headers);

    if (response.statusCode == 200) {
      final body = jsonDecode(response.body);
      if (body is Map && body['rows'] is List) {
        return GoodsListResult(
          rows: (body['rows'] as List)
              .map((e) => TradeGoods.fromJson(e))
              .toList(),
          total: body['total'] ?? 0,
        );
      }
      if (body is List) {
        final list = body.map((e) => TradeGoods.fromJson(e)).toList();
        return GoodsListResult(rows: list, total: list.length);
      }
    }
    return GoodsListResult(rows: []);
  }

  Future<GoodsListResult> getMyGoods({int pageNum = 1, int pageSize = 10, String? goodsStatus}) async {
    final params = <String, String>{
      'pageNum': pageNum.toString(),
      'pageSize': pageSize.toString(),
    };
    if (goodsStatus != null) params['goodsStatus'] = goodsStatus;
    final uri = Uri.parse('${Session.baseUrl}/app/goods/myList').replace(queryParameters: params);
    final response = await http.get(uri, headers: _session.headers);
    if (response.statusCode == 200) {
      final body = jsonDecode(response.body);
      if (body is Map && body['rows'] is List) {
        return GoodsListResult(
          rows: (body['rows'] as List)
              .map((e) => TradeGoods.fromJson(e))
              .toList(),
          total: body['total'] ?? 0,
        );
      }
    }
    return GoodsListResult(rows: []);
  }

  Future<bool> offlineGoods(int goodsId) async {
    debugPrint('[GoodsApi] 下架: PUT /app/goods/offline/$goodsId');
    final response = await http.put(
      Uri.parse('${Session.baseUrl}/app/goods/offline/$goodsId'),
      headers: _session.headers,
    );
    if (response.statusCode == 200) {
      final body = jsonDecode(response.body);
      if (body is Map) {
        return AjaxResult.fromJson(Map<String, dynamic>.from(body)).success;
      }
    }
    return false;
  }

  Future<TradeGoods> getGoodsDetail(int goodsId) async {
    debugPrint('[GoodsApi] 详情: GET /app/goods/detail/$goodsId');
    final response = await http.get(
      Uri.parse('${Session.baseUrl}/app/goods/detail/$goodsId'),
      headers: _session.headers,
    );
    if (response.statusCode == 200) {
      final body = jsonDecode(response.body);
      if (body is Map && body['data'] is Map) {
        return TradeGoods.fromJson(Map<String, dynamic>.from(body['data']));
      }
      if (body is Map && body['code'] != null && body['code'] != 200) {
        throw GoodsNotFoundException(body['msg'] ?? '商品不存在或已下架');
      }
    }
    if (response.statusCode == 404) {
      throw GoodsNotFoundException('商品不存在或已下架');
    }
    throw Exception('获取商品详情失败');
  }

  Future<AjaxResult> updateGoods(TradeGoods goods) async {
    debugPrint('[GoodsApi] 修改: PUT /app/goods/update');
    debugPrint('[GoodsApi] body: ${jsonEncode(goods.toJson())}');

    try {
      final response = await http.put(
        Uri.parse('${Session.baseUrl}/app/goods/update'),
        headers: _session.headers,
        body: jsonEncode(goods.toJson()),
      );
      debugPrint('[GoodsApi] 响应: ${response.statusCode} ${response.body}');
      return AjaxResult.fromJson(jsonDecode(response.body));
    } catch (e) {
      debugPrint('[GoodsApi] 异常: $e');
      rethrow;
    }
  }

  Future<bool> deleteGoods(int goodsId) async {
    final response = await http.delete(
      Uri.parse('${Session.baseUrl}/app/goods/$goodsId'),
      headers: _session.headers,
    );
    if (response.statusCode == 200) {
      final body = jsonDecode(response.body);
      if (body is Map) {
        return AjaxResult.fromJson(Map<String, dynamic>.from(body)).success;
      }
    }
    return false;
  }
}
